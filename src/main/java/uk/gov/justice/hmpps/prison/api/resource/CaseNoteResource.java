package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import springfox.documentation.annotations.ApiIgnore;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteEvent;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteStaffUsage;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteStaffUsageRequest;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteUsage;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteUsageByBookingId;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteUsageRequest;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Api(tags = {"/case-notes"})
public interface CaseNoteResource {

    @GetMapping("/staff-usage")
    @ApiOperation(value = "Count of case notes", notes = "Count of case notes", nickname = "getCaseNoteStaffUsageSummary")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = CaseNoteStaffUsage.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<CaseNoteStaffUsage> getCaseNoteStaffUsageSummary(@ApiParam(value = "a list of staffId numbers to use.", required = true) @RequestParam("staffId") List<String> staffId,
                                                          @ApiParam(value = "Number of month to look forward (if fromDate only defined), or back (if toDate only defined). Default is 1 month", defaultValue = "1") @RequestParam(value = "numMonths", required = false, defaultValue = "1") Integer numMonths,
                                                          @ApiParam(value = "Only case notes occurring on or after this date (in YYYY-MM-DD format) will be considered.  If not defined then the numMonth before the current date, unless a toDate is defined when it will be numMonths before toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "fromDate", required = false) LocalDate fromDate,
                                                          @ApiParam(value = "Only case notes occurring on or before this date (in YYYY-MM-DD format) will be considered. If not defined then the current date will be used, unless a fromDate is defined when it will be numMonths after fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "toDate", required = false) LocalDate toDate,
                                                          @ApiParam(value = "Case note type.") @RequestParam(value = "type", required = false) String type,
                                                          @ApiParam(value = "Case note sub-type.") @RequestParam(value = "subType", required = false) String subType);

    @GetMapping("/usage")
    @ApiOperation(value = "Count of case notes", notes = "Count of case notes", nickname = "getCaseNoteUsageSummary")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = CaseNoteUsage.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<CaseNoteUsage> getCaseNoteUsageSummary(@ApiParam(value = "a list of offender numbers to search.") @RequestParam(value = "offenderNo", required = false) List<String> offenderNo,
                                                @ApiParam(value = "Staff Id to filter by") @RequestParam(value = "staffId", required = false) Integer staffId,
                                                @ApiParam(value = "Agency Id to filter by") @RequestParam(value = "agencyId", required = false) String agencyId,
                                                @ApiParam(value = "Number of month to look forward (if fromDate only defined), or back (if toDate only defined). Default is 1 month", defaultValue = "1") @RequestParam(value = "numMonths", required = false, defaultValue = "1") Integer numMonths,
                                                @ApiParam(value = "Only case notes occurring on or after this date (in YYYY-MM-DD format) will be considered.  If not defined then the numMonth before the current date, unless a toDate is defined when it will be numMonths before toDate")  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "fromDate", required = false) LocalDate fromDate,
                                                @ApiParam(value = "Only case notes occurring on or before this date (in YYYY-MM-DD format) will be considered. If not defined then the current date will be used, unless a fromDate is defined when it will be numMonths after fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "toDate", required = false) LocalDate toDate,
                                                @ApiParam(value = "Case note type.") @RequestParam(value = "type", required = false) String type,
                                                @ApiParam(value = "Case note sub-type.") @RequestParam(value = "subType", required = false) String subType);

    @GetMapping("/summary")
    @ApiIgnore
    @ApiOperation(value = "Count of case notes by booking id", notes = "Count of case notes by booking id", nickname = "getCaseNoteSummaryByBookingId")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = CaseNoteUsageByBookingId.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<CaseNoteUsageByBookingId> getCaseNoteSummaryByBookingId(@ApiParam(value = "a list of booking ids to search.", required = true) @NotEmpty @RequestParam("bookingId") List<Integer> bookingId,
                                                                 @ApiParam(value = "Number of month to look forward (if fromDate only defined), or back (if toDate only defined). Default is 1 month", defaultValue = "1") @RequestParam(value = "numMonths", required = false, defaultValue = "1") Integer numMonths,
                                                                 @ApiParam(value = "Only case notes occurring on or after this date (in YYYY-MM-DD format) will be considered.  If not defined then the numMonth before the current date, unless a toDate is defined when it will be numMonths before toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "fromDate", required = false) LocalDate fromDate,
                                                                 @ApiParam(value = "Only case notes occurring on or before this date (in YYYY-MM-DD format) will be considered. If not defined then the current date will be used, unless a fromDate is defined when it will be numMonths after fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "toDate", required = false) LocalDate toDate,
                                                                 @ApiParam(value = "Case note type.") @RequestParam(value = "type", required = false) String type,
                                                                 @ApiParam(value = "Case note sub-type.") @RequestParam(value = "subType", required = false) String subType);

    @PostMapping("/staff-usage")
    @ApiOperation(value = "Retrieves list of case notes grouped by type/sub-type and staff", notes = "Retrieves list of case notes grouped by type/sub-type and staff", nickname = "getCaseNoteStaffUsageSummaryByPost")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The case note usage list is returned.", response = CaseNoteStaffUsage.class, responseContainer = "List")})
    List<CaseNoteStaffUsage> getCaseNoteStaffUsageSummaryByPost(@ApiParam(value = "", required = true) @RequestBody CaseNoteStaffUsageRequest body);

    @PostMapping("/usage")
    @ApiOperation(value = "Retrieves list of case notes grouped by type and offender", notes = "Retrieves list of case notes grouped by type and offender", nickname = "getCaseNoteUsageSummaryByPost")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The case note usage list is returned.", response = CaseNoteUsage.class, responseContainer = "List")})
    List<CaseNoteUsage> getCaseNoteUsageSummaryByPost(@ApiParam(value = "", required = true) @RequestBody CaseNoteUsageRequest body);

    @GetMapping("/events_no_limit")
    @ApiIgnore
    @ApiOperation(value = "Fetch all case notes of a given type since a given date", notes = "Returns all case notes - consumed by Delius<br/>" +
            "These are generated by  whenever a case note is created or amended for an offender.<br/>" +
            "*Note:* An alternative call [GET /case_notes/for_delius](#case-notes-feed-fetch-case-notes-direct-get) has been created for performance reasons.<br/>" +
            "Some case notes are automatically generated by the system in response to an action relating to the offender e.g. An alert being raised or a prison transfer.<br/>" +
            "The note type only filters at the top note type level not the sub type.<br/>" +
            "note_type can be presented multiples times in the URL to filter by multiple note types.", nickname = "getCaseNotesEvents")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = CaseNoteEvent.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<CaseNoteEvent> getCaseNotesEventsNoLimit(@ApiParam(value = "a list of types and optionally subtypes (joined with +) to search.", example = "ACP+ASSESSMENT", required = true) @NotEmpty @RequestParam("type") List<String> noteTypes,
                                                  @ApiParam(value = "Only case notes occurring on or after this date and time (ISO 8601 format without timezone e.g. YYYY-MM-DDTHH:MM:SS) will be considered.") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam("createdDate") LocalDateTime createdDate);

    @GetMapping("/events")
    @ApiOperation(value = "Fetch all case notes of a given type since a given date", notes = "Returns all case notes - consumed by Delius<br/>" +
            "These are generated by  whenever a case note is created or amended for an offender.<br/>" +
            "*Note:* An alternative call [GET /case_notes/for_delius](#case-notes-feed-fetch-case-notes-direct-get) has been created for performance reasons.<br/>" +
            "Some case notes are automatically generated by the system in response to an action relating to the offender e.g. An alert being raised or a prison transfer.<br/>" +
            "The note type only filters at the top note type level not the sub type.<br/>" +
            "note_type can be presented multiples times in the URL to filter by multiple note types.", nickname = "getCaseNotesEvents")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = CaseNoteEvent.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<CaseNoteEvent> getCaseNotesEvents(@ApiParam(value = "a list of types and optionally subtypes (joined with +) to search.", example = "ACP+ASSESSMENT", required = true) @NotEmpty @RequestParam("type") List<String> noteTypes,
                                           @ApiParam(value = "Only case notes occurring on or after this date and time (ISO 8601 format without timezone e.g. YYYY-MM-DDTHH:MM:SS) will be considered.") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam("createdDate") LocalDateTime createdDate,
                                           @ApiParam(name = "limit", value = "Number of events to return", example = "100") @RequestParam("limit") Long limit);
}
