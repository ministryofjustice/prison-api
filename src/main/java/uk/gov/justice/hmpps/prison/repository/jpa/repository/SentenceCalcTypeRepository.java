package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCharge;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalcType;

public interface SentenceCalcTypeRepository extends CrudRepository<SentenceCalcType, SentenceCalcType.PK> {
}
