package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.KeyDateAdjustment;

import java.util.List;

public interface OffenderKeyDateAdjustmentRepository extends CrudRepository<KeyDateAdjustment, Long> {
    List<KeyDateAdjustment> findAllByOffenderBooking_BookingId(Long bookingId);
    List<KeyDateAdjustment> findAllByOffenderBooking_BookingIdAndActive(Long bookingId, boolean active);
}
