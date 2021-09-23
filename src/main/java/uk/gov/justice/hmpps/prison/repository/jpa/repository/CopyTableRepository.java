package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CopyTable;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CopyTable.PK;

import java.util.List;


public interface CopyTableRepository extends CrudRepository<CopyTable, PK> {

    List<CopyTable> findByOperationCodeAndMovementTypeAndActiveAndExpiryDateIsNull(final String operationCode, final String movementType, final boolean active);
}
