package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalcType;

import java.util.List;

public interface SentenceCalcTypeRepository extends CrudRepository<SentenceCalcType, SentenceCalcType.PK> {

    @Cacheable("calculable_sentence_calculation_types")
    List<SentenceCalcType> findByCalculationTypeIsNotAndCategoryNotContaining(String excludeCalcType, String excludeCategoryContains);

}
