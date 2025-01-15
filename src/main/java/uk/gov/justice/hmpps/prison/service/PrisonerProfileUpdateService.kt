package uk.gov.justice.hmpps.prison.service

import org.slf4j.LoggerFactory
import org.springframework.dao.CannotAcquireLockException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.UpdateReligion
import uk.gov.justice.hmpps.prison.exception.DatabaseRowLockedException
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country.COUNTRY
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBelief
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProfileDetail
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileCode
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileType
import uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode.Pk
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBeliefRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderProfileDetailRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ProfileCodeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ProfileTypeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.findByTypeAndCategoryAndActiveOrNull
import uk.gov.justice.hmpps.prison.repository.jpa.repository.findByTypeAndCategoryOrNull
import uk.gov.justice.hmpps.prison.repository.jpa.repository.findLatestOffenderBookingByNomsIdOrNull
import java.time.LocalDateTime
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
  private val offenderBeliefRepository: OffenderBeliefRepository,
  private val staffUserAccountRepository: StaffUserAccountRepository,
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
  fun updateNationalityOfLatestBooking(prisonerNumber: String, nationality: String?) {
    val profileType = profileTypeRepository.profileType(NATIONALITY_PROFILE_TYPE).getOrThrow()
    val profileCode = profileCode(profileType, nationality)
    updateProfileDetailsOfBooking(latestBooking(prisonerNumber), prisonerNumber, profileType, profileCode, insertWhenMissing = true)
  }

  @Transactional
  fun updateReligionOfLatestBooking(prisonerNumber: String, request: UpdateReligion, username: String?) {
    val profileType = profileTypeRepository.profileTypeIgnoringActiveStatus(RELIGION_PROFILE_TYPE).getOrThrow()
    val profileCode = profileCode(profileType, request.religion)
      ?: throw EntityNotFoundException.withMessage(
        "Religion profile code with code %s not found",
        request.religion,
      )
    val latestBooking = latestBooking(prisonerNumber)
    val user = staffUserAccountRepository.findByUsername(username).orElseThrow {
      EntityNotFoundException.withMessage("Staff user account with provided username not found")
    }

    if (profileCodeDoesNotMatchExistingValue(prisonerNumber, profileType, profileCode)) {
      updateProfileDetailsOfBooking(latestBooking, prisonerNumber, profileType, profileCode)
      updateBeliefHistory(
        prisonerNumber,
        latestBooking,
        profileCode,
        request,
        user,
      )
    }
  }

  private fun updateProfileDetailsOfBooking(
    booking: OffenderBooking,
    prisonerNumber: String,
    profileType: ProfileType,
    profileCode: ProfileCode?,
    insertWhenMissing: Boolean = false,
  ) {
    try {
      val latestProfileEntryOfType =
        profileDetailRepository.findLinkedToLatestBookingForUpdate(prisonerNumber, profileType)
          .orElse(null)

      if (latestProfileEntryOfType != null) {
        if (profileCode == null) {
          booking.profileDetails.remove(latestProfileEntryOfType)
        } else {
          latestProfileEntryOfType.setProfileCode(profileCode)
        }
      } else if (insertWhenMissing) {
        booking.profileDetails.add(
          OffenderProfileDetail.builder()
            .id(OffenderProfileDetail.PK(booking, profileType, 1))
            .caseloadType("INST")
            .code(profileCode)
            .listSequence(profileType.listSequence)
            .build(),
        )
      }
    } catch (e: CannotAcquireLockException) {
      throw processLockError(e, prisonerNumber, "OFFENDER_PROFILE_DETAILS")
    }
  }

  private fun updateBeliefHistory(
    prisonerNumber: String,
    latestBooking: OffenderBooking,
    profileCode: ProfileCode,
    updateRequest: UpdateReligion,
    user: StaffUserAccount,
  ) {
    val now = LocalDateTime.now()
    val startDate = updateRequest.effectiveFromDate?.atStartOfDay() ?: now
    offenderBeliefRepository.getOffenderBeliefHistory(prisonerNumber, latestBooking.bookingId.toString())
      .filter { it.endDate?.isAfter(now) ?: true }
      .forEach {
        it.endDate = now
        it.modifyDatetime = now
        it.modifiedByUser = user
      }

    offenderBeliefRepository.save(
      OffenderBelief(
        booking = latestBooking,
        changeReason = updateRequest.comment?.isNotBlank() ?: false,
        comments = updateRequest.comment,
        rootOffender = latestBooking.rootOffender,
        beliefCode = profileCode,
        startDate = startDate,
        createDatetime = now,
        createdByUser = user,
        verified = updateRequest.verified,
      ),
    )
  }

  private fun profileCodeDoesNotMatchExistingValue(
    prisonerNumber: String,
    profileType: ProfileType,
    newValue: ProfileCode,
  ): Boolean {
    try {
      return profileDetailRepository.findLinkedToLatestBookingForUpdate(prisonerNumber, profileType)
        .map { it.code != newValue }
        .orElse(true)
    } catch (e: CannotAcquireLockException) {
      throw processLockError(e, prisonerNumber, "OFFENDER_PROFILE_DETAILS")
    }
  }

  private fun profileCode(profileType: ProfileType, value: String?) =
    value?.uppercase()?.let { profileCodeRepository.profile(profileType, it).getOrThrow() }

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

  private fun ProfileTypeRepository.profileTypeIgnoringActiveStatus(
    type: String,
    category: String = "PI",
  ): Result<ProfileType> =
    this.findByTypeAndCategoryOrNull(type, category)?.let { success(it) } ?: failure(
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
