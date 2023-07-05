package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import uk.gov.justice.hmpps.prison.api.model.PrisonerDetail
import uk.gov.justice.hmpps.prison.api.model.PrisonerDetailSearchCriteria
import uk.gov.justice.hmpps.prison.api.support.Order
import uk.gov.justice.hmpps.prison.api.support.PageRequest
import uk.gov.justice.hmpps.prison.core.SlowReportQuery
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import uk.gov.justice.hmpps.prison.service.GlobalSearchService
import java.time.LocalDate

@RestController
@Tag(name = "prisoners")
@RequestMapping(value = ["\${api.base.path}/prisoners"], produces = ["application/json"])
@Validated
class PrisonerResource(private val globalSearchService: GlobalSearchService) {
  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(responseCode = "400", description = "Invalid request.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "404", description = "Requested resource not found.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(summary = "List of offenders matching specified criteria.", description = "List of offenders matching specified criteria.")
  @GetMapping
  @PreAuthorize("hasAnyRole('SYSTEM_USER','GLOBAL_SEARCH')")
  @SlowReportQuery
  fun getPrisoners(
    @RequestParam(value = "includeAliases", required = false, defaultValue = "false")
    @Parameter(description = "If true the result set should include a row for every matched alias.  If the request includes some combination of firstName, lastName and dateOfBirth then this will be a subset of the OFFENDERS records for one or more offenders. Otherwise it will be every OFFENDERS record for each match on the other search criteria. Default is false.")
    includeAliases: Boolean,
    @RequestParam(value = "offenderNo", required = false)
    @Parameter(description = "List of offender NOMS numbers. NOMS numbers have the format:<b>ANNNNAA</b>")
    offenderNos: List<String?>?,
    @RequestParam(value = "pncNumber", required = false)
    @Parameter(description = "The offender's PNC (Police National Computer) number.")
    pncNumber: String?,
    @RequestParam(value = "croNumber", required = false)
    @Parameter(description = "The offender's CRO (Criminal Records Office) number.")
    croNumber: String?,
    @RequestParam(value = "firstName", required = false)
    @Parameter(description = "The first name of the offender.")
    firstName: String?,
    @RequestParam(value = "middleNames", required = false)
    @Parameter(description = "The middle name(s) of the offender.")
    middleNames: String?,
    @RequestParam(value = "lastName", required = false)
    @Parameter(description = "The last name of the offender.")
    lastName: String?,
    @RequestParam(value = "dob", required = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Parameter(description = "The offender's date of birth. Cannot be used in conjunction with <i>dobFrom</i> or <i>dobTo</i>. Must be specified using YYYY-MM-DD format.")
    dob: LocalDate?,
    @RequestParam(value = "dobFrom", required = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Parameter(description = "Start date for offender date of birth search. If <i>dobTo</i> is not specified, an implicit <i>dobTo</i> value of <i>dobFrom</i> + 10 years will be applied. If <i>dobTo</i> is specified, it will be adjusted, if necessary, to ensure it is not more than 10 years after <i>dobFrom</i>. Cannot be used in conjunction with <i>dob</i>. Must be specified using YYYY-MM-DD format.")
    dobFrom: LocalDate?,
    @RequestParam(value = "dobTo", required = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Parameter(description = "End date for offender date of birth search. If <i>dobFrom</i> is not specified, an implicit <i>dobFrom</i> value of <i>dobTo</i> - 10 years will be applied. Cannot be used in conjunction with <i>dob</i>. Must be specified using YYYY-MM-DD format.")
    dobTo: LocalDate?,
    @RequestParam(value = "location", required = false)
    @Parameter(description = "Offender's location filter (IN, OUT or ALL) - defaults to ALL.")
    location: String?,
    @RequestParam(value = "gender", required = false)
    @Parameter(description = "Offender's gender code (F - Female, M - Male, NK - Not Known or NS - Not Specified).")
    genderCode: String?,
    @RequestParam(value = "partialNameMatch", required = false, defaultValue = "false")
    @Parameter(description = "If <i>true</i>, the search will use partial, start-of-name matching of offender names (where provided). For example, if <i>lastName</i> criteria of 'AD' is specified, this will match an offender whose last name is 'ADAMS' but not an offender whose last name is 'HADAD'. This will typically increase the number of matching offenders found. This parameter can be used with any other search processing parameter (e.g. <i>prioritisedMatch</i> or <i>anyMatch</i>).")
    partialNameMatch: Boolean,
    @RequestParam(value = "prioritisedMatch", required = false, defaultValue = "false")
    @Parameter(description = "If <i>true</i>, search criteria prioritisation is used and searching/matching will stop as soon as one or more matching offenders are found. The criteria priority is:<br/><br/>1. <i>offenderNo</i><br/> 2. <i>pncNumber</i><br/>3. <i>croNumber</i><br/>4. <i>firstName</i>, <i>lastName</i>, <i>dob</i> <br/>5. <i>dobFrom</i>, <i>dobTo</i><br/><br/>As an example of how this works, if this parameter is set <i>true</i> and an <i>offenderNo</i> is specified and an offender having this offender number is found, searching will stop and that offender will be returned immediately. If no offender matching the specified <i>offenderNo</i> is found, the search will be repeated using the next priority criteria (<i>pncNumber</i>) and so on. Note that offender name and date of birth criteria have the same priority and will be used together to search for matching offenders (In this case the location filter will be ignored).")
    prioritisedMatch: Boolean,
    @RequestParam(value = "anyMatch", required = false, defaultValue = "false")
    @Parameter(description = "If <i>true</i>, offenders that match any of the specified criteria will be returned. The default search behaviour is to only return offenders that match <i>all</i> of the specified criteria. If the <i>prioritisedMatch</i> parameter is also set <i>true</i>, this parameter will only impact the behaviour of searching using offender name and date of birth criteria.")
    anyMatch: Boolean,
    @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false)
    @Parameter(description = "Requested offset of first record in returned collection of prisoner records.")
    pageOffset: Long,
    @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false)
    @Parameter(description = "Requested limit to number of prisoner records returned.")
    pageLimit: Long,
    @RequestHeader(value = "Sort-Fields", required = false)
    @Parameter(description = "Comma separated list of one or more of the following fields - <b>offenderNo, pncNumber, croNumber, firstName, lastName, dob</b>")
    sortFields: String?,
    @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false)
    @Parameter(description = "Sort order (ASC or DESC) - defaults to ASC.")
    sortOrder: Order,
  ): ResponseEntity<List<PrisonerDetail>> {
    val criteria = PrisonerDetailSearchCriteria.builder()
      .includeAliases(includeAliases)
      .offenderNos(offenderNos)
      .firstName(firstName)
      .middleNames(middleNames)
      .lastName(lastName)
      .pncNumber(pncNumber)
      .croNumber(croNumber)
      .location(location)
      .gender(genderCode)
      .dob(dob)
      .dobFrom(dobFrom)
      .dobTo(dobTo)
      .partialNameMatch(partialNameMatch)
      .anyMatch(anyMatch)
      .prioritisedMatch(prioritisedMatch)
      .build()
    log.info("Global Search with search criteria: {}", criteria)
    return globalSearchService.findOffenders(
      criteria,
      PageRequest(sortFields, sortOrder, pageOffset, pageLimit),
    )
      .also { log.info("Global Search returned {} records", it.totalRecords) }
      .response
  }

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(responseCode = "400", description = "Invalid request.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "404", description = "Requested resource not found.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(
    summary = "List of offenders globally matching the offenderNo.",
    description = "List of offenders globally matching the offenderNo. restricted- document and also say [] if no results",
  )
  @GetMapping("/{offenderNo}")
  fun getPrisonersOffenderNo(
    @PathVariable("offenderNo")
    @Parameter(description = "The offenderNo to search for", required = true)
    offenderNo: String,
  ): List<PrisonerDetail> {
    try {
      log.info("Global Search with search criteria offender No: {}", offenderNo)
      return globalSearchService.findOffender(
        offenderNo,
        PageRequest(null, null, 0L, 1000L),
      )
        .also {
          log.debug("Global Search returned {} records", it.totalRecords)
        }
        .items
    } catch (enfe: EntityNotFoundException) {
      return listOf()
    }
  }

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(responseCode = "400", description = "Invalid request.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "404", description = "Requested resource not found.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(summary = "List of offenders matching specified criteria. (POST version)", description = "List of offenders matching specified criteria.")
  @PostMapping
  @PreAuthorize("hasAnyRole('SYSTEM_USER','GLOBAL_SEARCH')")
  @SlowReportQuery
  fun getPrisoners(
    @RequestBody
    @Parameter(required = true)
    criteria: PrisonerDetailSearchCriteria,
    @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false)
    @Parameter(description = "Requested offset of first record in returned collection of prisoner records.")
    pageOffset: Long,
    @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false)
    @Parameter(description = "Requested limit to number of prisoner records returned.")
    pageLimit: Long,
    @RequestHeader(value = "Sort-Fields", required = false)
    @Parameter(description = "Comma separated list of one or more of the following fields - <b>offenderNo, pncNumber, croNumber, firstName, lastName, dob</b>")
    sortFields: String?,
    @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false)
    @Parameter(description = "Sort order (ASC or DESC) - defaults to ASC.")
    sortOrder: Order,
  ): ResponseEntity<List<PrisonerDetail>> {
    log.info("Global Search with search criteria: {}", criteria)
    return globalSearchService.findOffenders(
      criteria,
      PageRequest(sortFields, sortOrder, pageOffset, pageLimit),
    )
      .also { log.debug("Global Search returned {} records", it.totalRecords) }
      .response
  }

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(responseCode = "400", description = "Invalid request.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(
    summary = """
    Return a list of all unique prisoner numbers (also called NOMS ID or offenderNo). 
    Results are ordered by max(ROOT_OFFENDER_ID), therefore ensuring that new offenders are added to the end of the 
    results.
    This is an internal endpoint used by Prisoner Search to ensure that NOMIS and OpenSearch are in sync.
    Other services should use Prisoner Search instead to get the list of prisoners.
    Requires PRISONER_INDEX role.
    """,
  )
  @GetMapping("/prisoner-numbers")
  @PreAuthorize("hasAnyRole('SYSTEM_USER','PRISONER_INDEX')")
  @SlowReportQuery
  fun getPrisonerNumbers(
    @RequestParam(value = "page", defaultValue = "0", required = false)
    @Parameter(description = "The page number of the paged results")
    page: Int,
    @RequestParam(value = "size", defaultValue = "10", required = false)
    @Parameter(description = "Requested limit to number of prisoner numbers returned.")
    size: Int,
  ): Page<String> {
    val offenderNumbers = globalSearchService.getOffenderNumbers(page.toLong() * size, size.toLong())
    return PageImpl(
      offenderNumbers.items.map { it.offenderNumber },
      org.springframework.data.domain.PageRequest.of(page, size),
      offenderNumbers.totalRecords,
    )
  }

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
