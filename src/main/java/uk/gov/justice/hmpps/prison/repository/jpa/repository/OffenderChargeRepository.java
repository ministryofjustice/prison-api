package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCharge;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCourtCase;

import java.util.List;

public interface OffenderChargeRepository extends CrudRepository<OffenderCharge, Long> {
    List<OffenderCharge> findByOffenderBooking_BookingId(long bookingId);
}
