package uk.gov.justice.hmpps.prison.api.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
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
import uk.gov.justice.hmpps.prison.api.model.Account;
import uk.gov.justice.hmpps.prison.api.model.Alert;
import uk.gov.justice.hmpps.prison.api.model.AlertChanges;
import uk.gov.justice.hmpps.prison.api.model.AlertCreated;
import uk.gov.justice.hmpps.prison.api.model.Alias;
import uk.gov.justice.hmpps.prison.api.model.Assessment;
import uk.gov.justice.hmpps.prison.api.model.BedAssignment;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteCount;
import uk.gov.justice.hmpps.prison.api.model.ContactDetail;
import uk.gov.justice.hmpps.prison.api.model.CourtCase;
import uk.gov.justice.hmpps.prison.api.model.CourtEventOutcome;
import uk.gov.justice.hmpps.prison.api.model.CreateAlert;
import uk.gov.justice.hmpps.prison.api.model.CreatePersonalCareNeed;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.FixedTermRecallDetails;
import uk.gov.justice.hmpps.prison.api.model.InmateBasicDetails;
import uk.gov.justice.hmpps.prison.api.model.InmateDetail;
import uk.gov.justice.hmpps.prison.api.model.Keyworker;
import uk.gov.justice.hmpps.prison.api.model.MilitaryRecords;
import uk.gov.justice.hmpps.prison.api.model.NewAppointment;
import uk.gov.justice.hmpps.prison.api.model.OffenceDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenceHistoryDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenderIdentifier;
import uk.gov.justice.hmpps.prison.api.model.PersonalCareCounterDto;
import uk.gov.justice.hmpps.prison.api.model.PersonalCareNeeds;
import uk.gov.justice.hmpps.prison.api.model.PrisonDetails;
import uk.gov.justice.hmpps.prison.api.model.PrisonerBookingSummary;
import uk.gov.justice.hmpps.prison.api.model.PropertyContainer;
import uk.gov.justice.hmpps.prison.api.model.ReasonableAdjustments;
import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent;
import uk.gov.justice.hmpps.prison.api.model.SecondaryLanguage;
import uk.gov.justice.hmpps.prison.api.model.SentenceAdjustmentDetail;
import uk.gov.justice.hmpps.prison.api.model.SentenceCalcDates;
import uk.gov.justice.hmpps.prison.api.model.UpdateAttendance;
import uk.gov.justice.hmpps.prison.api.model.UpdateAttendanceBatch;
import uk.gov.justice.hmpps.prison.api.model.VisitBalances;
import uk.gov.justice.hmpps.prison.api.model.VisitDetails;
import uk.gov.justice.hmpps.prison.api.model.VisitSummary;
import uk.gov.justice.hmpps.prison.api.model.VisitWithVisitors;
import uk.gov.justice.hmpps.prison.api.model.adjudications.AdjudicationSummary;
import uk.gov.justice.hmpps.prison.api.model.calculation.CalculableSentenceEnvelope;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.core.HasWriteScope;
import uk.gov.justice.hmpps.prison.core.ProgrammaticAuthorisation;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.core.SlowReportQuery;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.VisitInformationFilter;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.security.VerifyAgencyAccess;
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
import uk.gov.justice.hmpps.prison.service.HealthService;
import uk.gov.justice.hmpps.prison.service.ImageService;
import uk.gov.justice.hmpps.prison.service.InmateAlertService;
import uk.gov.justice.hmpps.prison.service.InmateService;
import uk.gov.justice.hmpps.prison.service.NoContentException;
import uk.gov.justice.hmpps.prison.service.OffenderFixedTermRecallService;
import uk.gov.justice.hmpps.prison.service.keyworker.KeyWorkerAllocationService;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uk.gov.justice.hmpps.prison.util.ResourceUtils.nvl;

/**
 * Implementation of Booking (/bookings) endpoint.
 */
