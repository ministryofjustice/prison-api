package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementTypeAndReason;

public interface MovementTypeAndReasonRespository extends CrudRepository<MovementTypeAndReason, MovementTypeAndReason.Pk> {

}
