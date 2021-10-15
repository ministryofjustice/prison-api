package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Visit;

import java.util.List;

public interface VisitRepository extends CrudRepository<Visit, Long> {
    List<Visit> findByOffenderBooking(OffenderBooking booking);
}
