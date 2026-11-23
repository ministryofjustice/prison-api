package uk.gov.justice.hmpps.prison.repository.jpa.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSubAccount
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSubAccountId
import java.util.Optional

interface OffenderSubAccountRepository : JpaRepository<OffenderSubAccount, OffenderSubAccountId> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select a from OffenderSubAccount a where a.id = :id")
  fun findForUpdate(@Param("id") id: OffenderSubAccountId): Optional<OffenderSubAccount>
}
