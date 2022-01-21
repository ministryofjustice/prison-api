package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
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
import uk.gov.justice.hmpps.prison.api.model.NewCaseNote;
import uk.gov.justice.hmpps.prison.api.model.OffenceDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenceHistoryDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenderIdentifier;
import uk.gov.justice.hmpps.prison.api.model.OffenderNonAssociationDetails;
import uk.gov.justice.hmpps.prison.api.model.OffenderRelationship;
import uk.gov.justice.hmpps.prison.api.model.PersonalCareNeeds;
import uk.gov.justice.hmpps.prison.api.model.PhysicalAttributes;
import uk.gov.justice.hmpps.prison.api.model.PhysicalCharacteristic;
import uk.gov.justice.hmpps.prison.api.model.PhysicalMark;
import uk.gov.justice.hmpps.prison.api.model.PrisonDetails;
import uk.gov.justice.hmpps.prison.api.model.PrisonerBookingSummary;
import uk.gov.justice.hmpps.prison.api.model.PrivilegeSummary;
import uk.gov.justice.hmpps.prison.api.model.ProfileInformation;
import uk.gov.justice.hmpps.prison.api.model.PropertyContainer;
import uk.gov.justice.hmpps.prison.api.model.ReasonableAdjustments;
import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent;
import uk.gov.justice.hmpps.prison.api.model.SecondaryLanguage;
import uk.gov.justice.hmpps.prison.api.model.SentenceAdjustmentDetail;
import uk.gov.justice.hmpps.prison.api.model.SentenceCalcDates;
import uk.gov.justice.hmpps.prison.api.model.UpdateAttendance;
import uk.gov.justice.hmpps.prison.api.model.UpdateAttendanceBatch;
import uk.gov.justice.hmpps.prison.api.model.UpdateCaseNote;
import uk.gov.justice.hmpps.prison.api.model.VisitBalances;
import uk.gov.justice.hmpps.prison.api.model.VisitDetails;
import uk.gov.justice.hmpps.prison.api.model.VisitWithVisitors;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationSummary;
import uk.gov.justice.hmpps.prison.api.model.adjudications.ProvenAdjudicationSummary;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.core.HasWriteScope;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CaseNoteFilter;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.VisitInformationFilter;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.VisitInformationRepository.Prison;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;
import uk.gov.justice.hmpps.prison.service.AdjudicationService;
import uk.gov.justice.hmpps.prison.service.AppointmentsService;
import uk.gov.justice.hmpps.prison.service.BedAssignmentHistoryService;
import uk.gov.justice.hmpps.prison.service.BookingService;
import uk.gov.justice.hmpps.prison.service.CaseNoteService;
import uk.gov.justice.hmpps.prison.service.ContactService;
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException;
import uk.gov.justice.hmpps.prison.service.FinanceService;
import uk.gov.justice.hmpps.prison.service.ImageService;
import uk.gov.justice.hmpps.prison.service.IncidentService;
import uk.gov.justice.hmpps.prison.service.InmateAlertService;
import uk.gov.justice.hmpps.prison.service.InmateService;
import uk.gov.justice.hmpps.prison.service.MovementsService;
import uk.gov.justice.hmpps.prison.service.NoContentException;
import uk.gov.justice.hmpps.prison.service.OffenderNonAssociationsService;
import uk.gov.justice.hmpps.prison.service.keyworker.KeyWorkerAllocationService;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uk.gov.justice.hmpps.prison.util.ResourceUtils.nvl;

/**
 * Implementation of Booking (/bookings) endpoint.
 */
