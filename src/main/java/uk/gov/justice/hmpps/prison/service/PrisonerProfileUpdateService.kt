package uk.gov.justice.hmpps.prison.service

import org.slf4j.LoggerFactory
import org.springframework.dao.CannotAcquireLockException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.UpdateReligion
import uk.gov.justice.hmpps.prison.api.model.UpdateSmokerStatus
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
  fun updateNationalityOfLatestBooking(
    prisonerNumber: String,
    nationality: String?,
    otherNationalities: String? = null,
  ) {
    val nationalityType = profileTypeRepository.profileType(NATIONALITY_PROFILE_TYPE).getOrThrow()
    val otherNationalitiesType = profileTypeRepository.profileType(OTHER_NATIONALITIES_PROFILE_TYPE).getOrThrow()

    val booking = latestBooking(prisonerNumber)
    updateProfileDetailsOfBooking(booking, prisonerNumber, nationalityType, nationality)
    updateProfileDetailsOfBooking(booking, prisonerNumber, otherNationalitiesType, otherNationalities)
  }

  @Transactional
  fun updateReligionOfLatestBooking(prisonerNumber: String, request: UpdateReligion, username: String?) {
    val profileType = profileTypeRepository.profileType(RELIGION_PROFILE_TYPE).getOrThrow()
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
      // Profile details are updated by the OFFENDER_BELIEFS_T1 trigger
      updateBeliefHistory(prisonerNumber, latestBooking, profileCode, request, user)
    }
  }

  @Transactional
  fun updateSmokerStatusOfLatestBooking(prisonerNumber: String, request: UpdateSmokerStatus) {
    val profileType = profileTypeRepository.profileType(SMOKER_PROFILE_TYPE).getOrThrow()
    val profileCode = when (request.smokerStatus) {
      null -> null
      else -> profileCode(profileType, request.smokerStatus)
        ?: throw EntityNotFoundException.withMessage(
          "Smoker profile code with code %s not found",
          request.smokerStatus,
        )
    }

    val latestBooking = latestBooking(prisonerNumber)

    updateProfileDetailsOfBooking(latestBooking, prisonerNumber, profileType, profileCode?.id?.code)
  }

  private fun updateProfileDetailsOfBooking(
    booking: OffenderBooking,
    prisonerNumber: String,
    profileType: ProfileType,
    profileCodeValue: String?,
  ) {
    try {
      val latestProfileEntryOfType =
        profileDetailRepository.findLinkedToLatestBookingForUpdate(prisonerNumber, profileType)
          .orElse(null)

      if (latestProfileEntryOfType != null) {
        updateExistingProfileDetailsEntry(booking, latestProfileEntryOfType, profileType, profileCodeValue)
      } else if (profileCodeValue != null) {
        addProfileDetailsEntry(booking, profileType, profileCodeValue)
      }
    } catch (e: CannotAcquireLockException) {
      throw processLockError(e, prisonerNumber, "OFFENDER_PROFILE_DETAILS")
    }
  }

  private fun updateExistingProfileDetailsEntry(
    booking: OffenderBooking,
    profileDetailEntry: OffenderProfileDetail,
    profileType: ProfileType,
    profileCodeValue: String?,
  ) {
    if (profileCodeValue == null) {
      booking.profileDetails.remove(profileDetailEntry)
    } else {
      if (FREE_TEXT_PROFILE_CODES.contains(profileType.type)) {
        profileDetailEntry.profileCode = profileCodeValue
      } else {
        val profileCode = profileCode(profileType, profileCodeValue)
        profileDetailEntry.setProfileCode(profileCode)
      }
    }
  }

  private fun addProfileDetailsEntry(
    booking: OffenderBooking,
    profileType: ProfileType,
    profileCodeValue: String,
  ) {
    var builder = OffenderProfileDetail.builder()
      .id(OffenderProfileDetail.PK(booking, profileType, 1))
      .caseloadType("INST")
      .listSequence(profileType.listSequence)

    val profileCode = profileCode(profileType, profileCodeValue)
    if (profileCode != null) {
      builder = builder.code(profileCode)
    } else if (FREE_TEXT_PROFILE_CODES.contains(profileType.type)) {
      builder = builder.profileCode(profileCodeValue)
    }

    booking.profileDetails.add(builder.build())
  }

  private fun updateBeliefHistory(
    prisonerNumber: String,
    latestBooking: OffenderBooking,
    profileCode: ProfileCode,
    updateRequest: UpdateReligion,
    user: StaffUserAccount,
  ) {
    val now = LocalDateTime.now()
    val today = now.toLocalDate()
    val startDate = updateRequest.effectiveFromDate ?: today
    offenderBeliefRepository.getOffenderBeliefHistory(prisonerNumber, latestBooking.bookingId.toString())
      .filter { it.endDate?.isAfter(today) ?: true }
      .forEach {
        it.endDate = today
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

  private fun profileCode(profileType: ProfileType, value: String?) = value?.uppercase()?.let {
    if (FREE_TEXT_PROFILE_CODES.contains(profileType.type)) {
      profileCodeRepository.profile(profileType, it).getOrNull()
    } else {
      profileCodeRepository.profile(profileType, it).getOrThrow()
    }
  }

  private fun latestBooking(prisonerNumber: String) = offenderBookingRepository.findLatestOffenderBookingByNomsIdOrNull(prisonerNumber)
    ?: throw EntityNotFoundException.withMessage(
      "Prisoner with prisonerNumber %s and existing booking not found",
      prisonerNumber,
    )

  private fun ProfileTypeRepository.profileType(type: String, category: String = "PI"): Result<ProfileType> = this.findByTypeAndCategoryOrNull(type, category)?.let { success(it) } ?: failure(
    EntityNotFoundException.withId(type),
  )

  private fun ProfileCodeRepository.profile(type: ProfileType, code: String): Result<ProfileCode> = this.findByIdOrNull(ProfileCode.PK(type, code))?.let { success(it) } ?: failure(
    EntityNotFoundException.withMessage("Profile Code for ${type.type} and $code not found"),
  )

  private inline fun <reified T> Optional<T>.orElseThrowNotFound(message: String, prisonerNumber: String) = orElseThrow(EntityNotFoundException.withMessage(message, prisonerNumber))

  private fun country(code: String?): Result<Country>? = code?.takeIf { it.isNotBlank() }?.let {
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
    const val OTHER_NATIONALITIES_PROFILE_TYPE = "NATIO"
    const val RELIGION_PROFILE_TYPE = "RELF"
    const val SMOKER_PROFILE_TYPE = "SMOKE"
    val FREE_TEXT_PROFILE_CODES = listOf(OTHER_NATIONALITIES_PROFILE_TYPE)
  }
}
