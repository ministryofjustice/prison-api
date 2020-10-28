package uk.gov.justice.hmpps.prison.api.resource;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
import uk.gov.justice.hmpps.prison.api.model.CaseNoteUsage;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteUsageByBookingId;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteUsageRequest;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.service.CaseNoteService;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Validated
@RequestMapping("${api.base.path}/case-notes")
@AllArgsConstructor
public class CaseNoteResource {

    private final CaseNoteService caseNoteService;

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = CaseNoteStaffUsage.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Count of case notes", notes = "Count of case notes", nickname = "getCaseNoteStaffUsageSummary")
    @GetMapping("/staff-usage")
    public List<CaseNoteStaffUsage> getCaseNoteStaffUsageSummary(@RequestParam("staffId") @ApiParam(value = "a list of staffId numbers to use.", required = true) final List<String> staffIds, @RequestParam(value = "numMonths", required = false, defaultValue = "1") @ApiParam(value = "Number of month to look forward (if fromDate only defined), or back (if toDate only defined). Default is 1 month", defaultValue = "1") final Integer numMonths, @RequestParam(value = "fromDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Only case notes occurring on or after this date (in YYYY-MM-DD format) will be considered.  If not defined then the numMonth before the current date, unless a toDate is defined when it will be numMonths before toDate") final LocalDate fromDate, @RequestParam(value = "toDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Only case notes occurring on or before this date (in YYYY-MM-DD format) will be considered. If not defined then the current date will be used, unless a fromDate is defined when it will be numMonths after fromDate") final LocalDate toDate, @RequestParam(value = "type", required = false) @ApiParam("Case note type.") final String type, @RequestParam(value = "subType", required = false) @ApiParam("Case note sub-type.") final String subType) {
        final var staffIdList = staffIds.stream().map(Integer::valueOf).collect(Collectors.toList());
        return caseNoteService.getCaseNoteStaffUsage(type, subType, staffIdList, fromDate, toDate, ObjectUtils.defaultIfNull(numMonths, 1));
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "The case note usage list is returned.", response = CaseNoteStaffUsage.class, responseContainer = "List")})
    @ApiOperation(value = "Retrieves list of case notes grouped by type/sub-type and staff", notes = "Retrieves list of case notes grouped by type/sub-type and staff", nickname = "getCaseNoteStaffUsageSummaryByPost")
    @PostMapping("/staff-usage")
    public List<CaseNoteStaffUsage> getCaseNoteStaffUsageSummaryByPost(@RequestBody @ApiParam(value = "", required = true) final CaseNoteStaffUsageRequest request) {
        return caseNoteService.getCaseNoteStaffUsage(request.getType(), request.getSubType(), request.getStaffIds(), request.getFromDate(), request.getToDate(), ObjectUtils.defaultIfNull(request.getNumMonths(), 1));
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = CaseNoteUsage.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Count of case notes", notes = "Count of case notes", nickname = "getCaseNoteUsageSummary")
    @GetMapping("/usage")
    public List<CaseNoteUsage> getCaseNoteUsageSummary(@RequestParam(value = "offenderNo", required = false) @ApiParam("a list of offender numbers to search.") final List<String> offenderNo, @RequestParam(value = "staffId", required = false) @ApiParam("Staff Id to filter by") final Integer staffId, @RequestParam(value = "agencyId", required = false) @ApiParam("Agency Id to filter by") final String agencyId, @RequestParam(value = "numMonths", required = false, defaultValue = "1") @ApiParam(value = "Number of month to look forward (if fromDate only defined), or back (if toDate only defined). Default is 1 month", defaultValue = "1") final Integer numMonths, @RequestParam(value = "fromDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Only case notes occurring on or after this date (in YYYY-MM-DD format) will be considered.  If not defined then the numMonth before the current date, unless a toDate is defined when it will be numMonths before toDate") final LocalDate fromDate, @RequestParam(value = "toDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Only case notes occurring on or before this date (in YYYY-MM-DD format) will be considered. If not defined then the current date will be used, unless a fromDate is defined when it will be numMonths after fromDate") final LocalDate toDate, @RequestParam(value = "type", required = false) @ApiParam("Case note type.") final String type, @RequestParam(value = "subType", required = false) @ApiParam("Case note sub-type.") final String subType) {
        return caseNoteService.getCaseNoteUsage(type, subType, offenderNo, staffId, agencyId, fromDate, toDate, ObjectUtils.defaultIfNull(numMonths, 1));
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "The case note usage list is returned.", response = CaseNoteUsage.class, responseContainer = "List")})
    @ApiOperation(value = "Retrieves list of case notes grouped by type and offender", notes = "Retrieves list of case notes grouped by type and offender", nickname = "getCaseNoteUsageSummaryByPost")
    @PostMapping("/usage")
    public List<CaseNoteUsage> getCaseNoteUsageSummaryByPost(@RequestBody @ApiParam(value = "", required = true) final CaseNoteUsageRequest request) {
        return caseNoteService.getCaseNoteUsage(request.getType(), request.getSubType(), request.getOffenderNos(), request.getStaffId(), request.getAgencyId(), request.getFromDate(), request.getToDate(), ObjectUtils.defaultIfNull(request.getNumMonths(), 1));
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = CaseNoteEvent.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Fetch all case notes of a given type since a given date", notes = "Returns all case notes - consumed by Delius<br/>" +
            "These are generated by  whenever a case note is created or amended for an offender.<br/>" +
            "*Note:* An alternative call [GET /case_notes/for_delius](#case-notes-feed-fetch-case-notes-direct-get) has been created for performance reasons.<br/>" +
            "Some case notes are automatically generated by the system in response to an action relating to the offender e.g. An alert being raised or a prison transfer.<br/>" +
            "The note type only filters at the top note type level not the sub type.<br/>" +
            "note_type can be presented multiples times in the URL to filter by multiple note types.", nickname = "getCaseNotesEvents")
    @springfox.documentation.annotations.ApiIgnore
    @GetMapping("/events_no_limit")
    public List<CaseNoteEvent> getCaseNotesEventsNoLimit(@RequestParam("type") @NotEmpty @ApiParam(value = "a list of types and optionally subtypes (joined with +) to search.", example = "ACP+ASSESSMENT", required = true) final List<String> noteTypes, @RequestParam("createdDate") @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @ApiParam("Only case notes occurring on or after this date and time (ISO 8601 format without timezone e.g. YYYY-MM-DDTHH:MM:SS) will be considered.") final LocalDateTime createdDate) {
        return caseNoteService.getCaseNotesEvents(noteTypes, createdDate);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = CaseNoteEvent.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Fetch all case notes of a given type since a given date", notes = "Returns all case notes - consumed by Delius<br/>" +
            "These are generated by  whenever a case note is created or amended for an offender.<br/>" +
            "*Note:* An alternative call [GET /case_notes/for_delius](#case-notes-feed-fetch-case-notes-direct-get) has been created for performance reasons.<br/>" +
            "Some case notes are automatically generated by the system in response to an action relating to the offender e.g. An alert being raised or a prison transfer.<br/>" +
            "The note type only filters at the top note type level not the sub type.<br/>" +
            "note_type can be presented multiples times in the URL to filter by multiple note types.", nickname = "getCaseNotesEvents")
    @GetMapping("/events")
    public List<CaseNoteEvent> getCaseNotesEvents(@RequestParam("type") @NotEmpty @ApiParam(value = "a list of types and optionally subtypes (joined with +) to search.", example = "ACP+ASSESSMENT", required = true) final List<String> noteTypes, @RequestParam("createdDate") @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @ApiParam("Only case notes occurring on or after this date and time (ISO 8601 format without timezone e.g. YYYY-MM-DDTHH:MM:SS) will be considered.") final LocalDateTime createdDate, @RequestParam("limit") @ApiParam(name = "limit", value = "Number of events to return", example = "100") final Long limit) {
        return caseNoteService.getCaseNotesEvents(noteTypes, createdDate, limit);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = CaseNoteUsageByBookingId.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Count of case notes by booking id", notes = "Count of case notes by booking id", nickname = "getCaseNoteSummaryByBookingId")
    @springfox.documentation.annotations.ApiIgnore
    @GetMapping("/summary")
    public List<CaseNoteUsageByBookingId> getCaseNoteSummaryByBookingId(@RequestParam("bookingId") @NotEmpty @ApiParam(value = "a list of booking ids to search.", required = true) final List<Integer> bookingIds, @RequestParam(value = "numMonths", required = false, defaultValue = "1") @ApiParam(value = "Number of month to look forward (if fromDate only defined), or back (if toDate only defined). Default is 1 month", defaultValue = "1") final Integer numMonths, @RequestParam(value = "fromDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Only case notes occurring on or after this date (in YYYY-MM-DD format) will be considered.  If not defined then the numMonth before the current date, unless a toDate is defined when it will be numMonths before toDate") final LocalDate fromDate, @RequestParam(value = "toDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Only case notes occurring on or before this date (in YYYY-MM-DD format) will be considered. If not defined then the current date will be used, unless a fromDate is defined when it will be numMonths after fromDate") final LocalDate toDate, @RequestParam(value = "type", required = false) @ApiParam("Case note type.") final String type, @RequestParam(value = "subType", required = false) @ApiParam("Case note sub-type.") final String subType) {
        return caseNoteService.getCaseNoteUsageByBookingId(type, subType, bookingIds, fromDate, toDate, ObjectUtils.defaultIfNull(numMonths, 1));
    }
}