@RestController
@Api(tags = {"bookings"})
@RequestMapping("${api.base.path}/bookings")
@Validated
@AllArgsConstructor
@Slf4j
public class BookingResource {
    private final AuthenticationFacade authenticationFacade;
    private final BookingService bookingService;
    private final InmateService inmateService;
    private final CaseNoteService caseNoteService;
    private final InmateAlertService inmateAlertService;
    private final BedAssignmentHistoryService bedAssignmentHistoryService;
    private final FinanceService financeService;
    private final ContactService contactService;
    private final AdjudicationService adjudicationService;
    private final ImageService imageService;
    private final KeyWorkerAllocationService keyworkerService;
    private final IncidentService incidentService;
    private final MovementsService movementsService;
    private final AppointmentsService appointmentsService;
    private final OffenderNonAssociationsService offenderNonAssociationsService;

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Prisoners Booking Summary ", notes = "Returns data that is available to the users caseload privileges, at least one attribute of a prisonId, bookingId or offenderNo must be specified")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "page", dataType = "java.lang.Integer", paramType = "query",
            value = "Results page you want to retrieve (0..N). Default 0, e.g. the first page", example = "0"),
        @ApiImplicitParam(name = "size", dataType = "java.lang.Integer", paramType = "query",
            value = "Number of records per page. Default 10"),
        @ApiImplicitParam(name = "sort", dataType = "java.lang.String", paramType = "query", allowableValues = "lastName,firstName,offenderNo,bookingId,prisonId,ASC,DESC",
            value = "Sort as combined comma separated property and uppercase direction. Multiple sort params allowed to sort by multiple properties. Default to lastName,firstName,offenderNo ASC")})
    @GetMapping("/v2")
    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "VIEW_PRISONER_DATA"})
    public Page<PrisonerBookingSummary> getPrisonerBookingsV2(
        @RequestParam(value = "prisonId", required = false) @ApiParam(value = "Filter by prison Id", example = "MDI") final String prisonId,
        @RequestParam(value = "bookingId", required = false) @ApiParam("Filter by a list of booking ids") final List<Long> bookingIds,
        @RequestParam(value = "offenderNo", required = false) @ApiParam("Filter by a list of offender numbers") final List<String> offenderNos,
        @RequestParam(value = "iepLevel", defaultValue = "false", required = false) @ApiParam(value = "Return IEP level data", defaultValue = "false") final boolean iepLevel,
        @RequestParam(value = "legalInfo", defaultValue = "false", required = false) @ApiParam(value = "Return additional legal information (imprisonmentStatus, legalStatus, convictedStatus)", defaultValue = "false") final boolean legalInfo,
        @RequestParam(value = "image", defaultValue = "false", required = false) @ApiParam(value = "Return facial ID for latest prisoner image", defaultValue = "false") final boolean imageId,
        @ApiIgnore
        @PageableDefault(sort = {"lastName","firstName","offenderNo"}, direction = Direction.ASC) final Pageable pageable) {

        return bookingService.getPrisonerBookingSummary(prisonId, bookingIds, offenderNos, iepLevel, legalInfo, imageId, pageable);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Retrieves a specific movement for a booking", notes = "Must booking in user caseload or have system privilege")
    @GetMapping("/{bookingId}/movement/{sequenceNumber}")
    public Movement getMovementByBookingIdAndSequence(@PathVariable("bookingId") @ApiParam(value = "The booking id of offender", required = true) final Long bookingId, @PathVariable("sequenceNumber") @ApiParam(value = "The sequence Number of the movement", required = true) final Integer sequenceNumber) {
        return movementsService.getMovementByBookingIdAndSequence(bookingId, sequenceNumber).orElseThrow(EntityNotFoundException.withMessage(format("Movement Not found booking Id %d, seq %d", bookingId, sequenceNumber)));
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Offender detail.", notes = "Offender detail.", nickname = "getOffenderBooking")
    @GetMapping("/{bookingId}")
    public InmateDetail getOffenderBooking(@PathVariable("bookingId") @ApiParam(value = "The booking id of offender", required = true) final Long bookingId, @RequestParam(value = "basicInfo", required = false, defaultValue = "false") @ApiParam(value = "If set to true then only basic data is returned", defaultValue = "false") final boolean basicInfo, @RequestParam(value = "extraInfo", required = false, defaultValue = "false") @ApiParam(value = "Only used when requesting more than basic data, returns identifiers,offences,aliases,sentence dates,convicted status", defaultValue = "false") final boolean extraInfo) {

        return basicInfo && !extraInfo ?
                inmateService.getBasicInmateDetail(bookingId)
                : inmateService.findInmate(bookingId, extraInfo, false);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Offender detail.", notes = "Offender detail.", nickname = "getOffenderBookingByOffenderNo")
    @GetMapping("/offenderNo/{offenderNo}")
    public InmateDetail getOffenderBookingByOffenderNo(@PathVariable("offenderNo") @ApiParam(value = "The offenderNo of offender", required = true) final String offenderNo, @RequestParam(value = "fullInfo", required = false, defaultValue = "false") @ApiParam(value = "If set to true then full data is returned", defaultValue = "false") final boolean fullInfo, @RequestParam(value = "extraInfo", required = false, defaultValue = "false") @ApiParam(value = "Only used when fullInfo=true, returns identifiers,offences,aliases,sentence dates,convicted status", defaultValue = "false") final boolean extraInfo, @RequestParam(value = "csraSummary", required = false, defaultValue = "false") @ApiParam(value = "Only used when fullInfo=true, returns the applicable CSRA classification for this offender", defaultValue = "false") final boolean csraSummary) {
        return fullInfo || extraInfo ?
                inmateService.findOffender(offenderNo, extraInfo, csraSummary) :
                inmateService.getBasicInmateDetail(bookingService.getLatestBookingByOffenderNo(offenderNo).getBookingId());
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Offender detail.", notes = "Offender detail for offenders", nickname = "getBasicInmateDetailsForOffenders")
    @PostMapping("/offenders")
    public List<InmateBasicDetails> getBasicInmateDetailsForOffenders(@RequestBody @ApiParam(value = "The offenderNo of offender", required = true) final Set<String> offenders, @RequestParam(value = "activeOnly", required = false, defaultValue = "true") @ApiParam(value = "Returns only Offender details with an active booking if true, otherwise Offenders without an active booking are included", defaultValue = "true") final Boolean activeOnly) {
        final var active = activeOnly == null || activeOnly;
        return inmateService.getBasicInmateDetailsForOffenders(offenders, active);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Basic offender details by booking ids - POST version to allow for large numbers", notes = "Basic offender details by booking ids", nickname = "getBasicInmateDetailsForOffendersByBookingIds")
    @PostMapping("/offenders/{agencyId}/list")
    public List<InmateBasicDetails> getBasicInmateDetailsByBookingIds(@PathVariable("agencyId") @ApiParam(value = "The prison where the offenders are booked - the response is restricted to bookings at this prison", required = true) final String caseload, @RequestBody @ApiParam(value = "The bookingIds to identify the offenders", required = true) final Set<Long> bookingIds) {
        return inmateService.getBasicInmateDetailsByBookingIds(caseload, bookingIds);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "All scheduled activities for offender.", notes = "All scheduled activities for offender.", nickname = "getBookingActivities")
    @GetMapping("/{bookingId}/activities")
    public ResponseEntity<List<ScheduledEvent>> getBookingActivities(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId, @RequestParam(value = "fromDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Returned activities must be scheduled on or after this date (in YYYY-MM-DD format).") final LocalDate fromDate, @RequestParam(value = "toDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Returned activities must be scheduled on or before this date (in YYYY-MM-DD format).") final LocalDate toDate, @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @ApiParam(value = "Requested offset of first record in returned collection of activity records.", defaultValue = "0") final Long pageOffset, @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @ApiParam(value = "Requested limit to number of activity records returned.", defaultValue = "10") final Long pageLimit, @RequestHeader(value = "Sort-Fields", required = false) @ApiParam("Comma separated list of one or more of the following fields - <b>eventDate, startTime, endTime, eventLocation</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") final Order sortOrder) {
        final var activities = bookingService.getBookingActivities(
                bookingId,
                fromDate,
                toDate,
                nvl(pageOffset, 0L),
                nvl(pageLimit, 10L),
                sortFields,
                sortOrder);

        return ResponseEntity.ok()
                .headers(activities.getPaginationHeaders())
                .body(activities.getItems());
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Today's scheduled activities for offender.", notes = "Today's scheduled activities for offender.", nickname = "getBookingActivitiesForToday")
    @GetMapping("/{bookingId}/activities/today")
    public List<ScheduledEvent> getBookingActivitiesForToday(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId, @RequestHeader(value = "Sort-Fields", required = false) @ApiParam("Comma separated list of one or more of the following fields - <b>eventDate, startTime, endTime, eventLocation</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") final Order sortOrder) {
        final var today = LocalDate.now();

        return bookingService.getBookingActivities(
                bookingId,
                today,
                today,
                sortFields,
                sortOrder);
    }

    @ApiResponses({
            @ApiResponse(code = 201, message = "Attendance data has been updated"),
            @ApiResponse(code = 400, message = "Invalid request - e.g. validation error.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - user not authorised to attend activity.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Resource not found - booking or event does not exist or is not accessible to user.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Internal server error.", response = ErrorResponse.class)})
    @PutMapping("/offenderNo/{offenderNo}/activities/{activityId}/attendance")
    @ProxyUser
    public ResponseEntity<Void> updateAttendance(@PathVariable("offenderNo") @ApiParam(value = "The offenderNo of the prisoner", required = true, example = "A1234AA") final String offenderNo, @PathVariable("activityId") @ApiParam(value = "The activity id", required = true, example = "1212131") final Long activityId, @RequestBody @ApiParam(required = true, example = "{eventOutcome = 'ATT', performance = 'ACCEPT' outcomeComment = 'Turned up very late'}") @NotNull final UpdateAttendance updateAttendance) {
        bookingService.updateAttendance(offenderNo, activityId, updateAttendance);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @ApiResponses({
            @ApiResponse(code = 201, message = "Attendance data has been updated"),
            @ApiResponse(code = 400, message = "Invalid request - e.g. validation error.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - user not authorised to attend activity.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Resource not found - booking or event does not exist or is not accessible to user.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Internal server error.", response = ErrorResponse.class)})
    @ApiOperation(value = "Update offender attendance and pay.", notes = "Update offender attendance and pay.", nickname = "updateAttendance")
    @PutMapping("/{bookingId}/activities/{activityId}/attendance")
    @ProxyUser
    public ResponseEntity<Void> updateAttendance(@NotNull @PathVariable("bookingId") @ApiParam(value = "The booking Id of the prisoner", required = true, example = "213531") final Long bookingId, @NotNull @PathVariable("activityId") @ApiParam(value = "The activity id", required = true, example = "1212131") final Long activityId, @RequestBody @NotNull @ApiParam(required = true) final UpdateAttendance updateAttendance) {
        bookingService.updateAttendance(bookingId, activityId, updateAttendance);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @ApiResponses({
            @ApiResponse(code = 201, message = "Attendance data has been updated"),
            @ApiResponse(code = 400, message = "Invalid request - e.g. validation error.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - user not authorised to attend activity.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Resource not found - booking or event does not exist or is not accessible to user.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Internal server error.", response = ErrorResponse.class)})
    @ApiOperation(value = "Update attendance and pay for multiple bookings.", notes = "Update offender attendance and pay.", nickname = "updateAttendance")
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/activities/attendance")
    public ResponseEntity<Void> updateAttendanceForMultipleBookingIds(@RequestBody @ApiParam(required = true) final @NotNull UpdateAttendanceBatch body) {
        bookingService.updateAttendanceForMultipleBookingIds(body.getBookingActivities(), UpdateAttendance
                .builder()
                .eventOutcome(body.getEventOutcome())
                .performance(body.getPerformance())
                .outcomeComment(body.getOutcomeComment())
                .build());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Return a set Incidents for a given booking Id", notes = "Can be filtered by participation type and incident type")
    @GetMapping("/{bookingId}/incidents")
    public List<IncidentCase> getIncidentsByBookingId(@PathVariable("bookingId") @ApiParam(value = "bookingId", required = true) @NotNull final Long bookingId, @RequestParam("incidentType") @ApiParam(value = "incidentType", example = "ASSAULT", allowMultiple = true) final List<String> incidentTypes, @RequestParam("participationRoles") @ApiParam(value = "participationRoles", example = "ASSIAL", allowMultiple = true, allowableValues = "ACTINV,ASSIAL,FIGHT,IMPED,PERP,SUSASS,SUSINV,VICT,AI,PAS,AO") final List<String> participationRoles) {
        return incidentService.getIncidentCasesByBookingId(bookingId, incidentTypes, participationRoles);
    }

    @ApiResponses({
            @ApiResponse(code = 201, message = "Alert id.", response = AlertCreated.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)
    })
    @ApiOperation("Create an alert")
    @PostMapping("/{bookingId}/alert")
    @ProxyUser
    public ResponseEntity<AlertCreated> postAlert(@PathVariable("bookingId") @ApiParam(value = "bookingId", required = true) final Long bookingId, @Valid @RequestBody @ApiParam(value = "Alert details", required = true) final CreateAlert alert) {
        final var alertId = inmateAlertService.createNewAlert(bookingId, alert);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AlertCreated(alertId));
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)
    })
    @ApiOperation("Update an alert")
    @PutMapping("/{bookingId}/alert/{alertSeq}")
    @ProxyUser
    public Alert updateAlert(@PathVariable("bookingId") @ApiParam(value = "bookingId", required = true) final Long bookingId, @PathVariable("alertSeq") @ApiParam(value = "alertSeq", required = true) final Long alertSeq, @Valid @RequestBody @ApiParam(value = "Alert details", required = true) final AlertChanges alert) {
        return inmateAlertService.updateAlert(bookingId, alertSeq, alert);
    }

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Offender alerts.", notes = "Offender alerts.", nickname = "getOffenderAlerts")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "page", dataType = "java.lang.Integer", paramType = "query",
            value = "Results page you want to retrieve (0..N). Default 0, e.g. the first page", example = "0"),
        @ApiImplicitParam(name = "size", dataType = "java.lang.Integer", paramType = "query",
            value = "Number of records per page. Default 10"),
        @ApiImplicitParam(name = "sort", dataType = "java.lang.String", paramType = "query", allowableValues = "alertId,bookingId,alertType,alertCode,comment,dateCreated,dateExpires,active,ASC,DESC",
            value = "Sort as combined comma separated property and uppercase direction. Multiple sort params allowed to sort by multiple properties. Default to dateExpires,DESC and dateCreated,DESC")})
    @GetMapping("/{bookingId}/alerts/v2")
    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public Page<Alert> getOffenderAlertsV2(
        @PathVariable("bookingId") @ApiParam(value = "The booking id for the booking", required = true) final Long bookingId,
        @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "start alert date to search from", example = "2021-02-03") final LocalDate from,
        @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "end alert date to search up to (including this date)", example = "2021-02-04") final LocalDate to,
        @RequestParam(value = "alertType", required = false) @ApiParam(value = "Filter by alert type", example = "X") final String alertType,
        @RequestParam(value = "alertStatus", required = false) @ApiParam(value = "Filter by alert active status", example = "ACTIVE") final String alertStatus,
        @ApiIgnore
        @PageableDefault(sort = {"dateExpires", "dateCreated"}, direction = Sort.Direction.DESC) final Pageable pageable) {

        return inmateAlertService.getAlertsForBooking(bookingId, from, to, alertType, alertStatus, pageable);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Offender alert detail.", notes = "Offender alert detail.", nickname = "getOffenderAlert")
    @GetMapping("/{bookingId}/alerts/{alertId}")
    public Alert getOffenderAlert(@PathVariable("bookingId") @ApiParam(value = "The booking id of offender", required = true) final Long bookingId, @PathVariable("alertId") @ApiParam(value = "The Alert Id", required = true) final Long alertId) {
        return inmateAlertService.getInmateAlert(bookingId, alertId);
    }

    @ApiOperation("Get alerts for a list of offenders at a prison")
    @PostMapping("/offenderNo/{agencyId}/alerts")
    public List<Alert> getAlertsByOffenderNosAtAgency(@PathVariable("agencyId") @ApiParam(value = "The prison where the offenders are booked", required = true) final String agencyId, @RequestBody @ApiParam(value = "The required offender numbers (mandatory)", required = true) final List<String> offenderNos) {
        return inmateAlertService.getInmateAlertsByOffenderNosAtAgency(agencyId, offenderNos);
    }

    @ApiOperation("Get alerts for a list of offenders. Requires VIEW_PRISONER_DATA role")
    @PostMapping("/offenderNo/alerts")
    public List<Alert> getAlertsByOffenderNos(@RequestBody @NotEmpty(message = "A minimum of one offender number is required") @ApiParam(value = "The required offender numbers (mandatory)", required = true) final List<String> offenderNos) {
        return inmateAlertService.getInmateAlertsByOffenderNos(offenderNos, true, "bookingId,alertId", Order.ASC);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Offender aliases.", notes = "Offender aliases.", nickname = "getOffenderAliases")
    @GetMapping("/{bookingId}/aliases")
    public ResponseEntity<List<Alias>> getOffenderAliases(@PathVariable("bookingId") @ApiParam(value = "The booking id of offender", required = true) final Long bookingId, @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @ApiParam(value = "Requested offset of first record in returned collection of alias records.", defaultValue = "0") final Long pageOffset, @RequestHeader(value = "Page-Limit", defaultValue = "5", required = false) @ApiParam(value = "Requested limit to number of alias records returned.", defaultValue = "5") final Long pageLimit, @RequestHeader(value = "Sort-Fields", required = false) @ApiParam("Comma separated list of one or more of the following fields - <b>firstName, lastName, age, dob, middleName, nameType, createDate</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") final Order sortOrder) {
        return inmateService.findInmateAliases(
                bookingId,
                sortFields,
                sortOrder,
                nvl(pageOffset, 0L),
                nvl(pageLimit, 5L)).getResponse();
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Offender assessment detail.", notes = "Offender assessment detail.", nickname = "getAssessmentByCode")
    @GetMapping("/{bookingId}/assessment/{assessmentCode}")
    public Assessment getAssessmentByCode(@PathVariable("bookingId") @ApiParam(value = "The booking id of offender", required = true) final Long bookingId, @PathVariable("assessmentCode") @ApiParam(value = "Assessment Type Code", required = true) final String assessmentCode) {
        return inmateService.getInmateAssessmentByCode(bookingId, assessmentCode).orElseThrow(() -> {
            throw EntityNotFoundException.withMessage("Offender does not have a [" + assessmentCode + "] assessment on record.");
        });
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Assessment Information", notes = "Assessment Information", nickname = "getAssessments")
    @GetMapping("/{bookingId}/assessments")
    public List<Assessment> getAssessments(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId) {
        return inmateService.getAssessments(bookingId);
    }

    @ApiOperation(value = "Offender case notes.", notes = "Offender case notes.")
    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @GetMapping("/{bookingId}/caseNotes")
    @VerifyBookingAccess
    public Page<CaseNote> getOffenderCaseNotes(@PathVariable("bookingId") @ApiParam(value = "The booking id of offender", example = "23412312", required = true) final Long bookingId,
                                                               @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "start contact date to search from", example = "2021-02-03") final LocalDate from,
                                                               @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam(value = "end contact date to search up to (including this date)", example = "2021-02-04") final LocalDate to,
                                                               @RequestParam(value = "type", required = false) @ApiParam(value = "Filter by case note type", example = "GEN") final String type,
                                                               @RequestParam(value = "subType", required = false) @ApiParam(value = "Filter by case note sub-type", example = "OBS") final String subType,
                                                               @RequestParam(value = "prisonId", required = false) @ApiParam(value = "Filter by the ID of the prison", example = "LEI") final String prisonId,
                                                               @PageableDefault(sort = {"occurrenceDateTime"}, direction = Sort.Direction.DESC) final Pageable pageable) {

        final var caseNoteFilter = CaseNoteFilter.builder()
            .type(type)
            .subType(subType)
            .prisonId(prisonId)
            .startDate(from)
            .endDate(to)
            .bookingId(bookingId)
            .build();

        return caseNoteService.getCaseNotes(caseNoteFilter, pageable);
    }

    @ApiOperation(value = "Offender case note detail.", notes = "Offender case note detail.", nickname = "getOffenderCaseNote")
    @GetMapping("/{bookingId}/caseNotes/{caseNoteId}")
    public CaseNote getOffenderCaseNote(@PathVariable("bookingId") @ApiParam(value = "The booking id of offender", required = true) final Long bookingId, @PathVariable("caseNoteId") @ApiParam(value = "The case note id", required = true) final Long caseNoteId) {
        return caseNoteService.getCaseNote(bookingId, caseNoteId);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Offender IEP (Incentives & Earned Privileges) summary.", notes = "Offender IEP (Incentives & Earned Privileges) summary.", nickname = "getBookingIEPSummary")
    @GetMapping("/{bookingId}/iepSummary")
    public PrivilegeSummary getBookingIEPSummary(
        @PathVariable("bookingId") @ApiParam(value = "The booking id of offender", required = true) final Long bookingId,
        @RequestParam(value = "withDetails", required = false, defaultValue = "false") @ApiParam(value = "Toggle to return IEP detail entries in response (or not).", required = false) final boolean withDetails) {
        return bookingService.getBookingIEPSummary(bookingId, withDetails);
    }

    @ApiOperation("Add a new IEP (Incentives & Earned Privileges) level to an offender's booking.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/{bookingId}/iepLevels")
    @PreAuthorize("hasRole('MAINTAIN_IEP') and hasAuthority('SCOPE_write')")
    @ProxyUser
    public ResponseEntity<Void> addIepLevel(@PathVariable("bookingId") @ApiParam(value = "The booking id of the offender", required = true) final Long bookingId, @RequestBody @ApiParam(value = "The new IEP Level and accompanying comment (reason for change).", required = true) @NotNull final IepLevelAndComment iepLevel) {
        bookingService.addIepLevel(bookingId, authenticationFacade.getCurrentUsername(), iepLevel);
        return ResponseEntity.noContent().build();
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Offenders IEP (Incentives & Earned Privileges) summary.", notes = "Offenders IEP (Incentives & Earned Privileges) summary.", nickname = "getBookingIEPSummaryForOffenders")
    @GetMapping("/offenders/iepSummary")
    public Collection<PrivilegeSummary> getBookingIEPSummaryForOffenders(@RequestParam("bookings") @ApiParam(value = "The booking ids of offender", required = true) final List<Long> bookings, @RequestParam(value = "withDetails", required = false, defaultValue = "false") @ApiParam(value = "Toggle to return IEP detail entries in response (or not).", required = true) final boolean withDetails) {
        return bookingService.getBookingIEPSummary(bookings, withDetails).values();

    }

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Prisoners IEP (Incentives & Earned Privileges) summary for a list of booking IDs", notes = "Must have prisoner in users caseload access data")
    @PostMapping("/iepSummary")
    public Collection<PrivilegeSummary> getBookingIEPSummaryDetailForBookingIds(@NotNull @RequestBody @ApiParam(value = "The booking ids of prisoners", required = true) final List<Long> bookings) {
        return bookingService.getBookingIEPSummary(bookings, true).values();

    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Image detail (without image data).", notes = "Image detail (without image data).", nickname = "getMainImageForBookings")
    @GetMapping("/{bookingId}/image")
    public ImageDetail getMainImageForBookings(@PathVariable("bookingId") @ApiParam(value = "The booking id of offender", required = true) final Long bookingId) {
        return inmateService.getMainBookingImage(bookingId);
    }

    @ApiResponses({
            @ApiResponse(code = 404, message = "Requested resource not found."),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.")})
    @ApiOperation(value = "Image data (as bytes).", notes = "Image data (as bytes).", nickname = "getMainBookingImageDataByNo")
    @GetMapping(value = "/offenderNo/{offenderNo}/image/data", produces = "image/jpeg")
    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "VIEW_PRISONER_DATA"})
    public ResponseEntity<byte[]> getMainBookingImageDataByNo(@PathVariable("offenderNo") @ApiParam(value = "The offender No of offender", required = true) final String offenderNo, @RequestParam(value = "fullSizeImage", defaultValue = "false", required = false) @ApiParam(value = "Return full size image", defaultValue = "false") final boolean fullSizeImage) {
        return imageService.getImageContent(offenderNo, fullSizeImage)
                .map(bytes -> new ResponseEntity<>(bytes, HttpStatus.OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @ApiResponses({
            @ApiResponse(code = 404, message = "Requested resource not found."),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.")})
    @ApiOperation(value = "Image data (as bytes).", notes = "Image data (as bytes).", nickname = "getMainBookingImageData")
    @GetMapping(value = "/{bookingId}/image/data", produces = "image/jpeg")
    public ResponseEntity<byte[]> getMainBookingImageData(@PathVariable("bookingId") @ApiParam(value = "The booking id of offender", required = true) final Long bookingId, @RequestParam(value = "fullSizeImage", defaultValue = "false", required = false) @ApiParam(value = "Return full size image", defaultValue = "false") final boolean fullSizeImage) {
        final var mainBookingImage = inmateService.getMainBookingImage(bookingId);
        return imageService.getImageContent(mainBookingImage.getImageId(), fullSizeImage)
                .map(bytes -> new ResponseEntity<>(bytes, HttpStatus.OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Offender sentence detail (key dates and additional days awarded).", nickname = "getBookingSentenceDetail", notes = "<h3>Algorithm</h3><ul><li>If there is a confirmed release date, the offender release date is the confirmed release date.</li><li>If there is no confirmed release date for the offender, the offender release date is either the actual parole date or the home detention curfew actual date.</li><li>If there is no confirmed release date, actual parole date or home detention curfew actual date for the offender, the release date is the later of the nonDtoReleaseDate or midTermDate value (if either or both are present)</li></ul>")
    @GetMapping("/{bookingId}/sentenceDetail")
    public SentenceCalcDates getBookingSentenceDetail(
        @RequestHeader(value = "version", defaultValue = "1.0", required = false) @ApiParam(value = "Version of Sentence Calc Dates, 1.0 is default", defaultValue = "1.0") final String version,
        @PathVariable("bookingId") @ApiParam(value = "The booking id of offender", required = true) final Long bookingId) {
        if ("1.1".equals(version)) {
            return bookingService.getBookingSentenceCalcDatesV1_1(bookingId);
        }
        return bookingService.getBookingSentenceCalcDates(bookingId);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Offender sentence adjustments.", nickname = "getBookingSentenceAdjustments")
    @GetMapping("/{bookingId}/sentenceAdjustments")
    public SentenceAdjustmentDetail getBookingSentenceAdjustments(@PathVariable("bookingId") @ApiParam(value = "The booking id of offender", required = true) final Long bookingId) {
        return bookingService.getBookingSentenceAdjustments(bookingId);
    }

    @ApiResponses({
            @ApiResponse(code = 201, message = "The Case Note has been recorded. The updated object is returned including the status.", response = CaseNote.class),
            @ApiResponse(code = 409, message = "The case note has already been recorded under the booking. The current unmodified object (including status) is returned.", response = ErrorResponse.class)})
    @ApiOperation(value = "Create case note for offender.", notes = "Create case note for offender.", nickname = "createBookingCaseNote", hidden = true)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{bookingId}/caseNotes")
    @HasWriteScope
    @ProxyUser
    public CaseNote createBookingCaseNote(@PathVariable("bookingId") @ApiParam(value = "The booking id of offender", required = true) final Long bookingId, @RequestBody @ApiParam(required = true) final NewCaseNote body) {
        return caseNoteService.createCaseNote(bookingId, body, authenticationFacade.getCurrentUsername());
    }

    @ApiResponses({
            @ApiResponse(code = 201, message = "The Case Note has been recorded. The updated object is returned including the status.", response = CaseNote.class),
            @ApiResponse(code = 409, message = "The case note has already been recorded under the booking. The current unmodified object (including status) is returned.", response = ErrorResponse.class)})
    @ApiOperation(value = "Create case note for offender.", notes = "Create case note for offender.", nickname = "createOffenderCaseNote", hidden = true)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/offenderNo/{offenderNo}/caseNotes")
    @HasWriteScope
    @ProxyUser
    public CaseNote createOffenderCaseNote(@PathVariable("offenderNo") @ApiParam(value = "The offenderNo of offender", required = true) final String offenderNo, @RequestBody @ApiParam(required = true) final NewCaseNote body) {
        return caseNoteService.createCaseNote(offenderNo, body, authenticationFacade.getCurrentUsername());
    }

    @ApiResponses({
            @ApiResponse(code = 201, message = "Case Note amendment processed successfully. Updated case note is returned.", response = CaseNote.class),
            @ApiResponse(code = 400, message = "Invalid request - e.g. amendment text not provided.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - user not authorised to amend case note.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Resource not found - booking or case note does not exist or is not accessible to user.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Internal server error.", response = ErrorResponse.class)})
    @ApiOperation(value = "Amend offender case note.", notes = "Amend offender case note.", nickname = "updateOffenderCaseNote", hidden = true)
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/{bookingId}/caseNotes/{caseNoteId}")
    @HasWriteScope
    @ProxyUser
    public CaseNote updateOffenderCaseNote(@PathVariable("bookingId") @ApiParam(value = "The booking id of offender", required = true, example = "1231212") final Long bookingId, @PathVariable("caseNoteId") @ApiParam(value = "The case note id", required = true, example = "1212134") final Long caseNoteId, @RequestBody @ApiParam(required = true) final UpdateCaseNote body) {
        return caseNoteService.updateCaseNote(
                bookingId, caseNoteId, authenticationFacade.getCurrentUsername(), body.getText());
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Offender account balances.", notes = "Offender account balances.", nickname = "getBalances")
    @GetMapping("/{bookingId}/balances")
    public Account getBalances(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId) {
        return financeService.getBalances(bookingId);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "List of active property containers", notes = "List of active property containers", nickname = "getOffenderPropertyContainers")
    @GetMapping("/{bookingId}/property")
    public List<PropertyContainer> getOffenderPropertyContainers(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId) {
        return bookingService.getOffenderPropertyContainers(bookingId);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Get Offender main offence detail.", notes = "Offender main offence detail.")
    @GetMapping("/{bookingId}/mainOffence")
    public List<OffenceDetail> getMainOffence(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId) {
        return bookingService.getMainOffenceDetails(bookingId);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Get Offender main offence detail.", notes = "Post version to allow specifying a large number of bookingIds.")
    @PostMapping("/mainOffence")
    public List<OffenceDetail> getMainOffence(@RequestBody @ApiParam(value = "The bookingIds to identify the offenders", required = true) final Set<Long> bookingIds) {
        return bookingService.getMainOffenceDetails(bookingIds);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Offence history.", notes = "All Offences recorded for this offender.", nickname = "getOffenceHistory")
    @GetMapping("/offenderNo/{offenderNo}/offenceHistory")
    @PreAuthorize("hasAnyRole('SYSTEM_USER','VIEW_PRISONER_DATA','CREATE_CATEGORISATION','APPROVE_CATEGORISATION')")
    public List<OffenceHistoryDetail> getOffenceHistory(@PathVariable("offenderNo") @ApiParam(value = "The offender number", required = true) final String offenderNo, @RequestParam(value = "convictionsOnly", required = false, defaultValue = "true") @ApiParam(value = "include offences with convictions only", defaultValue = "true") final boolean convictionsOnly) {
        return bookingService.getOffenceHistory(offenderNo, convictionsOnly);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Offender Physical Attributes.", notes = "Offender Physical Attributes.", nickname = "getPhysicalAttributes")
    @GetMapping("/{bookingId}/physicalAttributes")
    public PhysicalAttributes getPhysicalAttributes(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId) {
        return inmateService.getPhysicalAttributes(bookingId);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Physical Characteristics", notes = "Physical Characteristics", nickname = "getPhysicalCharacteristics")
    @GetMapping("/{bookingId}/physicalCharacteristics")
    public List<PhysicalCharacteristic> getPhysicalCharacteristics(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId) {
        return inmateService.getPhysicalCharacteristics(bookingId);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Physical Mark Information", notes = "Physical Mark Information", nickname = "getPhysicalMarks")
    @GetMapping("/{bookingId}/physicalMarks")
    public List<PhysicalMark> getPhysicalMarks(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId) {
        return inmateService.getPhysicalMarks(bookingId);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Personal Care Needs", notes = "Personal Care Need", nickname = "getPersonalCareNeeds")
    @GetMapping("/{bookingId}/personal-care-needs")
    public PersonalCareNeeds getPersonalCareNeeds(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId, @RequestParam(value = "type", required = false) @NotEmpty(message = "problemTypes: must not be empty") @ApiParam(value = "a list of types and optionally subtypes (joined with +) to search.", example = "DISAB+RM", required = true) final List<String> problemTypes) {
        return inmateService.getPersonalCareNeeds(bookingId, problemTypes);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Military Records", notes = "Military Records", nickname = "getMilitaryRecords")
    @GetMapping("/{bookingId}/military-records")
    public MilitaryRecords getMilitaryRecords(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId) {
        return bookingService.getMilitaryRecords(bookingId);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Personal Care Needs  - POST version to allow for large numbers of offenders", notes = "Personal Care Needs", nickname = "getPersonalCareNeeds")
    @PostMapping("/offenderNo/personal-care-needs")
    public List<PersonalCareNeeds> getPersonalCareNeeds(@RequestBody @NotEmpty(message = "offenderNo: must not be empty") @ApiParam(value = "The required offender numbers (mandatory)", required = true) final List<String> offenderNos, @RequestParam(value = "type", required = false) @NotEmpty(message = "problemTypes: must not be empty") @ApiParam(value = "a list of types and optionally subtypes (joined with +) to search.", example = "DISAB+RM", required = true) final List<String> problemTypes) {
        return inmateService.getPersonalCareNeeds(offenderNos, problemTypes);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Reasonable Adjustment Information", notes = "Reasonable Adjustment Information", nickname = "getReasonableAdjustment")
    @GetMapping("/{bookingId}/reasonable-adjustments")
    public ReasonableAdjustments getReasonableAdjustments(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId, @RequestParam(value = "type", required = false) @NotEmpty(message = "treatmentCodes: must not be empty") @ApiParam(value = "a list of treatment codes to search.", example = "PEEP", required = true) final List<String> treatmentCodes) {
        return inmateService.getReasonableAdjustments(bookingId, treatmentCodes);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Profile Information", notes = "Profile Information", nickname = "getProfileInformation")
    @GetMapping("/{bookingId}/profileInformation")
    public List<ProfileInformation> getProfileInformation(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId) {
        return inmateService.getProfileInformation(bookingId);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "The contact details and their relationship to the offender", notes = "The contact details and their relationship to the offender", nickname = "getRelationships")
    @GetMapping("/{bookingId}/relationships")
    public List<Contact> getRelationships(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId, @RequestParam(value = "relationshipType", required = false) @ApiParam("filter by the relationship type") final String relationshipType) {
        return contactService.getRelationships(bookingId, relationshipType, true);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "The contact details and their relationship to the offender", notes = "The contact details and their relationship to the offender", nickname = "getRelationshipsByOffenderNo")
    @GetMapping("/offenderNo/{offenderNo}/relationships")
    public List<Contact> getRelationshipsByOffenderNo(@PathVariable("offenderNo") @ApiParam(value = "The offender Offender No", required = true) final String offenderNo, @RequestParam("relationshipType") @ApiParam("filter by the relationship type") final String relationshipType) {
        return contactService.getRelationshipsByOffenderNo(offenderNo, relationshipType);
    }

    @ApiResponses({
            @ApiResponse(code = 201, message = "If successful the Contact object is returned.", response = Contact.class)})
    @ApiOperation(value = "Create a relationship with an offender", notes = "Create a relationship with an offender", nickname = "createRelationship")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{bookingId}/relationships")
    @ProxyUser
    public Contact createRelationship(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId, @RequestBody @ApiParam(value = "The person details and their relationship to the offender", required = true) final OffenderRelationship relationshipDetail) {
        return contactService.createRelationship(bookingId, relationshipDetail);
    }

    @ApiResponses({
            @ApiResponse(code = 201, message = "If successful the Contact object is returned.", response = Contact.class)})
    @ApiOperation(value = "Create a relationship with an offender", notes = "Create a relationship with an offender", nickname = "createRelationshipByOffenderNo")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/offenderNo/{offenderNo}/relationships")
    @ProxyUser
    public Contact createRelationshipByOffenderNo(@PathVariable("offenderNo") @ApiParam(value = "The offender Offender No", required = true) final String offenderNo, @RequestBody @ApiParam(value = "The person details and their relationship to the offender", required = true) final OffenderRelationship relationshipDetail) {
        return contactService.createRelationshipByOffenderNo(offenderNo, relationshipDetail);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Today's scheduled events for offender.", notes = "Today's scheduled events for offender.", nickname = "getEventsToday")
    @GetMapping("/{bookingId}/events/today")
    public List<ScheduledEvent> getEventsToday(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId) {
        return bookingService.getEventsToday(bookingId);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Identifiers for this booking", notes = "Identifiers for this booking", nickname = "getOffenderIdentifiers")
    @GetMapping("/{bookingId}/identifiers")
    public List<OffenderIdentifier> getOffenderIdentifiers(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) @NotNull final Long bookingId, @RequestParam(value = "type", required = false) @ApiParam(value = "Filter By Type", example = "PNC") final String identifierType) {
        return inmateService.getOffenderIdentifiers(bookingId, identifierType);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "All scheduled events for offender.", notes = "All scheduled events for offender.", nickname = "getEvents")
    @GetMapping("/{bookingId}/events")
    public List<ScheduledEvent> getEvents(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId, @RequestParam(value = "fromDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Returned events must be scheduled on or after this date (in YYYY-MM-DD format).") final LocalDate fromDate, @RequestParam(value = "toDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Returned events must be scheduled on or before this date (in YYYY-MM-DD format).") final LocalDate toDate) {
        return bookingService.getEvents(bookingId, fromDate, toDate);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Scheduled events for offender for coming week (from current day).", notes = "Scheduled events for offender for coming week (from current day).", nickname = "getEventsThisWeek")
    @GetMapping("/{bookingId}/events/thisWeek")
    public List<ScheduledEvent> getEventsThisWeek(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId) {
        return bookingService.getEventsThisWeek(bookingId);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Scheduled events for offender for following week.", notes = "Scheduled events for offender for following week.", nickname = "getEventsNextWeek")
    @GetMapping("/{bookingId}/events/nextWeek")
    public List<ScheduledEvent> getEventsNextWeek(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId) {
        return bookingService.getEventsNextWeek(bookingId);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Offender contacts (e.g. next of kin).", notes = "Offender contacts (e.g. next of kin).", nickname = "getContacts")
    @GetMapping("/{bookingId}/contacts")
    public ContactDetail getContacts(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId) {
        return contactService.getContacts(bookingId);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Count of case notes", notes = "Count of case notes", nickname = "getCaseNoteCount")
    @GetMapping("/{bookingId}/caseNotes/{type}/{subType}/count")
    public CaseNoteCount getCaseNoteCount(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId, @PathVariable("type") @ApiParam(value = "Case note type.", required = true) final String type, @PathVariable("subType") @ApiParam(value = "Case note sub-type.", required = true) final String subType, @RequestParam(value = "fromDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Only case notes occurring on or after this date (in YYYY-MM-DD format) will be considered.") final LocalDate fromDate, @RequestParam(value = "toDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Only case notes occurring on or before this date (in YYYY-MM-DD format) will be considered.") final LocalDate toDate) {
        return caseNoteService.getCaseNoteCount(
                bookingId,
                type,
                subType,
                fromDate,
                toDate);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Offender adjudications summary (awards and sanctions).", notes = "Offender adjudications (awards and sanctions).", nickname = "getAdjudicationSummary")
    @GetMapping("/{bookingId}/adjudications")
    public AdjudicationSummary getAdjudicationSummary(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId, @RequestParam(value = "awardCutoffDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Only awards ending on or after this date (in YYYY-MM-DD format) will be considered.") final LocalDate awardCutoffDate, @RequestParam(value = "adjudicationCutoffDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Only proved adjudications ending on or after this date (in YYYY-MM-DD format) will be counted.") final LocalDate adjudicationCutoffDate) {
        return adjudicationService.getAdjudicationSummary(bookingId,
                awardCutoffDate, adjudicationCutoffDate);
    }

    @ApiResponses({
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Offender proven adjudications count")
    @PostMapping("/proven-adjudications")
    @PreAuthorize("hasRole('VIEW_ADJUDICATIONS')")
    public List<ProvenAdjudicationSummary> getProvenAdjudicationSummaryForBookings(
                @RequestParam(value = "adjudicationCutoffDate", required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                @ApiParam("Only proved adjudications ending on or after this date (in YYYY-MM-DD format) will be counted. Default is 3 months")
                final LocalDate adjudicationCutoffDate,
                @NotNull @RequestBody List<Long> bookingIds) {
        return adjudicationService.getProvenAdjudications(bookingIds, adjudicationCutoffDate);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "All scheduled visits for offender.", notes = "All scheduled visits for offender.", nickname = "getBookingVisits")
    @GetMapping("/{bookingId}/visits")
    public ResponseEntity<List<ScheduledEvent>> getBookingVisits(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId, @RequestParam(value = "fromDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Returned visits must be scheduled on or after this date (in YYYY-MM-DD format).") final LocalDate fromDate, @RequestParam(value = "toDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Returned visits must be scheduled on or before this date (in YYYY-MM-DD format).") final LocalDate toDate, @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @ApiParam(value = "Requested offset of first record in returned collection of visit records.", defaultValue = "0") final Long pageOffset, @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @ApiParam(value = "Requested limit to number of visit records returned.", defaultValue = "10") final Long pageLimit, @RequestHeader(value = "Sort-Fields", required = false) @ApiParam("Comma separated list of one or more of the following fields - <b>eventDate, startTime, endTime, eventLocation</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") final Order sortOrder) {
        return bookingService.getBookingVisits(
                bookingId,
                fromDate,
                toDate,
                nvl(pageOffset, 0L),
                nvl(pageLimit, 10L),
                sortFields,
                sortOrder)
                .getResponse();
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "visits with visitor list for offender.", notes = "visits with visitor list for offender.", nickname = "getBookingVisitsWithVisitor")
    @GetMapping("/{bookingId}/visits-with-visitors")
    public Page<VisitWithVisitors> getBookingVisitsWithVisitor(
        @PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId,
        @RequestParam(value = "fromDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Returned visits must be scheduled on or after this date (in YYYY-MM-DD format).") final LocalDate fromDate,
        @RequestParam(value = "toDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Returned visits must be scheduled on or before this date (in YYYY-MM-DD format).") final LocalDate toDate,
        @RequestParam(value = "visitType", required = false) @ApiParam(value = "Type of visit", allowableValues = "SCON, OFFI") final String visitType,
        @RequestParam(value = "visitStatus", required = false) @ApiParam(name = "Status of visit. code from VIS_COMPLETE domain, e.g: CANC (Cancelled) or SCH (Scheduled)", example = "SCH") final String visitStatus,
        @RequestParam(value = "cancellationReason", required = false) @ApiParam(name = "Reason for cancellation. code from MOVE_CANC_RS domain, e.g: VISCANC (Visitor Cancelled) or NO_VO (No Visiting Order)", example = "NSHOW") final String cancellationReason,
        @RequestParam(value = "prisonId", required = false) @ApiParam(value = "The prison id", example = "MDI") final String prisonId,
        @RequestParam(value = "page", required = false) @ApiParam(value = "Target page number, zero being the first page", defaultValue = "0") final Integer pageIndex,
        @RequestParam(value = "size", required = false) @ApiParam(value = "The number of results per page", defaultValue = "20") final Integer pageSize) {
        final var pageIndexValue = ofNullable(pageIndex).orElse(0);
        final var pageSizeValue = ofNullable(pageSize).orElse(20);
        final PageRequest pageRequest = PageRequest.of(pageIndexValue, pageSizeValue);

        return bookingService.getBookingVisitsWithVisitor(VisitInformationFilter.builder()
            .bookingId(bookingId)
            .fromDate(fromDate)
            .toDate(toDate)
            .visitType(visitType)
            .visitStatus(visitStatus)
            .cancellationReason(cancellationReason)
            .prisonId(prisonId)
            .build(), pageRequest);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Today's scheduled visits for offender.", notes = "Today's scheduled visits for offender.", nickname = "getBookingVisitsForToday")
    @GetMapping("/{bookingId}/visits/today")
    public List<ScheduledEvent> getBookingVisitsForToday(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId, @RequestHeader(value = "Sort-Fields", required = false) @ApiParam("Comma separated list of one or more of the following fields - <b>eventDate, startTime, endTime, eventLocation</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") final Order sortOrder) {
        final var today = LocalDate.now();
        return bookingService.getBookingVisits(
                bookingId,
                today,
                today,
                sortFields,
                sortOrder);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Balances visit orders and privilege visit orders for offender.", notes = "Balances visit orders and privilege visit orders for offender.", nickname = "getBookingVisitsBalances")
    @GetMapping("/offenderNo/{offenderNo}/visit/balances")
    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public VisitBalances getBookingVisitBalances(@PathVariable("offenderNo") @ApiParam(value = "The offenderNo of offender", required = true) final String offenderNo) {
        final var identifiers = bookingService.getOffenderIdentifiers(offenderNo).getBookingAndSeq().orElseThrow(EntityNotFoundException.withMessage("No bookings found for offender {}", offenderNo));

        return bookingService.getBookingVisitBalances(identifiers.getBookingId()).orElseThrow(EntityNotFoundException.withId(identifiers.getBookingId()));
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Key worker details.", notes = "Key worker details. This should not be used - call keywork API instead")
    @GetMapping("/offenderNo/{offenderNo}/key-worker")
    @Deprecated
    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public Keyworker getKeyworkerByOffenderNo(@PathVariable("offenderNo") @ApiParam(value = "The offenderNo of offender", required = true) final String offenderNo) {
        final var offenderIdentifiers = bookingService.getOffenderIdentifiers(offenderNo).getBookingAndSeq().orElseThrow(EntityNotFoundException.withMessage("No bookings found for offender {}", offenderNo));
        return keyworkerService.getKeyworkerDetailsByBooking(offenderIdentifiers.getBookingId());
    }

    @ApiResponses({
        @ApiResponse(code = 204, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "The next visit for the offender.", notes = "The next visit for the offender. Will return 200 with no body if no next visit is scheduled", nickname = "getBookingVisitsNext")
    @GetMapping("/{bookingId}/visits/next")
    public VisitDetails getBookingVisitsNext(
        @PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId,
        @RequestParam(value = "withVisitors", required = false, defaultValue = "false") @ApiParam(value = "Toggle to return Visitors in response (or not).", required = false) final boolean withVisitors) {
        return bookingService.getBookingVisitNext(bookingId, withVisitors).orElse(null);
    }

    @ApiResponses({
        @ApiResponse(code = 204, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "The list of prisons for which there are visits for the specified booking.", notes = "To be used for filtering visits by prison", nickname = "getBookingVisitsPrisons")
    @GetMapping("/{bookingId}/visits/prisons")
    public List<PrisonDetails> getBookingVisitsPrisons(
        @PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId) {
        return bookingService.getBookingVisitsPrisons(bookingId);
    }

    @ApiOperation(value = "All scheduled appointments for offender.", notes = "All scheduled appointments for offender.", nickname = "getBookingsBookingIdAppointments")
    @GetMapping("/{bookingId}/appointments")
    public ResponseEntity<List<ScheduledEvent>> getBookingsBookingIdAppointments(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId, @RequestParam(value = "fromDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Returned appointments must be scheduled on or after this date (in YYYY-MM-DD format).") final LocalDate fromDate, @RequestParam(value = "toDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @ApiParam("Returned appointments must be scheduled on or before this date (in YYYY-MM-DD format).") final LocalDate toDate, @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @ApiParam(value = "Requested offset of first record in returned collection of appointment records.", defaultValue = "0") final Long pageOffset, @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @ApiParam(value = "Requested limit to number of appointment records returned.", defaultValue = "10") final Long pageLimit, @RequestHeader(value = "Sort-Fields", required = false) @ApiParam("Comma separated list of one or more of the following fields - <b>eventDate, startTime, endTime, eventLocation</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") final Order sortOrder) {
        return bookingService.getBookingAppointments(
                bookingId,
                fromDate,
                toDate,
                nvl(pageOffset, 0L),
                nvl(pageLimit, 10L),
                sortFields,
                sortOrder)
                .getResponse();
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Today's scheduled appointments for offender.", notes = "Today's scheduled appointments for offender.", nickname = "getBookingAppointmentsForToday")
    @GetMapping("/{bookingId}/appointments/today")
    public List<ScheduledEvent> getBookingAppointmentsForToday(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId, @RequestHeader(value = "Sort-Fields", required = false) @ApiParam("Comma separated list of one or more of the following fields - <b>eventDate, startTime, endTime, eventLocation</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") final Order sortOrder) {
        final var today = LocalDate.now();
        return bookingService.getBookingAppointments(
                bookingId,
                today,
                today,
                sortFields,
                sortOrder);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Scheduled appointments for offender for coming week (from current day).", notes = "Scheduled appointments for offender for coming week (from current day).", nickname = "getBookingAppointmentsForThisWeek")
    @GetMapping("/{bookingId}/appointments/thisWeek")
    public List<ScheduledEvent> getBookingAppointmentsForThisWeek(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId, @RequestHeader(value = "Sort-Fields", required = false) @ApiParam("Comma separated list of one or more of the following fields - <b>eventDate, startTime, endTime, eventLocation</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") final Order sortOrder) {
        final var fromDate = LocalDate.now();
        final var toDate = fromDate.plusDays(6);

        return bookingService.getBookingAppointments(
                bookingId,
                fromDate,
                toDate,
                sortFields,
                sortOrder);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    @ApiOperation(value = "Scheduled appointments for offender for following week.", notes = "Scheduled appointments for offender for following week.", nickname = "getBookingAppointmentsForNextWeek")
    @GetMapping("/{bookingId}/appointments/nextWeek")
    public List<ScheduledEvent> getBookingAppointmentsForNextWeek(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId, @RequestHeader(value = "Sort-Fields", required = false) @ApiParam("Comma separated list of one or more of the following fields - <b>eventDate, startTime, endTime, eventLocation</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") final Order sortOrder) {
        final var fromDate = LocalDate.now().plusDays(7);
        final var toDate = fromDate.plusDays(6);

        return bookingService.getBookingAppointments(
                bookingId,
                fromDate,
                toDate,
                sortFields,
                sortOrder);
    }

    @ApiResponses({
            @ApiResponse(code = 201, message = "The appointment has been recorded. The updated object is returned including the status.", response = ScheduledEvent.class)})
    @ApiOperation(value = "Create appointment for offender.", notes = "Create appointment for offender.", nickname = "postBookingsBookingIdAppointments")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{bookingId}/appointments")
    @HasWriteScope
    @ProxyUser
    public ScheduledEvent postBookingsBookingIdAppointments(
            @PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId,
            @RequestBody @ApiParam(required = true) final NewAppointment newAppointment) {
        return appointmentsService.createBookingAppointment(bookingId, authenticationFacade.getCurrentUsername(), newAppointment);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Court Cases", notes = "Court Cases", nickname = "getCourtCases")
    @GetMapping("/{bookingId}/court-cases")
    public List<CourtCase> getCourtCases(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId, @RequestParam(value = "activeOnly", required = false, defaultValue = "true") @ApiParam("Only return active court cases") final boolean activeOnly) {
        return bookingService.getOffenderCourtCases(bookingId, activeOnly);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Get secondary languages", notes = "Get secondary languages", nickname = "getSecondaryLanguages")
    @GetMapping("/{bookingId}/secondary-languages")
    public List<SecondaryLanguage> getSecondaryLanguages(@PathVariable("bookingId") @ApiParam(value = "bookingId", required = true) final Long bookingId) {
        return inmateService.getSecondaryLanguages(bookingId);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Gets the offender non-association details for a given booking", notes = "Get offender non-association details", nickname = "getNonAssociationDetails")
    @GetMapping("/{bookingId}/non-association-details")
    public OffenderNonAssociationDetails getNonAssociationDetails(@PathVariable("bookingId") @ApiParam(value = "The offender booking id", required = true) final Long bookingId) {
        return offenderNonAssociationsService.retrieve(bookingId);
    }

    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    @ApiOperation(value = "Gets cell history for an offender booking", notes = "Default sort order is by assignment date descending", nickname = "getBedAssignmentsHistory")
    @GetMapping("/{bookingId}/cell-history")
    public Page<BedAssignment> getBedAssignmentsHistory(@PathVariable("bookingId") @ApiParam(value = "The offender booking linked to the court hearings.", required = true) final Long bookingId,
                                                        @RequestParam(value = "page", required = false) @ApiParam(value = "The page number to return. Index starts at 0", defaultValue = "0") final Integer page,
                                                        @RequestParam(value = "size", required = false) @ApiParam(value = "The number of results per page. Defaults to 20.", defaultValue = "20") final Integer size) {
        final var pageIndex = page != null ? page : 0;
        final var pageSize = size != null ? size : 20;
        return bedAssignmentHistoryService.getBedAssignmentsHistory(bookingId, PageRequest.of(pageIndex, pageSize, Sort.by("assignmentDate").descending()));
    }
}
