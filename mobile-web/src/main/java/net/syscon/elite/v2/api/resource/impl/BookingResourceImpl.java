package net.syscon.elite.v2.api.resource.impl;

import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.CaseNoteService;
import net.syscon.elite.service.InmateService;
import net.syscon.elite.service.InmatesAlertService;
import net.syscon.elite.v2.api.model.*;
import net.syscon.elite.v2.api.resource.BookingResource;
import net.syscon.elite.v2.api.support.Order;
import net.syscon.elite.v2.service.BookingService;
import net.syscon.util.MetaDataFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.Path;
import java.util.List;

import static net.syscon.util.ResourceUtils.nvl;

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

    @Autowired
    public BookingResourceImpl(BookingService bookingService, InmateService inmateService, CaseNoteService caseNoteService, InmatesAlertService inmateAlertService) {
        this.bookingService = bookingService;
        this.inmateService = inmateService;
        this.caseNoteService = caseNoteService;
        this.inmateAlertService = inmateAlertService;
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
    public GetBookingSentenceDetailResponse getBookingSentenceDetail(Long bookingId) {
        SentenceDetail sentenceDetail = bookingService.getBookingSentenceDetail(bookingId);
        return GetBookingSentenceDetailResponse.respond200WithApplicationJson(sentenceDetail);
    }

    @Override
    public GetBookingIEPSummaryResponse getBookingIEPSummary(Long bookingId, boolean withDetails) {
        PrivilegeSummary privilegeSummary = bookingService.getBookingIEPSummary(bookingId, withDetails);
        return GetBookingIEPSummaryResponse.respond200WithApplicationJson(privilegeSummary);
    }

    @Override
    public BookingResource.GetOffenderCaseNotesResponse getOffenderCaseNotes(Long bookingId, List<CaseNote> body, String query, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        List<CaseNote> caseNotes = caseNoteService.getCaseNotes(bookingId, query, sortFields, sortOrder, nvl(pageOffset, 0L), nvl(pageLimit, 10L));
        return null;
    }

    @Override
    public BookingResource.GetOffenderCaseNoteResponse getOffenderCaseNote(Long bookingId, Long caseNoteId) {
        caseNoteService.getCaseNote(bookingId, caseNoteId);
        return null;
    }

    @Override
    public BookingResource.GetOffenderCaseNotesResponse getOffenderCaseNotes_1(Long bookingId, NewCaseNote newCaseNote) {
        final CaseNote caseNote = caseNoteService.createCaseNote(bookingId, newCaseNote);
        return null;
    }

    @Override
    public BookingResource.GetOffenderCaseNoteResponse getOffenderCaseNote_2(Long bookingId, Long caseNoteId, UpdateCaseNote updateCaseNote) {
        final CaseNote caseNote = caseNoteService.updateCaseNote(bookingId, caseNoteId, updateCaseNote.getText());
        return null;
    }

}
