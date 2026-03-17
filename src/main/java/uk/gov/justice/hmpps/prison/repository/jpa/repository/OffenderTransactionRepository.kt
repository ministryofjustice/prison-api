package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransaction
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionId
import java.time.LocalDate
import java.util.Optional

@Repository
interface OffenderTransactionRepository : CrudRepository<OffenderTransaction, OffenderTransactionId> {

  @Query(value = "SELECT TXN_ID.nextval FROM dual", nativeQuery = true)
  fun getNextTransactionId(): Long

  fun findByClientUniqueRef(clientUniqueRef: String): Optional<OffenderTransaction>

  @Query(
    value = """
        SELECT ot FROM OffenderTransaction ot
           where ot.offenderId = :v_root_offender_id
             and ot.prisonId = :v_agy_loc_id
             and ot.subAccountType = :p_account_type
             and ot.entryDate >= :p_from_date
             and (:p_to_date is null or ot.entryDate <= :p_to_date)
           order by ot.entryDate desc, ot.id.transactionId desc, ot.id.transactionEntrySequence desc
        
        """,
  )
  fun findAccountTransactions(
    @Param("v_root_offender_id") rootOffenderId: Long,
    @Param("v_agy_loc_id") agencyLocationId: String,
    @Param("p_account_type") accountType: String,
    @Param("p_from_date") fromDate: LocalDate,
    @Param("p_to_date") toDate: LocalDate?,
  ): MutableList<OffenderTransaction>
}
