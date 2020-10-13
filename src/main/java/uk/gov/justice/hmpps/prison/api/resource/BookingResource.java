package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.justice.hmpps.prison.api.model.Account;
import uk.gov.justice.hmpps.prison.api.model.Alert;
import uk.gov.justice.hmpps.prison.api.model.AlertChanges;
import uk.gov.justice.hmpps.prison.api.model.AlertCreated;
import uk.gov.justice.hmpps.prison.api.model.Alias;
import uk.gov.justice.hmpps.prison.api.model.Assessment;
import uk.gov.justice.hmpps.prison.api.model.BedAssignment;
import uk.gov.justice.hmpps.prison.api.model.CaseNote;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteCount;
import uk.gov.justice.hmpps.prison.api.model.Contact;
import uk.gov.justice.hmpps.prison.api.model.ContactDetail;
import uk.gov.justice.hmpps.prison.api.model.CourtCase;
import uk.gov.justice.hmpps.prison.api.model.CreateAlert;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.IepLevelAndComment;
import uk.gov.justice.hmpps.prison.api.model.ImageDetail;
import uk.gov.justice.hmpps.prison.api.model.IncidentCase;
import uk.gov.justice.hmpps.prison.api.model.InmateBasicDetails;
import uk.gov.justice.hmpps.prison.api.model.InmateDetail;
import uk.gov.justice.hmpps.prison.api.model.Keyworker;
import uk.gov.justice.hmpps.prison.api.model.MilitaryRecords;
import uk.gov.justice.hmpps.prison.api.model.Movement;
import uk.gov.justice.hmpps.prison.api.model.NewAppointment;
import uk.gov.justice.hmpps.prison.api.model.NewBooking;
import uk.gov.justice.hmpps.prison.api.model.NewCaseNote;
import uk.gov.justice.hmpps.prison.api.model.OffenceDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenceHistoryDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.api.model.OffenderIdentifier;
import uk.gov.justice.hmpps.prison.api.model.OffenderNonAssociationDetails;
import uk.gov.justice.hmpps.prison.api.model.OffenderRelationship;
import uk.gov.justice.hmpps.prison.api.model.OffenderSummary;
import uk.gov.justice.hmpps.prison.api.model.PersonalCareNeeds;
import uk.gov.justice.hmpps.prison.api.model.PhysicalAttributes;
import uk.gov.justice.hmpps.prison.api.model.PhysicalCharacteristic;
import uk.gov.justice.hmpps.prison.api.model.PhysicalMark;
import uk.gov.justice.hmpps.prison.api.model.PrivilegeSummary;
import uk.gov.justice.hmpps.prison.api.model.ProfileInformation;
import uk.gov.justice.hmpps.prison.api.model.PropertyContainer;
import uk.gov.justice.hmpps.prison.api.model.ReasonableAdjustments;
import uk.gov.justice.hmpps.prison.api.model.RecallBooking;
import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent;
import uk.gov.justice.hmpps.prison.api.model.SecondaryLanguage;
import uk.gov.justice.hmpps.prison.api.model.SentenceAdjustmentDetail;
import uk.gov.justice.hmpps.prison.api.model.SentenceDetail;
import uk.gov.justice.hmpps.prison.api.model.UpdateAttendance;
import uk.gov.justice.hmpps.prison.api.model.UpdateAttendanceBatch;
import uk.gov.justice.hmpps.prison.api.model.UpdateCaseNote;
import uk.gov.justice.hmpps.prison.api.model.VisitBalances;
import uk.gov.justice.hmpps.prison.api.model.VisitDetails;
import uk.gov.justice.hmpps.prison.api.model.VisitWithVisitors;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationSummary;
import uk.gov.justice.hmpps.prison.api.support.Order;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Api(tags = {"/bookings"})
@SuppressWarnings("unused")
public interface BookingResource {

