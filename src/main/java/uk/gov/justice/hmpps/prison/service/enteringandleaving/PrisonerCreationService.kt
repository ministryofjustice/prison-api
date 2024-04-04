package uk.gov.justice.hmpps.prison.service.enteringandleaving

import org.apache.commons.codec.language.Soundex
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.InmateDetail
import uk.gov.justice.hmpps.prison.api.model.PrisonerIdentifier
import uk.gov.justice.hmpps.prison.api.model.RequestToCreate
import uk.gov.justice.hmpps.prison.exception.CustomErrorCodes
import uk.gov.justice.hmpps.prison.repository.PrisonerRepository
import uk.gov.justice.hmpps.prison.repository.jpa.model.Ethnicity
import uk.gov.justice.hmpps.prison.repository.jpa.model.Gender
import uk.gov.justice.hmpps.prison.repository.jpa.model.NomsIdSequence
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIdentifier
import uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode
import uk.gov.justice.hmpps.prison.repository.jpa.model.Suffix
import uk.gov.justice.hmpps.prison.repository.jpa.model.Title
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderIdentifierRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import uk.gov.justice.hmpps.prison.service.BadRequestException
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import uk.gov.justice.hmpps.prison.service.Pnc
import uk.gov.justice.hmpps.prison.service.transformers.OffenderTransformer
import java.time.LocalDate
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

@Service
@Transactional
class PrisonerCreationService(
  private val genderRepository: ReferenceCodeRepository<Gender>,
  private val ethnicityRepository: ReferenceCodeRepository<Ethnicity>,
  private val titleRepository: ReferenceCodeRepository<Title>,
  private val suffixRepository: ReferenceCodeRepository<Suffix>,
  private val offenderIdentifierRepository: OffenderIdentifierRepository,
  private val offenderRepository: OffenderRepository,
  private val offenderTransformer: OffenderTransformer,
  private val prisonerRepository: PrisonerRepository,
  private val bookingIntoPrisonService: BookingIntoPrisonService,
) {
  fun createPrisoner(requestToCreate: RequestToCreate): InmateDetail {
    val gender: Gender = gender(requestToCreate.gender).getOrThrow()
    val ethnicity: Ethnicity? = ethnicity(requestToCreate.ethnicity)?.getOrThrow()
    val title: Title? = title(requestToCreate.title)?.getOrThrow()
    val suffix: Suffix? = suffix(requestToCreate.suffix)?.getOrThrow()

    val validPncNumber: String? = validPncNumber(requestToCreate.pncNumber)?.getOrThrow()
    val validCroNumber: String? = validCroNumber(requestToCreate.croNumber)?.getOrThrow()
    val (firstName, lastName) = validUniqueUppercaseNames(requestToCreate).getOrThrow()
    val dateOfBirth = validDateOfBirth(requestToCreate.dateOfBirth).getOrThrow()

    val prisoner = offenderRepository.save(
      Offender.builder()
        .lastName(lastName)
        .firstName(firstName)
        .middleName(requestToCreate.middleName1?.uppercase())
        .middleName2(requestToCreate.middleName2?.uppercase())
        .birthDate(dateOfBirth)
        .gender(gender)
        .title(title)
        .suffix(suffix)
        .ethnicity(ethnicity)
        .createDate(LocalDate.now())
        .nomsId(getNextPrisonerIdentifier().getId())
        .idSourceCode("SEQ")
        .nameSequence("1234")
        .caseloadType("INST")
        .lastNameKey(lastName)
        .lastNameAlphaKey(lastName.take(1))
        .lastNameSoundex(Soundex().soundex(lastName))
        .build(),
    ).also { newPrisoner ->
      newPrisoner.rootOffenderId = newPrisoner.id
      newPrisoner.rootOffender = newPrisoner
      validPncNumber?.let { newPrisoner.addIdentifier("PNC", it) }
      validCroNumber?.let { newPrisoner.addIdentifier("CRO", it) }
    }

    return prisoner.takeIf { requestToCreate.booking != null }
      ?.let { bookingIntoPrisonService.newBookingWithoutUpdateLock(it, null, requestToCreate.booking) }
      ?: offenderTransformer.transformWithoutBooking(prisoner)
  }

  fun getNextPrisonerIdentifier(): PrisonerIdentifier {
    var retries = 0
    var updated: Boolean
    var nextSequence: NomsIdSequence
    var currentSequence: NomsIdSequence
    do {
      currentSequence = prisonerRepository.nomsIdSequence
      nextSequence = currentSequence.next()
      updated = prisonerRepository.updateNomsIdSequence(nextSequence, currentSequence) > 0
    } while (!updated && retries++ < 10)
    if (!updated) {
      throw RuntimeException("Prisoner Identifier cannot be generated, please try again")
    }
    return PrisonerIdentifier.builder().id(currentSequence.prisonerIdentifier).build()
  }

  private fun gender(code: String): Result<Gender> =
    genderRepository.findByIdOrNull(ReferenceCode.Pk(Gender.SEX, code))?.let { success(it) } ?: failure(
      EntityNotFoundException.withMessage("Gender $code not found"),
    )

  private fun ethnicity(code: String?): Result<Ethnicity>? =
    code?.takeIf { it.isNotBlank() }?.let {
      ethnicityRepository.findByIdOrNull(ReferenceCode.Pk(Ethnicity.ETHNICITY, code))?.let { success(it) }
        ?: failure(EntityNotFoundException.withMessage("Ethnicity $code not found"))
    }

  private fun title(code: String?): Result<Title>? =
    code?.takeIf { it.isNotBlank() }?.let {
      titleRepository.findByIdOrNull(ReferenceCode.Pk(Title.TITLE, code))?.let { success(it) }
        ?: failure(EntityNotFoundException.withMessage("Title $code not found"))
    }

  private fun suffix(code: String?): Result<Suffix>? =
    code?.takeIf { it.isNotBlank() }?.let {
      suffixRepository.findByIdOrNull(ReferenceCode.Pk(Suffix.SUFFIX, code))?.let { success(it) }
        ?: failure(EntityNotFoundException.withMessage("Suffix $code not found"))
    }

  private fun validPncNumber(pncNumber: String?): Result<String>? =
    pncNumber?.takeIf { it.isNotBlank() }?.let { pnc ->
      Pnc.getShortPncNumber(pnc).checkForDuplicatePncNumber().flatMap {
        Pnc.getLongPncNumber(pnc).checkForDuplicatePncNumber()
      }
    }

  private fun String.checkForDuplicatePncNumber(): Result<String> =
    offenderIdentifierRepository.findByIdentifierTypeAndIdentifier("PNC", this)
      .takeIf { matches -> matches.isNotEmpty() }?.let { matches ->
        failure(matches.toPncMatchFailure())
      } ?: success(Pnc.getLongPncNumber(this))

  private fun validCroNumber(croNumber: String?): Result<String>? =
    croNumber?.takeIf { it.isNotBlank() }?.checkForDuplicateCroNumber()

  private fun String.checkForDuplicateCroNumber(): Result<String> =
    offenderIdentifierRepository.findByIdentifierTypeAndIdentifier("CRO", this)
      .takeIf { matches -> matches.isNotEmpty() }?.let { matches ->
        failure(matches.toCroMatchFailure())
      } ?: success(this)

  private fun validUniqueUppercaseNames(requestToCreate: RequestToCreate): Result<Pair<String, String>> {
    val names = requestToCreate.firstName.uppercase() to requestToCreate.lastName.uppercase()
    if (requestToCreate.hasNoIdentifiers()) {
      return names.checkForDuplicatePrisonerByName(requestToCreate.dateOfBirth)
    }
    return success(requestToCreate.firstName.uppercase() to requestToCreate.lastName.uppercase())
  }

  private fun Pair<String, String>.checkForDuplicatePrisonerByName(dateOfBirth: LocalDate): Result<Pair<String, String>> =
    offenderRepository.findByLastNameAndFirstNameAndBirthDate(
      this.second,
      this.first,
      dateOfBirth,
    )
      .takeIf { matches -> matches.isNotEmpty() }?.let { matches ->
        failure(matches.toNameMatchFailure())
      } ?: success(this)

  private fun validDateOfBirth(dateOfBirth: LocalDate): Result<LocalDate> =
    dateOfBirth.takeUnless { it.isNotValidAgeForPrison() }?.let { success(it) }
      ?: failure(BadRequestException.withMessage("Date of birth must be between ${oldestPossibleDateOfBirth()} and ${youngestPossibleDateOfBirth()}"))
}

