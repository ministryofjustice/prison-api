package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationOffenceType;

import java.util.List;

public interface AdjudicationOffenceTypeRepository extends CrudRepository<AdjudicationOffenceType, Long> {
    List<AdjudicationOffenceType> findByOffenceCodeIn(List<String> offenceCodes);
}
