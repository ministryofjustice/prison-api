package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.PrisonerDetail;
import uk.gov.justice.hmpps.prison.api.model.PrisonerDetailSearchCriteria;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.core.SlowReportQuery;
import uk.gov.justice.hmpps.prison.service.GlobalSearchService;

import java.time.LocalDate;
import java.util.List;

@RestController
@Tag(name = "prisoners")
@RequestMapping(value = "${api.base.path}/prisoners", produces = "application/json")
@Slf4j
@Validated
@AllArgsConstructor
public class PrisonerResource {
    private final GlobalSearchService globalSearchService;

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of offenders matching specified criteria.", description = "List of offenders matching specified criteria.")
    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_USER','GLOBAL_SEARCH')")
    @SlowReportQuery
    public ResponseEntity<List<PrisonerDetail>> getPrisoners(
            @RequestParam(value = "includeAliases", required = false, defaultValue = "false") @Parameter(description = "If true the result set should include a row for every matched alias.  If the request includes some combination of firstName, lastName and dateOfBirth then this will be a subset of the OFFENDERS records for one or more offenders. Otherwise it will be every OFFENDERS record for each match on the other search criteria. Default is false.") final boolean includeAliases,
            @RequestParam(value = "offenderNo", required = false) @Parameter(description = "List of offender NOMS numbers. NOMS numbers have the format:<b>ANNNNAA</b>") final List<String> offenderNos,
            @RequestParam(value = "pncNumber", required = false) @Parameter(description = "The offender's PNC (Police National Computer) number.") final String pncNumber,
            @RequestParam(value = "croNumber", required = false) @Parameter(description = "The offender's CRO (Criminal Records Office) number.") final String croNumber,
            @RequestParam(value = "firstName", required = false) @Parameter(description = "The first name of the offender.") final String firstName,
            @RequestParam(value = "middleNames", required = false) @Parameter(description = "The middle name(s) of the offender.") final String middleNames,
            @RequestParam(value = "lastName", required = false) @Parameter(description = "The last name of the offender.") final String lastName,
            @RequestParam(value = "dob", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "The offender's date of birth. Cannot be used in conjunction with <i>dobFrom</i> or <i>dobTo</i>. Must be specified using YYYY-MM-DD format.") final LocalDate dob,
            @RequestParam(value = "dobFrom", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Start date for offender date of birth search. If <i>dobTo</i> is not specified, an implicit <i>dobTo</i> value of <i>dobFrom</i> + 10 years will be applied. If <i>dobTo</i> is specified, it will be adjusted, if necessary, to ensure it is not more than 10 years after <i>dobFrom</i>. Cannot be used in conjunction with <i>dob</i>. Must be specified using YYYY-MM-DD format.") final LocalDate dobFrom,
            @RequestParam(value = "dobTo", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "End date for offender date of birth search. If <i>dobFrom</i> is not specified, an implicit <i>dobFrom</i> value of <i>dobTo</i> - 10 years will be applied. Cannot be used in conjunction with <i>dob</i>. Must be specified using YYYY-MM-DD format.") final LocalDate dobTo,
            @RequestParam(value = "location", required = false) @Parameter(description = "Offender's location filter (IN, OUT or ALL) - defaults to ALL.") final String location,
            @RequestParam(value = "gender", required = false) @Parameter(description = "Offender's gender code (F - Female, M - Male, NK - Not Known or NS - Not Specified).") final String genderCode,
            @RequestParam(value = "partialNameMatch", required = false, defaultValue = "false") @Parameter(description = "If <i>true</i>, the search will use partial, start-of-name matching of offender names (where provided). For example, if <i>lastName</i> criteria of 'AD' is specified, this will match an offender whose last name is 'ADAMS' but not an offender whose last name is 'HADAD'. This will typically increase the number of matching offenders found. This parameter can be used with any other search processing parameter (e.g. <i>prioritisedMatch</i> or <i>anyMatch</i>).") final boolean partialNameMatch,
            @RequestParam(value = "prioritisedMatch", required = false, defaultValue = "false") @Parameter(description = "If <i>true</i>, search criteria prioritisation is used and searching/matching will stop as soon as one or more matching offenders are found. The criteria priority is:<br/><br/>1. <i>offenderNo</i><br/> 2. <i>pncNumber</i><br/>3. <i>croNumber</i><br/>4. <i>firstName</i>, <i>lastName</i>, <i>dob</i> <br/>5. <i>dobFrom</i>, <i>dobTo</i><br/><br/>As an example of how this works, if this parameter is set <i>true</i> and an <i>offenderNo</i> is specified and an offender having this offender number is found, searching will stop and that offender will be returned immediately. If no offender matching the specified <i>offenderNo</i> is found, the search will be repeated using the next priority criteria (<i>pncNumber</i>) and so on. Note that offender name and date of birth criteria have the same priority and will be used together to search for matching offenders (In this case the location filter will be ignored).") final boolean prioritisedMatch,
            @RequestParam(value = "anyMatch", required = false, defaultValue = "false") @Parameter(description = "If <i>true</i>, offenders that match any of the specified criteria will be returned. The default search behaviour is to only return offenders that match <i>all</i> of the specified criteria. If the <i>prioritisedMatch</i> parameter is also set <i>true</i>, this parameter will only impact the behaviour of searching using offender name and date of birth criteria.") final boolean anyMatch,
            @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @Parameter(description = "Requested offset of first record in returned collection of prisoner records.") final Long pageOffset,
            @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @Parameter(description = "Requested limit to number of prisoner records returned.") final Long pageLimit,
            @RequestHeader(value = "Sort-Fields", required = false) @Parameter(description = "Comma separated list of one or more of the following fields - <b>offenderNo, pncNumber, croNumber, firstName, lastName, dob</b>") final String sortFields,
            @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @Parameter(description = "Sort order (ASC or DESC) - defaults to ASC.") final Order sortOrder) {

        final var criteria = PrisonerDetailSearchCriteria.builder()
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
                .build();

        log.info("Global Search with search criteria: {}", criteria);
        final var offenders = globalSearchService.findOffenders(
                criteria,
                new PageRequest(sortFields, sortOrder, pageOffset, pageLimit));
        log.info("Global Search returned {} records", offenders.getTotalRecords());
        return ResponseEntity.ok()
                .headers(offenders.getPaginationHeaders())
                .body(offenders.getItems());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of offenders globally matching the offenderNo.", description = "List of offenders globally matching the offenderNo.")
    @GetMapping("/{offenderNo}")
    public List<PrisonerDetail> getPrisonersOffenderNo(@PathVariable("offenderNo") @Parameter(description = "The offenderNo to search for", required = true) final String offenderNo) {

        final var criteria = PrisonerDetailSearchCriteria.builder()
                .offenderNos(List.of(offenderNo))
                .build();

        log.info("Global Search with search criteria: {}", criteria);
        final var offenders = globalSearchService.findOffenders(
                criteria,
                new PageRequest(null, null, 0L, 1000L));
        log.debug("Global Search returned {} records", offenders.getTotalRecords());
        return offenders.getItems();
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of offenders matching specified criteria. (POST version)", description = "List of offenders matching specified criteria.")
    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_USER','GLOBAL_SEARCH')")
    @SlowReportQuery
    public ResponseEntity<List<PrisonerDetail>> getPrisoners(@RequestBody @Parameter(required = true) final PrisonerDetailSearchCriteria criteria,
                                                             @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @Parameter(description = "Requested offset of first record in returned collection of prisoner records.") final Long pageOffset,
                                                             @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @Parameter(description = "Requested limit to number of prisoner records returned.") final Long pageLimit,
                                                             @RequestHeader(value = "Sort-Fields", required = false) @Parameter(description = "Comma separated list of one or more of the following fields - <b>offenderNo, pncNumber, croNumber, firstName, lastName, dob</b>") final String sortFields,
                                                             @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @Parameter(description = "Sort order (ASC or DESC) - defaults to ASC.") final Order sortOrder) {
        log.info("Global Search with search criteria: {}", criteria);
        final var offenders = globalSearchService.findOffenders(
                criteria,
                new PageRequest(sortFields, sortOrder, pageOffset, pageLimit));
        log.debug("Global Search returned {} records", offenders.getTotalRecords());
        return ResponseEntity.ok()
                .headers(offenders.getPaginationHeaders())
                .body(offenders.getItems());
    }

}
