package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceTerm;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceTerm.PK;

public interface SentenceTermRepository extends CrudRepository<SentenceTerm, PK> {
}