private fun List<OffenderIdentifier>.toPncMatchFailure() = first().let {
  BadRequestException.withMessage(
    "Prisoner with PNC ${it.identifier} already exists with ID ${it.offender.nomsId}",
    CustomErrorCodes.PRISONER_ALREADY_EXIST,
  )
}

private fun List<OffenderIdentifier>.toCroMatchFailure() = first().let {
  BadRequestException.withMessage(
    "Prisoner with CRO ${it.identifier} already exists with ID ${it.offender.nomsId}",
    CustomErrorCodes.PRISONER_ALREADY_EXIST,
  )
}

private fun List<Offender>.toNameMatchFailure() = first().let {
  BadRequestException.withMessage(
    "Prisoner with lastname ${it.lastName}, firstname ${it.firstName} and dob ${it.birthDate} already exists with ID ${it.nomsId}",
    CustomErrorCodes.PRISONER_ALREADY_EXIST,
  )
}

private fun RequestToCreate.hasNoIdentifiers() = pncNumber.isNullOrBlank() && croNumber.isNullOrBlank()
private fun youngestPossibleDateOfBirth() = LocalDate.now().minusYears(16)
private fun oldestPossibleDateOfBirth() = LocalDate.now().minusYears(110)
private fun LocalDate.isNotValidAgeForPrison() =
  this.isAfter(youngestPossibleDateOfBirth()) || this.isBefore(oldestPossibleDateOfBirth())
