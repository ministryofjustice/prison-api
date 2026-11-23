package uk.gov.justice.hmpps.prison.repository.jpa.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTrustAccount
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTrustAccountId
import java.util.Optional

interface OffenderTrustAccountRepository : JpaRepository<OffenderTrustAccount, OffenderTrustAccountId> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select a from OffenderTrustAccount a where a.id = :id")
  fun findForUpdate(@Param("id") id: OffenderTrustAccountId): Optional<OffenderTrustAccount>
}
