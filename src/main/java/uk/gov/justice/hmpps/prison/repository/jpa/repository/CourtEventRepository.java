package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent;

import java.util.Optional;

public interface CourtEventRepository extends CrudRepository<CourtEvent, Long>, JpaSpecificationExecutor<CourtEvent> {
    Optional<CourtEvent> findByOffenderBooking_BookingIdAndId(final Long bookingId, final Long eventId);
}
