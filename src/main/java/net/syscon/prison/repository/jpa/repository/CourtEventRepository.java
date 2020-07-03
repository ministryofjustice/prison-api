package net.syscon.prison.repository.jpa.repository;

import net.syscon.prison.repository.jpa.model.CourtEvent;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CourtEventRepository extends CrudRepository<CourtEvent, Long>, JpaSpecificationExecutor<CourtEvent> {
    Optional<CourtEvent> findByOffenderBooking_BookingIdAndId(final Long bookingId, final Long eventId);
}
