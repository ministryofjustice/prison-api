package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.repository.CrudRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory

interface OffenderTransactionHistoryRepository : CrudRepository<OffenderTransactionHistory, OffenderTransactionHistory.Pk> {
  fun findByOffenderNomsId(offenderNo: String): List<OffenderTransactionHistory>
}
