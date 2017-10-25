package net.syscon.elite.repository;

import net.syscon.elite.api.model.PrivilegeDetail;
import net.syscon.elite.api.model.ScheduledEvent;
import net.syscon.elite.api.model.SentenceDetail;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.repository.mapping.Page;

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

    Page<ScheduledEvent> getBookingActivities(Long bookingId, long offset, long limit, String orderByFields, Order order);
}
