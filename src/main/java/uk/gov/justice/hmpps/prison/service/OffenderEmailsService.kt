package uk.gov.justice.hmpps.prison.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.api.model.Email
import uk.gov.justice.hmpps.prison.api.model.OffenderEmailAddressCreateRequest
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
      .internetAddress(offenderEmailAddressCreateRequest.emailAddress)
      .internetAddressClass("EMAIL").build()

    return AddressTransformer.translate(offenderEmailsRepository.save(email))
  }

  fun updateOffenderEmailAddress(
    prisonerNumber: String,
    emailAddressId: Long,
    offenderEmailRequest: OffenderEmailAddressCreateRequest,
  ): Email {
    val emailToUpdate =
      offenderEmailsRepository.findByRootNomsIdAndInternetAddressId(prisonerNumber, emailAddressId).orElseThrow(
        EntityNotFoundException.withMessage(
          "Email address with prisoner number %s and email address ID %s not found",
          prisonerNumber,
          emailAddressId,
        ),
      )

    emailToUpdate.internetAddress = offenderEmailRequest.emailAddress
    return AddressTransformer.translate(offenderEmailsRepository.save(emailToUpdate))
  }

  private inline fun <reified T> Optional<T>.orElseThrowNotFound(message: String, prisonerNumber: Any) = orElseThrow(EntityNotFoundException.withMessage(message, prisonerNumber))
}
