package net.syscon.elite.service;

import net.syscon.elite.api.model.*;

import java.util.List;

/**
 * Bookings API service interface.
 */
public interface BookingService {
    SentenceDetail getBookingSentenceDetail(Long bookingId);

    PrivilegeSummary getBookingIEPSummary(Long bookingId, boolean withDetails);

    List<ScheduledEvent> getBookingActivities(Long bookingId);

    void verifyBookingAccess(Long bookingId);

    MainSentence getMainSentence(Long bookingId);

    List<OffenderRelease> getReleases(List<String> offenderNos, long offset, long limit);
}
