package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementTypeAndReason;

import java.util.List;
import java.util.Optional;

public interface MovementTypeAndReasonRepository extends CrudRepository<MovementTypeAndReason, MovementTypeAndReason.Pk> {
    List<MovementTypeAndReason> findMovementTypeAndReasonById_Type(String type);

    @Query("from MovementTypeAndReason m where m.type = :type and m.reasonCode = :reasonCode")
    Optional<MovementTypeAndReason> getMovementTypeAndReason(String type, String reasonCode);
}
