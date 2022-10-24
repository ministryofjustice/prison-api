package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSentenceCharge;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalcType;

public interface OffenderSentenceChargeRepository extends CrudRepository<OffenderSentenceCharge, OffenderSentenceCharge.PK> {
}
