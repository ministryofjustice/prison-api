package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderNonAssociationDetail;

import java.util.List;

public interface OffenderNonAssociationDetailRepository extends CrudRepository<OffenderNonAssociationDetail, OffenderNonAssociationDetail.Pk> {

    @EntityGraph(type = EntityGraphType.FETCH, value = "non-association-details")
    List<OffenderNonAssociationDetail> findAllByOffenderBooking_Offender_NomsIdOrderByEffectiveDateAsc(final String nomisId);
}
