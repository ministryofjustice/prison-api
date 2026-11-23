package uk.gov.justice.hmpps.prison.repository.jpa.repository

import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTrustAccount
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTrustAccountId
import java.util.Optional

interface OffenderTrustAccountRepository : JpaRepository<OffenderTrustAccount, OffenderTrustAccountId> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @QueryHints(value = [QueryHint(name = "jakarta.persistence.lock.timeout", value = "1000")])
  @Query("select offenderTrustAccount from OffenderTrustAccount offenderTrustAccount where offenderTrustAccount.id = :id")
  fun findByIdWithLock(id: OffenderTrustAccountId): Optional<OffenderTrustAccount>
}
