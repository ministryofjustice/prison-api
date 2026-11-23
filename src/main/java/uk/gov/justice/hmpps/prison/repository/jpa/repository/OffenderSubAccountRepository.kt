package uk.gov.justice.hmpps.prison.repository.jpa.repository

import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSubAccount
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSubAccountId
import java.util.Optional

interface OffenderSubAccountRepository : JpaRepository<OffenderSubAccount, OffenderSubAccountId> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @QueryHints(value = [QueryHint(name = "jakarta.persistence.lock.timeout", value = "1000")])
  @Query("select offenderSubAccount from OffenderSubAccount offenderSubAccount where offenderSubAccount.id = :id")
  fun findByIdWithLock(id: OffenderSubAccountId): Optional<OffenderSubAccount>
}
