package net.syscon.elite.v2.api.resource.impl;

import net.syscon.elite.core.RestResource;
import net.syscon.elite.v2.api.model.CaseNote;
import net.syscon.elite.v2.api.model.NewCaseNote;
import net.syscon.elite.v2.api.model.PrivilegeSummary;
import net.syscon.elite.v2.api.model.SentenceDetail;
import net.syscon.elite.v2.api.model.UpdateCaseNote;
import net.syscon.elite.v2.api.resource.BookingResource;
import net.syscon.elite.v2.api.support.Order;
import net.syscon.elite.v2.service.BookingService;

import javax.ws.rs.Path;
import java.util.List;

/**
 * Implementation of Booking (/bookings) endpoint.
 */
@RestResource(value = "bookingResourceImplV2")
@Path("/v2/bookings")
public class BookingResourceImpl implements BookingResource {
    private final BookingService bookingService;

    public BookingResourceImpl(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Override
    public GetBookingsResponse getBookings(String query, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        return null;
    }

    @Override
    public GetOffenderBookingResponse getOffenderBooking(Long bookingId) {
        return null;
    }

    @Override
    public GetOffenderAlertsResponse getOffenderAlerts(Long bookingId, String query, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        return null;
    }

    @Override
    public GetOffenderAlertResponse getOffenderAlert(Long bookingId, String alertId) {
        return null;
    }

    @Override
    public GetOffenderAliasesResponse getOffenderAliases(Long bookingId, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        return null;
    }

    @Override
    public BookingResource.GetOffenderCaseNotesResponse getOffenderCaseNotes(Long bookingId, List<CaseNote> body, String query, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        return null;
    }

    @Override
    public BookingResource.GetOffenderCaseNoteResponse getOffenderCaseNote(Long bookingId, String caseNoteId) {
        return null;
    }

    @Override
    public GetBookingSentenceDetailResponse getBookingSentenceDetail(Long bookingId) {
        SentenceDetail sentenceDetail = bookingService.getBookingSentenceDetail(bookingId);

        return GetBookingSentenceDetailResponse.respond200WithApplicationJson(sentenceDetail);
    }

    @Override
    public GetBookingIEPSummaryResponse getBookingIEPSummary(String bookingId, boolean withDetails) {
        PrivilegeSummary privilegeSummary = bookingService.getBookingIEPSummary(Long.valueOf(bookingId), withDetails);

        return GetBookingIEPSummaryResponse.respond200WithApplicationJson(privilegeSummary);
    }

    @Override
    public BookingResource.GetOffenderCaseNotesResponse getOffenderCaseNotes_1(Long bookingId, NewCaseNote body) {
        return null;
    }

    @Override
    public BookingResource.GetOffenderCaseNoteResponse getOffenderCaseNote_2(Long bookingId, String caseNoteId, UpdateCaseNote body) {
        return null;
    }

    /**
     * 	@Autowired
    private InmateService inmateService;

     @Autowired
     private CaseNoteService caseNoteService;

     @Autowired
     private InmatesAlertService inmateAlertService;

     @Override
     public GetBookingResponse getBooking(String query, String orderBy, Order order, int offset, int limit)
     throws Exception {
     final List<InmatesSummary> inmates = inmateService.findAllInmates(query, offset, limit, orderBy, order);
     InmateSummaries inmateSummaries = new InmateSummaries(inmates, MetaDataFactory.createMetaData(limit, offset, inmates));
     return GetBookingResponse.withJsonOK(inmateSummaries);
     }

     @Override
     public GetBookingByBookingIdResponse getBookingByBookingId(String bookingId) throws Exception {
     InmateDetails inmate = inmateService.findInmate(Long.valueOf(bookingId));
     return GetBookingByBookingIdResponse.withJsonOK(inmate);
     }

     @Override
     public GetBookingByBookingIdCaseNotesResponse getBookingByBookingIdCaseNotes(String bookingId, String query,
     String orderBy, Order order, int offset, int limit) throws Exception {

     List<CaseNote> caseNotes = caseNoteService.getCaseNotes(bookingId, query, orderBy, order, offset, limit);
     CaseNotes cases = new CaseNotes(caseNotes, MetaDataFactory.createMetaData(limit, offset, caseNotes));
     return GetBookingByBookingIdCaseNotesResponse.withJsonOK(cases);
     }

     @Override
     public PostBookingByBookingIdCaseNotesResponse postBookingByBookingIdCaseNotes(String bookingId, NewCaseNote newCaseNote) throws Exception {
     CaseNote caseNote = caseNoteService.createCaseNote(bookingId, newCaseNote);
     return PostBookingByBookingIdCaseNotesResponse.withJsonCreated(caseNote);
     }

     @Override
     public GetBookingByBookingIdCaseNotesByCaseNoteIdResponse getBookingByBookingIdCaseNotesByCaseNoteId(
     String bookingId, String caseNoteId) throws Exception {
     final CaseNote caseNote = caseNoteService.getCaseNote(bookingId, Long.valueOf(caseNoteId));
     return GetBookingByBookingIdCaseNotesByCaseNoteIdResponse.withJsonOK(caseNote);
     }

     @Override
     public GetBookingByBookingIdAlertsResponse getBookingByBookingIdAlerts(String bookingId, String orderBy,
     Order order, String query, int offset, int limit) throws Exception {
     List<Alert> alerts = inmateAlertService.getInmateAlerts(bookingId, query, orderBy, order, offset, limit);
     Alerts alertsObj = new Alerts(alerts, MetaDataFactory.createMetaData(limit, offset, alerts));
     return GetBookingByBookingIdAlertsResponse.withJsonOK(alertsObj);
     }

     @Override
     public GetBookingByBookingIdAlertsByAlertIdResponse getBookingByBookingIdAlertsByAlertId(String bookingId,
     String alertId) throws Exception {
     Alert alert = inmateAlertService.getInmateAlert(bookingId, alertId);
     return GetBookingByBookingIdAlertsByAlertIdResponse.withJsonOK(alert);
     }

     @Override
     public GetBookingByBookingIdAliasesResponse getBookingByBookingIdAliases(String bookingId, String orderBy,
     Order order) throws Exception {
     try {
     List<Alias> aliases = inmateService.findInmateAliases(Long.valueOf(bookingId), orderBy, order);
     return GetBookingByBookingIdAliasesResponse.withJsonOK(aliases);
     } catch (final EmptyResultDataAccessException ex) {
     final String message = String.format("Booking \"%s\" not found", bookingId);
     logger.info(message);
     final HttpStatus httpStatus = new HttpStatus("404", "404", message, message, "");
     return GetBookingByBookingIdAliasesResponse.withJsonNotFound(httpStatus);
     }
     }

     @Override
     public PutBookingByBookingIdCaseNotesByCaseNoteIdResponse putBookingByBookingIdCaseNotesByCaseNoteId(
     String bookingId, String caseNoteId, UpdateCaseNote entity) throws Exception {
     CaseNote caseNote = caseNoteService.updateCaseNote(bookingId, Long.valueOf(caseNoteId), entity.getText());
     return PutBookingByBookingIdCaseNotesByCaseNoteIdResponse.withJsonCreated(caseNote);
     }
     */
}
