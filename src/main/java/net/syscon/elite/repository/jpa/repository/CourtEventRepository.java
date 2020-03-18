package net.syscon.elite.repository.jpa.repository;

import net.syscon.elite.repository.jpa.model.CourtEvent;
import net.syscon.elite.repository.jpa.model.OffenderBooking;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CourtEventRepository extends CrudRepository<CourtEvent, Long> {
    List<CourtEvent> findByOffenderBooking_BookingId(final Long bookingId);
}
