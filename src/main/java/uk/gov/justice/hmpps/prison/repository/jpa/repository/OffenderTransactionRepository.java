package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransaction;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransaction.Pk;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OffenderTransactionRepository extends CrudRepository<OffenderTransaction, Pk> {

    @Query(value = "SELECT TXN_ID.nextval FROM dual d", nativeQuery = true)
    Long getNextTransactionId();

    Optional<OffenderTransaction> findByClientUniqueRef(String clientUniqueRef);

    @Query(value = """
        SELECT ot FROM OffenderTransaction ot
           where ot.offenderId = :v_root_offender_id
             and ot.prisonId = :v_agy_loc_id
             and ot.subAccountType = :p_account_type
             and ot.entryDate >= :p_from_date
             and (:p_to_date is null or ot.entryDate <= :p_to_date)
           order by ot.entryDate desc, ot.transactionId desc, ot.transactionEntrySequence desc
        """)
    List<OffenderTransaction> findAccountTransactions(
        @Param("v_root_offender_id") Long rootOffenderId,
        @Param("v_agy_loc_id") String agencyLocationId,
        @Param("p_account_type") String accountType,
        @Param("p_from_date") LocalDate fromDate,
        @Param("p_to_date") LocalDate toDate);
}
