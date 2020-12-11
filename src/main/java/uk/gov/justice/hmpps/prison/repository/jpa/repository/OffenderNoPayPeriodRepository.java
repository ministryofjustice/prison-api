package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderNoPayPeriod;

import java.util.List;

public interface OffenderNoPayPeriodRepository extends CrudRepository<OffenderNoPayPeriod, Long> {

    List<OffenderNoPayPeriod> findAllByBookingId(Long bookingId);
}
