package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.resource.BookingResource;
import net.syscon.elite.api.resource.IncidentsResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.core.ProxyUser;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.*;
import net.syscon.elite.service.impl.IncidentService;
import net.syscon.elite.service.keyworker.KeyWorkerAllocationService;
import net.syscon.elite.service.support.WrappedErrorResponseException;
import net.syscon.elite.web.handler.ResourceExceptionHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static javax.ws.rs.core.Response.Status.CREATED;
import static net.syscon.util.DateTimeConverter.fromISO8601DateString;
import static net.syscon.util.ResourceUtils.nvl;

/**
 * Implementation of Booking (/bookings) endpoint.
 */
@RestResource
@Path("/bookings")
public class BookingResourceImpl implements BookingResource {
    private final AuthenticationFacade authenticationFacade;
    private final BookingService bookingService;
    private final InmateService inmateService;
    private final CaseNoteService caseNoteService;
    private final InmateAlertService inmateAlertService;
    private final FinanceService financeService;
    private final ContactService contactService;
    private final AdjudicationService adjudicationService;
    private final ImageService imageService;
    private final KeyWorkerAllocationService keyworkerService;
    private final BookingMaintenanceService bookingMaintenanceService;
    private final IdempotentRequestService idempotentRequestService;
    private final IncidentService incidentService;

    public BookingResourceImpl(final AuthenticationFacade authenticationFacade, final BookingService bookingService,
                               final InmateService inmateService, final CaseNoteService caseNoteService,
                               final InmateAlertService inmateAlertService, final FinanceService financeService,
                               final ContactService contactService, final AdjudicationService adjudicationService,
                               final ImageService imageService, final KeyWorkerAllocationService keyworkerService,
                               final BookingMaintenanceService bookingMaintenanceService,
                               final IdempotentRequestService idempotentRequestService, final IncidentService incidentService) {
        this.authenticationFacade = authenticationFacade;
        this.bookingService = bookingService;
        this.inmateService = inmateService;
        this.caseNoteService = caseNoteService;
        this.inmateAlertService = inmateAlertService;
        this.financeService = financeService;
        this.contactService = contactService;
        this.adjudicationService = adjudicationService;
        this.imageService = imageService;
        this.keyworkerService = keyworkerService;
        this.bookingMaintenanceService = bookingMaintenanceService;
        this.idempotentRequestService = idempotentRequestService;
        this.incidentService = incidentService;
    }

    @Override
    public GetOffenderBookingsResponse getOffenderBookings(final String query, final List<Long> bookingId, final List<String> offenderNo, final boolean iepLevel, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        final var allInmates = inmateService.findAllInmates(
                InmateSearchCriteria.builder()
                    .username(authenticationFacade.getCurrentUsername())
                    .query(query)
                    .iepLevel(iepLevel)
                    .offenderNos(offenderNo)
                    .bookingIds(bookingId)
                    .pageRequest(new PageRequest(sortFields, sortOrder, pageOffset, pageLimit))
                .build());

        return GetOffenderBookingsResponse.respond200WithApplicationJson(allInmates);
    }

