package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicHearingResult;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicHearingResult.PK;

public interface OicHearingResultRepository extends CrudRepository<OicHearingResult, PK> {
}
