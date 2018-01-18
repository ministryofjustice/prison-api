package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.resource.BookingResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.*;

import javax.ws.rs.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    public BookingResourceImpl(AuthenticationFacade authenticationFacade, BookingService bookingService,
                               InmateService inmateService, CaseNoteService caseNoteService,
                               InmateAlertService inmateAlertService, FinanceService financeService,
                               ContactService contactService, AdjudicationService adjudicationService) {
        this.authenticationFacade = authenticationFacade;
        this.bookingService = bookingService;
        this.inmateService = inmateService;
        this.caseNoteService = caseNoteService;
        this.inmateAlertService = inmateAlertService;
        this.financeService = financeService;
        this.contactService = contactService;
        this.adjudicationService = adjudicationService;
    }

    @Override
    public GetOffenderBookingsResponse getOffenderBookings(String query, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        Page<OffenderBooking> allInmates = inmateService.findAllInmates(
                authenticationFacade.getCurrentUsername(),
                query,
                sortFields,
                sortOrder,
                nvl(pageOffset, 0L),
                nvl(pageLimit, 10L));

        return GetOffenderBookingsResponse.respond200WithApplicationJson(allInmates);
    }

    @Override
    public GetOffenderBookingResponse getOffenderBooking(Long bookingId) {
        InmateDetail inmate = inmateService.findInmate(bookingId, authenticationFacade.getCurrentUsername());

        return GetOffenderBookingResponse.respond200WithApplicationJson(inmate);
    }

    @Override
    public GetBookingActivitiesResponse getBookingActivities(Long bookingId, String fromDate, String toDate, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        Page<ScheduledEvent> activities =  bookingService.getBookingActivities(
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
    public GetBookingActivitiesForTodayResponse getBookingActivitiesForToday(Long bookingId, String sortFields, Order sortOrder) {
        LocalDate today = LocalDate.now();

        List<ScheduledEvent> activities =  bookingService.getBookingActivities(
                bookingId,
                today,
                today,
                sortFields,
                sortOrder);

        return GetBookingActivitiesForTodayResponse.respond200WithApplicationJson(activities);
    }

    @Override
    public GetOffenderAlertsResponse getOffenderAlerts(Long bookingId, String query, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        Page<Alert> inmateAlerts = inmateAlertService.getInmateAlerts(
                bookingId,
                query,
                sortFields,
                sortOrder,
                nvl(pageOffset, 0L),
                nvl(pageLimit, 10L));

        return GetOffenderAlertsResponse.respond200WithApplicationJson(inmateAlerts);
    }

    @Override
    public GetOffenderAlertResponse getOffenderAlert(Long bookingId, Long alertId) {
        Alert inmateAlert = inmateAlertService.getInmateAlert(bookingId, alertId);

        return GetOffenderAlertResponse.respond200WithApplicationJson(inmateAlert);
    }

    @Override
    public GetOffenderAliasesResponse getOffenderAliases(Long bookingId, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        Page<Alias> aliases = inmateService.findInmateAliases(
                bookingId,
                sortFields,
                sortOrder,
                nvl(pageOffset, 0L),
                nvl(pageLimit, 5L));

        return GetOffenderAliasesResponse.respond200WithApplicationJson(aliases);
    }

    @Override
    public GetAssessmentByCodeResponse getAssessmentByCode(Long bookingId, String assessmentCode) {
        Optional<Assessment> inmateAssessmentByCode = inmateService.getInmateAssessmentByCode(bookingId, assessmentCode);

        if (!inmateAssessmentByCode.isPresent()) {
            throw EntityNotFoundException.withMessage("Offender does not have a [" + assessmentCode + "] assessment on record.");
        }

        return GetAssessmentByCodeResponse.respond200WithApplicationJson(inmateAssessmentByCode.get());
    }

    @Override
    public GetOffenderCaseNotesResponse getOffenderCaseNotes(Long bookingId, String from, String to,
            String query, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        Page<CaseNote> caseNotes = caseNoteService.getCaseNotes(
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
    public GetOffenderCaseNoteResponse getOffenderCaseNote(Long bookingId, Long caseNoteId) {
        CaseNote caseNote = caseNoteService.getCaseNote(bookingId, caseNoteId);

        return GetOffenderCaseNoteResponse.respond200WithApplicationJson(caseNote);
    }

    @Override
    public GetBookingIEPSummaryResponse getBookingIEPSummary(Long bookingId, boolean withDetails) {
        PrivilegeSummary privilegeSummary = bookingService.getBookingIEPSummary(bookingId, withDetails);

        return GetBookingIEPSummaryResponse.respond200WithApplicationJson(privilegeSummary);
    }

    @Override
    public GetBookingSentenceDetailResponse getBookingSentenceDetail(Long bookingId) {
        SentenceDetail sentenceDetail = bookingService.getBookingSentenceDetail(bookingId);

        return GetBookingSentenceDetailResponse.respond200WithApplicationJson(sentenceDetail);
    }

    @Override
    public CreateOffenderCaseNoteResponse createOffenderCaseNote(Long bookingId, NewCaseNote body) {
        CaseNote caseNote = caseNoteService.createCaseNote(bookingId, body, authenticationFacade.getCurrentUsername());

        return CreateOffenderCaseNoteResponse.respond201WithApplicationJson(caseNote);
    }

    @Override
    public UpdateOffenderCaseNoteResponse updateOffenderCaseNote(Long bookingId, Long caseNoteId, UpdateCaseNote body) {
        CaseNote caseNote = caseNoteService.updateCaseNote(
                bookingId, caseNoteId, authenticationFacade.getCurrentUsername(), body.getText());

        return UpdateOffenderCaseNoteResponse.respond201WithApplicationJson(caseNote);
    }

    @Override
    public GetBalancesResponse getBalances(Long bookingId) {
        Account account = financeService.getBalances(bookingId);

        return GetBalancesResponse.respond200WithApplicationJson(account);
    }

    @Override
    public GetMainOffenceResponse getMainOffence(Long bookingId) {
        List<OffenceDetail> offenceDetails = bookingService.getMainOffenceDetails(bookingId);

        return GetMainOffenceResponse.respond200WithApplicationJson(offenceDetails);
    }

    @Override
    public BookingResource.GetRelationshipsResponse getRelationships(Long bookingId, String relationshipType) {
        List<Contact> relationships = contactService.getRelationships(bookingId, relationshipType);
        return BookingResource.GetRelationshipsResponse.respond200WithApplicationJson(relationships);
    }

    @Override
    public GetRelationshipsByOffenderNoResponse getRelationshipsByOffenderNo(String offenderNo, String relationshipType) {
        List<Contact> relationships = contactService.getRelationshipsByOffenderNo(offenderNo, relationshipType);
        return GetRelationshipsByOffenderNoResponse.respond200WithApplicationJson(relationships);
    }

    @Override
    public CreateRelationshipResponse createRelationship(Long bookingId, OffenderRelationship relationshipDetail) {
        final Contact relationship = contactService.createRelationship(bookingId, relationshipDetail);
        return CreateRelationshipResponse.respond201WithApplicationJson(relationship);
    }

    @Override
    public CreateRelationshipByOffenderNoResponse createRelationshipByOffenderNo(String offenderNo, OffenderRelationship relationshipDetail) {
        final Contact relationship = contactService.createRelationshipByOffenderNo(offenderNo, relationshipDetail);
        return CreateRelationshipByOffenderNoResponse.respond201WithApplicationJson(relationship);
    }

    @Override
    public GetEventsTodayResponse getEventsToday(Long bookingId) {
        List<ScheduledEvent> scheduledEvents = bookingService.getEventsToday(bookingId);

        return GetEventsTodayResponse.respond200WithApplicationJson(scheduledEvents);
    }

    @Override
    public GetEventsThisWeekResponse getEventsThisWeek(Long bookingId) {
        List<ScheduledEvent> scheduledEvents = bookingService.getEventsThisWeek(bookingId);

        return GetEventsThisWeekResponse.respond200WithApplicationJson(scheduledEvents);
    }

    @Override
    public GetEventsNextWeekResponse getEventsNextWeek(Long bookingId) {
        List<ScheduledEvent> scheduledEvents = bookingService.getEventsNextWeek(bookingId);

        return GetEventsNextWeekResponse.respond200WithApplicationJson(scheduledEvents);
    }

    @Override
    public GetContactsResponse getContacts(Long bookingId) {
        final ContactDetail contacts = contactService.getContacts(bookingId);

        return GetContactsResponse.respond200WithApplicationJson(contacts);
    }

    @Override
    public GetCaseNoteCountResponse getCaseNoteCount(Long bookingId, String type, String subType, String fromDate, String toDate) {
        CaseNoteCount caseNoteCount = caseNoteService.getCaseNoteCount(
                bookingId,
                type,
                subType,
                fromISO8601DateString(fromDate),
                fromISO8601DateString(toDate));

        return GetCaseNoteCountResponse.respond200WithApplicationJson(caseNoteCount);
    }

    @Override
    public GetAdjudicationsResponse getAdjudications(Long bookingId, String awardCutoffDate, String adjudicationCutoffDate) {
        final AdjudicationDetail adjudicationDetail = adjudicationService.getAdjudications(bookingId,
                fromISO8601DateString(awardCutoffDate), fromISO8601DateString(adjudicationCutoffDate));

        return GetAdjudicationsResponse.respond200WithApplicationJson(adjudicationDetail);
    }

    @Override
    public GetBookingVisitsResponse getBookingVisits(Long bookingId, String fromDate, String toDate, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        Page<ScheduledEvent> visits =  bookingService.getBookingVisits(
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
    public GetBookingVisitsForTodayResponse getBookingVisitsForToday(Long bookingId, String sortFields, Order sortOrder) {
        LocalDate today = LocalDate.now();

        List<ScheduledEvent> visits =  bookingService.getBookingVisits(
                bookingId,
                today,
                today,
                sortFields,
                sortOrder);

        return GetBookingVisitsForTodayResponse.respond200WithApplicationJson(visits);
    }

    @Override
    public GetBookingVisitsLastResponse getBookingVisitsLast(Long bookingId) {
        Visit visit = bookingService.getBookingVisitLast(bookingId);

        return GetBookingVisitsLastResponse.respond200WithApplicationJson(visit);
    }

    @Override
    public GetBookingsBookingIdAppointmentsResponse getBookingsBookingIdAppointments(Long bookingId, String fromDate, String toDate, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        Page<ScheduledEvent> appointments =  bookingService.getBookingAppointments(
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
    public GetBookingAppointmentsForTodayResponse getBookingAppointmentsForToday(Long bookingId, String sortFields, Order sortOrder) {
        LocalDate today = LocalDate.now();

        List<ScheduledEvent> appointments =  bookingService.getBookingAppointments(
                bookingId,
                today,
                today,
                sortFields,
                sortOrder);

        return GetBookingAppointmentsForTodayResponse.respond200WithApplicationJson(appointments);
    }

    @Override
    public GetBookingAppointmentsForThisWeekResponse getBookingAppointmentsForThisWeek(Long bookingId, String sortFields, Order sortOrder) {
        LocalDate fromDate = LocalDate.now();
        LocalDate toDate = fromDate.plusDays(6);

        List<ScheduledEvent> appointments =  bookingService.getBookingAppointments(
                bookingId,
                fromDate,
                toDate,
                sortFields,
                sortOrder);

        return GetBookingAppointmentsForThisWeekResponse.respond200WithApplicationJson(appointments);
    }

    @Override
    public GetBookingAppointmentsForNextWeekResponse getBookingAppointmentsForNextWeek(Long bookingId, String sortFields, Order sortOrder) {
        LocalDate fromDate = LocalDate.now().plusDays(7);
        LocalDate toDate = fromDate.plusDays(6);

        List<ScheduledEvent> appointments =  bookingService.getBookingAppointments(
                bookingId,
                fromDate,
                toDate,
                sortFields,
                sortOrder);

        return GetBookingAppointmentsForNextWeekResponse.respond200WithApplicationJson(appointments);
    }

    @Override
    public PostBookingsBookingIdAppointmentsResponse postBookingsBookingIdAppointments(Long bookingId, NewAppointment newAppointment) {
        ScheduledEvent createdEvent = bookingService.createBookingAppointment(
                bookingId, authenticationFacade.getCurrentUsername(), newAppointment);

        return PostBookingsBookingIdAppointmentsResponse.respond201WithApplicationJson(createdEvent);
    }
}
