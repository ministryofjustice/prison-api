package uk.gov.justice.hmpps.prison.api.resource.impl;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
import uk.gov.justice.hmpps.prison.api.resource.BookingResource;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.core.HasWriteScope;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;
import uk.gov.justice.hmpps.prison.service.AdjudicationService;
import uk.gov.justice.hmpps.prison.service.AppointmentsService;
import uk.gov.justice.hmpps.prison.service.BedAssignmentHistoryService;
import uk.gov.justice.hmpps.prison.service.BookingMaintenanceService;
import uk.gov.justice.hmpps.prison.service.BookingService;
import uk.gov.justice.hmpps.prison.service.CaseNoteService;
import uk.gov.justice.hmpps.prison.service.ContactService;
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException;
import uk.gov.justice.hmpps.prison.service.FinanceService;
import uk.gov.justice.hmpps.prison.service.IdempotentRequestService;
import uk.gov.justice.hmpps.prison.service.ImageService;
import uk.gov.justice.hmpps.prison.service.IncidentService;
import uk.gov.justice.hmpps.prison.service.InmateAlertService;
import uk.gov.justice.hmpps.prison.service.InmateSearchCriteria;
import uk.gov.justice.hmpps.prison.service.InmateService;
import uk.gov.justice.hmpps.prison.service.MovementsService;
import uk.gov.justice.hmpps.prison.service.OffenderNonAssociationsService;
import uk.gov.justice.hmpps.prison.service.keyworker.KeyWorkerAllocationService;
import uk.gov.justice.hmpps.prison.service.support.WrappedErrorResponseException;
import uk.gov.justice.hmpps.prison.web.handler.ResourceExceptionHandler;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uk.gov.justice.hmpps.prison.util.ResourceUtils.nvl;

/**
 * Implementation of Booking (/bookings) endpoint.
 */
@RestController
@RequestMapping("${api.base.path}/bookings")
@Validated
@AllArgsConstructor
public class BookingResourceImpl implements BookingResource {
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
    private final BookingMaintenanceService bookingMaintenanceService;
    private final IdempotentRequestService idempotentRequestService;
    private final IncidentService incidentService;
    private final MovementsService movementsService;
    private final AppointmentsService appointmentsService;
    private final OffenderNonAssociationsService offenderNonAssociationsService;

    @Override
    public ResponseEntity<List<OffenderBooking>> getOffenderBookings(final String query, final List<Long> bookingId, final List<String> offenderNo, final boolean iepLevel, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        final var allInmates = inmateService.findAllInmates(
                InmateSearchCriteria.builder()
                        .username(authenticationFacade.getCurrentUsername())
                        .query(query)
                        .iepLevel(iepLevel)
                        .offenderNos(offenderNo)
                        .bookingIds(bookingId)
                        .pageRequest(new uk.gov.justice.hmpps.prison.api.support.PageRequest(sortFields, sortOrder, pageOffset, pageLimit))
                        .build());

        return ResponseEntity.ok()
                .headers(allInmates.getPaginationHeaders())
                .body(allInmates.getItems());
    }

