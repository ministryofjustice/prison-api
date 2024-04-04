package uk.gov.justice.hmpps.prison.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.AddressDto
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderAddressRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository

@Service
@Transactional(readOnly = true)
class OffenderAddressService(
  private val offenderRepository: OffenderRepository,
  private val offenderAddressRepository: OffenderAddressRepository,
) {
  fun getAddressesByOffenderNo(offenderNo: String): List<AddressDto> {
    val offender = offenderRepository.findRootOffenderByNomsId(offenderNo).orElseThrow()
    val addresses = offenderAddressRepository.findByOffenderId(offender.id)
    return AddressTransformer.translate(addresses)
  }
}