    @GetMapping
    @ApiOperation(value = "Summary information for all offenders accessible to current user.", notes = "Summary information for all offenders accessible to current user.", nickname = "getOffenderBookings")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Summary information for all offenders accessible to current user."),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    ResponseEntity<List<OffenderBooking>> getOffenderBookings(@ApiParam(value = "Search parameters with the format [connector]:&lt;fieldName&gt;:&lt;operator&gt;:&lt;value&gt;:[format],... <p>Connector operators - and, or <p>Supported Operators - eq, neq, gt, gteq, lt, lteq, like, in</p> <p>Supported Fields - bookingNo, bookingId, offenderNo, firstName, lastName, agencyId, assignedLivingUnitId, assignedOfficerUserId</p> ") @RequestParam(value = "query", required = false) String query,
                                                              @ApiParam(value = "The booking ids of offender") @RequestParam(value = "bookingId", required = false) List<Long> bookingId,
                                                              @ApiParam(value = "The required offender numbers") @RequestParam(value = "offenderNo", required = false) List<String> offenderNo,
                                                              @ApiParam(value = "return IEP level data", defaultValue = "false") @RequestParam(value = "iepLevel", defaultValue = "false", required = false) boolean iepLevel,
                                                              @ApiParam(value = "Requested offset of first record in returned collection of booking records.", defaultValue = "0") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
                                                              @ApiParam(value = "Requested limit to number of booking records returned.", defaultValue = "10") @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) Long pageLimit,
                                                              @ApiParam(value = "Comma separated list of one or more of the following fields - <b>bookingNo, bookingId, offenderNo, firstName, lastName, agencyId, assignedLivingUnitId</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                                              @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/{bookingId}")
    @ApiOperation(value = "Offender detail.", notes = "Offender detail.", nickname = "getOffenderBooking")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    InmateDetail getOffenderBooking(@ApiParam(value = "The booking id of offender", required = true) @PathVariable("bookingId") Long bookingId,
                                    @ApiParam(value = "If set to true then only basic data is returned", defaultValue = "false") @RequestParam(value = "basicInfo", required = false, defaultValue = "false") final boolean basicInfo,
                                    @ApiParam(value = "Only used when requesting more than basic data, returns identifiers,offences,aliases,sentence dates,convicted status", defaultValue = "false") @RequestParam(value = "extraInfo", required = false, defaultValue = "false") final boolean extraInfo);

    @GetMapping("/{bookingId}/movement/{sequenceNumber}")
    @ApiOperation(value = "Retrieves a specific movement for a booking", notes = "Must booking in user caseload or have system privilege")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    Movement getMovementByBookingIdAndSequence(@ApiParam(value = "The booking id of offender", required = true) @PathVariable("bookingId") final Long bookingId,
                                               @ApiParam(value = "The sequence Number of the movement", required = true) @PathVariable("sequenceNumber") final Integer sequenceNumber);

    @GetMapping("/{bookingId}/activities")
    @ApiOperation(value = "All scheduled activities for offender.", notes = "All scheduled activities for offender.", nickname = "getBookingActivities")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    ResponseEntity<List<ScheduledEvent>> getBookingActivities(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId,
                                                              @ApiParam(value = "Returned activities must be scheduled on or after this date (in YYYY-MM-DD format).") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "fromDate", required = false) LocalDate fromDate,
                                                              @ApiParam(value = "Returned activities must be scheduled on or before this date (in YYYY-MM-DD format).") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "toDate", required = false) LocalDate toDate,
                                                              @ApiParam(value = "Requested offset of first record in returned collection of activity records.", defaultValue = "0") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
                                                              @ApiParam(value = "Requested limit to number of activity records returned.", defaultValue = "10") @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) Long pageLimit,
                                                              @ApiParam(value = "Comma separated list of one or more of the following fields - <b>eventDate, startTime, endTime, eventLocation</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                                              @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/{bookingId}/activities/today")
    @ApiOperation(value = "Today's scheduled activities for offender.", notes = "Today's scheduled activities for offender.", nickname = "getBookingActivitiesForToday")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<ScheduledEvent> getBookingActivitiesForToday(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId,
                                                      @ApiParam(value = "Comma separated list of one or more of the following fields - <b>eventDate, startTime, endTime, eventLocation</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                                      @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/{bookingId}/adjudications")
    @ApiOperation(value = "Offender adjudications summary (awards and sanctions).", notes = "Offender adjudications (awards and sanctions).", nickname = "getAdjudicationSummary")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    AdjudicationSummary getAdjudicationSummary(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId,
                                               @ApiParam(value = "Only awards ending on or after this date (in YYYY-MM-DD format) will be considered.") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "awardCutoffDate", required = false) LocalDate awardCutoffDate,
                                               @ApiParam(value = "Only proved adjudications ending on or after this date (in YYYY-MM-DD format) will be counted.") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "adjudicationCutoffDate", required = false) LocalDate adjudicationCutoffDate);

    @GetMapping("/{bookingId}/alerts")
    @ApiOperation(value = "Offender alerts.", notes = "Offender alerts.", nickname = "getOffenderAlerts")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    ResponseEntity<List<Alert>> getOffenderAlerts(@ApiParam(value = "The booking id of offender", required = true) @PathVariable("bookingId") Long bookingId,
                                                  @ApiParam(value = "Search parameters with the format [connector]:&lt;fieldName&gt;:&lt;operator&gt;:&lt;value&gt;:[format],... <p>Connector operators - and, or <p>Supported Operators - eq, neq, gt, gteq, lt, lteq, like, in</p> <p>Supported Fields - alertType, alertCode, dateCreated, dateExpires</p> ", required = true) @RequestParam(value = "query", required = false) String query,
                                                  @ApiParam(value = "Requested offset of first record in returned collection of alert records.", defaultValue = "0") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
                                                  @ApiParam(value = "Requested limit to number of alert records returned.", defaultValue = "10") @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) Long pageLimit,
                                                  @ApiParam(value = "Comma separated list of one or more of the following fields - <b>alertType, alertCode, dateCreated, dateExpires</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                                  @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/{bookingId}/alerts/{alertId}")
    @ApiOperation(value = "Offender alert detail.", notes = "Offender alert detail.", nickname = "getOffenderAlert")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    Alert getOffenderAlert(@ApiParam(value = "The booking id of offender", required = true) @PathVariable("bookingId") Long bookingId,
                           @ApiParam(value = "The Alert Id", required = true) @PathVariable("alertId") Long alertId);

    @GetMapping("/{bookingId}/aliases")
    @ApiOperation(value = "Offender aliases.", notes = "Offender aliases.", nickname = "getOffenderAliases")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    ResponseEntity<List<Alias>> getOffenderAliases(@ApiParam(value = "The booking id of offender", required = true) @PathVariable("bookingId") Long bookingId,
                                                   @ApiParam(value = "Requested offset of first record in returned collection of alias records.", defaultValue = "0") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
                                                   @ApiParam(value = "Requested limit to number of alias records returned.", defaultValue = "5") @RequestHeader(value = "Page-Limit", defaultValue = "5", required = false) Long pageLimit,
                                                   @ApiParam(value = "Comma separated list of one or more of the following fields - <b>firstName, lastName, age, dob, middleName, nameType, createDate</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                                   @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/{bookingId}/appointments")
    @ApiOperation(value = "All scheduled appointments for offender.", notes = "All scheduled appointments for offender.", nickname = "getBookingsBookingIdAppointments")
    ResponseEntity<List<ScheduledEvent>> getBookingsBookingIdAppointments(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId,
                                                                          @ApiParam(value = "Returned appointments must be scheduled on or after this date (in YYYY-MM-DD format).") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "fromDate", required = false) LocalDate fromDate,
                                                                          @ApiParam(value = "Returned appointments must be scheduled on or before this date (in YYYY-MM-DD format).") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "toDate", required = false) LocalDate toDate,
                                                                          @ApiParam(value = "Requested offset of first record in returned collection of appointment records.", defaultValue = "0") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
                                                                          @ApiParam(value = "Requested limit to number of appointment records returned.", defaultValue = "10") @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) Long pageLimit,
                                                                          @ApiParam(value = "Comma separated list of one or more of the following fields - <b>eventDate, startTime, endTime, eventLocation</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                                                          @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/{bookingId}/appointments/nextWeek")
    @ApiOperation(value = "Scheduled appointments for offender for following week.", notes = "Scheduled appointments for offender for following week.", nickname = "getBookingAppointmentsForNextWeek")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<ScheduledEvent> getBookingAppointmentsForNextWeek(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId,
                                                           @ApiParam(value = "Comma separated list of one or more of the following fields - <b>eventDate, startTime, endTime, eventLocation</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                                           @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/{bookingId}/appointments/thisWeek")
    @ApiOperation(value = "Scheduled appointments for offender for coming week (from current day).", notes = "Scheduled appointments for offender for coming week (from current day).", nickname = "getBookingAppointmentsForThisWeek")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<ScheduledEvent> getBookingAppointmentsForThisWeek(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId,
                                                           @ApiParam(value = "Comma separated list of one or more of the following fields - <b>eventDate, startTime, endTime, eventLocation</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                                           @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/{bookingId}/appointments/today")
    @ApiOperation(value = "Today's scheduled appointments for offender.", notes = "Today's scheduled appointments for offender.", nickname = "getBookingAppointmentsForToday")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<ScheduledEvent> getBookingAppointmentsForToday(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId,
                                                        @ApiParam(value = "Comma separated list of one or more of the following fields - <b>eventDate, startTime, endTime, eventLocation</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                                        @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/{bookingId}/assessment/{assessmentCode}")
    @ApiOperation(value = "Offender assessment detail.", notes = "Offender assessment detail.", nickname = "getAssessmentByCode")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    Assessment getAssessmentByCode(@ApiParam(value = "The booking id of offender", required = true) @PathVariable("bookingId") Long bookingId,
                                   @ApiParam(value = "Assessment Type Code", required = true) @PathVariable("assessmentCode") String assessmentCode);

    @GetMapping("/{bookingId}/assessments")
    @ApiOperation(value = "Assessment Information", notes = "Assessment Information", nickname = "getAssessments")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<Assessment> getAssessments(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId);

    @GetMapping("/{bookingId}/balances")
    @ApiOperation(value = "Offender account balances.", notes = "Offender account balances.", nickname = "getBalances")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    Account getBalances(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId);

    @GetMapping("/{bookingId}/caseNotes")
    @ApiOperation(value = "Offender case notes.", notes = "Offender case notes.", nickname = "getOffenderCaseNotes")
    ResponseEntity<List<CaseNote>> getOffenderCaseNotes(@ApiParam(value = "The booking id of offender", required = true) @PathVariable("bookingId") Long bookingId,
                                                        @ApiParam(value = "start contact date to search from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "from", required = false) LocalDate from,
                                                        @ApiParam(value = "end contact date to search up to (including this date)") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "to", required = false) LocalDate to,
                                                        @ApiParam(value = "Search parameters with the format [connector]:&lt;fieldName&gt;:&lt;operator&gt;:&lt;value&gt;:[format],... <p>Connector operators - and, or <p>Supported Operators - eq, neq, gt, gteq, lt, lteq, like, in</p> <p>Supported Fields - creationDateTime, type, subType, source</p> ", required = true) @RequestParam(value = "query", required = false) String query,
                                                        @ApiParam(value = "Requested offset of first record in returned collection of caseNote records.", defaultValue = "0") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
                                                        @ApiParam(value = "Requested limit to number of caseNote records returned.", defaultValue = "10") @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) Long pageLimit,
                                                        @ApiParam(value = "Comma separated list of one or more of the following fields - <b>creationDateTime, type, subType, source</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                                        @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/{bookingId}/caseNotes/{caseNoteId}")
    @ApiOperation(value = "Offender case note detail.", notes = "Offender case note detail.", nickname = "getOffenderCaseNote")
    CaseNote getOffenderCaseNote(@ApiParam(value = "The booking id of offender", required = true) @PathVariable("bookingId") Long bookingId,
                                 @ApiParam(value = "The case note id", required = true) @PathVariable("caseNoteId") Long caseNoteId);

    @GetMapping("/{bookingId}/caseNotes/{type}/{subType}/count")
    @ApiOperation(value = "Count of case notes", notes = "Count of case notes", nickname = "getCaseNoteCount")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    CaseNoteCount getCaseNoteCount(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId,
                                   @ApiParam(value = "Case note type.", required = true) @PathVariable("type") String type,
                                   @ApiParam(value = "Case note sub-type.", required = true) @PathVariable("subType") String subType,
                                   @ApiParam(value = "Only case notes occurring on or after this date (in YYYY-MM-DD format) will be considered.") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "fromDate", required = false) LocalDate fromDate,
                                   @ApiParam(value = "Only case notes occurring on or before this date (in YYYY-MM-DD format) will be considered.") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "toDate", required = false) LocalDate toDate);

    @GetMapping("/{bookingId}/contacts")
    @ApiOperation(value = "Offender contacts (e.g. next of kin).", notes = "Offender contacts (e.g. next of kin).", nickname = "getContacts")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    ContactDetail getContacts(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId);

    @GetMapping("/{bookingId}/events/nextWeek")
    @ApiOperation(value = "Scheduled events for offender for following week.", notes = "Scheduled events for offender for following week.", nickname = "getEventsNextWeek")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<ScheduledEvent> getEventsNextWeek(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId);

    @GetMapping("/{bookingId}/events/thisWeek")
    @ApiOperation(value = "Scheduled events for offender for coming week (from current day).", notes = "Scheduled events for offender for coming week (from current day).", nickname = "getEventsThisWeek")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<ScheduledEvent> getEventsThisWeek(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId);

    @GetMapping("/{bookingId}/events")
    @ApiOperation(value = "All scheduled events for offender.", notes = "All scheduled events for offender.", nickname = "getEvents")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<ScheduledEvent> getEvents(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId,
                                   @ApiParam(value = "Returned events must be scheduled on or after this date (in YYYY-MM-DD format).") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "fromDate", required = false) LocalDate fromDate,
                                   @ApiParam(value = "Returned events must be scheduled on or before this date (in YYYY-MM-DD format).") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "toDate", required = false) LocalDate toDate);

    @GetMapping("/{bookingId}/events/today")
    @ApiOperation(value = "Today's scheduled events for offender.", notes = "Today's scheduled events for offender.", nickname = "getEventsToday")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<ScheduledEvent> getEventsToday(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId);

    @GetMapping("/{bookingId}/identifiers")
    @ApiOperation(value = "Identifiers for this booking", notes = "Identifiers for this booking", nickname = "getOffenderIdentifiers")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<OffenderIdentifier> getOffenderIdentifiers(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") @NotNull Long bookingId,
                                                    @ApiParam(value = "Filter By Type", example = "PNC") @RequestParam(value = "type", required = false) final String type);

    @GetMapping("/{bookingId}/iepSummary")
    @ApiOperation(value = "Offender IEP (Incentives & Earned Privileges) summary.", notes = "Offender IEP (Incentives & Earned Privileges) summary.", nickname = "getBookingIEPSummary")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    PrivilegeSummary getBookingIEPSummary(@ApiParam(value = "The booking id of offender", required = true) @PathVariable("bookingId") Long bookingId,
                                          @ApiParam(value = "Toggle to return IEP detail entries in response (or not).", required = true) @RequestParam(value = "withDetails", required = false, defaultValue = "false") boolean withDetails);

    @PostMapping("/{bookingId}/iepLevels")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation("Add a new IEP (Incentives & Earned Privileges) level to an offender's booking.")
    ResponseEntity<Void> addIepLevel(@ApiParam(value = "The booking id of the offender", required = true) @PathVariable("bookingId") Long bookingId,
                     @ApiParam(value = "The new IEP Level and accompanying comment (reason for change).", required = true) @NotNull @RequestBody IepLevelAndComment iepLevel);

    @GetMapping("/offenders/iepSummary")
    @ApiOperation(value = "Offenders IEP (Incentives & Earned Privileges) summary.", notes = "Offenders IEP (Incentives & Earned Privileges) summary.", nickname = "getBookingIEPSummaryForOffenders")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    Collection<PrivilegeSummary> getBookingIEPSummaryForOffenders(
            @ApiParam(value = "The booking ids of offender", required = true) @RequestParam("bookings") List<Long> bookings,
            @ApiParam(value = "Toggle to return IEP detail entries in response (or not).", required = true) @RequestParam(value = "withDetails", required = false, defaultValue = "false") boolean withDetails);

    @GetMapping("/{bookingId}/image")
    @ApiOperation(value = "Image detail (without image data).", notes = "Image detail (without image data).", nickname = "getMainImageForBookings")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    ImageDetail getMainImageForBookings(@ApiParam(value = "The booking id of offender", required = true) @PathVariable("bookingId") Long bookingId);

    @GetMapping(value = "/{bookingId}/image/data", produces = "image/jpeg")
    @ApiOperation(value = "Image data (as bytes).", notes = "Image data (as bytes).", nickname = "getMainBookingImageData")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Requested resource not found."),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.")})
    ResponseEntity<byte[]> getMainBookingImageData(@ApiParam(value = "The booking id of offender", required = true) @PathVariable("bookingId") Long bookingId,
                                              @ApiParam(value = "Return full size image", defaultValue = "false") @RequestParam(value = "fullSizeImage", defaultValue = "false", required = false) boolean fullSizeImage);

    @GetMapping("/{bookingId}/mainOffence")
    @ApiOperation(value = "Get Offender main offence detail.", notes = "Offender main offence detail.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<OffenceDetail> getMainOffence(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId);

    @GetMapping("/{bookingId}/property")
    @ApiOperation(value = "List of active property containers", notes = "List of active property containers", nickname = "getOffenderPropertyContainers")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<PropertyContainer> getOffenderPropertyContainers(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId);

    @PostMapping("/mainOffence")
    @ApiOperation(value = "Get Offender main offence detail.", notes = "Post version to allow specifying a large number of bookingIds.")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<OffenceDetail> getMainOffence(@ApiParam(value = "The bookingIds to identify the offenders", required = true) @RequestBody Set<Long> body);

    @GetMapping("/offenderNo/{offenderNo}/offenceHistory")
    @ApiOperation(value = "Offence history.", notes = "All Offences recorded for this offender.", nickname = "getOffenceHistory")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<OffenceHistoryDetail> getOffenceHistory(@ApiParam(value = "The offender number", required = true ) @PathVariable("offenderNo") String offenderNo,
                                                 @ApiParam(value = "include offences with convictions only", defaultValue = "true") @RequestParam(value = "convictionsOnly", required = false, defaultValue = "true") boolean convictionsOnly);

    @GetMapping("/{bookingId}/physicalAttributes")
    @ApiOperation(value = "Offender Physical Attributes.", notes = "Offender Physical Attributes.", nickname = "getPhysicalAttributes")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    PhysicalAttributes getPhysicalAttributes(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId);

    @GetMapping("/{bookingId}/physicalCharacteristics")
    @ApiOperation(value = "Physical Characteristics", notes = "Physical Characteristics", nickname = "getPhysicalCharacteristics")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<PhysicalCharacteristic> getPhysicalCharacteristics(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId);

    @GetMapping("/{bookingId}/physicalMarks")
    @ApiOperation(value = "Physical Mark Information", notes = "Physical Mark Information", nickname = "getPhysicalMarks")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<PhysicalMark> getPhysicalMarks(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId);

    @GetMapping("/{bookingId}/personal-care-needs")
    @ApiOperation(value = "Personal Care Needs", notes = "Personal Care Need", nickname = "getPersonalCareNeeds")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    PersonalCareNeeds getPersonalCareNeeds(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId,
                                           @ApiParam(value = "a list of types and optionally subtypes (joined with +) to search.", example = "DISAB+RM", required = true) @NotEmpty(message = "problemTypes: must not be empty") @RequestParam(value = "type", required = false) List<String> problemTypes);

    @GetMapping("/{bookingId}/military-records")
    @ApiOperation(value = "Military Records", notes = "Military Records", nickname = "getMilitaryRecords")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    MilitaryRecords getMilitaryRecords(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId);

    @PostMapping("/offenderNo/personal-care-needs")
    @ApiOperation(value = "Personal Care Needs  - POST version to allow for large numbers of offenders", notes = "Personal Care Needs", nickname = "getPersonalCareNeeds")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<PersonalCareNeeds> getPersonalCareNeeds(@ApiParam(value = "The required offender numbers (mandatory)", required = true) @NotEmpty(message = "offenderNo: must not be empty") @RequestBody List<String> offenderNo,
                                                 @ApiParam(value = "a list of types and optionally subtypes (joined with +) to search.", example = "DISAB+RM", required = true) @NotEmpty(message = "problemTypes: must not be empty") @RequestParam(value = "type", required = false) List<String> problemTypes);

    @GetMapping("/{bookingId}/reasonable-adjustments")
    @ApiOperation(value = "Reasonable Adjustment Information", notes = "Reasonable Adjustment Information", nickname = "getReasonableAdjustment")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    ReasonableAdjustments getReasonableAdjustments(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId,
                                                   @ApiParam(value = "a list of treatment codes to search.", example = "PEEP", required = true) @NotEmpty(message = "treatmentCodes: must not be empty") @RequestParam(value = "type", required = false) List<String> treatmentCodes);

    @GetMapping("/{bookingId}/profileInformation")
    @ApiOperation(value = "Profile Information", notes = "Profile Information", nickname = "getProfileInformation")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<ProfileInformation> getProfileInformation(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId);

    @GetMapping("/{bookingId}/relationships")
    @ApiOperation(value = "The contact details and their relationship to the offender", notes = "The contact details and their relationship to the offender", nickname = "getRelationships")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<Contact> getRelationships(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId,
                                   @ApiParam(value = "filter by the relationship type") @RequestParam(value = "relationshipType", required = false) String relationshipType);

    @GetMapping("/{bookingId}/sentenceDetail")
    @ApiOperation(value = "Offender sentence detail (key dates and additional days awarded).", nickname = "getBookingSentenceDetail",
            notes = "<h3>Algorithm</h3><ul><li>If there is a confirmed release date, the offender release date is the confirmed release date.</li><li>If there is no confirmed release date for the offender, the offender release date is either the actual parole date or the home detention curfew actual date.</li><li>If there is no confirmed release date, actual parole date or home detention curfew actual date for the offender, the release date is the later of the nonDtoReleaseDate or midTermDate value (if either or both are present)</li></ul>")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    SentenceDetail getBookingSentenceDetail(@ApiParam(value = "The booking id of offender", required = true) @PathVariable("bookingId") Long bookingId);

    @GetMapping("/{bookingId}/sentenceAdjustments")
    @ApiOperation(value = "Offender sentence adjustments.", nickname = "getBookingSentenceAdjustments")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    SentenceAdjustmentDetail getBookingSentenceAdjustments(@ApiParam(value = "The booking id of offender", required = true) @PathVariable("bookingId") Long bookingId);

    @GetMapping("/{bookingId}/visits")
    @ApiOperation(value = "All scheduled visits for offender.", notes = "All scheduled visits for offender.", nickname = "getBookingVisits")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    ResponseEntity<List<ScheduledEvent>> getBookingVisits(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId,
                                                          @ApiParam(value = "Returned visits must be scheduled on or after this date (in YYYY-MM-DD format).") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "fromDate", required = false) LocalDate fromDate,
                                                          @ApiParam(value = "Returned visits must be scheduled on or before this date (in YYYY-MM-DD format).") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "toDate", required = false) LocalDate toDate,
                                                          @ApiParam(value = "Requested offset of first record in returned collection of visit records.", defaultValue = "0") @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) Long pageOffset,
                                                          @ApiParam(value = "Requested limit to number of visit records returned.", defaultValue = "10") @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) Long pageLimit,
                                                          @ApiParam(value = "Comma separated list of one or more of the following fields - <b>eventDate, startTime, endTime, eventLocation</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                                          @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/{bookingId}/visits-with-visitors")
    @ApiOperation(value = "All visits for offender.", notes = "All visits for offender.", nickname = "getBookingVisitsWithVisitor")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    Page<VisitWithVisitors<VisitDetails>> getBookingVisitsWithVisitor(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId,
                                                                      @ApiParam(value = "Returned visits must be scheduled on or after this date (in YYYY-MM-DD format).") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "fromDate", required = false) LocalDate fromDate,
                                                                      @ApiParam(value = "Returned visits must be scheduled on or before this date (in YYYY-MM-DD format).") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value = "toDate", required = false) LocalDate toDate,
                                                                      @ApiParam(value = "Type of visit. One of SCON, OFFI") @RequestParam(value = "visitType", required = false) String visitType,
                                                                      @ApiParam(value = "Target page number, zero being the first page") @RequestParam(value = "page", required = false) final Integer page,
                                                                      @ApiParam(value = "The number of results per page") @RequestParam(value = "size", required = false) final Integer size);

    @GetMapping("/{bookingId}/visits/last")
    @ApiOperation(value = "The most recent visit for the offender.", notes = "The most recent visit for the offender.", nickname = "getBookingVisitsLast")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    VisitDetails getBookingVisitsLast(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId);

    @GetMapping("/{bookingId}/visits/next")
    @ApiOperation(value = "The next visit for the offender.", notes = "The next visit for the offender.", nickname = "getBookingVisitsNext")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    VisitDetails getBookingVisitsNext(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId);

    @GetMapping("/{bookingId}/visits/today")
    @ApiOperation(value = "Today's scheduled visits for offender.", notes = "Today's scheduled visits for offender.", nickname = "getBookingVisitsForToday")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<ScheduledEvent> getBookingVisitsForToday(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId,
                                                  @ApiParam(value = "Comma separated list of one or more of the following fields - <b>eventDate, startTime, endTime, eventLocation</b>") @RequestHeader(value = "Sort-Fields", required = false) String sortFields,
                                                  @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) Order sortOrder);

    @GetMapping("/offenderNo/{offenderNo}/visit/balances")
    @ApiOperation(value = "Balances visit orders and privilege visit orders for offender.", notes = "Balances visit orders and privilege visit orders for offender.", nickname = "getBookingVisitsBalances")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    VisitBalances getBookingVisitBalances(@ApiParam(value = "The offenderNo of offender", required = true) @PathVariable("offenderNo") String offenderNo);

    @GetMapping("/offenderNo/{offenderNo}")
    @ApiOperation(value = "Offender detail.", notes = "Offender detail.", nickname = "getOffenderBookingByOffenderNo")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    InmateDetail getOffenderBookingByOffenderNo(@ApiParam(value = "The offenderNo of offender", required = true) @PathVariable("offenderNo") String offenderNo,
                                                @ApiParam(value = "If set to true then full data is returned", defaultValue = "false") @RequestParam(value = "fullInfo", required = false, defaultValue = "false") final boolean fullInfo,
                                                @ApiParam(value = "Only used when fullInfo=true, returns identifiers,offences,aliases,sentence dates,convicted status", defaultValue = "false") @RequestParam(value = "extraInfo", required = false, defaultValue = "false") final boolean extraInfo);

    @PostMapping("/offenders")
    @ApiOperation(value = "Offender detail.", notes = "Offender detail for offenders", nickname = "getBasicInmateDetailsForOffenders")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<InmateBasicDetails> getBasicInmateDetailsForOffenders(@ApiParam(value = "The offenderNo of offender", required = true) @RequestBody Set<String> offenders,
                                                               @ApiParam(value = "Returns only Offender details with an active booking if true, otherwise Offenders without an active booking are included", defaultValue = "true") @RequestParam(value = "activeOnly", required = false, defaultValue = "true") Boolean activeOnly);

    @PostMapping("/offenders/{agencyId}/list")
    @ApiOperation(value = "Basic offender details by booking ids - POST version to allow for large numbers", notes = "Basic offender details by booking ids", nickname = "getBasicInmateDetailsForOffendersByBookingIds")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<InmateBasicDetails> getBasicInmateDetailsByBookingIds(@ApiParam(value = "The prison where the offenders are booked - the response is restricted to bookings at this prison", required = true) @PathVariable("agencyId") String agencyId,
                                                               @ApiParam(value = "The bookingIds to identify the offenders", required = true) @RequestBody Set<Long> bookingIds);

    @GetMapping(value = "/offenderNo/{offenderNo}/image/data", produces = "image/jpeg")
    @ApiOperation(value = "Image data (as bytes).", notes = "Image data (as bytes).", nickname = "getMainBookingImageDataByNo")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Requested resource not found."),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.")})
    ResponseEntity<byte[]> getMainBookingImageDataByNo(@ApiParam(value = "The offender No of offender", required = true) @PathVariable("offenderNo") String offenderNo,
                                                  @ApiParam(value = "Return full size image", defaultValue = "false") @RequestParam(value = "fullSizeImage", defaultValue = "false", required = false) boolean fullSizeImage);

    @Deprecated
    @GetMapping("/offenderNo/{offenderNo}/key-worker")
    @ApiOperation(value = "Key worker details.", notes = "Key worker details. This should not be used - call keywork API instead")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    Keyworker getKeyworkerByOffenderNo(@ApiParam(value = "The offenderNo of offender", required = true) @PathVariable("offenderNo") String offenderNo);

    @GetMapping("/offenderNo/{offenderNo}/relationships")
    @ApiOperation(value = "The contact details and their relationship to the offender", notes = "The contact details and their relationship to the offender", nickname = "getRelationshipsByOffenderNo")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    List<Contact> getRelationshipsByOffenderNo(@ApiParam(value = "The offender Offender No", required = true) @PathVariable("offenderNo") String offenderNo,
                                               @ApiParam(value = "filter by the relationship type") @RequestParam("relationshipType") String relationshipType);

    @PostMapping
    @ApiOperation(value = "Create booking for offender (additionally creating offender record if one does not already exist).", notes = "<b>(BETA)</b> Create booking for offender (additionally creating offender record if one does not already exist).", nickname = "createOffenderBooking")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Offender booking created.", response = OffenderSummary.class),
            @ApiResponse(code = 204, message = "Request in progress."),
            @ApiResponse(code = 400, message = "Request to create offender booking failed. Consult response for reason.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "User not authorised to create offender booking.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    ResponseEntity<OffenderSummary> createOffenderBooking(@ApiParam(value = "Details required to enable creation of new offender booking (and offender, if necessary).", required = true) @Valid @RequestBody NewBooking body);

    @PostMapping("/{bookingId}/appointments")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create appointment for offender.", notes = "Create appointment for offender.", nickname = "postBookingsBookingIdAppointments")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "The appointment has been recorded. The updated object is returned including the status.", response = ScheduledEvent.class)})
    ScheduledEvent postBookingsBookingIdAppointments(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId,
                                                     @ApiParam(required = true) @RequestBody NewAppointment body);

    @PostMapping("/{bookingId}/caseNotes")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create case note for offender.", notes = "Create case note for offender.", nickname = "createBookingCaseNote")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "The Case Note has been recorded. The updated object is returned including the status.", response = CaseNote.class),
            @ApiResponse(code = 409, message = "The case note has already been recorded under the booking. The current unmodified object (including status) is returned.", response = ErrorResponse.class)})
    CaseNote createBookingCaseNote(@ApiParam(value = "The booking id of offender", required = true) @PathVariable("bookingId") Long bookingId,
                                   @ApiParam(required = true) @RequestBody NewCaseNote body);

    @PostMapping("/{bookingId}/relationships")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create a relationship with an offender", notes = "Create a relationship with an offender", nickname = "createRelationship")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "If successful the Contact object is returned.", response = Contact.class)})
    Contact createRelationship(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId,
                               @ApiParam(value = "The person details and their relationship to the offender", required = true) @RequestBody OffenderRelationship body);

    @PostMapping("/offenderNo/{agencyId}/alerts")
    @ApiOperation(value = "Get alerts for a list of offenders at a prison")
    List<Alert> getAlertsByOffenderNosAtAgency(@ApiParam(value = "The prison where the offenders are booked", required = true) @PathVariable("agencyId") String agencyId,
                                               @ApiParam(value = "The required offender numbers (mandatory)", required = true) @RequestBody List<String> body);

    @PostMapping("/offenderNo/alerts")
    @ApiOperation(value = "Get alerts for a list of offenders. Requires SYSTEM_READ_ONLY role")
    List<Alert> getAlertsByOffenderNos(@ApiParam(value = "The required offender numbers (mandatory)", required = true) @NotEmpty(message = "A minimum of one offender number is required") @RequestBody List<String> body);

    @PostMapping("/offenderNo/{offenderNo}/caseNotes")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create case note for offender.", notes = "Create case note for offender.", nickname = "createOffenderCaseNote")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "The Case Note has been recorded. The updated object is returned including the status.", response = CaseNote.class),
            @ApiResponse(code = 409, message = "The case note has already been recorded under the booking. The current unmodified object (including status) is returned.", response = ErrorResponse.class)})
    CaseNote createOffenderCaseNote(@ApiParam(value = "The offenderNo of offender", required = true) @PathVariable("offenderNo") String offenderNo,
                                    @ApiParam(required = true) @RequestBody NewCaseNote body);

    @PostMapping("/offenderNo/{offenderNo}/relationships")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Create a relationship with an offender", notes = "Create a relationship with an offender", nickname = "createRelationshipByOffenderNo")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "If successful the Contact object is returned.", response = Contact.class)})
    Contact createRelationshipByOffenderNo(@ApiParam(value = "The offender Offender No", required = true) @PathVariable("offenderNo") String offenderNo,
                                           @ApiParam(value = "The person details and their relationship to the offender", required = true) @RequestBody OffenderRelationship body);

    @PutMapping
    @ApiOperation(value = "Process recall for offender.", notes = "<b>(BETA)</b> Process recall for offender.", nickname = "recallOffenderBooking")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Offender successfully recalled."),
            @ApiResponse(code = 400, message = "Request to recall offender failed. Consult response for reason.", response = ErrorResponse.class)})
    OffenderSummary recallOffenderBooking(@ApiParam(value = "Details required to enable recall of offender.", required = true) @RequestBody @Valid RecallBooking body);

    @PutMapping("/{bookingId}/caseNotes/{caseNoteId}")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Amend offender case note.", notes = "Amend offender case note.", nickname = "updateOffenderCaseNote")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Case Note amendment processed successfully. Updated case note is returned.", response = CaseNote.class),
            @ApiResponse(code = 400, message = "Invalid request - e.g. amendment text not provided.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - user not authorised to amend case note.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Resource not found - booking or case note does not exist or is not accessible to user.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Internal server error.", response = ErrorResponse.class)})
    CaseNote updateOffenderCaseNote(@ApiParam(value = "The booking id of offender", required = true, example = "1231212") @PathVariable("bookingId") Long bookingId,
                                    @ApiParam(value = "The case note id", required = true, example = "1212134") @PathVariable("caseNoteId") Long caseNoteId,
                                    @ApiParam(required = true) @RequestBody UpdateCaseNote body);

    @PutMapping("/offenderNo/{offenderNo}/activities/{activityId}/attendance")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Attendance data has been updated"),
            @ApiResponse(code = 400, message = "Invalid request - e.g. validation error.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - user not authorised to attend activity.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Resource not found - booking or event does not exist or is not accessible to user.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Internal server error.", response = ErrorResponse.class)})
    ResponseEntity<Void> updateAttendance(@ApiParam(value = "The offenderNo of the prisoner", required = true, example = "A1234AA") @PathVariable("offenderNo") String offenderNo,
                                          @ApiParam(value = "The activity id", required = true, example = "1212131") @PathVariable("activityId") Long activityId,
                                          @ApiParam(required = true, example = "{eventOutcome = 'ATT', performance = 'ACCEPT' outcomeComment = 'Turned up very late'}") @NotNull @RequestBody UpdateAttendance updateAttendance);

    @PutMapping("/{bookingId}/activities/{activityId}/attendance")
    @ApiOperation(value = "Update offender attendance and pay.", notes = "Update offender attendance and pay.", nickname = "updateAttendance")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Attendance data has been updated"),
            @ApiResponse(code = 400, message = "Invalid request - e.g. validation error.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - user not authorised to attend activity.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Resource not found - booking or event does not exist or is not accessible to user.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Internal server error.", response = ErrorResponse.class)})
    ResponseEntity<Void> updateAttendance(@ApiParam(value = "The booking Id of the prisoner", required = true, example = "213531") @PathVariable("bookingId") @NotNull Long bookingId,
                                          @ApiParam(value = "The activity id", required = true, example = "1212131") @PathVariable("activityId") @NotNull Long activityId,
                                          @ApiParam(required = true) @NotNull @RequestBody UpdateAttendance body);

    @PutMapping("/activities/attendance")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Update attendance and pay for multiple bookings.", notes = "Update offender attendance and pay.", nickname = "updateAttendance")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Attendance data has been updated"),
            @ApiResponse(code = 400, message = "Invalid request - e.g. validation error.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - user not authorised to attend activity.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Resource not found - booking or event does not exist or is not accessible to user.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Internal server error.", response = ErrorResponse.class)})
    ResponseEntity<Void> updateAttendanceForMultipleBookingIds(@ApiParam(required = true) @NotNull @RequestBody UpdateAttendanceBatch body);

    @GetMapping("/{bookingId}/incidents")
    @ApiOperation(value = "Return a set Incidents for a given booking Id", notes = "Can be filtered by participation type and incident type")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<IncidentCase> getIncidentsByBookingId(@ApiParam(value = "bookingId", required = true) @PathVariable("bookingId") @NotNull Long bookingId,
                                               @ApiParam(value = "incidentType", example = "ASSAULT", allowMultiple = true) @RequestParam("incidentType") List<String> incidentTypes,
                                               @ApiParam(value = "participationRoles", example = "ASSIAL", allowMultiple = true, allowableValues = "ACTINV,ASSIAL,FIGHT,IMPED,PERP,SUSASS,SUSINV,VICT,AI,PAS,AO") @RequestParam("participationRoles") List<String> participationRoles);


    @PostMapping("/{bookingId}/alert")
    @ApiOperation(value = "Create an alert")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Alert id.", response = AlertCreated.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)
    })
    ResponseEntity<AlertCreated> postAlert(
            @ApiParam(value = "bookingId", required = true) @PathVariable("bookingId") Long bookingId,
            @ApiParam(value = "Alert details", required = true) @RequestBody @Valid CreateAlert alert
    );

    @PutMapping("/{bookingId}/alert/{alertSeq}")
    @ApiOperation(value = "Update an alert")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)
    })
    Alert updateAlert(
            @ApiParam(value = "bookingId", required = true) @PathVariable("bookingId") Long bookingId,
            @ApiParam(value = "alertSeq", required = true) @PathVariable("alertSeq") Long alertSeq,
            @ApiParam(value = "Alert details", required = true) @RequestBody @Valid AlertChanges alert
    );

    @GetMapping("/{bookingId}/court-cases")
    @ApiOperation(value = "Court Cases", notes = "Court Cases", nickname = "getCourtCases")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<CourtCase> getCourtCases(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId,
                                  @ApiParam(value = "Only return active court cases") @RequestParam(value = "activeOnly", required = false, defaultValue = "true") final boolean activeOnly);

    @GetMapping("/{bookingId}/secondary-languages")
    @ApiOperation(value="Get secondary languages", notes="Get secondary languages", nickname="getSecondaryLanguages")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<SecondaryLanguage> getSecondaryLanguages(@ApiParam(value = "bookingId", required = true) @PathVariable("bookingId") Long bookingId);

    @GetMapping("/{bookingId}/non-association-details")
    @ApiOperation(value = "Gets the offender non-association details for a given booking", notes = "Get offender non-association details", nickname = "getNonAssociationDetails")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    OffenderNonAssociationDetails getNonAssociationDetails(@ApiParam(value = "The offender booking id", required = true) @PathVariable("bookingId") Long bookingId);

    @GetMapping("{bookingId}/cell-history")
    @ApiResponses(value = {
                        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
                        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
                        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    Page<BedAssignment> getBedAssignmentsHistory(
            @ApiParam(value = "The offender booking linked to the court hearings.", required = true) @PathVariable("bookingId") Long bookingId,
            @ApiParam(value = "The page number to return. Index starts at 0", defaultValue = "0") @RequestParam(value = "page", required = false) Integer page,
            @ApiParam(value = "The number of results per page. Defaults to 20.", defaultValue = "20") @RequestParam(value = "size", required = false) Integer size);
}
