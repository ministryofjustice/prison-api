package net.syscon.elite.v2.service;

import net.syscon.elite.v2.api.model.PrivilegeSummary;
import net.syscon.elite.v2.api.model.SentenceDetail;

/**
 * Bookings API (v2) service interface.
 */
public interface BookingService {
    SentenceDetail getBookingSentenceDetail(Long bookingId);

    PrivilegeSummary getBookingIEPSummary(Long bookingId, boolean withDetails);
}
