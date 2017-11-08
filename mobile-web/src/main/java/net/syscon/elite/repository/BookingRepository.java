package net.syscon.elite.repository;

import net.syscon.elite.api.model.OffenderRelease;
import net.syscon.elite.api.model.PrivilegeDetail;
import net.syscon.elite.api.model.ScheduledEvent;
import net.syscon.elite.api.model.SentenceDetail;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Bookings API repository interface.
 */
public interface BookingRepository {
    Optional<SentenceDetail> getBookingSentenceDetail(Long bookingId);

    List<PrivilegeDetail> getBookingIEPDetails(Long bookingId);

    boolean verifyBookingAccess(Long bookingId, Set<String> agencyIds);

    Page<ScheduledEvent> getBookingActivities(Long bookingId, LocalDate fromDate, LocalDate toDate, long offset, long limit, String orderByFields, Order order);

    Page<OffenderRelease> getOffenderReleaseSummary(LocalDate toReleaseDate, String query, long offset, long limit, String orderByFields, Order order, Set<String> allowedCaseloadsOnly);
}
