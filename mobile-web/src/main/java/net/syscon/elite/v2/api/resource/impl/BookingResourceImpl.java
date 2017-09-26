package net.syscon.elite.v2.api.resource.impl;

import net.syscon.elite.core.RestResource;
import net.syscon.elite.v2.api.model.PrivilegeSummary;
import net.syscon.elite.v2.api.model.SentenceDetail;
import net.syscon.elite.v2.api.resource.BookingResource;
import net.syscon.elite.v2.service.BookingService;

import javax.ws.rs.Path;

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
    public GetBookingSentenceDetailResponse getBookingSentenceDetail(String bookingId) {
        SentenceDetail sentenceDetail = bookingService.getBookingSentenceDetail(Long.valueOf(bookingId));

        return GetBookingSentenceDetailResponse.respond200WithApplicationJson(sentenceDetail);
    }

    @Override
    public GetBookingIEPSummaryResponse getBookingIEPSummary(String bookingId, boolean withDetails) {
        PrivilegeSummary privilegeSummary = bookingService.getBookingIEPSummary(Long.valueOf(bookingId), withDetails);

        return GetBookingIEPSummaryResponse.respond200WithApplicationJson(privilegeSummary);
    }
}
