package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.CourtEvent;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CourtEventRepository extends CrudRepository<CourtEvent, Long>, JpaSpecificationExecutor<CourtEvent> {
    Optional<CourtEvent> findByOffenderBooking_BookingIdAndId(final Long bookingId, final Long eventId);
}
