package uk.gov.justice.hmpps.prison.service

import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.dao.CannotAcquireLockException
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.api.model.OffenderPhoneNumberCreateRequest
import uk.gov.justice.hmpps.prison.api.model.Telephone
import uk.gov.justice.hmpps.prison.exception.DatabaseRowLockedException
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
  ): Telephone {
    val offender = offenderRepository.findRootOffenderByNomsId(prisonerNumber)
      .orElseThrowNotFound("Prisoner with prisonerNumber %s not found", prisonerNumber)

    if (!referenceDomainService.isReferenceCodeActive("PHONE_USAGE", offenderPhoneNumberRequest.phoneNumberType)) {
      throw BadRequestException.withMessage("Phone number type ${offenderPhoneNumberRequest.phoneNumberType} is not valid")
    }

    val phone = OffenderPhone.builder().offender(offender).phoneNo(offenderPhoneNumberRequest.phoneNumber)
      .phoneType(offenderPhoneNumberRequest.phoneNumberType).build()

    return AddressTransformer.translate(offenderPhoneRepository.save(phone))
  }

  fun updateOffenderPhoneNumber(
    prisonerNumber: String,
    phoneNumberId: Long,
    offenderPhoneNumberRequest: OffenderPhoneNumberCreateRequest,
  ): Telephone {
    if (!referenceDomainService.isReferenceCodeActive("PHONE_USAGE", offenderPhoneNumberRequest.phoneNumberType)) {
      throw BadRequestException.withMessage("Phone number type ${offenderPhoneNumberRequest.phoneNumberType} is not valid")
    }

    try {
      val phoneToUpdate = offenderPhoneRepository.findByRootNomsIdAndPhoneId(prisonerNumber, phoneNumberId).orElseThrow(
        EntityNotFoundException.withMessage(
          "Phone number with prisoner number %s and phone ID %s not found",
          prisonerNumber,
          phoneNumberId,
        ),
      )

      phoneToUpdate.phoneType = offenderPhoneNumberRequest.phoneNumberType
      phoneToUpdate.phoneNo = offenderPhoneNumberRequest.phoneNumber
      return AddressTransformer.translate(offenderPhoneRepository.save(phoneToUpdate))
    } catch (e: CannotAcquireLockException) {
      throw processLockError(e, prisonerNumber)
    }
  }

  private inline fun <reified T> Optional<T>.orElseThrowNotFound(message: String, prisonerNumber: Any) = orElseThrow(EntityNotFoundException.withMessage(message, prisonerNumber))

  private fun processLockError(e: CannotAcquireLockException, prisonerNumber: String): Exception {
    log.error("Error getting lock", e)
    return if (true == e.cause?.message?.contains("ORA-30006")) {
      DatabaseRowLockedException("Failed to get PHONES lock for prisonerNumber=$prisonerNumber")
    } else {
      e
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(OffenderPhonesService::class.java)
  }
}
