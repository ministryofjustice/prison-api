package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderRestriction;

import java.util.List;

public interface OffenderRestrictionRepository extends CrudRepository<OffenderRestriction, Long> {
    List<OffenderRestriction> findByOffenderBookingIdOrderByStartDateDesc(Long bookingId);
}
