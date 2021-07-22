package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementTypeAndReason;

import java.util.List;

public interface MovementTypeAndReasonRespository extends CrudRepository<MovementTypeAndReason, MovementTypeAndReason.Pk> {
    List<MovementTypeAndReason> findMovementTypeAndReasonByTypeIs(String movementType);
}