    @Override
    @PreAuthorize("#oauth2.hasScope('write') && hasRole('BOOKING_CREATE')")
    @ProxyUser
    public CreateOffenderBookingResponse createOffenderBooking(final NewBooking newBooking) {
        // Step 1.
        // This service supports idempotent request control. First step is to check for existence of a response for
        // a previous request that used the same correlationId. If no correlationId is provided, this step can be skipped.
        final var correlationId = newBooking.getCorrelationId();
        final var isIdempotentRequest = StringUtils.isNotBlank(correlationId);

        if (isIdempotentRequest) {
            final var irc = idempotentRequestService.getAndSet(correlationId);

            if (irc.isComplete()) {
                // Immediately process and return response.
                if (CREATED.getStatusCode() == irc.getResponseStatus()) {
                    final var offenderSummary = idempotentRequestService.extractJsonResponse(irc, OffenderSummary.class);

                    return CreateOffenderBookingResponse.respond201WithApplicationJson(offenderSummary);
                } else {
                    final var errorResponse = idempotentRequestService.extractJsonResponse(irc, ErrorResponse.class);

                    throw new WrappedErrorResponseException(errorResponse);
                }
            } else if (irc.isPending()) {
                return CreateOffenderBookingResponse.respond204WithApplicationJson();
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

        return CreateOffenderBookingResponse.respond201WithApplicationJson(offenderSummary);
    }

    @Override
    @PreAuthorize("#oauth2.hasScope('write') && hasRole('BOOKING_RECALL')")
    @ProxyUser
    public RecallOffenderBookingResponse recallOffenderBooking(final RecallBooking recallBooking) {
        final var offenderSummary =
                bookingMaintenanceService.recallBooking(authenticationFacade.getCurrentUsername(), recallBooking);

        return RecallOffenderBookingResponse.respond200WithApplicationJson(offenderSummary);
    }

    @Override
    public GetOffenderBookingResponse getOffenderBooking(final Long bookingId, final boolean basicInfo) {

        final var inmate = basicInfo ?
                  inmateService.getBasicInmateDetail(bookingId)
                : inmateService.findInmate(bookingId, authenticationFacade.getCurrentUsername());

        return GetOffenderBookingResponse.respond200WithApplicationJson(inmate);
    }

    @Override
    public GetOffenderBookingByOffenderNoResponse getOffenderBookingByOffenderNo(final String offenderNo, final boolean fullInfo) {

        final var bookingId = bookingService.getBookingIdByOffenderNo(offenderNo);

        final var inmate = fullInfo ?
                inmateService.findInmate(bookingId, authenticationFacade.getCurrentUsername()) :
                inmateService.getBasicInmateDetail(bookingId);

        return GetOffenderBookingByOffenderNoResponse.respond200WithApplicationJson(inmate);
    }

    @Override
    public List<InmateBasicDetails> getBasicInmateDetailsForOffenders(final Set<String> offenders) {
        return inmateService.getBasicInmateDetailsForOffenders(offenders);
    }

    @Override
    public List<InmateBasicDetails> getBasicInmateDetailsByBookingIds(final String caseload, final Set<Long> bookingIds) {
        return inmateService.getBasicInmateDetailsByBookingIds(caseload, bookingIds);
    }


    @Override
    public GetBookingActivitiesResponse getBookingActivities(final Long bookingId, final String fromDate, final String toDate, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        final var activities = bookingService.getBookingActivities(
                bookingId,
                fromISO8601DateString(fromDate),
                fromISO8601DateString(toDate),
                nvl(pageOffset, 0L),
                nvl(pageLimit, 10L),
                sortFields,
                sortOrder);

        return GetBookingActivitiesResponse.respond200WithApplicationJson(activities);
    }

    @Override
    public GetBookingActivitiesForTodayResponse getBookingActivitiesForToday(final Long bookingId, final String sortFields, final Order sortOrder) {
        final var today = LocalDate.now();

        final var activities = bookingService.getBookingActivities(
                bookingId,
                today,
                today,
                sortFields,
                sortOrder);

        return GetBookingActivitiesForTodayResponse.respond200WithApplicationJson(activities);
    }

    @Override
    @ProxyUser
    public UpdateAttendanceResponse updateAttendance(final String offenderNo, final Long activityId, final UpdateAttendance body) {
        bookingService.updateAttendance(offenderNo, activityId, body);
        return UpdateAttendanceResponse.respond201WithApplicationJson();
    }

    public IncidentsResource.IncidentListResponse getIncidentsByBookingId(@NotNull final Long bookingId, final List<String> incidentTypes, final List<String> participationRoles) {

        return new IncidentsResource.IncidentListResponse(Response.status(200)
                .header("Content-Type", MediaType.APPLICATION_JSON).build(),
                incidentService.getIncidentCasesByBookingId(bookingId, incidentTypes, participationRoles));

    }

    @Override
    public GetOffenderAlertsResponse getOffenderAlerts(final Long bookingId, final String query, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        final var inmateAlerts = inmateAlertService.getInmateAlerts(
                bookingId,
                query,
                sortFields,
                sortOrder,
                nvl(pageOffset, 0L),
                nvl(pageLimit, 10L));

        return GetOffenderAlertsResponse.respond200WithApplicationJson(inmateAlerts);
    }

    @Override
    public GetOffenderAlertsResponse getOffenderAlertsByOffenderNo(final String offenderNo, final String query, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        final var bookingId = bookingService.getBookingIdByOffenderNo(offenderNo);
        return getOffenderAlerts(bookingId, query, pageOffset, pageLimit, sortFields, sortOrder);
    }

    @Override
    public GetOffenderAlertResponse getOffenderAlert(final Long bookingId, final Long alertId) {
        final var inmateAlert = inmateAlertService.getInmateAlert(bookingId, alertId);

        return GetOffenderAlertResponse.respond200WithApplicationJson(inmateAlert);
    }

    @Override
    public GetAlertsByOffenderNosResponse getAlertsByOffenderNosAtAgency(final String agencyId, final List<String> offenderNos) {
        final var inmateAlerts = inmateAlertService.getInmateAlertsByOffenderNosAtAgency(agencyId, offenderNos);

        return GetAlertsByOffenderNosResponse.respond200WithApplicationJson(inmateAlerts);
    }

    @Override
    public GetAlertsByOffenderNosResponse getAlertsByOffenderNos(final List<String> offenderNos) {
        final var inmateAlerts = inmateAlertService.getInmateAlertsByOffenderNos(offenderNos, true, null, "bookingId,alertId", Order.ASC);

        return GetAlertsByOffenderNosResponse.respond200WithApplicationJson(inmateAlerts);
    }

    @Override
    public GetOffenderAliasesResponse getOffenderAliases(final Long bookingId, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        final var aliases = inmateService.findInmateAliases(
                bookingId,
                sortFields,
                sortOrder,
                nvl(pageOffset, 0L),
                nvl(pageLimit, 5L));

        return GetOffenderAliasesResponse.respond200WithApplicationJson(aliases);
    }

    @Override
    public GetAssessmentByCodeResponse getAssessmentByCode(final Long bookingId, final String assessmentCode) {
        final var inmateAssessmentByCode = inmateService.getInmateAssessmentByCode(bookingId, assessmentCode);

        if (!inmateAssessmentByCode.isPresent()) {
            throw EntityNotFoundException.withMessage("Offender does not have a [" + assessmentCode + "] assessment on record.");
        }

        return GetAssessmentByCodeResponse.respond200WithApplicationJson(inmateAssessmentByCode.get());
    }

    @Override
    public GetAssessmentsResponse getAssessments(final Long bookingId) {
        return GetAssessmentsResponse.respond200WithApplicationJson(inmateService.getAssessments(bookingId));
    }

    @Override
    public GetOffenderCaseNotesResponse getOffenderCaseNotes(final Long bookingId, final String from, final String to,
                                                             final String query, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        final var caseNotes = caseNoteService.getCaseNotes(
                bookingId,
                query,
                fromISO8601DateString(from),
                fromISO8601DateString(to),
                sortFields,
                sortOrder,
                nvl(pageOffset, 0L),
                nvl(pageLimit, 10L));

        return GetOffenderCaseNotesResponse.respond200WithApplicationJson(caseNotes);
    }

    @Override
    public GetOffenderCaseNoteResponse getOffenderCaseNote(final Long bookingId, final Long caseNoteId) {
        final var caseNote = caseNoteService.getCaseNote(bookingId, caseNoteId);

        return GetOffenderCaseNoteResponse.respond200WithApplicationJson(caseNote);
    }

    @Override
    public GetBookingIEPSummaryResponse getBookingIEPSummary(final Long bookingId, final boolean withDetails) {
        final var privilegeSummary = bookingService.getBookingIEPSummary(bookingId, withDetails);

        return GetBookingIEPSummaryResponse.respond200WithApplicationJson(privilegeSummary);
    }

    @Override
    public GetBookingIEPSummaryForOffendersResponse getBookingIEPSummaryForOffenders(final List<Long> bookings, final boolean withDetails) {
        final var result = bookingService.getBookingIEPSummary(bookings, withDetails);
        final var privilegeSummaries = new ArrayList<>(result.values());

        return GetBookingIEPSummaryForOffendersResponse.respond200WithApplicationJson(privilegeSummaries);
    }

    @Override
    public GetMainImageForBookingsResponse getMainImageForBookings(final Long bookingId) {
        return GetMainImageForBookingsResponse.respond200WithApplicationJson(inmateService.getMainBookingImage(bookingId));
    }


    @Override
    public GetMainBookingImageDataByNoResponse getMainBookingImageDataByNo(final String offenderNo) {
        final var data = imageService.getImageContent(offenderNo);
        if (data != null) {
            try {
                final var temp = File.createTempFile("userimage", ".tmp");
                FileUtils.copyInputStreamToFile(new ByteArrayInputStream(data), temp);
                return GetMainBookingImageDataByNoResponse.respond200WithApplicationJson(temp);
            } catch (final IOException e) {
                final var errorResponse = ErrorResponse.builder()
                        .errorCode(500)
                        .userMessage("An error occurred loading the image for offender No "+ offenderNo)
                        .build();
                return GetMainBookingImageDataByNoResponse.respond500WithApplicationJson(errorResponse);
            }
        } else {
            final var errorResponse = ErrorResponse.builder()
                    .errorCode(404)
                    .userMessage("No image was found for offender No "+ offenderNo)
                    .build();
            return GetMainBookingImageDataByNoResponse.respond404WithApplicationJson(errorResponse);
        }
    }

    @Override
    public GetMainBookingImageDataResponse getMainBookingImageData(final Long bookingId) {
        final var mainBookingImage = inmateService.getMainBookingImage(bookingId);
        final var imageId = mainBookingImage.getImageId();
        final var data = imageService.getImageContent(imageId);
        if (data != null) {
            try {
                final var temp = File.createTempFile("userimage", ".tmp");
                FileUtils.copyInputStreamToFile(new ByteArrayInputStream(data), temp);
                return GetMainBookingImageDataResponse.respond200WithApplicationJson(temp);
            } catch (final IOException e) {
                final var errorResponse = ErrorResponse.builder()
                        .errorCode(500)
                        .userMessage("An error occurred loading the image ID "+ imageId)
                        .build();
                return GetMainBookingImageDataResponse.respond500WithApplicationJson(errorResponse);
            }
        } else {
            final var errorResponse = ErrorResponse.builder()
                    .errorCode(404)
                    .userMessage("No image was found with ID "+ imageId)
                    .build();
            return GetMainBookingImageDataResponse.respond404WithApplicationJson(errorResponse);
        }
    }

    @Override
    public GetBookingSentenceDetailResponse getBookingSentenceDetail(final Long bookingId) {
        final var sentenceDetail = bookingService.getBookingSentenceDetail(bookingId);

        return GetBookingSentenceDetailResponse.respond200WithApplicationJson(sentenceDetail);
    }

    @Override
    @PreAuthorize("#oauth2.hasScope('write')")
    @ProxyUser
    public CreateBookingCaseNoteResponse createBookingCaseNote(final Long bookingId, final NewCaseNote body) {
        final var caseNote = caseNoteService.createCaseNote(bookingId, body, authenticationFacade.getCurrentUsername());

        return CreateBookingCaseNoteResponse.respond201WithApplicationJson(caseNote);
    }

    @Override
    @PreAuthorize("#oauth2.hasScope('write')")
    @ProxyUser
    public CreateOffenderCaseNoteResponse createOffenderCaseNote(final String offenderNo, final NewCaseNote body) {
        final var latestBookingByOffenderNo = bookingService.getLatestBookingByOffenderNo(offenderNo);
        final var caseNote = caseNoteService.createCaseNote(latestBookingByOffenderNo.getBookingId(), body, authenticationFacade.getCurrentUsername());

        return CreateOffenderCaseNoteResponse.respond201WithApplicationJson(caseNote);
    }

    @Override
    @PreAuthorize("#oauth2.hasScope('write')")
    @ProxyUser
    public UpdateOffenderCaseNoteResponse updateOffenderCaseNote(final Long bookingId, final Long caseNoteId, final UpdateCaseNote body) {
        final var caseNote = caseNoteService.updateCaseNote(
                bookingId, caseNoteId, authenticationFacade.getCurrentUsername(), body.getText());

        return UpdateOffenderCaseNoteResponse.respond201WithApplicationJson(caseNote);
    }

    @Override
    public GetBalancesResponse getBalances(final Long bookingId) {
        final var account = financeService.getBalances(bookingId);

        return GetBalancesResponse.respond200WithApplicationJson(account);
    }

    @Override
    public GetMainOffenceResponse getMainOffence(final Long bookingId) {
        final var offenceDetails = bookingService.getMainOffenceDetails(bookingId);

        return GetMainOffenceResponse.respond200WithApplicationJson(offenceDetails);
    }

    @Override
    public Response getOffenceHistory(final String offenderNo) {
        final var list = bookingService.getOffenceHistory(offenderNo);
        return Response.status(200)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .entity(list)
                .build();
    }

    @Override
    public GetPhysicalAttributesResponse getPhysicalAttributes(final Long bookingId) {
        return GetPhysicalAttributesResponse.respond200WithApplicationJson(inmateService.getPhysicalAttributes(bookingId));
    }

    @Override
    public GetPhysicalCharacteristicsResponse getPhysicalCharacteristics(final Long bookingId) {
        return GetPhysicalCharacteristicsResponse.respond200WithApplicationJson(inmateService.getPhysicalCharacteristics(bookingId));
    }

    @Override
    public GetPhysicalMarksResponse getPhysicalMarks(final Long bookingId) {
        return GetPhysicalMarksResponse.respond200WithApplicationJson(inmateService.getPhysicalMarks(bookingId));
    }

    @Override
    public GetProfileInformationResponse getProfileInformation(final Long bookingId) {
        return GetProfileInformationResponse.respond200WithApplicationJson(inmateService.getProfileInformation(bookingId));
    }

    @Override
    public BookingResource.GetRelationshipsResponse getRelationships(final Long bookingId, final String relationshipType) {
        final var relationships = contactService.getRelationships(bookingId, relationshipType);
        return BookingResource.GetRelationshipsResponse.respond200WithApplicationJson(relationships);
    }

    @Override
    public GetRelationshipsByOffenderNoResponse getRelationshipsByOffenderNo(final String offenderNo, final String relationshipType) {
        final var relationships = contactService.getRelationshipsByOffenderNo(offenderNo, relationshipType);
        return GetRelationshipsByOffenderNoResponse.respond200WithApplicationJson(relationships);
    }

    @Override
    @ProxyUser
    public CreateRelationshipResponse createRelationship(final Long bookingId, final OffenderRelationship relationshipDetail) {
        final var relationship = contactService.createRelationship(bookingId, relationshipDetail);
        return CreateRelationshipResponse.respond201WithApplicationJson(relationship);
    }

    @Override
    @ProxyUser
    public CreateRelationshipByOffenderNoResponse createRelationshipByOffenderNo(final String offenderNo, final OffenderRelationship relationshipDetail) {
        final var relationship = contactService.createRelationshipByOffenderNo(offenderNo, relationshipDetail);
        return CreateRelationshipByOffenderNoResponse.respond201WithApplicationJson(relationship);
    }

    @Override
    public GetEventsTodayResponse getEventsToday(final Long bookingId) {
        final var scheduledEvents = bookingService.getEventsToday(bookingId);

        return GetEventsTodayResponse.respond200WithApplicationJson(scheduledEvents);
    }

    @Override
    public GetOffenderIdentifiersResponse getOffenderIdentifiers(final Long bookingId) {
        return GetOffenderIdentifiersResponse.respond200WithApplicationJson(inmateService.getOffenderIdentifiers(bookingId));
    }

    @Override
    public GetEventsThisWeekResponse getEventsThisWeek(final Long bookingId) {
        final var scheduledEvents = bookingService.getEventsThisWeek(bookingId);

        return GetEventsThisWeekResponse.respond200WithApplicationJson(scheduledEvents);
    }

    @Override
    public GetEventsNextWeekResponse getEventsNextWeek(final Long bookingId) {
        final var scheduledEvents = bookingService.getEventsNextWeek(bookingId);

        return GetEventsNextWeekResponse.respond200WithApplicationJson(scheduledEvents);
    }

    @Override
    public GetContactsResponse getContacts(final Long bookingId) {
        final var contacts = contactService.getContacts(bookingId);

        return GetContactsResponse.respond200WithApplicationJson(contacts);
    }

    @Override
    public GetCaseNoteCountResponse getCaseNoteCount(final Long bookingId, final String type, final String subType, final String fromDate, final String toDate) {
        final var caseNoteCount = caseNoteService.getCaseNoteCount(
                bookingId,
                type,
                subType,
                fromISO8601DateString(fromDate),
                fromISO8601DateString(toDate));

        return GetCaseNoteCountResponse.respond200WithApplicationJson(caseNoteCount);
    }

    @Override
    public GetAdjudicationsResponse getAdjudications(final Long bookingId, final String awardCutoffDate, final String adjudicationCutoffDate) {
        final var adjudicationDetail = adjudicationService.getAdjudications(bookingId,
                fromISO8601DateString(awardCutoffDate), fromISO8601DateString(adjudicationCutoffDate));

        return GetAdjudicationsResponse.respond200WithApplicationJson(adjudicationDetail);
    }

    @Override
    public GetBookingVisitsResponse getBookingVisits(final Long bookingId, final String fromDate, final String toDate, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        final var visits = bookingService.getBookingVisits(
                bookingId,
                fromISO8601DateString(fromDate),
                fromISO8601DateString(toDate),
                nvl(pageOffset, 0L),
                nvl(pageLimit, 10L),
                sortFields,
                sortOrder);

        return GetBookingVisitsResponse.respond200WithApplicationJson(visits);
    }

    @Override
    public GetBookingVisitsForTodayResponse getBookingVisitsForToday(final Long bookingId, final String sortFields, final Order sortOrder) {
        final var today = LocalDate.now();

        final var visits = bookingService.getBookingVisits(
                bookingId,
                today,
                today,
                sortFields,
                sortOrder);

        return GetBookingVisitsForTodayResponse.respond200WithApplicationJson(visits);
    }


    @Override
    public GetKeyworkerByOffenderNoResponse getKeyworkerByOffenderNo(final String offenderNo) {
        final var bookingId = bookingService.getBookingIdByOffenderNo(offenderNo);

        final var keyworker = keyworkerService.getKeyworkerDetailsByBooking(bookingId);
        return GetKeyworkerByOffenderNoResponse.respond200WithApplicationJson(keyworker);
    }

    @Override
    public GetBookingVisitsLastResponse getBookingVisitsLast(final Long bookingId) {
        final var visit = bookingService.getBookingVisitLast(bookingId);

        return GetBookingVisitsLastResponse.respond200WithApplicationJson(visit);
    }

    @Override
    public GetBookingVisitsNextResponse getBookingVisitsNext(final Long bookingId) {
        final var visit = bookingService.getBookingVisitNext(bookingId);

        return GetBookingVisitsNextResponse.respond200WithApplicationJson(visit);
    }

    @Override
    public GetBookingsBookingIdAppointmentsResponse getBookingsBookingIdAppointments(final Long bookingId, final String fromDate, final String toDate, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        final var appointments = bookingService.getBookingAppointments(
                bookingId,
                fromISO8601DateString(fromDate),
                fromISO8601DateString(toDate),
                nvl(pageOffset, 0L),
                nvl(pageLimit, 10L),
                sortFields,
                sortOrder);

        return GetBookingsBookingIdAppointmentsResponse.respond200WithApplicationJson(appointments);
    }

    @Override
    public GetBookingAppointmentsForTodayResponse getBookingAppointmentsForToday(final Long bookingId, final String sortFields, final Order sortOrder) {
        final var today = LocalDate.now();

        final var appointments = bookingService.getBookingAppointments(
                bookingId,
                today,
                today,
                sortFields,
                sortOrder);

        return GetBookingAppointmentsForTodayResponse.respond200WithApplicationJson(appointments);
    }

    @Override
    public GetBookingAppointmentsForThisWeekResponse getBookingAppointmentsForThisWeek(final Long bookingId, final String sortFields, final Order sortOrder) {
        final var fromDate = LocalDate.now();
        final var toDate = fromDate.plusDays(6);

        final var appointments = bookingService.getBookingAppointments(
                bookingId,
                fromDate,
                toDate,
                sortFields,
                sortOrder);

        return GetBookingAppointmentsForThisWeekResponse.respond200WithApplicationJson(appointments);
    }

    @Override
    public GetBookingAppointmentsForNextWeekResponse getBookingAppointmentsForNextWeek(final Long bookingId, final String sortFields, final Order sortOrder) {
        final var fromDate = LocalDate.now().plusDays(7);
        final var toDate = fromDate.plusDays(6);

        final var appointments = bookingService.getBookingAppointments(
                bookingId,
                fromDate,
                toDate,
                sortFields,
                sortOrder);

        return GetBookingAppointmentsForNextWeekResponse.respond200WithApplicationJson(appointments);
    }

    @Override
    @PreAuthorize("#oauth2.hasScope('write')")
    @ProxyUser
    public PostBookingsBookingIdAppointmentsResponse postBookingsBookingIdAppointments(final Long bookingId, final NewAppointment newAppointment) {
        final var createdEvent = bookingService.createBookingAppointment(
                bookingId, authenticationFacade.getCurrentUsername(), newAppointment);

        return PostBookingsBookingIdAppointmentsResponse.respond201WithApplicationJson(createdEvent);
    }


}
