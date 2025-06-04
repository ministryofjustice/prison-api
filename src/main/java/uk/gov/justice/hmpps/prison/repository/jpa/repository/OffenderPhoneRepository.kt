package uk.gov.justice.hmpps.prison.repository.jpa.repository

import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.CrudRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderPhone
import java.util.Optional

interface OffenderPhoneRepository : CrudRepository<OffenderPhone, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @QueryHints(value = [QueryHint(name = "jakarta.persistence.lock.timeout", value = "1000")])
  @Query("Select offPhone from OffenderPhone offPhone where offPhone.offender.rootOffender.nomsId = :nomsId and offPhone.phoneId = :phoneId")
  fun findByRootNomsIdAndPhoneIdForUpdate(nomsId: String, phoneId: Long?): Optional<OffenderPhone>
}
