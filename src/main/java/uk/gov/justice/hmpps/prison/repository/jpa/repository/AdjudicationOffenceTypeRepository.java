package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationOffenceType;

public interface AdjudicationOffenceTypeRepository extends CrudRepository<AdjudicationOffenceType, Long> {
    AdjudicationOffenceType findByOffenceCode(String offenceCode);
}
