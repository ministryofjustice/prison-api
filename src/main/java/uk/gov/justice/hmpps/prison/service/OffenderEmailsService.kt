package uk.gov.justice.hmpps.prison.service

import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.dao.CannotAcquireLockException
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.api.model.Email
import uk.gov.justice.hmpps.prison.api.model.OffenderEmailAddressCreateRequest
import uk.gov.justice.hmpps.prison.exception.DatabaseRowLockedException
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderInternetAddress
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderInternetAddressRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import java.util.Optional

@Service
@Transactional
class OffenderEmailsService(
  private val offenderRepository: OffenderRepository,
  private val offenderEmailsRepository: OffenderInternetAddressRepository,
) {

  fun getEmailsByPrisonerNumber(prisonerNumber: String): List<Email> {
    val offender = offenderRepository.findRootOffenderByNomsId(prisonerNumber)
      .orElseThrowNotFound("Prisoner with prisonerNumber %s not found", prisonerNumber)
    return offender.emailAddresses.map { email ->
      Email.builder().emailAddressId(email.internetAddressId).email(email.internetAddress).build()
    }
  }

  fun addOffenderEmailAddress(
    prisonerNumber: String,
    offenderEmailAddressCreateRequest: OffenderEmailAddressCreateRequest,
  ): Email {
    val offender = offenderRepository.findRootOffenderByNomsId(prisonerNumber)
      .orElseThrowNotFound("Prisoner with prisonerNumber %s not found", prisonerNumber)

    val email = OffenderInternetAddress.builder().offender(offender)
      .internetAddress(offenderEmailAddressCreateRequest.emailAddress).internetAddressClass("EMAIL").build()

    return AddressTransformer.translate(offenderEmailsRepository.save(email))
  }

  fun updateOffenderEmailAddress(
    prisonerNumber: String,
    emailAddressId: Long,
    offenderEmailRequest: OffenderEmailAddressCreateRequest,
  ): Email {
    try {
      val emailToUpdate =
        offenderEmailsRepository.findByRootNomsIdAndInternetAddressIdForUpdate(prisonerNumber, emailAddressId).orElseThrow(
          EntityNotFoundException.withMessage(
            "Email address with prisoner number %s and email address ID %s not found",
            prisonerNumber,
            emailAddressId,
          ),
        )

      emailToUpdate.internetAddress = offenderEmailRequest.emailAddress
      return AddressTransformer.translate(offenderEmailsRepository.save(emailToUpdate))
    } catch (e: CannotAcquireLockException) {
      throw processLockError(e, prisonerNumber)
    }
  }

  private inline fun <reified T> Optional<T>.orElseThrowNotFound(message: String, prisonerNumber: Any) = orElseThrow(EntityNotFoundException.withMessage(message, prisonerNumber))

  private fun processLockError(e: CannotAcquireLockException, prisonerNumber: String): Exception {
    log.error("Error getting lock", e)
    return if (true == e.cause?.message?.contains("ORA-30006")) {
      DatabaseRowLockedException("Failed to get INTERNET_ADDRESS lock for prisonerNumber=$prisonerNumber")
    } else {
      e
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(OffenderEmailsService::class.java)
  }
}
