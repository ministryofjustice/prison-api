package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.Contact;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.model.adjudications.AdjudicationSummary;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.ResponseDelegate;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.List;
import java.util.Set;

@Api(tags = {"/bookings"})
@SuppressWarnings("unused")
public interface BookingResource {

    @GET
    @Path("/")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Summary information for all offenders accessible to current user.", notes = "Summary information for all offenders accessible to current user.", nickname = "getOffenderBookings")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Summary information for all offenders accessible to current user.", response = OffenderBooking.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    GetOffenderBookingsResponse getOffenderBookings(@ApiParam(value = "Search parameters with the format [connector]:&lt;fieldName&gt;:&lt;operator&gt;:&lt;value&gt;:[format],... <p>Connector operators - and, or <p>Supported Operators - eq, neq, gt, gteq, lt, lteq, like, in</p> <p>Supported Fields - bookingNo, bookingId, offenderNo, firstName, lastName, agencyId, assignedLivingUnitId, assignedOfficerUserId</p> ", required = true) @QueryParam("query") String query,
                                                    @ApiParam(value = "The booking ids of offender") @QueryParam("bookingId") List<Long> bookingId,
                                                    @ApiParam(value = "The required offender numbers") @QueryParam("offenderNo") List<String> offenderNo,
                                                    @ApiParam(value = "return IEP level data", defaultValue = "false") @QueryParam("iepLevel") boolean iepLevel,
                                                    @ApiParam(value = "Requested offset of first record in returned collection of booking records.", defaultValue = "0") @HeaderParam("Page-Offset") Long pageOffset,
                                                    @ApiParam(value = "Requested limit to number of booking records returned.", defaultValue = "10") @HeaderParam("Page-Limit") Long pageLimit,
                                                    @ApiParam(value = "Comma separated list of one or more of the following fields - <b>bookingNo, bookingId, offenderNo, firstName, lastName, agencyId, assignedLivingUnitId</b>") @HeaderParam("Sort-Fields") String sortFields,
                                                    @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @HeaderParam("Sort-Order") Order sortOrder);

    @GET
    @Path("/{bookingId}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Offender detail.", notes = "Offender detail.", nickname = "getOffenderBooking")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = InmateDetail.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    GetOffenderBookingResponse getOffenderBooking(@ApiParam(value = "The booking id of offender", required = true) @PathParam("bookingId") Long bookingId,
                                                  @ApiParam(value = "If set to true then only basic data is returned", defaultValue = "false") @QueryParam("basicInfo") boolean basicInfo);

