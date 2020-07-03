package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;

import java.util.List;

public interface OffenderBookingRepository extends CrudRepository<OffenderBooking, Long> {
    List<OffenderBooking> findByOffenderNomsIdAndActiveFlag(String nomsId, String activeFlag);
}
