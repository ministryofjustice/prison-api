package uk.gov.justice.hmpps.prison.api.resource;


import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteEvent;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteStaffUsage;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteStaffUsageRequest;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteTypeCount;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteTypeSummaryRequest;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteUsage;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteUsageByBookingId;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteUsageRequest;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.core.SlowReportQuery;
import uk.gov.justice.hmpps.prison.service.CaseNoteService;

import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@Validated
@Tag(name = "case-notes")
@RequestMapping(value = "${api.base.path}/case-notes", produces = "application/json")
@AllArgsConstructor
public class CaseNoteResource {

    private final CaseNoteService caseNoteService;

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Count of case notes", description = "Count of case notes")
    @GetMapping("/staff-usage")
    @SlowReportQuery
    public List<CaseNoteStaffUsage> getCaseNoteStaffUsageSummary(@RequestParam("staffId") @Parameter(description = "a list of staffId numbers to use.", required = true) final List<String> staffIds, @RequestParam(value = "numMonths", required = false, defaultValue = "1") @Parameter(description = "Number of month to look forward (if fromDate only defined), or back (if toDate only defined). Default is 1 month") final Integer numMonths, @RequestParam(value = "fromDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Only case notes occurring on or after this date (in YYYY-MM-DD format) will be considered.  If not defined then the numMonth before the current date, unless a toDate is defined when it will be numMonths before toDate") final LocalDate fromDate, @RequestParam(value = "toDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Only case notes occurring on or before this date (in YYYY-MM-DD format) will be considered. If not defined then the current date will be used, unless a fromDate is defined when it will be numMonths after fromDate") final LocalDate toDate, @RequestParam(value = "type", required = false) @Parameter(description = "Case note type.") final String type, @RequestParam(value = "subType", required = false) @Parameter(description = "Case note sub-type.") final String subType) {
        final var staffIdList = staffIds.stream().map(Integer::valueOf).toList();
        return caseNoteService.getCaseNoteStaffUsage(type, subType, staffIdList, fromDate, toDate, ObjectUtils.defaultIfNull(numMonths, 1));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The case note usage list is returned.")})
    @Operation(summary = "Retrieves list of case notes grouped by type/sub-type and staff", description = "Retrieves list of case notes grouped by type/sub-type and staff")
    @PostMapping("/staff-usage")
    @SlowReportQuery
    public List<CaseNoteStaffUsage> getCaseNoteStaffUsageSummaryByPost(@RequestBody @Parameter(required = true) final CaseNoteStaffUsageRequest request) {
        return caseNoteService.getCaseNoteStaffUsage(request.getType(), request.getSubType(), request.getStaffIds(), request.getFromDate(), request.getToDate(), ObjectUtils.defaultIfNull(request.getNumMonths(), 1));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Count of case notes", description = "Count of case notes")
    @GetMapping("/usage")
    @SlowReportQuery
    public List<CaseNoteUsage> getCaseNoteUsageSummary(@RequestParam(value = "offenderNo", required = false) @Parameter(description = "a list of offender numbers to search.") final List<String> offenderNo, @RequestParam(value = "staffId", required = false) @Parameter(description = "Staff Id to filter by") final Integer staffId, @RequestParam(value = "agencyId", required = false) @Parameter(description = "Agency Id to filter by") final String agencyId, @RequestParam(value = "numMonths", required = false, defaultValue = "1") @Parameter(description = "Number of month to look forward (if fromDate only defined), or back (if toDate only defined). Default is 1 month") final Integer numMonths, @RequestParam(value = "fromDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Only case notes occurring on or after this date (in YYYY-MM-DD format) will be considered.  If not defined then the numMonth before the current date, unless a toDate is defined when it will be numMonths before toDate") final LocalDate fromDate, @RequestParam(value = "toDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Only case notes occurring on or before this date (in YYYY-MM-DD format) will be considered. If not defined then the current date will be used, unless a fromDate is defined when it will be numMonths after fromDate") final LocalDate toDate, @RequestParam(value = "type", required = false) @Parameter(description = "Case note type.") final String type, @RequestParam(value = "subType", required = false) @Parameter(description = "Case note sub-type.") final String subType) {
        return caseNoteService.getCaseNoteUsage(type, subType, offenderNo, staffId, agencyId, fromDate, toDate, ObjectUtils.defaultIfNull(numMonths, 1));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The case note usage list is returned.")})
    @Operation(summary = "Retrieves list of case notes grouped by type and offender", description = "Retrieves list of case notes grouped by type and offender")
    @PostMapping("/usage")
    @SlowReportQuery
    public List<CaseNoteUsage> getCaseNoteUsageSummaryByPost(@RequestBody @Parameter(required = true) final CaseNoteUsageRequest request) {
        return caseNoteService.getCaseNoteUsage(request.getType(), request.getSubType(), request.getOffenderNos(), request.getStaffId(), request.getAgencyId(), request.getFromDate(), request.getToDate(), ObjectUtils.defaultIfNull(request.getNumMonths(), 1));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "The case note usage list is returned.")})
    @Operation(summary = "Retrieves list of case notes grouped by types, bookings and from dates", description = "Retrieves list of case notes grouped by type/sub and offender")
    @PostMapping("/usage-by-types")
    @SlowReportQuery
    public List<CaseNoteTypeCount> getCaseNoteUsageSummaryByDates(@RequestBody @Parameter(required = true) final CaseNoteTypeSummaryRequest request) {
        return caseNoteService.getCaseNoteUsageByBookingIdTypeAndDate(request.getTypes(), request.getBookingFromDateSelection());
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Fetch all case notes of a given type since a given date", description = "Returns all case notes - consumed by Delius<br/>" +
            "These are generated by  whenever a case note is created or amended for an offender.<br/>" +
            "*Note:* An alternative call [GET /case_notes/for_delius](#case-notes-feed-fetch-case-notes-direct-get) has been created for performance reasons.<br/>" +
            "Some case notes are automatically generated by the system in response to an action relating to the offender e.g. An alert being raised or a prison transfer.<br/>" +
            "The note type only filters at the top note type level not the sub type.<br/>" +
            "note_type can be presented multiples times in the URL to filter by multiple note types.")
    @Hidden
    @GetMapping("/events_no_limit")
    @SlowReportQuery
    public List<CaseNoteEvent> getCaseNotesEventsNoLimit(@RequestParam("type") @NotEmpty @Parameter(description = "a list of types and optionally subtypes (joined with +) to search.", example = "ACP+ASSESSMENT", required = true) final List<String> noteTypes, @RequestParam("createdDate") @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @Parameter(description = "Only case notes occurring on or after this date and time (ISO 8601 format without timezone e.g. YYYY-MM-DDTHH:MM:SS) will be considered.") final LocalDateTime createdDate) {
        return caseNoteService.getCaseNotesEvents(noteTypes, createdDate);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Fetch all case notes of a given type since a given date", description = "Returns all case notes - consumed by Delius<br/>" +
            "These are generated by  whenever a case note is created or amended for an offender.<br/>" +
            "*Note:* An alternative call [GET /case_notes/for_delius](#case-notes-feed-fetch-case-notes-direct-get) has been created for performance reasons.<br/>" +
            "Some case notes are automatically generated by the system in response to an action relating to the offender e.g. An alert being raised or a prison transfer.<br/>" +
            "The note type only filters at the top note type level not the sub type.<br/>" +
            "note_type can be presented multiples times in the URL to filter by multiple note types.")
    @GetMapping("/events")
    public List<CaseNoteEvent> getCaseNotesEvents(@RequestParam("type") @NotEmpty @Parameter(description = "a list of types and optionally subtypes (joined with +) to search.", example = "ACP+ASSESSMENT", required = true) final List<String> noteTypes, @RequestParam("createdDate") @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @Parameter(description = "Only case notes occurring on or after this date and time (ISO 8601 format without timezone e.g. YYYY-MM-DDTHH:MM:SS) will be considered.") final LocalDateTime createdDate, @RequestParam("limit") @Parameter(name = "limit", description = "Number of events to return", example = "100") final Long limit) {
        return caseNoteService.getCaseNotesEvents(noteTypes, createdDate, limit);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Count of case notes by booking id", description = "Count of case notes by booking id")
    @Hidden
    @GetMapping("/summary")
    @SlowReportQuery
    public List<CaseNoteUsageByBookingId> getCaseNoteSummaryByBookingId(@RequestParam("bookingId") @NotEmpty @Parameter(description = "a list of booking ids to search.", required = true) final List<Long> bookingIds, @RequestParam(value = "numMonths", required = false, defaultValue = "1") @Parameter(description = "Number of month to look forward (if fromDate only defined), or back (if toDate only defined). Default is 1 month") final Integer numMonths, @RequestParam(value = "fromDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Only case notes occurring on or after this date (in YYYY-MM-DD format) will be considered.  If not defined then the numMonth before the current date, unless a toDate is defined when it will be numMonths before toDate") final LocalDate fromDate, @RequestParam(value = "toDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Only case notes occurring on or before this date (in YYYY-MM-DD format) will be considered. If not defined then the current date will be used, unless a fromDate is defined when it will be numMonths after fromDate") final LocalDate toDate, @RequestParam(value = "type", required = false) @Parameter(description = "Case note type.") final String type, @RequestParam(value = "subType", required = false) @Parameter(description = "Case note sub-type.") final String subType) {
        return caseNoteService.getCaseNoteUsageByBookingId(type, subType, bookingIds, fromDate, toDate, ObjectUtils.defaultIfNull(numMonths, 1));
    }
}