    @GET
    @Path("/{bookingId}/activities")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "All scheduled activities for offender.", notes = "All scheduled activities for offender.", nickname = "getBookingActivities")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ScheduledEvent.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    GetBookingActivitiesResponse getBookingActivities(@ApiParam(value = "The offender booking id", required = true) @PathParam("bookingId") Long bookingId,
                                                      @ApiParam(value = "Returned activities must be scheduled on or after this date (in YYYY-MM-DD format).") @QueryParam("fromDate") String fromDate,
                                                      @ApiParam(value = "Returned activities must be scheduled on or before this date (in YYYY-MM-DD format).") @QueryParam("toDate") String toDate,
                                                      @ApiParam(value = "Requested offset of first record in returned collection of activity records.", defaultValue = "0") @HeaderParam("Page-Offset") Long pageOffset,
                                                      @ApiParam(value = "Requested limit to number of activity records returned.", defaultValue = "10") @HeaderParam("Page-Limit") Long pageLimit,
                                                      @ApiParam(value = "Comma separated list of one or more of the following fields - <b>eventDate, startTime, endTime, eventLocation</b>") @HeaderParam("Sort-Fields") String sortFields,
                                                      @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @HeaderParam("Sort-Order") Order sortOrder);

    @GET
    @Path("/{bookingId}/activities/today")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Today's scheduled activities for offender.", notes = "Today's scheduled activities for offender.", nickname = "getBookingActivitiesForToday")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ScheduledEvent.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    GetBookingActivitiesForTodayResponse getBookingActivitiesForToday(@ApiParam(value = "The offender booking id", required = true) @PathParam("bookingId") Long bookingId,
                                                                      @ApiParam(value = "Comma separated list of one or more of the following fields - <b>eventDate, startTime, endTime, eventLocation</b>") @HeaderParam("Sort-Fields") String sortFields,
                                                                      @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @HeaderParam("Sort-Order") Order sortOrder);

    @GET
    @Path("/{bookingId}/adjudications")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Offender adjudications summary (awards and sanctions).", notes = "Offender adjudications (awards and sanctions).", nickname = "getAdjudicationSummary")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = AdjudicationSummary.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    AdjudicationSummary getAdjudicationSummary(@ApiParam(value = "The offender booking id", required = true) @PathParam("bookingId") Long bookingId,
                                               @ApiParam(value = "Only awards ending on or after this date (in YYYY-MM-DD format) will be considered.") @QueryParam("awardCutoffDate") String awardCutoffDate,
                                               @ApiParam(value = "Only proved adjudications ending on or after this date (in YYYY-MM-DD format) will be counted.") @QueryParam("adjudicationCutoffDate") String adjudicationCutoffDate);

    @GET
    @Path("/{bookingId}/alerts")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Offender alerts.", notes = "Offender alerts.", nickname = "getOffenderAlerts")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Alert.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    GetOffenderAlertsResponse getOffenderAlerts(@ApiParam(value = "The booking id of offender", required = true) @PathParam("bookingId") Long bookingId,
                                                @ApiParam(value = "Search parameters with the format [connector]:&lt;fieldName&gt;:&lt;operator&gt;:&lt;value&gt;:[format],... <p>Connector operators - and, or <p>Supported Operators - eq, neq, gt, gteq, lt, lteq, like, in</p> <p>Supported Fields - alertType, alertCode, dateCreated, dateExpires</p> ", required = true) @QueryParam("query") String query,
                                                @ApiParam(value = "Requested offset of first record in returned collection of alert records.", defaultValue = "0") @HeaderParam("Page-Offset") Long pageOffset,
                                                @ApiParam(value = "Requested limit to number of alert records returned.", defaultValue = "10") @HeaderParam("Page-Limit") Long pageLimit,
                                                @ApiParam(value = "Comma separated list of one or more of the following fields - <b>alertType, alertCode, dateCreated, dateExpires</b>") @HeaderParam("Sort-Fields") String sortFields,
                                                @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @HeaderParam("Sort-Order") Order sortOrder);

    @GET
    @Path("/offenderNo/{offenderNo}/alerts")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Offender alerts offenderNo.", notes = "Offender alerts by offenderNo.", nickname = "getOffenderAlertsByOffenderNo")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Alert.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    GetOffenderAlertsResponse getOffenderAlertsByOffenderNo(@ApiParam(value = "The offender no of the offender", required = true) @PathParam("offenderNo") String offenderNo,
                                                            @ApiParam(value = "Search parameters with the format [connector]:&lt;fieldName&gt;:&lt;operator&gt;:&lt;value&gt;:[format],... <p>Connector operators - and, or <p>Supported Operators - eq, neq, gt, gteq, lt, lteq, like, in</p> <p>Supported Fields - alertType, alertCode, dateCreated, dateExpires</p> ", required = true) @QueryParam("query") String query,
                                                            @ApiParam(value = "Requested offset of first record in returned collection of alert records.", defaultValue = "0") @HeaderParam("Page-Offset") Long pageOffset,
                                                            @ApiParam(value = "Requested limit to number of alert records returned.", defaultValue = "10") @HeaderParam("Page-Limit") Long pageLimit,
                                                            @ApiParam(value = "Comma separated list of one or more of the following fields - <b>alertType, alertCode, dateCreated, dateExpires</b>") @HeaderParam("Sort-Fields") String sortFields,
                                                            @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @HeaderParam("Sort-Order") Order sortOrder);


    @GET
    @Path("/{bookingId}/alerts/{alertId}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Offender alert detail.", notes = "Offender alert detail.", nickname = "getOffenderAlert")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Alert.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    GetOffenderAlertResponse getOffenderAlert(@ApiParam(value = "The booking id of offender", required = true) @PathParam("bookingId") Long bookingId,
                                              @ApiParam(value = "The Alert Id", required = true) @PathParam("alertId") Long alertId);

    @GET
    @Path("/{bookingId}/aliases")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Offender aliases.", notes = "Offender aliases.", nickname = "getOffenderAliases")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Alias.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    GetOffenderAliasesResponse getOffenderAliases(@ApiParam(value = "The booking id of offender", required = true) @PathParam("bookingId") Long bookingId,
                                                  @ApiParam(value = "Requested offset of first record in returned collection of alias records.", defaultValue = "0") @HeaderParam("Page-Offset") Long pageOffset,
                                                  @ApiParam(value = "Requested limit to number of alias records returned.", defaultValue = "10") @HeaderParam("Page-Limit") Long pageLimit,
                                                  @ApiParam(value = "Comma separated list of one or more of the following fields - <b>firstName, lastName, age, dob, middleName, nameType, createDate</b>") @HeaderParam("Sort-Fields") String sortFields,
                                                  @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @HeaderParam("Sort-Order") Order sortOrder);

    @GET
    @Path("/{bookingId}/appointments")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "All scheduled appointments for offender.", notes = "All scheduled appointments for offender.", nickname = "getBookingsBookingIdAppointments")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "", response = ScheduledEvent.class, responseContainer = "List")})
    GetBookingsBookingIdAppointmentsResponse getBookingsBookingIdAppointments(@ApiParam(value = "The offender booking id", required = true) @PathParam("bookingId") Long bookingId,
                                                                              @ApiParam(value = "Returned appointments must be scheduled on or after this date (in YYYY-MM-DD format).") @QueryParam("fromDate") String fromDate,
                                                                              @ApiParam(value = "Returned appointments must be scheduled on or before this date (in YYYY-MM-DD format).") @QueryParam("toDate") String toDate,
                                                                              @ApiParam(value = "Requested offset of first record in returned collection of appointment records.", defaultValue = "0") @HeaderParam("Page-Offset") Long pageOffset,
                                                                              @ApiParam(value = "Requested limit to number of appointment records returned.", defaultValue = "10") @HeaderParam("Page-Limit") Long pageLimit,
                                                                              @ApiParam(value = "Comma separated list of one or more of the following fields - <b>eventDate, startTime, endTime, eventLocation</b>") @HeaderParam("Sort-Fields") String sortFields,
                                                                              @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @HeaderParam("Sort-Order") Order sortOrder);

    @GET
    @Path("/{bookingId}/appointments/nextWeek")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Scheduled appointments for offender for following week.", notes = "Scheduled appointments for offender for following week.", nickname = "getBookingAppointmentsForNextWeek")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ScheduledEvent.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    GetBookingAppointmentsForNextWeekResponse getBookingAppointmentsForNextWeek(@ApiParam(value = "The offender booking id", required = true) @PathParam("bookingId") Long bookingId,
                                                                                @ApiParam(value = "Comma separated list of one or more of the following fields - <b>eventDate, startTime, endTime, eventLocation</b>") @HeaderParam("Sort-Fields") String sortFields,
                                                                                @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @HeaderParam("Sort-Order") Order sortOrder);

    @GET
    @Path("/{bookingId}/appointments/thisWeek")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Scheduled appointments for offender for coming week (from current day).", notes = "Scheduled appointments for offender for coming week (from current day).", nickname = "getBookingAppointmentsForThisWeek")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ScheduledEvent.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    GetBookingAppointmentsForThisWeekResponse getBookingAppointmentsForThisWeek(@ApiParam(value = "The offender booking id", required = true) @PathParam("bookingId") Long bookingId,
                                                                                @ApiParam(value = "Comma separated list of one or more of the following fields - <b>eventDate, startTime, endTime, eventLocation</b>") @HeaderParam("Sort-Fields") String sortFields,
                                                                                @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @HeaderParam("Sort-Order") Order sortOrder);

    @GET
    @Path("/{bookingId}/appointments/today")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Today's scheduled appointments for offender.", notes = "Today's scheduled appointments for offender.", nickname = "getBookingAppointmentsForToday")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ScheduledEvent.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    GetBookingAppointmentsForTodayResponse getBookingAppointmentsForToday(@ApiParam(value = "The offender booking id", required = true) @PathParam("bookingId") Long bookingId,
                                                                          @ApiParam(value = "Comma separated list of one or more of the following fields - <b>eventDate, startTime, endTime, eventLocation</b>") @HeaderParam("Sort-Fields") String sortFields,
                                                                          @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @HeaderParam("Sort-Order") Order sortOrder);

    @GET
    @Path("/{bookingId}/assessment/{assessmentCode}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Offender assessment detail.", notes = "Offender assessment detail.", nickname = "getAssessmentByCode")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Assessment.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    GetAssessmentByCodeResponse getAssessmentByCode(@ApiParam(value = "The booking id of offender", required = true) @PathParam("bookingId") Long bookingId,
                                                    @ApiParam(value = "Assessment Type Code", required = true) @PathParam("assessmentCode") String assessmentCode);

    @GET
    @Path("/{bookingId}/assessments")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Assessment Information", notes = "Assessment Information", nickname = "getAssessments")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Assessment.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    GetAssessmentsResponse getAssessments(@ApiParam(value = "The offender booking id", required = true) @PathParam("bookingId") Long bookingId);

    @GET
    @Path("/{bookingId}/balances")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Offender account balances.", notes = "Offender account balances.", nickname = "getBalances")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Account.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    GetBalancesResponse getBalances(@ApiParam(value = "The offender booking id", required = true) @PathParam("bookingId") Long bookingId);

    @GET
    @Path("/{bookingId}/caseNotes")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Offender case notes.", notes = "Offender case notes.", nickname = "getOffenderCaseNotes")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "", response = CaseNote.class, responseContainer = "List")})
    GetOffenderCaseNotesResponse getOffenderCaseNotes(@ApiParam(value = "The booking id of offender", required = true) @PathParam("bookingId") Long bookingId,
                                                      @ApiParam(value = "start contact date to search from", required = true) @QueryParam("from") String from,
                                                      @ApiParam(value = "end contact date to search up to (including this date)", required = true) @QueryParam("to") String to,
                                                      @ApiParam(value = "Search parameters with the format [connector]:&lt;fieldName&gt;:&lt;operator&gt;:&lt;value&gt;:[format],... <p>Connector operators - and, or <p>Supported Operators - eq, neq, gt, gteq, lt, lteq, like, in</p> <p>Supported Fields - creationDateTime, type, subType, source</p> ", required = true) @QueryParam("query") String query,
                                                      @ApiParam(value = "Requested offset of first record in returned collection of caseNote records.", defaultValue = "0") @HeaderParam("Page-Offset") Long pageOffset,
                                                      @ApiParam(value = "Requested limit to number of caseNote records returned.", defaultValue = "10") @HeaderParam("Page-Limit") Long pageLimit,
                                                      @ApiParam(value = "Comma separated list of one or more of the following fields - <b>creationDateTime, type, subType, source</b>") @HeaderParam("Sort-Fields") String sortFields,
                                                      @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @HeaderParam("Sort-Order") Order sortOrder);

    @GET
    @Path("/{bookingId}/caseNotes/{caseNoteId}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Offender case note detail.", notes = "Offender case note detail.", nickname = "getOffenderCaseNote")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "", response = CaseNote.class)})
    GetOffenderCaseNoteResponse getOffenderCaseNote(@ApiParam(value = "The booking id of offender", required = true) @PathParam("bookingId") Long bookingId,
                                                    @ApiParam(value = "The case note id", required = true) @PathParam("caseNoteId") Long caseNoteId);

    @GET
    @Path("/{bookingId}/caseNotes/{type}/{subType}/count")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Count of case notes", notes = "Count of case notes", nickname = "getCaseNoteCount")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = CaseNoteCount.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    GetCaseNoteCountResponse getCaseNoteCount(@ApiParam(value = "The offender booking id", required = true) @PathParam("bookingId") Long bookingId,
                                              @ApiParam(value = "Case note type.", required = true) @PathParam("type") String type,
                                              @ApiParam(value = "Case note sub-type.", required = true) @PathParam("subType") String subType,
                                              @ApiParam(value = "Only case notes occurring on or after this date (in YYYY-MM-DD format) will be considered.") @QueryParam("fromDate") String fromDate,
                                              @ApiParam(value = "Only case notes occurring on or before this date (in YYYY-MM-DD format) will be considered.") @QueryParam("toDate") String toDate);

    @GET
    @Path("/{bookingId}/contacts")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Offender contacts (e.g. next of kin).", notes = "Offender contacts (e.g. next of kin).", nickname = "getContacts")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ContactDetail.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    GetContactsResponse getContacts(@ApiParam(value = "The offender booking id", required = true) @PathParam("bookingId") Long bookingId);

    @GET
    @Path("/{bookingId}/events/nextWeek")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Scheduled events for offender for following week.", notes = "Scheduled events for offender for following week.", nickname = "getEventsNextWeek")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ScheduledEvent.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    GetEventsNextWeekResponse getEventsNextWeek(@ApiParam(value = "The offender booking id", required = true) @PathParam("bookingId") Long bookingId);

    @GET
    @Path("/{bookingId}/events/thisWeek")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Scheduled events for offender for coming week (from current day).", notes = "Scheduled events for offender for coming week (from current day).", nickname = "getEventsThisWeek")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ScheduledEvent.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    GetEventsThisWeekResponse getEventsThisWeek(@ApiParam(value = "The offender booking id", required = true) @PathParam("bookingId") Long bookingId);

    @GET
    @Path("/{bookingId}/events/today")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Today's scheduled events for offender.", notes = "Today's scheduled events for offender.", nickname = "getEventsToday")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ScheduledEvent.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    GetEventsTodayResponse getEventsToday(@ApiParam(value = "The offender booking id", required = true) @PathParam("bookingId") Long bookingId);

    @GET
    @Path("/{bookingId}/identifiers")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Identifiers for this booking", notes = "Identifiers for this booking", nickname = "getOffenderIdentifiers")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = OffenderIdentifier.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    GetOffenderIdentifiersResponse getOffenderIdentifiers(@ApiParam(value = "The offender booking id", required = true) @PathParam("bookingId") Long bookingId);

    @GET
    @Path("/{bookingId}/iepSummary")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Offender IEP (Incentives & Earned Privileges) summary.", notes = "Offender IEP (Incentives & Earned Privileges) summary.", nickname = "getBookingIEPSummary")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PrivilegeSummary.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    GetBookingIEPSummaryResponse getBookingIEPSummary(@ApiParam(value = "The booking id of offender", required = true) @PathParam("bookingId") Long bookingId,
                                                      @ApiParam(value = "Toggle to return IEP detail entries in response (or not).", required = true) @QueryParam("withDetails") boolean withDetails);

    @POST
    @Path("/{bookingId}/iepLevels")
    @Consumes({"application/json"})
    @ApiOperation("Add a new IEP (Incentives & Earned Privileges) level to an offender's booking.")
    void addIepLevel(@ApiParam(value = "The booking id of the offender", required = true) @PathParam("bookingId") Long bookingId,
                     @ApiParam(value = "The new IEP Level and accompanying comment (reason for change).", required = true) IepLevelAndComment ipeLevel);

    @GET
    @Path("/offenders/iepSummary")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Offenders IEP (Incentives & Earned Privileges) summary.", notes = "Offenders IEP (Incentives & Earned Privileges) summary.", nickname = "getBookingIEPSummaryForOffenders")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PrivilegeSummary.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    GetBookingIEPSummaryForOffendersResponse getBookingIEPSummaryForOffenders(
            @ApiParam(value = "The booking ids of offender", required = true) @QueryParam("bookings") List<Long> bookings,
            @ApiParam(value = "Toggle to return IEP detail entries in response (or not).", required = true) @QueryParam("withDetails") boolean withDetails);

    @GET
    @Path("/{bookingId}/image")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Image detail (without image data).", notes = "Image detail (without image data).", nickname = "getMainImageForBookings")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ImageDetail.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    GetMainImageForBookingsResponse getMainImageForBookings(@ApiParam(value = "The booking id of offender", required = true) @PathParam("bookingId") Long bookingId);

    @GET
    @Path("/{bookingId}/image/data")
    @Consumes({"application/json"})
    @Produces({"image/jpeg"})
    @ApiOperation(value = "Image data (as bytes).", notes = "Image data (as bytes).", nickname = "getMainBookingImageData")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = File.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    GetMainBookingImageDataResponse getMainBookingImageData(@ApiParam(value = "The booking id of offender", required = true) @PathParam("bookingId") Long bookingId,
                                                            @ApiParam(value = "Return full size image", defaultValue = "false") @QueryParam("fullSizeImage") @DefaultValue("false") boolean fullSizeImage);

    @GET
    @Path("/{bookingId}/mainOffence")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Offender main offence detail.", notes = "Offender main offence detail.", nickname = "getMainOffence")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = OffenceDetail.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    GetMainOffenceResponse getMainOffence(@ApiParam(value = "The offender booking id", required = true) @PathParam("bookingId") Long bookingId);

    @GET
    @Path("/offenderNo/{offenderNo}/offenceHistory")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Offence history.", notes = "All Offences recorded for this offender.", nickname = "getOffenceHistory")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = OffenceHistoryDetail.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    Response getOffenceHistory(@ApiParam(value = "The offender number", required = true) @PathParam("offenderNo") String offenderNo);

    @GET
    @Path("/{bookingId}/physicalAttributes")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Offender Physical Attributes.", notes = "Offender Physical Attributes.", nickname = "getPhysicalAttributes")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PhysicalAttributes.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    GetPhysicalAttributesResponse getPhysicalAttributes(@ApiParam(value = "The offender booking id", required = true) @PathParam("bookingId") Long bookingId);

    @GET
    @Path("/{bookingId}/physicalCharacteristics")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Physical Characteristics", notes = "Physical Characteristics", nickname = "getPhysicalCharacteristics")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PhysicalCharacteristic.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    GetPhysicalCharacteristicsResponse getPhysicalCharacteristics(@ApiParam(value = "The offender booking id", required = true) @PathParam("bookingId") Long bookingId);

    @GET
    @Path("/{bookingId}/physicalMarks")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Physical Mark Information", notes = "Physical Mark Information", nickname = "getPhysicalMarks")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PhysicalMark.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    GetPhysicalMarksResponse getPhysicalMarks(@ApiParam(value = "The offender booking id", required = true) @PathParam("bookingId") Long bookingId);

    @GET
    @Path("/{bookingId}/personal-care-needs")
    @Produces({"application/json"})
    @ApiOperation(value = "Personal Care Needs", notes = "Personal Care Need", nickname = "getPersonalCareNeeds")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PersonalCareNeed.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    PersonalCareNeeds getPersonalCareNeeds(@ApiParam(value = "The offender booking id", required = true) @PathParam("bookingId") Long bookingId,
                                           @ApiParam(value = "a list of types and optionally subtypes (joined with +) to search.", example = "DISAB+RM", required = true) @QueryParam("type") List<String> problemTypes);

    @GET
    @Path("/{bookingId}/reasonable-adjustments")
    @Produces({"application/json"})
    @ApiOperation(value = "Reasonable Adjustment Information", notes = "Reasonable Adjustment Information", nickname = "getReasonableAdjustment")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ReasonableAdjustment.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    ReasonableAdjustments getReasonableAdjustments(@ApiParam(value = "The offender booking id", required = true) @PathParam("bookingId") Long bookingId,
                                                   @ApiParam(value = "a list of treatment codes to search.", example = "PEEP", required = true) @QueryParam("type") List<String> treatmentCodes);

    @GET
    @Path("/{bookingId}/profileInformation")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Profile Information", notes = "Profile Information", nickname = "getProfileInformation")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ProfileInformation.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    GetProfileInformationResponse getProfileInformation(@ApiParam(value = "The offender booking id", required = true) @PathParam("bookingId") Long bookingId);

    @GET
    @Path("/{bookingId}/relationships")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "The contact details and their relationship to the offender", notes = "The contact details and their relationship to the offender", nickname = "getRelationships")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Contact.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    GetRelationshipsResponse getRelationships(@ApiParam(value = "The offender booking id", required = true) @PathParam("bookingId") Long bookingId,
                                              @ApiParam(value = "filter by the relationship type") @QueryParam("relationshipType") String relationshipType);

    @GET
    @Path("/{bookingId}/sentenceDetail")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Offender sentence detail (key dates and additional days awarded).", notes = "Offender sentence detail (key dates and additional days awarded).", nickname = "getBookingSentenceDetail")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = SentenceDetail.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    GetBookingSentenceDetailResponse getBookingSentenceDetail(@ApiParam(value = "The booking id of offender", required = true) @PathParam("bookingId") Long bookingId);

    @GET
    @Path("/{bookingId}/visits")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "All scheduled visits for offender.", notes = "All scheduled visits for offender.", nickname = "getBookingVisits")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ScheduledEvent.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    GetBookingVisitsResponse getBookingVisits(@ApiParam(value = "The offender booking id", required = true) @PathParam("bookingId") Long bookingId,
                                              @ApiParam(value = "Returned visits must be scheduled on or after this date (in YYYY-MM-DD format).") @QueryParam("fromDate") String fromDate,
                                              @ApiParam(value = "Returned visits must be scheduled on or before this date (in YYYY-MM-DD format).") @QueryParam("toDate") String toDate,
                                              @ApiParam(value = "Requested offset of first record in returned collection of visit records.", defaultValue = "0") @HeaderParam("Page-Offset") Long pageOffset,
                                              @ApiParam(value = "Requested limit to number of visit records returned.", defaultValue = "10") @HeaderParam("Page-Limit") Long pageLimit,
                                              @ApiParam(value = "Comma separated list of one or more of the following fields - <b>eventDate, startTime, endTime, eventLocation</b>") @HeaderParam("Sort-Fields") String sortFields,
                                              @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @HeaderParam("Sort-Order") Order sortOrder);

    @GET
    @Path("/{bookingId}/visits/last")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "The most recent visit for the offender.", notes = "The most recent visit for the offender.", nickname = "getBookingVisitsLast")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Visit.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    GetBookingVisitsLastResponse getBookingVisitsLast(@ApiParam(value = "The offender booking id", required = true) @PathParam("bookingId") Long bookingId);

    @GET
    @Path("/{bookingId}/visits/next")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "The next visit for the offender.", notes = "The next visit for the offender.", nickname = "getBookingVisitsNext")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Visit.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    GetBookingVisitsNextResponse getBookingVisitsNext(@ApiParam(value = "The offender booking id", required = true) @PathParam("bookingId") Long bookingId);

    @GET
    @Path("/{bookingId}/visits/today")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Today's scheduled visits for offender.", notes = "Today's scheduled visits for offender.", nickname = "getBookingVisitsForToday")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ScheduledEvent.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    GetBookingVisitsForTodayResponse getBookingVisitsForToday(@ApiParam(value = "The offender booking id", required = true) @PathParam("bookingId") Long bookingId,
                                                              @ApiParam(value = "Comma separated list of one or more of the following fields - <b>eventDate, startTime, endTime, eventLocation</b>") @HeaderParam("Sort-Fields") String sortFields,
                                                              @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @HeaderParam("Sort-Order") Order sortOrder);

    @GET
    @Path("/offenderNo/{offenderNo}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Offender detail.", notes = "Offender detail.", nickname = "getOffenderBookingByOffenderNo")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = InmateDetail.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    GetOffenderBookingByOffenderNoResponse getOffenderBookingByOffenderNo(@ApiParam(value = "The offenderNo of offender", required = true) @PathParam("offenderNo") String offenderNo,
                                                                          @ApiParam(value = "If set to true then full data is returned", defaultValue = "false") @QueryParam("fullInfo") boolean fullInfo);

    @POST
    @Path("/offenders")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Offender detail.", notes = "Offender detail for offenders", nickname = "getBasicInmateDetailsForOffenders")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = InmateBasicDetails.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<InmateBasicDetails> getBasicInmateDetailsForOffenders(@ApiParam(value = "The offenderNo of offender", required = true) Set<String> offenders,
                                                               @ApiParam(value = "Returns only Offender details with an active booking if true, otherwise Offenders without an active booking are included", defaultValue = "true") @QueryParam("activeOnly") Boolean activeOnly);

    @POST
    @Path("/offenders/{agencyId}/list")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Basic offender details by booking ids - POST version to allow for large numbers", notes = "Basic offender details by booking ids", nickname = "getBasicInmateDetailsForOffendersByBookingIds")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = InmateBasicDetails.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<InmateBasicDetails> getBasicInmateDetailsByBookingIds(@ApiParam(value = "The prison where the offenders are booked - the response is restricted to bookings at this prison", required = true) @PathParam("agencyId") String agencyId,
                                                               @ApiParam(value = "The bookingIds to identify the offenders", required = true) Set<Long> bookingIds);

    @GET
    @Path("/offenderNo/{offenderNo}/image/data")
    @Consumes({"application/json"})
    @Produces({"image/jpeg"})
    @ApiOperation(value = "Image data (as bytes).", notes = "Image data (as bytes).", nickname = "getMainBookingImageDataByNo")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = File.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    GetMainBookingImageDataByNoResponse getMainBookingImageDataByNo(@ApiParam(value = "The offender No of offender", required = true) @PathParam("offenderNo") String offenderNo,
                                                                    @ApiParam(value = "Return full size image", defaultValue = "false") @QueryParam("fullSizeImage") @DefaultValue("false") boolean fullSizeImage);

    @GET
    @Path("/offenderNo/{offenderNo}/key-worker")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Key worker details.", notes = "Key worker details.", nickname = "getKeyworkerByOffenderNo")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Keyworker.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    GetKeyworkerByOffenderNoResponse getKeyworkerByOffenderNo(@ApiParam(value = "The offenderNo of offender", required = true) @PathParam("offenderNo") String offenderNo);

    @GET
    @Path("/offenderNo/{offenderNo}/relationships")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "The contact details and their relationship to the offender", notes = "The contact details and their relationship to the offender", nickname = "getRelationshipsByOffenderNo")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Contact.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    GetRelationshipsByOffenderNoResponse getRelationshipsByOffenderNo(@ApiParam(value = "The offender Offender No", required = true) @PathParam("offenderNo") String offenderNo,
                                                                      @ApiParam(value = "filter by the relationship type") @QueryParam("relationshipType") String relationshipType);

    @POST
    @Path("/")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Create booking for offender (additionally creating offender record if one does not already exist).", notes = "<b>(BETA)</b> Create booking for offender (additionally creating offender record if one does not already exist).", nickname = "createOffenderBooking")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Offender booking created.", response = OffenderSummary.class),
            @ApiResponse(code = 204, message = "Request in progress."),
            @ApiResponse(code = 400, message = "Request to create offender booking failed. Consult response for reason.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "User not authorised to create offender booking.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    CreateOffenderBookingResponse createOffenderBooking(@ApiParam(value = "Details required to enable creation of new offender booking (and offender, if necessary).", required = true) NewBooking body);

    @POST
    @Path("/{bookingId}/appointments")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Create appointment for offender.", notes = "Create appointment for offender.", nickname = "postBookingsBookingIdAppointments")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "The appointment has been recorded. The updated object is returned including the status.", response = ScheduledEvent.class)})
    PostBookingsBookingIdAppointmentsResponse postBookingsBookingIdAppointments(@ApiParam(value = "The offender booking id", required = true) @PathParam("bookingId") Long bookingId,
                                                                                @ApiParam(value = "", required = true) NewAppointment body);

    @POST
    @Path("/{bookingId}/caseNotes")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Create case note for offender.", notes = "Create case note for offender.", nickname = "createBookingCaseNote")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "The Case Note has been recorded. The updated object is returned including the status.", response = CaseNote.class),
            @ApiResponse(code = 409, message = "The case note has already been recorded under the booking. The current unmodified object (including status) is returned.", response = ErrorResponse.class)})
    CreateBookingCaseNoteResponse createBookingCaseNote(@ApiParam(value = "The booking id of offender", required = true) @PathParam("bookingId") Long bookingId,
                                                        @ApiParam(value = "", required = true) NewCaseNote body);

    @POST
    @Path("/{bookingId}/relationships")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Create a relationship with an offender", notes = "Create a relationship with an offender", nickname = "createRelationship")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "If successful the Contact object is returned.", response = Contact.class)})
    CreateRelationshipResponse createRelationship(@ApiParam(value = "The offender booking id", required = true) @PathParam("bookingId") Long bookingId,
                                                  @ApiParam(value = "The person details and their relationship to the offender", required = true) OffenderRelationship body);

    @POST
    @Path("/offenderNo/{agencyId}/alerts")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Get alerts for a list of offenders at a prison")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "", response = Alert.class, responseContainer = "List")})
    GetAlertsByOffenderNosResponse getAlertsByOffenderNosAtAgency(@ApiParam(value = "The prison where the offenders are booked", required = true) @PathParam("agencyId") String agencyId,
                                                                  @ApiParam(value = "The required offender numbers (mandatory)", required = true) List<String> body);

    @POST
    @Path("/offenderNo/alerts")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Get alerts for a list of offenders. Requires SYSTEM_READ_ONLY role")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "", response = Alert.class, responseContainer = "List")})
    GetAlertsByOffenderNosResponse getAlertsByOffenderNos(@ApiParam(value = "The required offender numbers (mandatory)", required = true) List<String> body);

    @POST
    @Path("/offenderNo/{offenderNo}/caseNotes")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Create case note for offender.", notes = "Create case note for offender.", nickname = "createOffenderCaseNote")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "The Case Note has been recorded. The updated object is returned including the status.", response = CaseNote.class),
            @ApiResponse(code = 409, message = "The case note has already been recorded under the booking. The current unmodified object (including status) is returned.", response = ErrorResponse.class)})
    CreateOffenderCaseNoteResponse createOffenderCaseNote(@ApiParam(value = "The offenderNo of offender", required = true) @PathParam("offenderNo") String offenderNo,
                                                          @ApiParam(value = "", required = true) NewCaseNote body);

    @POST
    @Path("/offenderNo/{offenderNo}/relationships")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Create a relationship with an offender", notes = "Create a relationship with an offender", nickname = "createRelationshipByOffenderNo")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "If successful the Contact object is returned.", response = Contact.class)})
    CreateRelationshipByOffenderNoResponse createRelationshipByOffenderNo(@ApiParam(value = "The offender Offender No", required = true) @PathParam("offenderNo") String offenderNo,
                                                                          @ApiParam(value = "The person details and their relationship to the offender", required = true) OffenderRelationship body);

    @PUT
    @Path("/")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Process recall for offender.", notes = "<b>(BETA)</b> Process recall for offender.", nickname = "recallOffenderBooking")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Offender successfully recalled.", response = OffenderSummary.class),
            @ApiResponse(code = 400, message = "Request to recall offender failed. Consult response for reason.", response = ErrorResponse.class)})
    RecallOffenderBookingResponse recallOffenderBooking(@ApiParam(value = "Details required to enable recall of offender.", required = true) RecallBooking body);

    @PUT
    @Path("/{bookingId}/caseNotes/{caseNoteId}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Amend offender case note.", notes = "Amend offender case note.", nickname = "updateOffenderCaseNote")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Case Note amendment processed successfully. Updated case note is returned.", response = CaseNote.class),
            @ApiResponse(code = 400, message = "Invalid request - e.g. amendment text not provided.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - user not authorised to amend case note.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Resource not found - booking or case note does not exist or is not accessible to user.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Internal server error.", response = ErrorResponse.class)})
    UpdateOffenderCaseNoteResponse updateOffenderCaseNote(@ApiParam(value = "The booking id of offender", required = true, example = "1231212") @PathParam("bookingId") Long bookingId,
                                                          @ApiParam(value = "The case note id", required = true, example = "1212134") @PathParam("caseNoteId") Long caseNoteId,
                                                          @ApiParam(value = "", required = true) UpdateCaseNote body);

    @PUT
    @Path("/offenderNo/{offenderNo}/activities/{activityId}/attendance")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Attendance data has been updated"),
            @ApiResponse(code = 400, message = "Invalid request - e.g. validation error.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - user not authorised to attend activity.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Resource not found - booking or event does not exist or is not accessible to user.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Internal server error.", response = ErrorResponse.class)})
    UpdateAttendanceResponse updateAttendance(@ApiParam(value = "The offenderNo of the prisoner", required = true, example = "A1234AA") @PathParam("offenderNo") String offenderNo,
                                              @ApiParam(value = "The activity id", required = true, example = "1212131") @PathParam("activityId") Long activityId,
                                              @ApiParam(value = "", required = true, example = "{eventOutcome = 'ATT', performance = 'ACCEPT' outcomeComment = 'Turned up very late'}") UpdateAttendance updateAttendance);

    @PUT
    @Path("/{bookingId}/activities/{activityId}/attendance")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Update offender attendance and pay.", notes = "Update offender attendance and pay.", nickname = "updateAttendance")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Attendance data has been updated"),
            @ApiResponse(code = 400, message = "Invalid request - e.g. validation error.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - user not authorised to attend activity.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Resource not found - booking or event does not exist or is not accessible to user.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Internal server error.", response = ErrorResponse.class)})
    UpdateAttendanceResponse updateAttendance(@ApiParam(value = "The booking Id of the prisoner", required = true, example = "213531") @PathParam("bookingId") @NotNull Long bookingId,
                                              @ApiParam(value = "The activity id", required = true,example = "1212131") @PathParam("activityId") @NotNull Long activityId,
                                              @ApiParam(value = "", required = true) @NotNull UpdateAttendance body);
    @PUT
    @Path("/activities/attendance")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Update attendance and pay for multiple bookings.", notes = "Update offender attendance and pay.", nickname = "updateAttendance")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Attendance data has been updated"),
            @ApiResponse(code = 400, message = "Invalid request - e.g. validation error.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - user not authorised to attend activity.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Resource not found - booking or event does not exist or is not accessible to user.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Internal server error.", response = ErrorResponse.class)})
    UpdateAttendanceResponse updateAttendanceForMultipleBookingIds(@ApiParam(required = true) @NotNull UpdateAttendanceBatch body);

    @GET
    @Path("/{bookingId}/incidents")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Return a set Incidents for a given booking Id", notes = "Can be filtered by participation type and incident type")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = IncidentCase.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    IncidentsResource.IncidentListResponse getIncidentsByBookingId(@ApiParam(value = "bookingId", required = true) @PathParam("bookingId") @NotNull Long bookingId,
                                                                   @ApiParam(value = "incidentType", example = "ASSAULT", allowMultiple = true) @QueryParam("incidentType") List<String> incidentTypes,
                                                                   @ApiParam(value = "participationRoles", example = "ASSIAL", allowMultiple = true, allowableValues = "ACTINV,ASSIAL,FIGHT,IMPED,PERP,SUSASS,SUSINV,VICT,AI,PAS,AO") @QueryParam("participationRoles") List<String> participationRoles);


    @POST
    @Path("/{bookingId}/alert")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Create an alert")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Alert id.", response = AlertCreated.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)
    })
    Response postAlert(
            @ApiParam(value = "bookingId") @PathParam("bookingId") Long bookingId,
            @ApiParam(value = "Alert details", required = true) @RequestBody @Valid CreateAlert alert
    );

    @PUT
    @Path("/{bookingId}/alert/{alertSeq}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Expire an alert")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Expired alert", response = Alert.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)
    })
    Response setAlertExpiry(
            @ApiParam(value = "bookingId") @PathParam("bookingId") Long bookingId,
            @ApiParam(value = "alertSeq") @PathParam("alertSeq") Long alertSeq,
            @ApiParam(value = "Alert expiry details", required = true) @RequestBody @Valid ExpireAlert alert
    );

    class GetOffenderBookingsResponse extends ResponseDelegate {

        private GetOffenderBookingsResponse(final Response response) {
            super(response);
        }

        private GetOffenderBookingsResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetOffenderBookingsResponse respond200WithApplicationJson(final Page<OffenderBooking> page) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON)
                    .header("Total-Records", page.getTotalRecords())
                    .header("Page-Offset", page.getPageOffset())
                    .header("Page-Limit", page.getPageLimit());
            responseBuilder.entity(page.getItems());
            return new GetOffenderBookingsResponse(responseBuilder.build(), page.getItems());
        }
    }

    class GetOffenderBookingResponse extends ResponseDelegate {

        private GetOffenderBookingResponse(final Response response) {
            super(response);
        }

        private GetOffenderBookingResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetOffenderBookingResponse respond200WithApplicationJson(final InmateDetail entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetOffenderBookingResponse(responseBuilder.build(), entity);
        }
    }

    class GetBookingActivitiesResponse extends ResponseDelegate {

        private GetBookingActivitiesResponse(final Response response) {
            super(response);
        }

        private GetBookingActivitiesResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetBookingActivitiesResponse respond200WithApplicationJson(final Page<ScheduledEvent> page) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON)
                    .header("Total-Records", page.getTotalRecords())
                    .header("Page-Offset", page.getPageOffset())
                    .header("Page-Limit", page.getPageLimit());
            responseBuilder.entity(page.getItems());
            return new GetBookingActivitiesResponse(responseBuilder.build(), page.getItems());
        }
    }

    class GetBookingActivitiesForTodayResponse extends ResponseDelegate {

        private GetBookingActivitiesForTodayResponse(final Response response) {
            super(response);
        }

        private GetBookingActivitiesForTodayResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetBookingActivitiesForTodayResponse respond200WithApplicationJson(final List<ScheduledEvent> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetBookingActivitiesForTodayResponse(responseBuilder.build(), entity);
        }
    }

    class GetOffenderAlertsResponse extends ResponseDelegate {

        private GetOffenderAlertsResponse(final Response response) {
            super(response);
        }

        private GetOffenderAlertsResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetOffenderAlertsResponse respond200WithApplicationJson(final Page<Alert> page) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON)
                    .header("Total-Records", page.getTotalRecords())
                    .header("Page-Offset", page.getPageOffset())
                    .header("Page-Limit", page.getPageLimit());
            responseBuilder.entity(page.getItems());
            return new GetOffenderAlertsResponse(responseBuilder.build(), page.getItems());
        }

    }

    class GetOffenderAlertResponse extends ResponseDelegate {

        private GetOffenderAlertResponse(final Response response) {
            super(response);
        }

        private GetOffenderAlertResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetOffenderAlertResponse respond200WithApplicationJson(final Alert entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetOffenderAlertResponse(responseBuilder.build(), entity);
        }
    }

    class GetOffenderAliasesResponse extends ResponseDelegate {

        private GetOffenderAliasesResponse(final Response response) {
            super(response);
        }

        private GetOffenderAliasesResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetOffenderAliasesResponse respond200WithApplicationJson(final Page<Alias> page) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON)
                    .header("Total-Records", page.getTotalRecords())
                    .header("Page-Offset", page.getPageOffset())
                    .header("Page-Limit", page.getPageLimit());
            responseBuilder.entity(page.getItems());
            return new GetOffenderAliasesResponse(responseBuilder.build(), page.getItems());
        }
    }

    class GetBookingsBookingIdAppointmentsResponse extends ResponseDelegate {

        private GetBookingsBookingIdAppointmentsResponse(final Response response) {
            super(response);
        }

        private GetBookingsBookingIdAppointmentsResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetBookingsBookingIdAppointmentsResponse respond200WithApplicationJson(final Page<ScheduledEvent> page) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON)
                    .header("Total-Records", page.getTotalRecords())
                    .header("Page-Offset", page.getPageOffset())
                    .header("Page-Limit", page.getPageLimit());
            responseBuilder.entity(page.getItems());
            return new GetBookingsBookingIdAppointmentsResponse(responseBuilder.build(), page.getItems());
        }
    }

    class GetBookingAppointmentsForNextWeekResponse extends ResponseDelegate {

        private GetBookingAppointmentsForNextWeekResponse(final Response response) {
            super(response);
        }

        private GetBookingAppointmentsForNextWeekResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetBookingAppointmentsForNextWeekResponse respond200WithApplicationJson(final List<ScheduledEvent> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetBookingAppointmentsForNextWeekResponse(responseBuilder.build(), entity);
        }
    }

    class GetBookingAppointmentsForThisWeekResponse extends ResponseDelegate {

        private GetBookingAppointmentsForThisWeekResponse(final Response response) {
            super(response);
        }

        private GetBookingAppointmentsForThisWeekResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetBookingAppointmentsForThisWeekResponse respond200WithApplicationJson(final List<ScheduledEvent> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetBookingAppointmentsForThisWeekResponse(responseBuilder.build(), entity);
        }
    }

    class GetBookingAppointmentsForTodayResponse extends ResponseDelegate {

        private GetBookingAppointmentsForTodayResponse(final Response response) {
            super(response);
        }

        private GetBookingAppointmentsForTodayResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetBookingAppointmentsForTodayResponse respond200WithApplicationJson(final List<ScheduledEvent> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetBookingAppointmentsForTodayResponse(responseBuilder.build(), entity);
        }
    }

    class GetAssessmentByCodeResponse extends ResponseDelegate {

        private GetAssessmentByCodeResponse(final Response response) {
            super(response);
        }

        private GetAssessmentByCodeResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetAssessmentByCodeResponse respond200WithApplicationJson(final Assessment entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAssessmentByCodeResponse(responseBuilder.build(), entity);
        }
    }

    class GetAssessmentsResponse extends ResponseDelegate {

        private GetAssessmentsResponse(final Response response) {
            super(response);
        }

        private GetAssessmentsResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetAssessmentsResponse respond200WithApplicationJson(final List<Assessment> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAssessmentsResponse(responseBuilder.build(), entity);
        }
    }

    class GetBalancesResponse extends ResponseDelegate {

        private GetBalancesResponse(final Response response) {
            super(response);
        }

        private GetBalancesResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetBalancesResponse respond200WithApplicationJson(final Account entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetBalancesResponse(responseBuilder.build(), entity);
        }
    }

    class GetOffenderCaseNotesResponse extends ResponseDelegate {

        private GetOffenderCaseNotesResponse(final Response response) {
            super(response);
        }

        private GetOffenderCaseNotesResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetOffenderCaseNotesResponse respond200WithApplicationJson(final Page<CaseNote> page) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON)
                    .header("Total-Records", page.getTotalRecords())
                    .header("Page-Offset", page.getPageOffset())
                    .header("Page-Limit", page.getPageLimit());
            responseBuilder.entity(page.getItems());
            return new GetOffenderCaseNotesResponse(responseBuilder.build(), page.getItems());
        }
    }

    class GetOffenderCaseNoteResponse extends ResponseDelegate {

        private GetOffenderCaseNoteResponse(final Response response) {
            super(response);
        }

        private GetOffenderCaseNoteResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetOffenderCaseNoteResponse respond200WithApplicationJson(final CaseNote entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetOffenderCaseNoteResponse(responseBuilder.build(), entity);
        }
    }

    class GetCaseNoteCountResponse extends ResponseDelegate {

        private GetCaseNoteCountResponse(final Response response) {
            super(response);
        }

        private GetCaseNoteCountResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetCaseNoteCountResponse respond200WithApplicationJson(final CaseNoteCount entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetCaseNoteCountResponse(responseBuilder.build(), entity);
        }
    }

    class GetContactsResponse extends ResponseDelegate {

        private GetContactsResponse(final Response response) {
            super(response);
        }

        private GetContactsResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetContactsResponse respond200WithApplicationJson(final ContactDetail entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetContactsResponse(responseBuilder.build(), entity);
        }
    }

    class GetEventsNextWeekResponse extends ResponseDelegate {

        private GetEventsNextWeekResponse(final Response response) {
            super(response);
        }

        private GetEventsNextWeekResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetEventsNextWeekResponse respond200WithApplicationJson(final List<ScheduledEvent> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetEventsNextWeekResponse(responseBuilder.build(), entity);
        }
    }

    class GetEventsThisWeekResponse extends ResponseDelegate {

        private GetEventsThisWeekResponse(final Response response) {
            super(response);
        }

        private GetEventsThisWeekResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetEventsThisWeekResponse respond200WithApplicationJson(final List<ScheduledEvent> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetEventsThisWeekResponse(responseBuilder.build(), entity);
        }
    }

    class GetEventsTodayResponse extends ResponseDelegate {

        private GetEventsTodayResponse(final Response response) {
            super(response);
        }

        private GetEventsTodayResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetEventsTodayResponse respond200WithApplicationJson(final List<ScheduledEvent> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetEventsTodayResponse(responseBuilder.build(), entity);
        }
    }

    class GetOffenderIdentifiersResponse extends ResponseDelegate {

        private GetOffenderIdentifiersResponse(final Response response) {
            super(response);
        }

        private GetOffenderIdentifiersResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetOffenderIdentifiersResponse respond200WithApplicationJson(final List<OffenderIdentifier> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetOffenderIdentifiersResponse(responseBuilder.build(), entity);
        }
    }

    class GetBookingIEPSummaryResponse extends ResponseDelegate {

        private GetBookingIEPSummaryResponse(final Response response) {
            super(response);
        }

        private GetBookingIEPSummaryResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetBookingIEPSummaryResponse respond200WithApplicationJson(final PrivilegeSummary entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetBookingIEPSummaryResponse(responseBuilder.build(), entity);
        }
    }


    class GetBookingIEPSummaryForOffendersResponse extends ResponseDelegate {

        private GetBookingIEPSummaryForOffendersResponse(final Response response) {
            super(response);
        }

        private GetBookingIEPSummaryForOffendersResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetBookingIEPSummaryForOffendersResponse respond200WithApplicationJson(final List<PrivilegeSummary> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetBookingIEPSummaryForOffendersResponse(responseBuilder.build(), entity);
        }
    }

    class GetMainImageForBookingsResponse extends ResponseDelegate {

        private GetMainImageForBookingsResponse(final Response response) {
            super(response);
        }

        private GetMainImageForBookingsResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetMainImageForBookingsResponse respond200WithApplicationJson(final ImageDetail entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetMainImageForBookingsResponse(responseBuilder.build(), entity);
        }
    }

    class GetMainBookingImageDataResponse extends ResponseDelegate {

        private GetMainBookingImageDataResponse(final Response response) {
            super(response);
        }

        private GetMainBookingImageDataResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetMainBookingImageDataResponse respond200WithApplicationJson(final File entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", "image/jpeg");
            responseBuilder.entity(entity);
            return new GetMainBookingImageDataResponse(responseBuilder.build(), entity);
        }

        public static GetMainBookingImageDataResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetMainBookingImageDataResponse(responseBuilder.build(), entity);
        }

        public static GetMainBookingImageDataResponse respond500WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetMainBookingImageDataResponse(responseBuilder.build(), entity);
        }
    }

    class GetMainOffenceResponse extends ResponseDelegate {

        private GetMainOffenceResponse(final Response response) {
            super(response);
        }

        private GetMainOffenceResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetMainOffenceResponse respond200WithApplicationJson(final List<OffenceDetail> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetMainOffenceResponse(responseBuilder.build(), entity);
        }
    }

    class GetPhysicalAttributesResponse extends ResponseDelegate {

        private GetPhysicalAttributesResponse(final Response response) {
            super(response);
        }

        private GetPhysicalAttributesResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetPhysicalAttributesResponse respond200WithApplicationJson(final PhysicalAttributes entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetPhysicalAttributesResponse(responseBuilder.build(), entity);
        }
    }

    class GetPhysicalCharacteristicsResponse extends ResponseDelegate {

        private GetPhysicalCharacteristicsResponse(final Response response) {
            super(response);
        }

        private GetPhysicalCharacteristicsResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetPhysicalCharacteristicsResponse respond200WithApplicationJson(final List<PhysicalCharacteristic> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetPhysicalCharacteristicsResponse(responseBuilder.build(), entity);
        }
    }

    class GetPhysicalMarksResponse extends ResponseDelegate {

        private GetPhysicalMarksResponse(final Response response) {
            super(response);
        }

        private GetPhysicalMarksResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetPhysicalMarksResponse respond200WithApplicationJson(final List<PhysicalMark> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetPhysicalMarksResponse(responseBuilder.build(), entity);
        }
    }

    class GetProfileInformationResponse extends ResponseDelegate {

        private GetProfileInformationResponse(final Response response) {
            super(response);
        }

        private GetProfileInformationResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetProfileInformationResponse respond200WithApplicationJson(final List<ProfileInformation> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetProfileInformationResponse(responseBuilder.build(), entity);
        }
    }

    class GetRelationshipsResponse extends ResponseDelegate {

        private GetRelationshipsResponse(final Response response) {
            super(response);
        }

        private GetRelationshipsResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetRelationshipsResponse respond200WithApplicationJson(final List<Contact> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetRelationshipsResponse(responseBuilder.build(), entity);
        }
    }

    class GetBookingSentenceDetailResponse extends ResponseDelegate {

        private GetBookingSentenceDetailResponse(final Response response) {
            super(response);
        }

        private GetBookingSentenceDetailResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetBookingSentenceDetailResponse respond200WithApplicationJson(final SentenceDetail entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetBookingSentenceDetailResponse(responseBuilder.build(), entity);
        }
    }

    class GetBookingVisitsResponse extends ResponseDelegate {

        private GetBookingVisitsResponse(final Response response) {
            super(response);
        }

        private GetBookingVisitsResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetBookingVisitsResponse respond200WithApplicationJson(final Page<ScheduledEvent> page) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON)
                    .header("Total-Records", page.getTotalRecords())
                    .header("Page-Offset", page.getPageOffset())
                    .header("Page-Limit", page.getPageLimit());
            responseBuilder.entity(page.getItems());
            return new GetBookingVisitsResponse(responseBuilder.build(), page.getItems());
        }
    }

    class GetBookingVisitsLastResponse extends ResponseDelegate {

        private GetBookingVisitsLastResponse(final Response response) {
            super(response);
        }

        private GetBookingVisitsLastResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetBookingVisitsLastResponse respond200WithApplicationJson(final Visit entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetBookingVisitsLastResponse(responseBuilder.build(), entity);
        }
    }

    class GetBookingVisitsNextResponse extends ResponseDelegate {

        private GetBookingVisitsNextResponse(final Response response) {
            super(response);
        }

        private GetBookingVisitsNextResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetBookingVisitsNextResponse respond200WithApplicationJson(final Visit entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetBookingVisitsNextResponse(responseBuilder.build(), entity);
        }
    }

    class GetBookingVisitsForTodayResponse extends ResponseDelegate {

        private GetBookingVisitsForTodayResponse(final Response response) {
            super(response);
        }

        private GetBookingVisitsForTodayResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetBookingVisitsForTodayResponse respond200WithApplicationJson(final List<ScheduledEvent> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetBookingVisitsForTodayResponse(responseBuilder.build(), entity);
        }
    }

    class GetOffenderBookingByOffenderNoResponse extends ResponseDelegate {

        private GetOffenderBookingByOffenderNoResponse(final Response response) {
            super(response);
        }

        private GetOffenderBookingByOffenderNoResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetOffenderBookingByOffenderNoResponse respond200WithApplicationJson(final InmateDetail entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetOffenderBookingByOffenderNoResponse(responseBuilder.build(), entity);
        }
    }

    class GetMainBookingImageDataByNoResponse extends ResponseDelegate {

        private GetMainBookingImageDataByNoResponse(final Response response) {
            super(response);
        }

        private GetMainBookingImageDataByNoResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetMainBookingImageDataByNoResponse respond200WithApplicationJson(final File entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", "image/jpeg");
            responseBuilder.entity(entity);
            return new GetMainBookingImageDataByNoResponse(responseBuilder.build(), entity);
        }

        public static GetMainBookingImageDataByNoResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetMainBookingImageDataByNoResponse(responseBuilder.build(), entity);
        }

        public static GetMainBookingImageDataByNoResponse respond500WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetMainBookingImageDataByNoResponse(responseBuilder.build(), entity);
        }
    }

    class GetKeyworkerByOffenderNoResponse extends ResponseDelegate {

        private GetKeyworkerByOffenderNoResponse(final Response response) {
            super(response);
        }

        private GetKeyworkerByOffenderNoResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetKeyworkerByOffenderNoResponse respond200WithApplicationJson(final Keyworker entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetKeyworkerByOffenderNoResponse(responseBuilder.build(), entity);
        }
    }

    class GetRelationshipsByOffenderNoResponse extends ResponseDelegate {

        private GetRelationshipsByOffenderNoResponse(final Response response) {
            super(response);
        }

        private GetRelationshipsByOffenderNoResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetRelationshipsByOffenderNoResponse respond200WithApplicationJson(final List<Contact> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetRelationshipsByOffenderNoResponse(responseBuilder.build(), entity);
        }
    }

    class CreateOffenderBookingResponse extends ResponseDelegate {

        private CreateOffenderBookingResponse(final Response response) {
            super(response);
        }

        private CreateOffenderBookingResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static CreateOffenderBookingResponse respond201WithApplicationJson(final OffenderSummary entity) {
            final var responseBuilder = Response.status(201)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new CreateOffenderBookingResponse(responseBuilder.build(), entity);
        }

        public static CreateOffenderBookingResponse respond204WithApplicationJson() {
            final var responseBuilder = Response.status(204)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            return new CreateOffenderBookingResponse(responseBuilder.build());
        }
    }


    class PostBookingsBookingIdAppointmentsResponse extends ResponseDelegate {

        private PostBookingsBookingIdAppointmentsResponse(final Response response) {
            super(response);
        }

        private PostBookingsBookingIdAppointmentsResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static PostBookingsBookingIdAppointmentsResponse respond201WithApplicationJson(final ScheduledEvent entity) {
            final var responseBuilder = Response.status(201)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new PostBookingsBookingIdAppointmentsResponse(responseBuilder.build(), entity);
        }
    }

    class CreateBookingCaseNoteResponse extends ResponseDelegate {

        private CreateBookingCaseNoteResponse(final Response response) {
            super(response);
        }

        private CreateBookingCaseNoteResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static CreateBookingCaseNoteResponse respond201WithApplicationJson(final CaseNote entity) {
            final var responseBuilder = Response.status(201)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new CreateBookingCaseNoteResponse(responseBuilder.build(), entity);
        }
    }

    class CreateRelationshipResponse extends ResponseDelegate {

        private CreateRelationshipResponse(final Response response) {
            super(response);
        }

        private CreateRelationshipResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static CreateRelationshipResponse respond201WithApplicationJson(final Contact entity) {
            final var responseBuilder = Response.status(201)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new CreateRelationshipResponse(responseBuilder.build(), entity);
        }
    }

    class GetAlertsByOffenderNosResponse extends ResponseDelegate {

        private GetAlertsByOffenderNosResponse(final Response response) {
            super(response);
        }

        private GetAlertsByOffenderNosResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetAlertsByOffenderNosResponse respond200WithApplicationJson(final List<Alert> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAlertsByOffenderNosResponse(responseBuilder.build(), entity);
        }
    }

    class CreateOffenderCaseNoteResponse extends ResponseDelegate {

        private CreateOffenderCaseNoteResponse(final Response response) {
            super(response);
        }

        private CreateOffenderCaseNoteResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static CreateOffenderCaseNoteResponse respond201WithApplicationJson(final CaseNote entity) {
            final var responseBuilder = Response.status(201)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new CreateOffenderCaseNoteResponse(responseBuilder.build(), entity);
        }
    }

    class CreateRelationshipByOffenderNoResponse extends ResponseDelegate {

        private CreateRelationshipByOffenderNoResponse(final Response response) {
            super(response);
        }

        private CreateRelationshipByOffenderNoResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static CreateRelationshipByOffenderNoResponse respond201WithApplicationJson(final Contact entity) {
            final var responseBuilder = Response.status(201)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new CreateRelationshipByOffenderNoResponse(responseBuilder.build(), entity);
        }
    }

    class RecallOffenderBookingResponse extends ResponseDelegate {

        private RecallOffenderBookingResponse(final Response response) {
            super(response);
        }

        private RecallOffenderBookingResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static RecallOffenderBookingResponse respond200WithApplicationJson(final OffenderSummary entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new RecallOffenderBookingResponse(responseBuilder.build(), entity);
        }
    }

    class UpdateOffenderCaseNoteResponse extends ResponseDelegate {

        private UpdateOffenderCaseNoteResponse(final Response response) {
            super(response);
        }

        private UpdateOffenderCaseNoteResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static UpdateOffenderCaseNoteResponse respond201WithApplicationJson(final CaseNote entity) {
            final var responseBuilder = Response.status(201)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new UpdateOffenderCaseNoteResponse(responseBuilder.build(), entity);
        }
    }

    class UpdateAttendanceResponse extends ResponseDelegate {

        private UpdateAttendanceResponse(final Response response) {
            super(response);
        }

        private UpdateAttendanceResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static UpdateAttendanceResponse respond201WithApplicationJson() {
            final var responseBuilder = Response.status(201)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            return new UpdateAttendanceResponse(responseBuilder.build());
        }
    }
}
