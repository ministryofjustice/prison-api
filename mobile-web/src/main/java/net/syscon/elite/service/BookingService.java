package net.syscon.elite.service;

import net.syscon.elite.api.model.PrivilegeSummary;
import net.syscon.elite.api.model.ScheduledEvent;
import net.syscon.elite.api.model.SentenceDetail;

import java.util.List;

/**
 * Bookings API service interface.
 */
public interface BookingService {
    SentenceDetail getBookingSentenceDetail(Long bookingId);

    PrivilegeSummary getBookingIEPSummary(Long bookingId, boolean withDetails);

    List<ScheduledEvent> getBookingActivities(Long bookingId);
}
