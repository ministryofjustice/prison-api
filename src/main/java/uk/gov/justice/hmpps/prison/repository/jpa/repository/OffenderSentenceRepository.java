package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSentence;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSentence.PK;

import java.util.List;

public interface OffenderSentenceRepository extends CrudRepository<OffenderSentence, PK> {
    @EntityGraph(value = "sentence-entity-graph")
    List<OffenderSentence> findByOffenderBooking_BookingId_AndCalculationType_CalculationTypeNotLike(Long bookingId, String calculationType);
}
