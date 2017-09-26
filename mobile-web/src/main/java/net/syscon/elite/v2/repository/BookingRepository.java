package net.syscon.elite.v2.repository;

import net.syscon.elite.v2.api.model.PrivilegeDetail;
import net.syscon.elite.v2.api.model.SentenceDetail;

import java.util.List;
import java.util.Optional;

/**
 * Bookings API (v2) repository interface.
 */
public interface BookingRepository {
    Optional<SentenceDetail> getBookingSentenceDetail(Long bookingId);

    List<PrivilegeDetail> getBookingIEPDetails(Long bookingId);
}