@RestController
@Tag(name = "bookings")
@RequestMapping(value = "${api.base.path}/bookings", produces = "application/json")
@Validated
@AllArgsConstructor
@Slf4j
public class BookingResource {
    private final AuthenticationFacade authenticationFacade;
    private final BookingService bookingService;
    private final InmateService inmateService;
    private final HealthService healthService;
    private final CaseNoteService caseNoteService;
    private final InmateAlertService inmateAlertService;
    private final BedAssignmentHistoryService bedAssignmentHistoryService;
    private final FinanceService financeService;
    private final ContactService contactService;
    private final AdjudicationService adjudicationService;
    private final ImageService imageService;
    private final KeyWorkerAllocationService keyworkerService;
    private final AppointmentsService appointmentsService;
    private final OffenderFixedTermRecallService fixedTermRecallService;

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Prisoners Booking Summary ", description = "Returns data that is available to the users caseload privileges, at least one attribute of a prisonId, bookingId or offenderNo must be specified")
    @GetMapping("/v2")
    @VerifyBookingAccess(overrideRoles = {"VIEW_PRISONER_DATA"})
    public Page<PrisonerBookingSummary> getPrisonerBookingsV2(
        @RequestParam(value = "prisonId", required = false) @Parameter(description = "Filter by prison Id", example = "MDI") final String prisonId,
        @RequestParam(value = "bookingId", required = false) @Parameter(description = "Filter by a list of booking ids") final List<Long> bookingIds,
        @RequestParam(value = "offenderNo", required = false) @Parameter(description = "Filter by a list of offender numbers") final List<String> offenderNos,
        @RequestParam(value = "legalInfo", defaultValue = "false", required = false) @Parameter(description = "Return additional legal information (imprisonmentStatus, legalStatus, convictedStatus)") final boolean legalInfo,
        @RequestParam(value = "image", defaultValue = "false", required = false) @Parameter(description = "Return facial ID for latest prisoner image") final boolean imageId,
        @ParameterObject @PageableDefault(sort = {"lastName", "firstName", "offenderNo"}, direction = Direction.ASC) final Pageable pageable) {

        return bookingService.getPrisonerBookingSummary(prisonId, bookingIds, offenderNos, legalInfo, imageId, pageable);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Offender detail.", description = "Offender detail.")
    @GetMapping("/{bookingId}")
    @VerifyBookingAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public InmateDetail getOffenderBooking(@PathVariable("bookingId") @Parameter(description = "The booking id of offender", required = true) final Long bookingId, @RequestParam(value = "basicInfo", required = false, defaultValue = "false") @Parameter(description = "If set to true then only basic data is returned") final boolean basicInfo, @RequestParam(value = "extraInfo", required = false, defaultValue = "false") @Parameter(description = "Only used when requesting more than basic data, returns identifiers,offences,aliases,sentence dates,convicted status") final boolean extraInfo) {

        return basicInfo && !extraInfo ?
            inmateService.getBasicInmateDetail(bookingId)
            : inmateService.findInmate(bookingId, extraInfo, false);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Offender detail.", description = "Offender detail.")
    @GetMapping("/offenderNo/{offenderNo}")
    @VerifyOffenderAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public InmateDetail getOffenderBookingByOffenderNo(@PathVariable("offenderNo") @Parameter(description = "The offenderNo of offender", required = true) final String offenderNo, @RequestParam(value = "fullInfo", required = false, defaultValue = "false") @Parameter(description = "If set to true then full data is returned") final boolean fullInfo, @RequestParam(value = "extraInfo", required = false, defaultValue = "false") @Parameter(description = "Only used when fullInfo=true, returns identifiers,offences,aliases,sentence dates,convicted status") final boolean extraInfo, @RequestParam(value = "csraSummary", required = false, defaultValue = "false") @Parameter(description = "Only used when fullInfo=true, returns the applicable CSRA classification for this offender") final boolean csraSummary) {
        return fullInfo || extraInfo ?
            inmateService.findOffender(offenderNo, extraInfo, csraSummary) :
            inmateService.getBasicInmateDetail(bookingService.getLatestBookingByOffenderNo(offenderNo).getBookingId());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Offender detail.", description = "Requires role VIEW_PRISONER_DATA or GLOBAL_SEARCH, or prisoners are in caseload")
    @ProgrammaticAuthorisation("Access based on VIEW_PRISONER_DATA or GLOBAL_SEARCH, or prisoners are in caseload")
    @PostMapping("/offenders")
    @SlowReportQuery
    public List<InmateBasicDetails> getBasicInmateDetailsForOffenders(@RequestBody @Parameter(description = "The offenderNo of offender", required = true) final Set<String> offenders, @RequestParam(value = "activeOnly", required = false, defaultValue = "true") @Parameter(description = "Returns only Offender details with an active booking if true, otherwise Offenders without an active booking are included") final Boolean activeOnly) {
        final var active = activeOnly == null || activeOnly;
        return inmateService.getBasicInmateDetailsForOffenders(offenders, active);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "All scheduled activities for offender.", description = "Requires booking id to be in caseload, or role VIEW_SCHEDULES")
    @VerifyBookingAccess(overrideRoles = {"VIEW_SCHEDULES"})
    @GetMapping("/{bookingId}/activities")
    @SlowReportQuery
    public ResponseEntity<List<ScheduledEvent>> getBookingActivities(@PathVariable("bookingId") @Parameter(description = "The offender booking id", required = true) final Long bookingId, @RequestParam(value = "fromDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Returned activities must be scheduled on or after this date (in YYYY-MM-DD format).") final LocalDate fromDate, @RequestParam(value = "toDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Returned activities must be scheduled on or before this date (in YYYY-MM-DD format).") final LocalDate toDate, @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @Parameter(description = "Requested offset of first record in returned collection of activity records.") final Long pageOffset, @RequestHeader(value = "Page-Limit", defaultValue = "10", required = false) @Parameter(description = "Requested limit to number of activity records returned.") final Long pageLimit, @RequestHeader(value = "Sort-Fields", required = false) @Parameter(description = "Comma separated list of one or more of the following fields - <b>eventDate, startTime, endTime, eventLocation</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @Parameter(description = "Sort order (ASC or DESC) - defaults to ASC.") final Order sortOrder) {
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
        @ApiResponse(responseCode = "201", description = "Attendance data has been updated"),
        @ApiResponse(responseCode = "400", description = "Invalid request - e.g. validation error.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "403", description = "Forbidden - user not authorised to attend activity.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Resource not found - booking or event does not exist or is not accessible to user.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Internal server error.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Update offender attendance and pay.", description = "Requires role PAY")
    @PreAuthorize("hasRole('PAY')")
    @PutMapping("/offenderNo/{offenderNo}/activities/{activityId}/attendance")
    @ProxyUser
    public ResponseEntity<Void> updateAttendance(@PathVariable("offenderNo") @Parameter(description = "The offenderNo of the prisoner", required = true, example = "A1234AA") final String offenderNo,
                                                 @PathVariable("activityId") @Parameter(description = "The activity id", required = true, example = "1212131") final Long activityId,
                                                 @RequestBody @Parameter(required = true, example = "{eventOutcome = 'ATT', performance = 'ACCEPT' outcomeComment = 'Turned up very late'}") @NotNull final UpdateAttendance updateAttendance) {
        bookingService.updateAttendance(offenderNo, activityId, updateAttendance);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Attendance data has been updated"),
        @ApiResponse(responseCode = "400", description = "Invalid request - e.g. validation error.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "403", description = "Forbidden - user not authorised to attend activity.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Resource not found - booking or event does not exist or is not accessible to user.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "423", description = "Record in use for this booking id (possibly in P-Nomis).", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Internal server error.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Update offender attendance and pay.", description = "Requires role PAY")
    @PreAuthorize("hasRole('PAY')")
    @PutMapping("/{bookingId}/activities/{activityId}/attendance")
    @ProxyUser
    public ResponseEntity<Void> updateAttendance(
        @NotNull @PathVariable("bookingId") @Parameter(description = "The booking Id of the prisoner", required = true, example = "213531") final Long bookingId,
        @NotNull @PathVariable("activityId") @Parameter(description = "The activity id", required = true, example = "1212131") final Long activityId,
        @RequestParam(value = "lockTimeout", required = false, defaultValue = "false") @Parameter(description = "Whether to timeout if locked", example = "true") final Boolean lockTimeout,
        @RequestBody @NotNull @Parameter(required = true) final UpdateAttendance updateAttendance
    ) {
        bookingService.updateAttendance(bookingId, activityId, updateAttendance, lockTimeout);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Attendance data has been updated"),
        @ApiResponse(responseCode = "400", description = "Invalid request - e.g. validation error.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "403", description = "Forbidden - user not authorised to attend activity.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Resource not found - booking or event does not exist or is not accessible to user.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Internal server error.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Update attendance and pay for multiple bookings.", description = "Requires role PAY")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PAY')")
    @PutMapping("/activities/attendance")
    public ResponseEntity<Void> updateAttendanceForMultipleBookingIds(@RequestBody @Parameter(required = true) final @NotNull UpdateAttendanceBatch body) {
        bookingService.updateAttendanceForMultipleBookingIds(body.getBookingActivities(), UpdateAttendance
            .builder()
            .eventOutcome(body.getEventOutcome())
            .performance(body.getPerformance())
            .outcomeComment(body.getOutcomeComment())
            .build());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Alert id.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AlertCreated.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Operation(summary = "Create an alert", description = "Requires role UPDATE_ALERT")
    @PreAuthorize("hasAnyRole('UPDATE_ALERT')")
    @PostMapping("/{bookingId}/alert")
    @ProxyUser
    public ResponseEntity<AlertCreated> postAlert(@PathVariable("bookingId") @Parameter(description = "bookingId", required = true) final Long bookingId, @Valid @RequestBody @Parameter(description = "Alert details", required = true) final CreateAlert alert) {
        final var alertId = inmateAlertService.createNewAlert(bookingId, alert);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AlertCreated(alertId));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "423", description = "Record in use for this booking id (possibly in P-Nomis).", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Operation(summary = "Update an alert", description = "Requires role UPDATE_ALERT")
    @PreAuthorize("hasAnyRole('UPDATE_ALERT')")
    @PutMapping("/{bookingId}/alert/{alertSeq}")
    @ProxyUser
    public Alert updateAlert(
        @PathVariable("bookingId") @Parameter(description = "bookingId", required = true) final Long bookingId,
        @PathVariable("alertSeq") @Parameter(description = "alertSeq", required = true) final Long alertSeq,
        @RequestParam(value = "lockTimeout", required = false, defaultValue = "false") @Parameter(description = "Whether to timeout if locked", example = "true") final Boolean lockTimeout,
        @Valid @RequestBody @Parameter(description = "Alert details", required = true) final AlertChanges alert
    ) {
        return inmateAlertService.updateAlert(bookingId, alertSeq, alert, lockTimeout);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Offender alerts.", description = "Offender alerts.")
    @GetMapping("/{bookingId}/alerts/v2")
    @VerifyBookingAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public Page<Alert> getOffenderAlertsV2(
        @PathVariable("bookingId") @Parameter(description = "The booking id for the booking", required = true) final Long bookingId,
        @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "start alert date to search from", example = "2021-02-03") final LocalDate from,
        @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "end alert date to search up to (including this date)", example = "2021-02-04") final LocalDate to,
        @RequestParam(value = "alertType", required = false) @Parameter(description = "Filter by alert type", example = "X") final String alertType,
        @RequestParam(value = "alertStatus", required = false) @Parameter(description = "Filter by alert active status", example = "ACTIVE") final String alertStatus,
        @ParameterObject @PageableDefault(sort = {"dateExpires", "dateCreated"}, direction = Sort.Direction.DESC) final Pageable pageable) {

        return inmateAlertService.getAlertsForBooking(bookingId, from, to, alertType, alertStatus, pageable);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Offender alert detail.", description = "Offender alert detail.")
    @GetMapping("/{bookingId}/alerts/{alertId}")
    @VerifyBookingAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public Alert getOffenderAlert(@PathVariable("bookingId") @Parameter(description = "The booking id of offender", required = true) final Long bookingId, @PathVariable("alertId") @Parameter(description = "The Alert Id", required = true) final Long alertId) {
        return inmateAlertService.getInmateAlert(bookingId, alertId);
    }

    @Operation(summary = "Get alerts for a list of offenders at a prison")
    @PostMapping("/offenderNo/{agencyId}/alerts")
    @SlowReportQuery
    @VerifyAgencyAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public List<Alert> getAlertsByOffenderNosAtAgency(@PathVariable("agencyId") @Parameter(description = "The prison where the offenders are booked", required = true) final String agencyId, @RequestBody @Parameter(description = "The required offender numbers (mandatory)", required = true) final List<String> offenderNos) {
        return inmateAlertService.getInmateAlertsByOffenderNosAtAgency(agencyId, offenderNos);
    }

    @Operation(summary = "Get alerts for a list of offenders. Requires VIEW_PRISONER_DATA role")
    @PostMapping("/offenderNo/alerts")
    @SlowReportQuery
    @PreAuthorize("hasAnyRole('GLOBAL_SEARCH', 'VIEW_PRISONER_DATA','CREATE_CATEGORISATION','APPROVE_CATEGORISATION')")
    public List<Alert> getAlertsByOffenderNos(@RequestBody @NotEmpty(message = "A minimum of one offender number is required") @Parameter(description = "The required offender numbers (mandatory)", required = true) final List<String> offenderNos) {
        return inmateAlertService.getInmateAlertsByOffenderNos(offenderNos, true, "bookingId,alertId", Order.ASC);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Offender aliases.", description = "Offender aliases.")
    @VerifyBookingAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    @GetMapping("/{bookingId}/aliases")
    public ResponseEntity<List<Alias>> getOffenderAliases(@PathVariable("bookingId") @Parameter(description = "The booking id of offender", required = true) final Long bookingId, @RequestHeader(value = "Page-Offset", defaultValue = "0", required = false) @Parameter(description = "Requested offset of first record in returned collection of alias records.") final Long pageOffset, @RequestHeader(value = "Page-Limit", defaultValue = "5", required = false) @Parameter(description = "Requested limit to number of alias records returned.") final Long pageLimit, @RequestHeader(value = "Sort-Fields", required = false) @Parameter(description = "Comma separated list of one or more of the following fields - <b>firstName, lastName, age, dob, middleName, nameType, createDate</b>") final String sortFields, @RequestHeader(value = "Sort-Order", defaultValue = "ASC", required = false) @Parameter(description = "Sort order (ASC or DESC) - defaults to ASC.") final Order sortOrder) {
        return inmateService.findInmateAliases(
            bookingId,
            sortFields,
            sortOrder,
            nvl(pageOffset, 0L),
            nvl(pageLimit, 5L)).getResponse();
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Assessment Information", description = "Assessment Information. Requires booking access (via caseload) or VIEW_ASSESSMENTS role.")
    @GetMapping("/{bookingId}/assessments")
    @VerifyBookingAccess(overrideRoles = {"VIEW_ASSESSMENTS"})
    public List<Assessment> getAssessments(@PathVariable("bookingId") @Parameter(description = "The offender booking id", required = true) final Long bookingId) {
        return inmateService.getAssessments(bookingId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Requested resource not found."),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.")})
    @Operation(summary = "Image data (as bytes).", description = "Image data (as bytes).")
    @GetMapping(value = "/offenderNo/{offenderNo}/image/data", produces = "image/jpeg")
    @VerifyOffenderAccess(accessDeniedError = true, overrideRoles = {"VIEW_PRISONER_DATA"})
    public ResponseEntity<byte[]> getMainBookingImageDataByNo(@PathVariable("offenderNo") @Parameter(description = "The offender No of offender", required = true) final String offenderNo, @RequestParam(value = "fullSizeImage", defaultValue = "false", required = false) @Parameter(description = "Return full size image") final boolean fullSizeImage) {
        return imageService.getImageContent(offenderNo, fullSizeImage)
            .map(bytes -> new ResponseEntity<>(bytes, HttpStatus.OK))
            .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Requested resource not found."),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.")})
    @Operation(summary = "Image data (as bytes).", description = "Image data (as bytes).")
    @GetMapping(value = "/{bookingId}/image/data", produces = "image/jpeg")
    @SlowReportQuery
    @VerifyBookingAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public ResponseEntity<byte[]> getMainBookingImageData(@PathVariable("bookingId") @Parameter(description = "The booking id of offender", required = true) final Long bookingId, @RequestParam(value = "fullSizeImage", defaultValue = "false", required = false) @Parameter(description = "Return full size image") final boolean fullSizeImage) {
        final var mainBookingImage = inmateService.getMainBookingImage(bookingId);
        return imageService.getImageContent(mainBookingImage.getImageId(), fullSizeImage)
            .map(bytes -> new ResponseEntity<>(bytes, HttpStatus.OK))
            .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Offender sentence detail (key dates and additional days awarded)", description = """
        <h3>Algorithm</h3>
        <ul>
          <li>If there is a confirmed release date, the offender release date is the confirmed release date.</li>
          <li>If there is no confirmed release date for the offender, the offender release date is either the actual parole date or the home detention curfew actual date.</li>
          <li>If there is no confirmed release date, actual parole date or home detention curfew actual date for the offender, the release date is the later of the nonDtoReleaseDate or midTermDate value (if either or both are present)</li>
        </ul>
        Requires booking to be in caseload, or role GLOBAL_SEARCH or VIEW_PRISONER_DATA
        """)
    @ProgrammaticAuthorisation("Access check at service level")
    @GetMapping("/{bookingId}/sentenceDetail")
    @SlowReportQuery
    public SentenceCalcDates getBookingSentenceDetail(
        @RequestHeader(value = "version", defaultValue = "1.0", required = false) @Parameter(description = "Version of Sentence Calc Dates, 1.0 is default") final String version,
        @PathVariable("bookingId") @Parameter(description = "The booking id of offender", required = true) final Long bookingId) {
        if ("1.1".equals(version)) {
            return bookingService.getBookingSentenceCalcDatesV1_1(bookingId);
        }
        return bookingService.getBookingSentenceCalcDates(bookingId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Offender sentence adjustments.", description = "Requires booking to be in caseload, or role GLOBAL_SEARCH or VIEW_PRISONER_DATA")
    @VerifyBookingAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    @GetMapping("/{bookingId}/sentenceAdjustments")
    public SentenceAdjustmentDetail getBookingSentenceAdjustments(@PathVariable("bookingId") @Parameter(description = "The booking id of offender", required = true) final Long bookingId) {
        return bookingService.getBookingSentenceAdjustments(bookingId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Offender account balances.", description = "Requires booking to be in caseload, or role GLOBAL_SEARCH or VIEW_PRISONER_DATA")
    @VerifyBookingAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    @GetMapping("/{bookingId}/balances")
    public Account getBalances(@PathVariable("bookingId") @Parameter(description = "The offender booking id", required = true) final Long bookingId) {
        return financeService.getBalances(bookingId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "List of active property containers", description = "Requires booking to be in caseload, or role VIEW_PRISONER_DATA")
    @VerifyBookingAccess(overrideRoles = {"VIEW_PRISONER_DATA"})
    @GetMapping("/{bookingId}/property")
    public List<PropertyContainer> getOffenderPropertyContainers(@PathVariable("bookingId") @Parameter(description = "The offender booking id", required = true) final Long bookingId) {
        return bookingService.getOffenderPropertyContainers(bookingId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Get Offender main offence detail.", description = "Requires booking to be in caseload, or role GLOBAL_SEARCH or VIEW_PRISONER_DATA")
    @VerifyBookingAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    @GetMapping("/{bookingId}/mainOffence")
    public List<OffenceDetail> getMainOffence(@PathVariable("bookingId") @Parameter(description = "The offender booking id", required = true) final Long bookingId) {
        return bookingService.getMainOffenceDetails(bookingId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Get Offender main offence detail.", description = "Post version to allow specifying a large number of bookingIds. Requires role VIEW_PRISONER_DATA")
    @PreAuthorize("hasRole('VIEW_PRISONER_DATA')")
    @PostMapping("/mainOffence")
    @SlowReportQuery
    public List<OffenceDetail> getMainOffence(@RequestBody @Parameter(description = "The bookingIds to identify the offenders", required = true) final Set<Long> bookingIds) {
        return bookingService.getMainOffenceDetails(bookingIds);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Offence history.", description = "All Offences recorded for this offender.")
    @GetMapping("/offenderNo/{offenderNo}/offenceHistory")
    @PreAuthorize("hasAnyRole('VIEW_PRISONER_DATA','CREATE_CATEGORISATION','APPROVE_CATEGORISATION')")
    @SlowReportQuery
    public List<OffenceHistoryDetail> getOffenceHistory(@PathVariable("offenderNo") @Parameter(description = "The offender number", required = true) final String offenderNo, @RequestParam(value = "convictionsOnly", required = false, defaultValue = "true") @Parameter(description = "include offences with convictions only") final boolean convictionsOnly) {
        return bookingService.getOffenceHistory(offenderNo, convictionsOnly);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Offence histories.", description = "Offence histories for a set of booking ids.")
    @PostMapping("/offence-history")
    @PreAuthorize("hasRole('VIEW_PRISONER_DATA')")
    @SlowReportQuery
    public List<OffenceHistoryDetail> getOffenceHistoryForBookings(@RequestBody @Parameter(description = "The booking ids", required = true) final Set<Long> bookingIds) {
        return bookingService.getActiveOffencesForBookings(bookingIds);
    }


    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Personal Care Needs", description = "Personal Care Need. Requires booking access (via caseload) or GLOBAL_SEARCH or VIEW_PRISONER_DATA role.")
    @GetMapping("/{bookingId}/personal-care-needs")
    @VerifyBookingAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public PersonalCareNeeds getPersonalCareNeeds(
        @PathVariable("bookingId") @Parameter(description = "The offender booking id", required = true) final Long bookingId,
        @RequestParam(value = "type", required = false) @NotEmpty(message = "type: must not be empty") @Parameter(description = "a list of types and optionally subtypes (joined with +) to search.", example = "DISAB+RM", required = true) final List<String> problemTypes
    ) {
        return healthService.getPersonalCareNeeds(bookingId, problemTypes);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Personal Care Needs  - POST version to allow for large numbers of offenders", description = "Requires role VIEW_PRISONER_DATA or GLOBAL_SEARCH")
    @PreAuthorize("hasAnyRole('GLOBAL_SEARCH', 'VIEW_PRISONER_DATA')")
    @PostMapping("/offenderNo/personal-care-needs")
    public List<PersonalCareNeeds> getPersonalCareNeeds(
        @RequestBody @NotEmpty(message = "offenderNo: must not be empty") @Parameter(description = "The required offender numbers (mandatory)", required = true) final List<String> offenderNos,
        @RequestParam(value = "type", required = false) @NotEmpty(message = "type: must not be empty") @Parameter(description = "a list of types and optionally subtypes (joined with +) to search.", example = "DISAB+RM", required = true) final List<String> problemTypes
    ) {
        return healthService.getPersonalCareNeeds(offenderNos, problemTypes);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Personal Care Needs Counter - POST version to allow to count heath problem by type for large numbers of offenders", description = "Requires booking to be in caseload, or role GLOBAL_SEARCH or VIEW_PRISONER_DATA")
    @VerifyBookingAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    @PostMapping("/offenderNo/personal-care-needs/count")
    public List<PersonalCareCounterDto> countPersonalCareNeeds(@RequestBody @NotEmpty(message = "offenderNo: must not be empty") @Parameter(description = "The required offender numbers (mandatory)", required = true) final List<String> offenderNos,
                                                               @RequestParam(value = "type") @NotEmpty(message = "type: must not be empty") @Parameter(description = "problem type", example = "DISAB", required = true) final String problemType,
                                                               @RequestParam(value = "fromStartDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Personal needs care must be on or after this date (in YYYY-MM-DD format).") final LocalDate fromStartDate,
                                                               @RequestParam(value = "toStartDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Personal needs care must be on or before this date (in YYYY-MM-DD format).") final LocalDate toStartDate) {
        return healthService.countPersonalCareNeedsByOffenderNoAndProblemTypeBetweenDates(offenderNos, problemType, fromStartDate, toStartDate);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "If successful the Personal Care Need is returned."),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Personal Care Needs", description = "Requires role MAINTAIN_HEALTH_PROBLEMS and scope write")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{bookingId}/personal-care-needs")
    @ProxyUser
    @PreAuthorize("hasRole('MAINTAIN_HEALTH_PROBLEMS') and hasAuthority('SCOPE_write')")
    public void addPersonalCareNeed(
        @PathVariable("bookingId") @Parameter(description = "The offender booking id", required = true) final Long bookingId,
        @Valid @NotNull @RequestBody final CreatePersonalCareNeed createPersonalCareNeed) {
        healthService.addPersonalCareNeed(bookingId, createPersonalCareNeed);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Military Records", description = "Requires booking to be in caseload, or role VIEW_PRISONER_DATA")
    @ProgrammaticAuthorisation("Checked in service for legacy reasons")
    @GetMapping("/{bookingId}/military-records")
    public MilitaryRecords getMilitaryRecords(@PathVariable("bookingId") @Parameter(description = "The offender booking id", required = true) final Long bookingId) {
        return bookingService.getMilitaryRecords(bookingId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Reasonable Adjustment Information", description = "Reasonable Adjustment Information. Requires booking access (via caseload) or GLOBAL_SEARCH or VIEW_PRISONER_DATA role.")
    @GetMapping("/{bookingId}/reasonable-adjustments")
    @VerifyBookingAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public ReasonableAdjustments getReasonableAdjustments(@PathVariable("bookingId") @Parameter(description = "The offender booking id", required = true) final Long bookingId, @RequestParam(value = "type", required = false) @NotEmpty(message = "treatmentCodes: must not be empty") @Parameter(description = "a list of treatment codes to search.", example = "PEEP", required = true) final List<String> treatmentCodes) {
        return inmateService.getReasonableAdjustments(bookingId, treatmentCodes);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Today's scheduled events for offender.", description = "Requires role VIEW_PRISONER_DATA or GLOBAL_SEARCH, or booking is in caseload")
    @VerifyBookingAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    @GetMapping("/{bookingId}/events/today")
    public List<ScheduledEvent> getEventsToday(@PathVariable("bookingId") @Parameter(description = "The offender booking id", required = true) final Long bookingId) {
        return bookingService.getEventsToday(bookingId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Identifiers for this booking", description = "Identifiers for this booking")
    @GetMapping("/{bookingId}/identifiers")
    @VerifyBookingAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public List<OffenderIdentifier> getOffenderIdentifiers(@PathVariable("bookingId") @Parameter(description = "The offender booking id", required = true) @NotNull final Long bookingId, @RequestParam(value = "type", required = false) @Parameter(description = "Filter By Type", example = "PNC") final String identifierType) {
        return inmateService.getOffenderIdentifiers(bookingId, identifierType);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "All scheduled events for offender.", description = "Requires role VIEW_PRISONER_DATA or GLOBAL_SEARCH, or booking is in caseload")
    @VerifyBookingAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    @GetMapping("/{bookingId}/events")
    public List<ScheduledEvent> getEvents(@PathVariable("bookingId") @Parameter(description = "The offender booking id", required = true) final Long bookingId, @RequestParam(value = "fromDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Returned events must be scheduled on or after this date (in YYYY-MM-DD format).") final LocalDate fromDate, @RequestParam(value = "toDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Returned events must be scheduled on or before this date (in YYYY-MM-DD format).") final LocalDate toDate) {
        return bookingService.getEvents(bookingId, fromDate, toDate);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Scheduled events for offender for coming week (from current day).", description = "Requires role VIEW_PRISONER_DATA or GLOBAL_SEARCH, or booking is in caseload")
    @VerifyBookingAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    @GetMapping("/{bookingId}/events/thisWeek")
    public List<ScheduledEvent> getEventsThisWeek(@PathVariable("bookingId") @Parameter(description = "The offender booking id", required = true) final Long bookingId) {
        return bookingService.getEventsThisWeek(bookingId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Scheduled events for offender for following week.", description = "Requires role VIEW_PRISONER_DATA or GLOBAL_SEARCH, or booking is in caseload")
    @VerifyBookingAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    @GetMapping("/{bookingId}/events/nextWeek")
    public List<ScheduledEvent> getEventsNextWeek(@PathVariable("bookingId") @Parameter(description = "The offender booking id", required = true) final Long bookingId) {
        return bookingService.getEventsNextWeek(bookingId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Offender contacts (e.g. next of kin).", description = "Requires role VIEW_PRISONER_DATA or GLOBAL_SEARCH, or booking is in caseload")
    @VerifyBookingAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    @GetMapping("/{bookingId}/contacts")
    public ContactDetail getContacts(@PathVariable("bookingId") @Parameter(description = "The offender booking id", required = true) final Long bookingId) {
        return contactService.getContacts(bookingId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Count of case notes", description = "Count of case notes")
    @GetMapping("/{bookingId}/caseNotes/{type}/{subType}/count")
    @SlowReportQuery
    @VerifyBookingAccess(overrideRoles = {"VIEW_CASE_NOTES"})
    public CaseNoteCount getCaseNoteCount(@PathVariable("bookingId") @Parameter(description = "The offender booking id", required = true) final Long bookingId, @PathVariable("type") @Parameter(description = "Case note type.", required = true) final String type, @PathVariable("subType") @Parameter(description = "Case note sub-type.", required = true) final String subType, @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Only case notes occurring on or after this date (in YYYY-MM-DD format) will be considered.") final LocalDate fromDate, @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Only case notes occurring on or before this date (in YYYY-MM-DD format) will be considered.") final LocalDate toDate) {
        return caseNoteService.getCaseNoteCount(
            bookingId,
            type,
            subType,
            fromDate,
            toDate);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Offender adjudications summary (awards and sanctions).",
        description = "Deprecated - use Adjudications API to get adjudications, requires VIEW_ADJUDICATIONS",
        deprecated = true, hidden = true)
    @GetMapping("/{bookingId}/adjudications")
    @VerifyBookingAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA", "VIEW_ADJUDICATIONS"})
    public AdjudicationSummary getAdjudicationSummary(@PathVariable("bookingId") @Parameter(description = "The offender booking id", required = true) final Long bookingId, @RequestParam(value = "awardCutoffDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Only awards ending on or after this date (in YYYY-MM-DD format) will be considered.") final LocalDate awardCutoffDate, @RequestParam(value = "adjudicationCutoffDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Only proved adjudications ending on or after this date (in YYYY-MM-DD format) will be counted.") final LocalDate adjudicationCutoffDate) {
        return adjudicationService.getAdjudicationSummary(bookingId,
            awardCutoffDate, adjudicationCutoffDate);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "visits with visitor list for offender.", description = "visits with visitor list for offender.")
    @GetMapping("/{bookingId}/visits-with-visitors")
    @SlowReportQuery
    @VerifyBookingAccess
    public Page<VisitWithVisitors> getBookingVisitsWithVisitor(
        @PathVariable("bookingId") @Parameter(description = "The offender booking id", required = true) final Long bookingId,
        @RequestParam(value = "fromDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Returned visits must be scheduled on or after this date (in YYYY-MM-DD format).") final LocalDate fromDate,
        @RequestParam(value = "toDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Returned visits must be scheduled on or before this date (in YYYY-MM-DD format).") final LocalDate toDate,
        @RequestParam(value = "visitType", required = false) @Parameter(description = "Type of visit", schema = @Schema(implementation = String.class, allowableValues = {"SCON", "OFFI"})) final String visitType,
        @RequestParam(value = "visitStatus", required = false) @Parameter(name = "Status of visit. code from VIS_COMPLETE domain, e.g: CANC (Cancelled) or SCH (Scheduled)", example = "SCH") final String visitStatus,
        @RequestParam(value = "cancellationReason", required = false) @Parameter(name = "Reason for cancellation. code from MOVE_CANC_RS domain, e.g: VISCANC (Visitor Cancelled) or NO_VO (No Visiting Order)", example = "NSHOW") final String cancellationReason,
        @RequestParam(value = "prisonId", required = false) @Parameter(description = "The prison id", example = "MDI") final String prisonId,
        @RequestParam(value = "page", required = false, defaultValue = "0") @Parameter(description = "Target page number, zero being the first page") final Integer pageIndex,
        @RequestParam(value = "size", required = false, defaultValue = "20") @Parameter(description = "The number of results per page") final Integer pageSize) {
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
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Balances visit orders and privilege visit orders for offender.", description = "Balances visit orders and privilege visit orders for offender.")
    @GetMapping("/offenderNo/{offenderNo}/visit/balances")
    @VerifyOffenderAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA", "VISIT_SCHEDULER"})
    @SlowReportQuery
    public VisitBalances getBookingVisitBalances(
        @PathVariable("offenderNo") @Parameter(description = "The offenderNo of offender", required = true) final String offenderNo,
        @RequestParam(value = "allowNoContent", required = false) @Parameter(description = "Allow no content (204) response if no data rather than returning a not found (404)") final boolean allowNoContent
    ) {
        final var identifiers = bookingService.getOffenderIdentifiers(offenderNo, false, "GLOBAL_SEARCH", "VIEW_PRISONER_DATA", "VISIT_SCHEDULER").getBookingAndSeq().orElseThrow(EntityNotFoundException.withMessage("No bookings found for offender {}", offenderNo));
        final var bookingId = identifiers.getBookingId();
        return bookingService.getBookingVisitBalances(bookingId).orElseThrow(
            allowNoContent ? NoContentException.withId(bookingId) : EntityNotFoundException.withId(bookingId));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Key worker details.", description = "Key worker details. This should not be used - call keyworker API instead")
    @GetMapping("/offenderNo/{offenderNo}/key-worker")
    @Deprecated
    @VerifyOffenderAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA", "KEY_WORKER"})
    public Keyworker getKeyworkerByOffenderNo(@PathVariable("offenderNo") @Parameter(description = "The offenderNo of offender", required = true) final String offenderNo) {
        final var offenderIdentifiers = bookingService.getOffenderIdentifiers(offenderNo, false, "GLOBAL_SEARCH", "VIEW_PRISONER_DATA", "KEY_WORKER").getBookingAndSeq()
            .orElseThrow(EntityNotFoundException.withMessage("No bookings found for offender %s", offenderNo));
        return keyworkerService.getKeyworkerDetailsByBooking(offenderIdentifiers.getBookingId());
    }

    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "The next visit for the offender.", description = "The next visit for the offender. Will return 200 with no body if no next visit is scheduled. Requires role VIEW_PRISONER_DATA or GLOBAL_SEARCH, or booking is in caseload")
    @VerifyBookingAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    @GetMapping("/{bookingId}/visits/next")
    @SlowReportQuery
    public VisitDetails getBookingVisitsNext(
        @PathVariable("bookingId") @Parameter(description = "The offender booking id", required = true) final Long bookingId,
        @RequestParam(value = "withVisitors", required = false, defaultValue = "false") @Parameter(description = "Toggle to return Visitors in response (or not).") final boolean withVisitors) {
        return bookingService.getBookingVisitNext(bookingId, withVisitors).orElse(null);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found or no permissions to see it.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "The summary of the visits for the offender.", description = "Will return whether there are any visits and also the date of the next scheduled visit. Requires role VIEW_PRISONER_DATA or GLOBAL_SEARCH, or booking is in caseload")
    @VerifyBookingAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    @GetMapping("/{bookingId}/visits/summary")
    @SlowReportQuery
    public VisitSummary getBookingVisitsSummary(
        @PathVariable("bookingId") @Parameter(description = "The offender booking id", required = true) final Long bookingId) {
        return bookingService.getBookingVisitsSummary(bookingId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "The list of prisons for which there are visits for the specified booking.", description = "To be used for filtering visits by prison. Requires role VIEW_PRISONER_DATA or GLOBAL_SEARCH, or booking is in caseload")
    @VerifyBookingAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    @GetMapping("/{bookingId}/visits/prisons")
    public List<PrisonDetails> getBookingVisitsPrisons(
        @PathVariable("bookingId") @Parameter(description = "The offender booking id", required = true) final Long bookingId) {
        return bookingService.getBookingVisitsPrisons(bookingId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "The appointment has been recorded. The updated object is returned including the status.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ScheduledEvent.class))})})
    @Operation(summary = "Create appointment for offender.", description = "Requires role VIEW_PRISONER_DATA or GLOBAL_SEARCH, or booking is in caseload")
    @ResponseStatus(HttpStatus.CREATED)
    @VerifyBookingAccess(overrideRoles = "GLOBAL_APPOINTMENT")
    @PostMapping("/{bookingId}/appointments")
    @HasWriteScope
    @ProxyUser
    public ScheduledEvent postBookingsBookingIdAppointments(
        @PathVariable("bookingId") @Parameter(description = "The offender booking id", required = true) final Long bookingId,
        @RequestBody @Parameter(required = true) final NewAppointment newAppointment) {
        return appointmentsService.createBookingAppointment(bookingId, authenticationFacade.getCurrentUsername(), newAppointment);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Court Cases", description = "Requires role VIEW_PRISONER_DATA or GLOBAL_SEARCH, or booking is in caseload")
    @VerifyBookingAccess(overrideRoles = {"GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    @GetMapping("/{bookingId}/court-cases")
    public List<CourtCase> getCourtCases(@PathVariable("bookingId") @Parameter(description = "The offender booking id", required = true) final Long bookingId, @RequestParam(value = "activeOnly", required = false, defaultValue = "true") @Parameter(description = "Only return active court cases") final boolean activeOnly) {
        return bookingService.getOffenderCourtCases(bookingId, activeOnly);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Get secondary languages", description = "Get secondary languages")
    @GetMapping("/{bookingId}/secondary-languages")
    @VerifyBookingAccess(overrideRoles = {"VIEW_PRISONER_DATA"})
    public List<SecondaryLanguage> getSecondaryLanguages(@PathVariable("bookingId") @Parameter(description = "bookingId", required = true) final Long bookingId) {
        return inmateService.getSecondaryLanguages(bookingId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "Requested resource not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Gets cell history for an offender booking", description = "Default sort order is by assignment date descending.  Requires a relationship (via caseload) with the prisoner or VIEW_PRISONER_DATA role.")
    @GetMapping("/{bookingId}/cell-history")
    @VerifyBookingAccess(overrideRoles = {"VIEW_PRISONER_DATA", "MAINTAIN_CELL_MOVEMENTS"})
    @SlowReportQuery
    public Page<BedAssignment> getBedAssignmentsHistory(@PathVariable("bookingId") @Parameter(description = "The offender booking linked to the court hearings.", required = true) final Long bookingId,
                                                        @RequestParam(value = "page", required = false, defaultValue = "0") @Parameter(description = "The page number to return. Index starts at 0") final Integer page,
                                                        @RequestParam(value = "size", required = false, defaultValue = "20") @Parameter(description = "The number of results per page. Defaults to 20.") final Integer size) {
        final var pageIndex = page != null ? page : 0;
        final var pageSize = size != null ? size : 20;
        return bedAssignmentHistoryService.getBedAssignmentsHistory(bookingId, PageRequest.of(pageIndex, pageSize, Sort.by("assignmentDate").descending()));
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "404", description = "No Fixed Term Recall exists for this booking", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
    @Operation(summary = "Gets the Fixed Term Recall details for a booking", description = "Requires role VIEW_PRISONER_DATA, or booking is in caseload")
    @ProgrammaticAuthorisation("Access is checked in service for legacy reasons")
    @GetMapping("/{bookingId}/fixed-term-recall")
    public FixedTermRecallDetails getFixedTermRecallDetails(@PathVariable("bookingId") @Parameter(description = "The offenders bookingID", required = true) final Long bookingId) {
        return fixedTermRecallService.getFixedTermRecallDetails(bookingId);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    @PreAuthorize("hasRole('VIEW_PRISONER_DATA')")
    @Operation(summary = "Get court event outcome reason codes for active court cases for a booking")
    @PostMapping("/court-event-outcomes")
    public List<CourtEventOutcome> getCourtEventOutcomes(@RequestBody @Parameter(description = "The booking ids", required = true) final Set<Long> bookingIds) {
        return bookingService.getOffenderCourtEventOutcomes(bookingIds);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
        @ApiResponse(responseCode = "500", description = "Unrecoverable error occurred whilst processing request.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})
    })
    @Operation(summary = "Details of the active sentence envelope, a combination of the person information, the active booking and calculable sentences for offenders")
    @PreAuthorize("hasRole('RELEASE_DATE_MANUAL_COMPARER')")
    @GetMapping("/latest/calculable-sentence-envelope")
    public List<CalculableSentenceEnvelope> getCalculableSentenceEnvelopeByOffenderNos(@RequestParam(value = "offenderNo") @Parameter(description = "Filter by a list of offender numbers") final Set<String> offenderNumbers) {
        return bookingService.getCalculableSentenceEnvelopeByOffenderNumbers(offenderNumbers);
    }
}
