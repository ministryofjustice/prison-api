package net.syscon.elite.service;

import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;

import java.time.LocalDate;

/**
 * Bookings API service interface.
 */
public interface BookingService {
    SentenceDetail getBookingSentenceDetail(Long bookingId);

    PrivilegeSummary getBookingIEPSummary(Long bookingId, boolean withDetails);

    Page<ScheduledEvent> getBookingActivities(Long bookingId, LocalDate fromDate, LocalDate toDate, long offset, long limit, String orderByFields, Order order);

    void verifyBookingAccess(Long bookingId);

    MainSentence getMainSentence(Long bookingId);

    Page<OffenderRelease> getOffenderReleaseSummary(LocalDate fromReleaseDate, String query, long offset, long limit, String orderByFields, Order order, boolean allowedCaseloadsOnly);
}
