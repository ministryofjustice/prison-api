package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSentence;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSentence.PK;

import java.util.List;
import java.util.Set;

public interface OffenderSentenceRepository extends CrudRepository<OffenderSentence, PK> {
    @EntityGraph(value = "sentence-entity-graph")
    List<OffenderSentence> findByOffenderBooking_BookingId_AndCalculationType_CalculationTypeNotLikeAndCalculationType_CategoryNot(
        Long bookingId, String calculationType, String category);

    @EntityGraph(value = "sentence-entity-graph")
    List<OffenderSentence> findByOffenderBooking_BookingIdInAndCalculationType_CalculationTypeNotLikeAndCalculationType_CategoryNot(
        Set<Long> bookingIds, String calculationType, String category);
}
