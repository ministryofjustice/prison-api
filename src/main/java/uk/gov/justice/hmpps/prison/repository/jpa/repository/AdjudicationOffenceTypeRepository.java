package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AdjudicationOffenceType;

import java.util.List;

public interface AdjudicationOffenceTypeRepository extends CrudRepository<AdjudicationOffenceType, Long> {
    @Query("Select aot from AdjudicationOffenceType aot where aot.offenceCode in :offenceCodes")
    List<AdjudicationOffenceType> findByOffenceCodes(List<String> offenceCodes);
}
