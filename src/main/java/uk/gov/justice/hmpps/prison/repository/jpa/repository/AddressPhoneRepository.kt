package uk.gov.justice.hmpps.prison.repository.jpa.repository

import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.CrudRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.AddressPhone
import java.util.Optional

interface AddressPhoneRepository : CrudRepository<AddressPhone, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @QueryHints(value = [QueryHint(name = "jakarta.persistence.lock.timeout", value = "1000")])
  @Query(
    "select phone from OffenderAddress address join address.phones phone " +
      "where address.addressId = :addressId and address.offender.id = :offenderId and phone.phoneId = :phoneId",
  )
  fun findByAddressIdAndOffenderIdAndPhoneIdForUpdate(
    addressId: Long,
    offenderId: Long,
    phoneId: Long,
  ): Optional<AddressPhone>
}
