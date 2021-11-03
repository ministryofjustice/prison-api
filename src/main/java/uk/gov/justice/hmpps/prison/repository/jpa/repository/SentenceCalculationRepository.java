package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalculation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalculation.PK;

public interface SentenceCalculationRepository extends CrudRepository<SentenceCalculation, PK> {
}