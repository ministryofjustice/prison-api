package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
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
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess
import uk.gov.justice.hmpps.prison.service.GlobalSearchService
import uk.gov.justice.hmpps.prison.service.PrisonerSearchService

@RestController
@Tag(name = "prisoners")
@RequestMapping(value = [$$"${api.base.path}/prisoners"], produces = ["application/json"])
@Validated
class PrisonerResource(
  private val globalSearchService: GlobalSearchService,
  private val prisonerSearchService: PrisonerSearchService,
) {

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(responseCode = "400", description = "Invalid request.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(
    summary = "List of offenders globally matching the offenderNo.",
    description = "List of offenders globally matching the offenderNo, Requires offender agency to be in user caseload or VIEW_PRISONER_DATA role. " +
      "Returns an empty array if no results are found or if does not have correct permissions",
  )
  @VerifyOffenderAccess(overrideRoles = ["VIEW_PRISONER_DATA"])
  @GetMapping("/{offenderNo}")
  fun getPrisonersOffenderNo(
    @PathVariable("offenderNo")
    @Parameter(description = "The offenderNo to search for", required = true)
    offenderNo: String,
  ): List<PrisonerDetail> {
    log.debug("Global Search with search criteria offender No: {}", offenderNo)
    return globalSearchService.findOffender(
      offenderNo,
      PageRequest(null, null, 0L, 1000L),
    )
      .also {
        log.debug("Global Search returned {} records", it.totalRecords)
      }
      .items
  }

  @ApiResponses(
    ApiResponse(responseCode = "200", description = "OK"),
    ApiResponse(responseCode = "400", description = "Invalid request.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "404", description = "Requested resource not found.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
    ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]),
  )
  @Operation(summary = "List of offenders matching specified criteria. (POST version)", description = "List of offenders matching specified criteria. Requires GLOBAL_SEARCH role.")
  @PostMapping
  @PreAuthorize("hasRole('GLOBAL_SEARCH')")
  @SlowReportQuery
  fun getPrisonersByPostRequest(
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
    summary = "Return a list of all unique prisoner numbers (also called NOMS ID or offenderNo).",
    description = """Results are ordered by ROOT_OFFENDER_ID ascending, therefore ensuring that new offenders are added to the end of the
    results.
    This is an internal endpoint used by Prisoner Search to ensure that NOMIS and OpenSearch are in sync.
    Other services should use Prisoner Search instead to get the list of prisoners.
    Requires PRISONER_INDEX role.
    """,
  )
  @GetMapping("/prisoner-numbers")
  @PreAuthorize("hasAnyRole('PRISONER_INDEX')")
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

  @GetMapping("/prisoner-numbers/active")
  @PreAuthorize("hasAnyRole('PRISONER_INDEX')")
  @Operation(
    summary = "Gets the identifiers for all active prisoners",
    description = """Return a list of all unique prisoner numbers (also called NOMS ID or offenderNo) with an active booking.
          Results are ordered by ROOT_OFFENDER_ID ascending, therefore ensuring that new offenders are added to the end of the
          results.
          This is an internal endpoint used by Prisoner Search to ensure that NOMIS and OpenSearch are in sync.
          Other services should use Prisoner Search instead to get the list of prisoners.
          Requires PRISONER_INDEX role.
    """,
    responses = [
      ApiResponse(responseCode = "200", description = "paged list of prisoner ids"),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden to access this endpoint when role NOMIS_PRISONER_API__SYNCHRONISATION__RW not present",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun getActivePrisonerIdentifiers(
    @PageableDefault(sort = ["rootOffenderId"], direction = Sort.Direction.ASC)
    @ParameterObject
    pageRequest: Pageable,
  ): Page<String> = prisonerSearchService.findAllActivePrisoners(pageRequest)

  private companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
