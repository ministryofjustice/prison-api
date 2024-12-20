package uk.gov.justice.hmpps.prison.service

import org.slf4j.LoggerFactory
import org.springframework.dao.CannotAcquireLockException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.exception.DatabaseRowLockedException
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country.COUNTRY
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProfileDetail
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileCode
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileType
import uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode.Pk
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderProfileDetailRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ProfileCodeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ProfileTypeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.findByTypeAndCategoryAndActiveOrNull
import uk.gov.justice.hmpps.prison.repository.jpa.repository.findLatestOffenderBookingByNomsIdOrNull
import java.util.Optional
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

@Service
class PrisonerProfileUpdateService(
  private val offenderRepository: OffenderRepository,
  private val countryRepository: ReferenceCodeRepository<Country>,
  private val profileTypeRepository: ProfileTypeRepository,
  private val profileCodeRepository: ProfileCodeRepository,
  private val profileDetailRepository: OffenderProfileDetailRepository,
  private val offenderBookingRepository: OffenderBookingRepository,
) {
  @Transactional
  fun updateBirthPlaceOfCurrentAlias(prisonerNumber: String, birthPlace: String?) {
    try {
      offenderRepository.findLinkedToLatestBookingForUpdate(prisonerNumber)
        .orElseThrowNotFound("Prisoner with prisonerNumber %s and existing booking not found", prisonerNumber)
        .let { it.birthPlace = birthPlace?.uppercase()?.ifBlank { null } }
    } catch (e: CannotAcquireLockException) {
      throw processLockError(e, prisonerNumber, "OFFENDERS")
    }
  }

  @Transactional
  fun updateBirthCountryOfCurrentAlias(prisonerNumber: String, birthCountry: String?) {
    try {
      offenderRepository.findLinkedToLatestBookingForUpdate(prisonerNumber)
        .orElseThrowNotFound("Prisoner with prisonerNumber %s and existing booking not found", prisonerNumber)
        .let { it.birthCountry = country(birthCountry)?.getOrThrow() }
    } catch (e: CannotAcquireLockException) {
      throw processLockError(e, prisonerNumber, "OFFENDERS")
    }
  }

  @Transactional
  fun updateNationalityOfLatestBooking(prisonerNumber: String, nationality: String?) =
    updateProfileDetailsOfLatestBooking(prisonerNumber, NATIONALITY_PROFILE_TYPE, nationality)

  @Transactional
  fun updateReligionOfLatestBooking(prisonerNumber: String, religion: String?) =
    updateProfileDetailsOfLatestBooking(prisonerNumber, RELIGION_PROFILE_TYPE, religion)

  private fun updateProfileDetailsOfLatestBooking(prisonerNumber: String, type: String, value: String?) {
    val profileType: ProfileType = profileTypeRepository.profileType(type).getOrThrow()
    val profileCode: ProfileCode? =
      value?.uppercase()?.let { profileCodeRepository.profile(profileType, it).getOrThrow() }

    try {
      val latestBooking = latestBooking(prisonerNumber)
      val latestProfileEntryOfType =
        profileDetailRepository.findLinkedToLatestBookingForUpdate(prisonerNumber, profileType)
          .orElse(null)

      if (value == null && latestProfileEntryOfType != null) {
        latestBooking.profileDetails.remove(latestProfileEntryOfType)
      } else {
        if (latestProfileEntryOfType != null) {
          latestProfileEntryOfType.setProfileCode(profileCode)
        } else {
          latestBooking.profileDetails.add(
            OffenderProfileDetail.builder()
              .id(OffenderProfileDetail.PK(latestBooking, profileType, 1))
              .caseloadType("INST")
              .code(profileCode)
              .listSequence(profileType.listSequence)
              .build(),
          )
        }
      }
    } catch (e: CannotAcquireLockException) {
      throw processLockError(e, prisonerNumber, "OFFENDER_PROFILE_DETAILS")
    }
  }

  private fun latestBooking(prisonerNumber: String) =
    offenderBookingRepository.findLatestOffenderBookingByNomsIdOrNull(prisonerNumber)
      ?: throw EntityNotFoundException.withMessage(
        "Prisoner with prisonerNumber %s and existing booking not found",
        prisonerNumber,
      )

  private fun ProfileTypeRepository.profileType(
    type: String,
    category: String = "PI",
    active: Boolean = true,
  ): Result<ProfileType> =
    this.findByTypeAndCategoryAndActiveOrNull(type, category, active)?.let { success(it) } ?: failure(
      EntityNotFoundException.withId(type),
    )

  private fun ProfileCodeRepository.profile(type: ProfileType, code: String): Result<ProfileCode> =
    this.findByIdOrNull(ProfileCode.PK(type, code))?.let { success(it) } ?: failure(
      EntityNotFoundException.withMessage("Profile Code for ${type.type} and $code not found"),
    )

  private inline fun <reified T> Optional<T>.orElseThrowNotFound(message: String, prisonerNumber: String) =
    orElseThrow(EntityNotFoundException.withMessage(message, prisonerNumber))

  private fun country(code: String?): Result<Country>? =
    code?.takeIf { it.isNotBlank() }?.let {
      countryRepository.findByIdOrNull(Pk(COUNTRY, code))?.let { success(it) }
        ?: failure(EntityNotFoundException.withMessage("Country $code not found"))
    }

  private fun processLockError(e: CannotAcquireLockException, prisonerNumber: String, table: String): Exception {
    log.error("Error getting lock", e)
    return if (true == e.cause?.message?.contains("ORA-30006")) {
      DatabaseRowLockedException("Failed to get $table lock for prisonerNumber=$prisonerNumber")
    } else {
      e
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
    const val NATIONALITY_PROFILE_TYPE = "NAT"
    const val RELIGION_PROFILE_TYPE = "RELF"
  }
}
