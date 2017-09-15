package net.syscon.elite.v2.repository;

import net.syscon.elite.v2.api.model.SentenceDetail;

import java.util.Optional;

/**
 * Bookings API (v2) repository interface.
 */
public interface BookingRepository {
    Optional<SentenceDetail> getBookingSentenceDetail(Long bookingId);
}
