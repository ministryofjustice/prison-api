package uk.gov.justice.hmpps.prison.service

import org.slf4j.LoggerFactory
import org.springframework.dao.CannotAcquireLockException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.exception.DatabaseRowLockedException
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country.COUNTRY
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileCode
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileType
import uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode.Pk
import uk.gov.justice.hmpps.prison.repository.jpa.repository.*
import java.util.*
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

@Service
class PrisonerProfileUpdateService(
  private val offenderRepository: OffenderRepository,
  private val offenderProfileDetailRepository: OffenderProfileDetailRepository,
  private val countryRepository: ReferenceCodeRepository<Country>,
  private val profileTypeRepository: ProfileTypeRepository,
  private val profileCodeRepository: ProfileCodeRepository,
) {

  @Transactional
  fun updateBirthPlaceOfCurrentAlias(prisonerNumber: String, birthPlace: String?) {
    try {
      offenderRepository.findLinkedToLatestBookingForUpdate(prisonerNumber)
        .orElseThrowNotFound("Prisoner with prisonerNumber %s and existing booking not found", prisonerNumber)
        .let { it.birthPlace = birthPlace?.uppercase()?.ifBlank { null } }
    } catch (e: CannotAcquireLockException) {
      throw processLockError(e, prisonerNumber)
    }
  }

  @Transactional
  fun updateBirthCountryOfCurrentAlias(prisonerNumber: String, birthCountry: String?) {
    try {
      offenderRepository.findLinkedToLatestBookingForUpdate(prisonerNumber)
        .orElseThrowNotFound("Prisoner with prisonerNumber %s and existing booking not found", prisonerNumber)
        .let { it.birthCountry = country(birthCountry)?.getOrThrow() }
    } catch (e: CannotAcquireLockException) {
      throw processLockError(e, prisonerNumber)
    }
  }

  @Transactional
  fun updateNationalityOfCurrentBooking(prisonerNumber: String, nationality: String?) {
    val profileType: ProfileType = profileTypeRepository.nationalityProfile().getOrThrow()
    val profileCode: ProfileCode? =
      nationality?.uppercase()?.let { profileCodeRepository.profile(profileType, it).getOrThrow() }

    try {
      offenderProfileDetailRepository.findNationalityLinkedToLatestBookingForUpdate(prisonerNumber)
        .orElseThrowNotFound("Prisoner with prisonerNumber %s and existing booking not found", prisonerNumber)
        .setProfileCode(profileCode)
    } catch (e: CannotAcquireLockException) {
      throw processLockError(e, prisonerNumber)
    }
  }

  private fun ProfileTypeRepository.nationalityProfile(): Result<ProfileType> =
    this.findByTypeAndCategoryAndActiveOrNull("NAT", "PI", true)?.let { success(it) } ?: failure(
      EntityNotFoundException.withId("NAT"),
    )

  private fun ProfileCodeRepository.profile(type: ProfileType, code: String): Result<ProfileCode> =
    this.findByIdOrNull(ProfileCode.PK(type, code))?.let { success(it) } ?: failure(
      EntityNotFoundException.withMessage("Profile Code for NAT and $code not found"),
    )

  private inline fun <reified T> Optional<T>.orElseThrowNotFound(message: String, prisonerNumber: String) =
    orElseThrow(EntityNotFoundException.withMessage(message, prisonerNumber))

  private fun country(code: String?): Result<Country>? =
    code?.takeIf { it.isNotBlank() }?.let {
      countryRepository.findByIdOrNull(Pk(COUNTRY, code))?.let { success(it) }
        ?: failure(EntityNotFoundException.withMessage("Country $code not found"))
    }

  private fun processLockError(e: CannotAcquireLockException, prisonerNumber: String): Exception {
    log.error("Error getting lock", e)
    return if (true == e.cause?.message?.contains("ORA-30006")) {
      DatabaseRowLockedException("Failed to get OFFENDERS lock for prisonerNumber=$prisonerNumber")
    } else {
      e
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
