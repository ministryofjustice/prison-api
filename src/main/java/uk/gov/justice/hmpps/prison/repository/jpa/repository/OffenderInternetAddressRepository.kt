package uk.gov.justice.hmpps.prison.repository.jpa.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderInternetAddress
import java.util.Optional

interface OffenderInternetAddressRepository : CrudRepository<OffenderInternetAddress, Long> {
  @Query("Select offInternetAddress from OffenderInternetAddress offInternetAddress where offInternetAddress.offender.rootOffender.nomsId = :nomsId and offInternetAddress.internetAddressId = :internetAddressId")
  fun findByRootNomsIdAndInternetAddressId(nomsId: String, internetAddressId: Long?): Optional<OffenderInternetAddress>
}
