package uk.gov.justice.hmpps.prison.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.api.model.OffenderPhoneNumberCreateRequest
import uk.gov.justice.hmpps.prison.api.model.Telephone
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderPhone
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderPhoneRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import java.util.Optional

@Service
@Transactional
class OffenderPhonesService(
  private val offenderRepository: OffenderRepository,
  private val offenderPhoneRepository: OffenderPhoneRepository,
  private val referenceDomainService: ReferenceDomainService,
) {
  fun getPhoneNumbersByOffenderNo(prisonerNumber: String): List<Telephone> {
    val offender = offenderRepository.findRootOffenderByNomsId(prisonerNumber)
      .orElseThrowNotFound("Prisoner with prisonerNumber %s not found", prisonerNumber)
    return offender.phones.map(AddressTransformer::translate)
  }

  fun addOffenderPhoneNumber(
    prisonerNumber: String,
    offenderPhoneNumberRequest: OffenderPhoneNumberCreateRequest,
  ): OffenderPhone {
    val offender = offenderRepository.findRootOffenderByNomsId(prisonerNumber)
      .orElseThrowNotFound("Prisoner with prisonerNumber %s not found", prisonerNumber)

    if (!referenceDomainService.isReferenceCodeActive("PHONE_USAGE", offenderPhoneNumberRequest.phoneNumberType)) {
      throw BadRequestException.withMessage("Phone number type ${offenderPhoneNumberRequest.phoneNumberType} is not valid")
    }

    val phone = OffenderPhone
      .builder()
      .offender(offender)
      .phoneNo(offenderPhoneNumberRequest.phoneNumber)
      .phoneType(offenderPhoneNumberRequest.phoneNumberType).build()

    return offenderPhoneRepository.save(phone)
  }

  fun updateOffenderPhoneNumber(
    prisonerNumber: String,
    phoneNumberId: Long,
    offenderPhoneNumberRequest: OffenderPhoneNumberCreateRequest,
  ): OffenderPhone {
    val phoneToUpdate = offenderPhoneRepository.findByRootNomsIdAndPhoneId(prisonerNumber, phoneNumberId)
      .orElseThrow(
        EntityNotFoundException.withMessage(
          "Phone number with prisoner number %s and phone ID %s not found",
          prisonerNumber,
          phoneNumberId,
        ),
      )

    if (!referenceDomainService.isReferenceCodeActive("PHONE_USAGE", offenderPhoneNumberRequest.phoneNumberType)) {
      throw BadRequestException.withMessage("Phone number type ${offenderPhoneNumberRequest.phoneNumberType} is not valid")
    }

    phoneToUpdate.phoneType = offenderPhoneNumberRequest.phoneNumberType
    phoneToUpdate.phoneNo = offenderPhoneNumberRequest.phoneNumber
    return offenderPhoneRepository.save(phoneToUpdate)
  }

  private inline fun <reified T> Optional<T>.orElseThrowNotFound(message: String, prisonerNumber: Any) = orElseThrow(EntityNotFoundException.withMessage(message, prisonerNumber))
}