    @Override
    @HasWriteScope
    @PreAuthorize("hasRole('BOOKING_CREATE')")
    @ProxyUser
    public ResponseEntity<OffenderSummary> createOffenderBooking(@Valid final NewBooking newBooking) {
        // Step 1.
        // This service supports idempotent request control. First step is to check for existence of a response for
        // a previous request that used the same correlationId. If no correlationId is provided, this step can be skipped.
        final var correlationId = newBooking.getCorrelationId();
        final var isIdempotentRequest = StringUtils.isNotBlank(correlationId);

        if (isIdempotentRequest) {
            final var irc = idempotentRequestService.getAndSet(correlationId);

            if (irc.isComplete()) {
                // Immediately process and return response.
                if (HttpStatus.CREATED.value() == irc.getResponseStatus()) {
                    final var offenderSummary = idempotentRequestService.extractJsonResponse(irc, OffenderSummary.class);

                    return ResponseEntity.status(HttpStatus.CREATED).body(offenderSummary);
                } else {
                    final var errorResponse = idempotentRequestService.extractJsonResponse(irc, ErrorResponse.class);

                    throw new WrappedErrorResponseException(errorResponse);
                }
            } else if (irc.isPending()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
        }

        // Step 2.
        // Delegate to service to create booking
        final OffenderSummary offenderSummary;

        try {
            offenderSummary =
                    bookingMaintenanceService.createBooking(authenticationFacade.getCurrentUsername(), newBooking);
        } catch (final Exception ex) {
            if (isIdempotentRequest) {
                final var errorResponse = ResourceExceptionHandler.processResponse(ex);

                idempotentRequestService.convertAndStoreResponse(correlationId, errorResponse, errorResponse.getStatus());

                throw new WrappedErrorResponseException(errorResponse);
            }

            throw ex;
        }

        if (isIdempotentRequest) {
            idempotentRequestService.convertAndStoreResponse(correlationId, offenderSummary, 201);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(offenderSummary);
    }

    @Override
    @HasWriteScope
    @PreFilter("hasRole('BOOKING_RECALL')")
    @ProxyUser
    public OffenderSummary recallOffenderBooking(@Valid final RecallBooking recallBooking) {
        return bookingMaintenanceService.recallBooking(authenticationFacade.getCurrentUsername(), recallBooking);
    }

    @Override
    public Movement getMovementByBookingIdAndSequence(final Long bookingId, final Integer sequenceNumber) {
        return movementsService.getMovementByBookingIdAndSequence(bookingId, sequenceNumber).orElseThrow(EntityNotFoundException.withMessage(format("Movement Not found booking Id %d, seq %d", bookingId, sequenceNumber)));
    }

    @Override
    public InmateDetail getOffenderBooking(final Long bookingId, final boolean basicInfo, final boolean extraInfo) {

        return basicInfo && !extraInfo ?
                inmateService.getBasicInmateDetail(bookingId)
                : inmateService.findInmate(bookingId, extraInfo);
    }

    @Override
    public InmateDetail getOffenderBookingByOffenderNo(final String offenderNo, final boolean fullInfo, final boolean extraInfo) {
        final var offenderSummary = bookingService.getLatestBookingByOffenderNo(offenderNo);
        return fullInfo || extraInfo ?
                inmateService.findInmate(offenderSummary.getBookingId(), extraInfo) :
                inmateService.getBasicInmateDetail(offenderSummary.getBookingId());
    }

    @Override
    public List<InmateBasicDetails> getBasicInmateDetailsForOffenders(final Set<String> offenders, final Boolean activeOnly) {
        final var active = activeOnly == null ? true : activeOnly;
        return inmateService.getBasicInmateDetailsForOffenders(offenders, active);
    }

    @Override
    public List<InmateBasicDetails> getBasicInmateDetailsByBookingIds(final String caseload, final Set<Long> bookingIds) {
        return inmateService.getBasicInmateDetailsByBookingIds(caseload, bookingIds);
    }

    @Override
    public ResponseEntity<List<ScheduledEvent>> getBookingActivities(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
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

    @Override
    public List<ScheduledEvent> getBookingActivitiesForToday(final Long bookingId, final String sortFields, final Order sortOrder) {
        final var today = LocalDate.now();

        return bookingService.getBookingActivities(
                bookingId,
                today,
                today,
                sortFields,
                sortOrder);
    }

    @Override
    @ProxyUser
    public ResponseEntity<Void> updateAttendance(final String offenderNo, final Long activityId, @NotNull final UpdateAttendance updateAttendance) {
        bookingService.updateAttendance(offenderNo, activityId, updateAttendance);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @ProxyUser
    public ResponseEntity<Void> updateAttendance(final Long bookingId, final Long activityId, final UpdateAttendance updateAttendance) {
        bookingService.updateAttendance(bookingId, activityId, updateAttendance);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<Void> updateAttendanceForMultipleBookingIds(final @NotNull UpdateAttendanceBatch body) {
        bookingService.updateAttendanceForMultipleBookingIds(body.getBookingActivities(), UpdateAttendance
                .builder()
                .eventOutcome(body.getEventOutcome())
                .performance(body.getPerformance())
                .outcomeComment(body.getOutcomeComment())
                .build());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    public List<IncidentCase> getIncidentsByBookingId(@NotNull final Long bookingId, final List<String> incidentTypes, final List<String> participationRoles) {
        return incidentService.getIncidentCasesByBookingId(bookingId, incidentTypes, participationRoles);
    }

    @Override
    @ProxyUser
    public ResponseEntity<AlertCreated> postAlert(final Long bookingId, final CreateAlert alert) {
        final var alertId = inmateAlertService.createNewAlert(bookingId, alert);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AlertCreated(alertId));
    }

    @Override
    @ProxyUser
    public Alert updateAlert(final Long bookingId, final Long alertSeq, final AlertChanges alert) {
        return inmateAlertService.updateAlert(bookingId, alertSeq, alert);
    }

    @Override
    public ResponseEntity<List<Alert>> getOffenderAlerts(final Long bookingId, final String query, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        final var inmateAlerts = inmateAlertService.getInmateAlerts(
                bookingId,
                query,
                sortFields,
                sortOrder,
                nvl(pageOffset, 0L),
                nvl(pageLimit, 10L));

        return ResponseEntity.ok()
                .headers(inmateAlerts.getPaginationHeaders())
                .body(inmateAlerts.getItems());
    }

    @Override
    public Alert getOffenderAlert(final Long bookingId, final Long alertId) {
        return inmateAlertService.getInmateAlert(bookingId, alertId);
    }

    @Override
    public List<Alert> getAlertsByOffenderNosAtAgency(final String agencyId, final List<String> offenderNos) {
        return inmateAlertService.getInmateAlertsByOffenderNosAtAgency(agencyId, offenderNos);
    }

    @Override
    public List<Alert> getAlertsByOffenderNos(final List<String> offenderNos) {
        return inmateAlertService.getInmateAlertsByOffenderNos(offenderNos, true, null, "bookingId,alertId", Order.ASC);
    }

    @Override
    public ResponseEntity<List<Alias>> getOffenderAliases(final Long bookingId, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        return inmateService.findInmateAliases(
                bookingId,
                sortFields,
                sortOrder,
                nvl(pageOffset, 0L),
                nvl(pageLimit, 5L)).getResponse();
    }

    @Override
    public Assessment getAssessmentByCode(final Long bookingId, final String assessmentCode) {
        return inmateService.getInmateAssessmentByCode(bookingId, assessmentCode).orElseThrow(() -> {
            throw EntityNotFoundException.withMessage("Offender does not have a [" + assessmentCode + "] assessment on record.");
        });
    }

    @Override
    public List<Assessment> getAssessments(final Long bookingId) {
        return inmateService.getAssessments(bookingId);
    }

    @Override
    public ResponseEntity<List<CaseNote>> getOffenderCaseNotes(final Long bookingId, final LocalDate from, final LocalDate to,
                                                               final String query, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        return caseNoteService.getCaseNotes(
                bookingId,
                query,
                from,
                to,
                sortFields,
                sortOrder,
                nvl(pageOffset, 0L),
                nvl(pageLimit, 10L)).getResponse();
    }

    @Override
    public CaseNote getOffenderCaseNote(final Long bookingId, final Long caseNoteId) {
        return caseNoteService.getCaseNote(bookingId, caseNoteId);
    }

    @Override
    public PrivilegeSummary getBookingIEPSummary(final Long bookingId, final boolean withDetails) {
        return bookingService.getBookingIEPSummary(bookingId, withDetails);
    }

    @Override
    @HasWriteScope
    @PreAuthorize("hasRole('MAINTAIN_IEP')")
    @ProxyUser
    public ResponseEntity<Void> addIepLevel(final Long bookingId, @NotNull final IepLevelAndComment iepLevel) {
        bookingService.addIepLevel(bookingId, authenticationFacade.getCurrentUsername(), iepLevel);
        return ResponseEntity.noContent().build();
    }

    @Override
    public Collection<PrivilegeSummary> getBookingIEPSummaryForOffenders(final List<Long> bookings, final boolean withDetails) {
        return bookingService.getBookingIEPSummary(bookings, withDetails).values();

    }

    @Override
    public ImageDetail getMainImageForBookings(final Long bookingId) {
        return inmateService.getMainBookingImage(bookingId);
    }

    @Override
    public ResponseEntity<byte[]> getMainBookingImageDataByNo(final String offenderNo, final boolean fullSizeImage) {
        return imageService.getImageContent(offenderNo, fullSizeImage)
                .map(bytes -> new ResponseEntity<>(bytes, HttpStatus.OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @Override
    public ResponseEntity<byte[]> getMainBookingImageData(final Long bookingId, final boolean fullSizeImage) {
        final var mainBookingImage = inmateService.getMainBookingImage(bookingId);
        return imageService.getImageContent(mainBookingImage.getImageId(), fullSizeImage)
                .map(bytes -> new ResponseEntity<>(bytes, HttpStatus.OK))
                .orElse(new ResponseEntity<>(NOT_FOUND));
    }

    @Override
    public SentenceDetail getBookingSentenceDetail(final Long bookingId) {
        return bookingService.getBookingSentenceDetail(bookingId);    }

    @Override
    public SentenceAdjustmentDetail getBookingSentenceAdjustments(final Long bookingId) {
        return bookingService.getBookingSentenceAdjustments(bookingId);    }

    @Override
    @HasWriteScope
    @ProxyUser
    public CaseNote createBookingCaseNote(final Long bookingId, final NewCaseNote body) {
        return caseNoteService.createCaseNote(bookingId, body, authenticationFacade.getCurrentUsername());
    }

    @Override
    @HasWriteScope
    @ProxyUser
    public CaseNote createOffenderCaseNote(final String offenderNo, final NewCaseNote body) {
        return caseNoteService.createCaseNote(offenderNo, body, authenticationFacade.getCurrentUsername());
    }

    @Override
    @HasWriteScope
    @ProxyUser
    public CaseNote updateOffenderCaseNote(final Long bookingId, final Long caseNoteId, final UpdateCaseNote body) {
        return caseNoteService.updateCaseNote(
                bookingId, caseNoteId, authenticationFacade.getCurrentUsername(), body.getText());
    }

    @Override
    public Account getBalances(final Long bookingId) {
        return financeService.getBalances(bookingId);
    }

    @Override
    public List<PropertyContainer> getOffenderPropertyContainers(final Long bookingId) {
        return bookingService.getOffenderPropertyContainers(bookingId);
    }

    @Override
    public List<OffenceDetail> getMainOffence(final Long bookingId) {
        return bookingService.getMainOffenceDetails(bookingId);
    }

    @Override
    public List<OffenceDetail> getMainOffence(final Set<Long> bookingIds) {
        return bookingService.getMainOffenceDetails(bookingIds);
    }

    @Override
    @PreAuthorize("hasAnyRole('SYSTEM_USER','SYSTEM_READ_ONLY','CREATE_CATEGORISATION','APPROVE_CATEGORISATION')")
    public List<OffenceHistoryDetail> getOffenceHistory(final String offenderNo, final boolean convictionsOnly) {
        return bookingService.getOffenceHistory(offenderNo, convictionsOnly);
    }

    @Override
    public PhysicalAttributes getPhysicalAttributes(final Long bookingId) {
        return inmateService.getPhysicalAttributes(bookingId);
    }

    @Override
    public List<PhysicalCharacteristic> getPhysicalCharacteristics(final Long bookingId) {
        return inmateService.getPhysicalCharacteristics(bookingId);
    }

    @Override
    public List<PhysicalMark> getPhysicalMarks(final Long bookingId) {
        return inmateService.getPhysicalMarks(bookingId);
    }

    @Override
    public PersonalCareNeeds getPersonalCareNeeds(final Long bookingId, final List<String> problemTypes) {
        return inmateService.getPersonalCareNeeds(bookingId, problemTypes);
    }

    @Override
    public MilitaryRecords getMilitaryRecords(final Long bookingId) {
        return bookingService.getMilitaryRecords(bookingId);
    }

    @Override
    public List<PersonalCareNeeds> getPersonalCareNeeds(final List<String> offenderNos, final List<String> problemTypes) {
        return inmateService.getPersonalCareNeeds(offenderNos, problemTypes);
    }

    @Override
    public ReasonableAdjustments getReasonableAdjustments(final Long bookingId, final List<String> treatmentCodes) {
        return inmateService.getReasonableAdjustments(bookingId, treatmentCodes);
    }

    @Override
    public List<ProfileInformation> getProfileInformation(final Long bookingId) {
        return inmateService.getProfileInformation(bookingId);
    }

    @Override
    public List<Contact> getRelationships(final Long bookingId, final String relationshipType) {
        return contactService.getRelationships(bookingId, relationshipType, true);
    }

    @Override
    public List<Contact> getRelationshipsByOffenderNo(final String offenderNo, final String relationshipType) {
        return contactService.getRelationshipsByOffenderNo(offenderNo, relationshipType);
    }

    @Override
    @ProxyUser
    public Contact createRelationship(final Long bookingId, final OffenderRelationship relationshipDetail) {
        return contactService.createRelationship(bookingId, relationshipDetail);
    }

    @Override
    @ProxyUser
    public Contact createRelationshipByOffenderNo(final String offenderNo, final OffenderRelationship relationshipDetail) {
        return contactService.createRelationshipByOffenderNo(offenderNo, relationshipDetail);
    }

    @Override
    public List<ScheduledEvent> getEventsToday(final Long bookingId) {
        return bookingService.getEventsToday(bookingId);
    }

    @Override
    public List<OffenderIdentifier> getOffenderIdentifiers(@NotNull final Long bookingId, final String identifierType) {
        return inmateService.getOffenderIdentifiers(bookingId, identifierType);
    }

    @Override
    public List<ScheduledEvent> getEvents(final Long bookingId, final LocalDate fromDate, final LocalDate toDate) {
        return bookingService.getEvents(bookingId, fromDate, toDate);
    }

    @Override
    public List<ScheduledEvent> getEventsThisWeek(final Long bookingId) {
        return bookingService.getEventsThisWeek(bookingId);
    }

    @Override
    public List<ScheduledEvent> getEventsNextWeek(final Long bookingId) {
        return bookingService.getEventsNextWeek(bookingId);
    }

    @Override
    public ContactDetail getContacts(final Long bookingId) {
        return contactService.getContacts(bookingId);
    }

    @Override
    public CaseNoteCount getCaseNoteCount(final Long bookingId, final String type, final String subType, final LocalDate fromDate, final LocalDate toDate) {
        return caseNoteService.getCaseNoteCount(
                bookingId,
                type,
                subType,
                fromDate,
                toDate);
    }

    @Override
    public AdjudicationSummary getAdjudicationSummary(final Long bookingId, final LocalDate awardCutoffDate, final LocalDate adjudicationCutoffDate) {
        return adjudicationService.getAdjudicationSummary(bookingId,
                awardCutoffDate, adjudicationCutoffDate);
    }

    @Override
    public ResponseEntity<List<ScheduledEvent>> getBookingVisits(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
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

    @Override
    public Page<VisitWithVisitors<VisitDetails>> getBookingVisitsWithVisitor(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final String visitType, final Integer page, final Integer size) {
        final var pageIndex = page != null ? page : 0;
        final var pageSize = size != null ? size : 20;
        final PageRequest pageRequest = PageRequest.of(pageIndex, pageSize);
        return bookingService.getBookingVisitsWithVisitor(bookingId, fromDate, toDate, visitType, pageRequest);
    }

    @Override
    public List<ScheduledEvent> getBookingVisitsForToday(final Long bookingId, final String sortFields, final Order sortOrder) {
        final var today = LocalDate.now();
        return bookingService.getBookingVisits(
                bookingId,
                today,
                today,
                sortFields,
                sortOrder);
    }

    @Override
    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public VisitBalances getBookingVisitBalances(final String offenderNo) {
        final var identifiers = bookingService.getOffenderIdentifiers(offenderNo).getBookingAndSeq().orElseThrow(EntityNotFoundException.withMessage("No bookings found for offender {}", offenderNo));

        return bookingService.getBookingVisitBalances(identifiers.getBookingId()).orElseThrow(EntityNotFoundException.withId(identifiers.getBookingId()));
    }

    @Override
    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH"})
    public Keyworker getKeyworkerByOffenderNo(final String offenderNo) {
        final var offenderIdentifiers = bookingService.getOffenderIdentifiers(offenderNo).getBookingAndSeq().orElseThrow(EntityNotFoundException.withMessage("No bookings found for offender {}", offenderNo));
        return keyworkerService.getKeyworkerDetailsByBooking(offenderIdentifiers.getBookingId());
    }

    @Override
    public VisitDetails getBookingVisitsLast(final Long bookingId) {
        return bookingService.getBookingVisitLast(bookingId);
    }

    @Override
    public VisitDetails getBookingVisitsNext(final Long bookingId) {
        return bookingService.getBookingVisitNext(bookingId);
    }

    @Override
    public ResponseEntity<List<ScheduledEvent>> getBookingsBookingIdAppointments(final Long bookingId, final LocalDate fromDate, final LocalDate toDate, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
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

    @Override
    public List<ScheduledEvent> getBookingAppointmentsForToday(final Long bookingId, final String sortFields, final Order sortOrder) {
        final var today = LocalDate.now();
        return bookingService.getBookingAppointments(
                bookingId,
                today,
                today,
                sortFields,
                sortOrder);
    }

    @Override
    public List<ScheduledEvent> getBookingAppointmentsForThisWeek(final Long bookingId, final String sortFields, final Order sortOrder) {
        final var fromDate = LocalDate.now();
        final var toDate = fromDate.plusDays(6);

        return bookingService.getBookingAppointments(
                bookingId,
                fromDate,
                toDate,
                sortFields,
                sortOrder);
    }

    @Override
    public List<ScheduledEvent> getBookingAppointmentsForNextWeek(final Long bookingId, final String sortFields, final Order sortOrder) {
        final var fromDate = LocalDate.now().plusDays(7);
        final var toDate = fromDate.plusDays(6);

        return bookingService.getBookingAppointments(
                bookingId,
                fromDate,
                toDate,
                sortFields,
                sortOrder);
    }

    @Override
    @HasWriteScope
    @ProxyUser
    public ScheduledEvent postBookingsBookingIdAppointments(final Long bookingId, final NewAppointment newAppointment) {
        return appointmentsService.createBookingAppointment(
                bookingId, authenticationFacade.getCurrentUsername(), newAppointment);
    }

    @Override
    public List<CourtCase> getCourtCases(final Long bookingId, final boolean activeOnly) {
        return bookingService.getOffenderCourtCases(bookingId, activeOnly);
    }

    @Override
    public List<SecondaryLanguage> getSecondaryLanguages(final Long bookingId) {
        return inmateService.getSecondaryLanguages(bookingId);
    }

    @Override
    public OffenderNonAssociationDetails getNonAssociationDetails(final Long bookingId) {
        return offenderNonAssociationsService.retrieve(bookingId);
    }

    @Override
    public Page<BedAssignment> getBedAssignmentsHistory(final Long bookingId, final Integer page, final Integer size) {
        final var pageIndex = page != null ? page : 0;
        final var pageSize = size != null ? size : 20;
        final PageRequest pageRequest = PageRequest.of(pageIndex, pageSize);
        return bedAssignmentHistoryService.getBedAssignmentsHistory(bookingId, PageRequest.of(pageIndex, pageSize));
    }

}
