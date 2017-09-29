package net.syscon.elite.service;

import net.syscon.elite.api.model.PrivilegeSummary;
import net.syscon.elite.api.model.SentenceDetail;

/**
 * Bookings API (v2) service interface.
 */
public interface BookingService {
    SentenceDetail getBookingSentenceDetail(Long bookingId);

    PrivilegeSummary getBookingIEPSummary(Long bookingId, boolean withDetails);
}
