package uk.gov.justice.hmpps.prison.repository.jpa.repository

import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.CrudRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderInternetAddress
import java.util.Optional

interface OffenderInternetAddressRepository : CrudRepository<OffenderInternetAddress, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @QueryHints(value = [QueryHint(name = "jakarta.persistence.lock.timeout", value = "1000")])
  @Query("Select offInternetAddress from OffenderInternetAddress offInternetAddress where offInternetAddress.offender.rootOffender.nomsId = :nomsId and offInternetAddress.internetAddressId = :internetAddressId")
  fun findByRootNomsIdAndInternetAddressIdForUpdate(nomsId: String, internetAddressId: Long?): Optional<OffenderInternetAddress>
}
