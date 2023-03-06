package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransaction;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransaction.Pk;

public interface OffenderTransactionRepository extends CrudRepository<OffenderTransaction, Pk> {

    @Query(value = "SELECT TXN_ID.nextval FROM dual d", nativeQuery = true)
    Long getNextTransactionId();
}
