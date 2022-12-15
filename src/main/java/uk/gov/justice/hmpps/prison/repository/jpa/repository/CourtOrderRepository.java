package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtOrder;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceTerm;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceTerm.PK;

public interface CourtOrderRepository extends CrudRepository<CourtOrder, Long> {
}
