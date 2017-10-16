package net.syscon.elite.api.resource.impl;

import static net.syscon.util.ResourceUtils.nvl;

import java.util.List;

import javax.ws.rs.Path;

import org.springframework.beans.factory.annotation.Autowired;

import net.syscon.elite.api.model.Account;
import net.syscon.elite.api.model.Alert;
import net.syscon.elite.api.model.Alias;
import net.syscon.elite.api.model.CaseNote;
import net.syscon.elite.api.model.InmateDetail;
import net.syscon.elite.api.model.NewCaseNote;
import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.model.PrivilegeSummary;
import net.syscon.elite.api.model.SentenceDetail;
import net.syscon.elite.api.model.UpdateCaseNote;
import net.syscon.elite.api.resource.BookingResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.CaseNoteService;
import net.syscon.elite.service.FinanceService;
import net.syscon.elite.service.InmateService;
import net.syscon.elite.service.InmatesAlertService;
import net.syscon.util.MetaDataFactory;

/**
 * Implementation of Booking (/bookings) endpoint.
 */
@RestResource
@Path("/bookings")
public class BookingResourceImpl implements BookingResource {
    private final BookingService bookingService;
    private final InmateService inmateService;
    private final CaseNoteService caseNoteService;
    private final InmatesAlertService inmateAlertService;
    private final FinanceService financeService;

    @Autowired
    public BookingResourceImpl(BookingService bookingService, InmateService inmateService,
            CaseNoteService caseNoteService, InmatesAlertService inmateAlertService, FinanceService financeService) {
        this.bookingService = bookingService;
        this.inmateService = inmateService;
        this.caseNoteService = caseNoteService;
        this.inmateAlertService = inmateAlertService;
        this.financeService = financeService;
    }

    @Override
    public GetOffenderBookingsResponse getOffenderBookings(String query, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        final List<OffenderBooking> allInmates = inmateService.findAllInmates(query, nvl(pageOffset, 0L), nvl(pageLimit, 10L), sortFields, sortOrder);
        return GetOffenderBookingsResponse.respond200WithApplicationJson(allInmates, MetaDataFactory.getTotalRecords(allInmates), nvl(pageOffset, 0L), nvl(pageLimit, 10L));
    }

    @Override
    public GetOffenderBookingResponse getOffenderBooking(Long bookingId) {
        final InmateDetail inmate = inmateService.findInmate(bookingId);
        return GetOffenderBookingResponse.respond200WithApplicationJson(inmate);
    }

    @Override
    public GetOffenderAlertsResponse getOffenderAlerts(Long bookingId, String query, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        final List<Alert> inmateAlerts = inmateAlertService.getInmateAlerts(bookingId, query, sortFields, sortOrder, nvl(pageOffset, 0L), nvl(pageLimit, 10L));
        return GetOffenderAlertsResponse.respond200WithApplicationJson(inmateAlerts, MetaDataFactory.getTotalRecords(inmateAlerts), nvl(pageOffset, 0L), nvl(pageLimit, 10L));
    }

    @Override
    public GetOffenderAlertResponse getOffenderAlert(Long bookingId, Long alertId) {
        return GetOffenderAlertResponse.respond200WithApplicationJson(inmateAlertService.getInmateAlert(bookingId, alertId));
    }

    @Override
    public GetOffenderAliasesResponse getOffenderAliases(Long bookingId, String sortFields, Order sortOrder) {
        final List<Alias> aliases = inmateService.findInmateAliases(bookingId, sortFields, sortOrder);
        return GetOffenderAliasesResponse.respond200WithApplicationJson(aliases);
    }

    @Override
    public GetBookingsBookingIdCaseNotesResponse getBookingsBookingIdCaseNotes(Long bookingId, String query, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        List<CaseNote> caseNotes = caseNoteService.getCaseNotes(bookingId, query, sortFields, sortOrder, nvl(pageOffset, 0L), nvl(pageLimit, 10L));
        return GetBookingsBookingIdCaseNotesResponse.respond200WithApplicationJson(caseNotes, MetaDataFactory.getTotalRecords(caseNotes), nvl(pageOffset, 0L), nvl(pageLimit, 10L));
    }

    @Override
    public GetBookingsBookingIdCaseNotesCaseNoteIdResponse getBookingsBookingIdCaseNotesCaseNoteId(Long bookingId, Long caseNoteId) {
        return GetBookingsBookingIdCaseNotesCaseNoteIdResponse.respond200WithApplicationJson(caseNoteService.getCaseNote(bookingId, caseNoteId));
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
    public PostBookingsBookingIdCaseNotesResponse postBookingsBookingIdCaseNotes(Long bookingId, NewCaseNote body) {
        final CaseNote caseNote = caseNoteService.createCaseNote(bookingId, body);
        return PostBookingsBookingIdCaseNotesResponse.respond201WithApplicationJson(caseNote);
    }

    @Override
    public PUTBookingsBookingIdCaseNotesCaseNoteIdResponse pUTBookingsBookingIdCaseNotesCaseNoteId(Long bookingId, Long caseNoteId, UpdateCaseNote body) {
        final CaseNote caseNote = caseNoteService.updateCaseNote(bookingId, caseNoteId, body.getText());
        return PUTBookingsBookingIdCaseNotesCaseNoteIdResponse.respond201WithApplicationJson(caseNote);
    }
    
  
    @Override
    public GetAccountResponse getAccount(Long bookingId) {
        final Account account = financeService.getAccount(bookingId);
        return GetAccountResponse.respond200WithApplicationJson(account);
    }

}
