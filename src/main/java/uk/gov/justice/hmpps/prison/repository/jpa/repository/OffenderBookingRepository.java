package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;

import java.util.Optional;

public interface OffenderBookingRepository extends CrudRepository<OffenderBooking, Long> {
    Optional<OffenderBooking> findByOffenderNomsIdAndActiveFlag(String nomsId, String activeFlag);
    Optional<OffenderBooking> findByOffenderNomsIdAndBookingSequence(String nomsId, Integer bookingSequence);
}
