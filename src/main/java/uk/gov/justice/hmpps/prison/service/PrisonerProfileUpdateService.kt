package uk.gov.justice.hmpps.prison.service

import org.apache.commons.codec.language.Soundex
import org.slf4j.LoggerFactory
import org.springframework.dao.CannotAcquireLockException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.AddressDto
import uk.gov.justice.hmpps.prison.api.model.CorePersonCommunicationNeeds
import uk.gov.justice.hmpps.prison.api.model.CorePersonLanguagePreferences
import uk.gov.justice.hmpps.prison.api.model.CorePersonLanguagePreferencesRequest
import uk.gov.justice.hmpps.prison.api.model.CorePersonPhysicalAttributes
import uk.gov.justice.hmpps.prison.api.model.CorePersonPhysicalAttributesRequest
import uk.gov.justice.hmpps.prison.api.model.CorePersonRecordAlias
import uk.gov.justice.hmpps.prison.api.model.CorePersonSecondaryLanguage
import uk.gov.justice.hmpps.prison.api.model.CorePersonSecondaryLanguageRequest
import uk.gov.justice.hmpps.prison.api.model.CreateAddress
import uk.gov.justice.hmpps.prison.api.model.CreateAlias
import uk.gov.justice.hmpps.prison.api.model.ReferenceDataValue
import uk.gov.justice.hmpps.prison.api.model.UpdateAlias
import uk.gov.justice.hmpps.prison.api.model.UpdateReligion
import uk.gov.justice.hmpps.prison.api.model.UpdateSexualOrientation
import uk.gov.justice.hmpps.prison.api.model.UpdateSmokerStatus
import uk.gov.justice.hmpps.prison.exception.DatabaseRowLockedException
import uk.gov.justice.hmpps.prison.repository.jpa.model.AddressUsageType
import uk.gov.justice.hmpps.prison.repository.jpa.model.City
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country.COUNTRY
import uk.gov.justice.hmpps.prison.repository.jpa.model.County
import uk.gov.justice.hmpps.prison.repository.jpa.model.Ethnicity
import uk.gov.justice.hmpps.prison.repository.jpa.model.Ethnicity.ETHNICITY
import uk.gov.justice.hmpps.prison.repository.jpa.model.Gender
import uk.gov.justice.hmpps.prison.repository.jpa.model.Gender.SEX
import uk.gov.justice.hmpps.prison.repository.jpa.model.LanguageReferenceCode
import uk.gov.justice.hmpps.prison.repository.jpa.model.NameType
import uk.gov.justice.hmpps.prison.repository.jpa.model.NameType.NAME_TYPE
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAddress
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBelief
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderLanguage
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderPhysicalAttributeId
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderPhysicalAttributes
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProfileDetail
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileCode
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileType
import uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode
import uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode.Pk
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount
import uk.gov.justice.hmpps.prison.repository.jpa.model.Title
import uk.gov.justice.hmpps.prison.repository.jpa.model.Title.TITLE
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderAddressRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBeliefRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderIdentifierRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderLanguageRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderProfileDetailRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ProfileCodeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ProfileTypeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.findByTypeAndCategoryOrNull
import uk.gov.justice.hmpps.prison.repository.jpa.repository.findLatestOffenderBookingByNomsIdOrNull
import uk.gov.justice.hmpps.prison.util.centimetresToFeetAndInches
import uk.gov.justice.hmpps.prison.util.kilogramsToPounds
import java.time.LocalDateTime
import java.util.Optional
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success
import kotlin.jvm.optionals.getOrNull

@Service
class PrisonerProfileUpdateService(
  private val offenderRepository: OffenderRepository,
  private val titleRepository: ReferenceCodeRepository<Title>,
  private val genderRepository: ReferenceCodeRepository<Gender>,
  private val ethnicityRepository: ReferenceCodeRepository<Ethnicity>,
  private val nameTypeRepository: ReferenceCodeRepository<NameType>,
  private val addressRepository: OffenderAddressRepository,
  private val cityRepository: ReferenceCodeRepository<City>,
  private val countyRepository: ReferenceCodeRepository<County>,
  private val countryRepository: ReferenceCodeRepository<Country>,
  private val addressUsageRepository: ReferenceCodeRepository<AddressUsageType>,
  private val profileTypeRepository: ProfileTypeRepository,
  private val profileCodeRepository: ProfileCodeRepository,
  private val profileDetailRepository: OffenderProfileDetailRepository,
  private val offenderBookingRepository: OffenderBookingRepository,
  private val offenderBeliefRepository: OffenderBeliefRepository,
  private val staffUserAccountRepository: StaffUserAccountRepository,
  private val offenderLanguageRepository: OffenderLanguageRepository,
  private val languageCodeRepository: ReferenceCodeRepository<LanguageReferenceCode>,
  private val offenderIdentifierRepository: OffenderIdentifierRepository,
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
  fun updateSexualOrientationOfLatestBooking(prisonerNumber: String, request: UpdateSexualOrientation) {
    val profileType = profileTypeRepository.profileType(SEXUAL_ORIENTATION_PROFILE_TYPE).getOrThrow()
    val profileCode = when (request.sexualOrientation) {
      null -> null
      else -> profileCode(profileType, request.sexualOrientation)
        ?: throw EntityNotFoundException.withMessage(
          "Sexual orientation profile code with code %s not found",
          request.sexualOrientation,
        )
    }

    val latestBooking = latestBooking(prisonerNumber)

    updateProfileDetailsOfBooking(latestBooking, prisonerNumber, profileType, profileCode?.id?.code)
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

  @Transactional
  fun getPhysicalAttributes(prisonerNumber: String): CorePersonPhysicalAttributes {
    try {
      val booking = offenderBookingRepository.findLatestOffenderBookingByNomsId(prisonerNumber)
        .orElseThrowNotFound("Prisoner with prisonerNumber %s and existing booking not found", prisonerNumber)
      return booking.latestPhysicalAttributes?.let { attributes ->
        CorePersonPhysicalAttributes(
          height = attributes.heightCentimetres,
          weight = attributes.weightKgs,
          hair = booking.profileDetails.find { it.id.type.type == HAIR_PROFILE_TYPE }?.code,
          face = booking.profileDetails.find { it.id.type.type == FACE_PROFILE_TYPE }?.code,
          facialHair = booking.profileDetails.find { it.id.type.type == FACIAL_HAIR_PROFILE_TYPE }?.code,
          build = booking.profileDetails.find { it.id.type.type == BUILD_PROFILE_TYPE }?.code,
          leftEyeColour = booking.profileDetails.find { it.id.type.type == L_EYE_C_PROFILE_TYPE }?.code,
          rightEyeColour = booking.profileDetails.find { it.id.type.type == R_EYE_C_PROFILE_TYPE }?.code,
          shoeSize = booking.profileDetails.find { it.id.type.type == SHOESIZE_PROFILE_TYPE }?.profileCode,
        )
      } ?: CorePersonPhysicalAttributes()
    } catch (e: CannotAcquireLockException) {
      throw processLockError(e, prisonerNumber, "OFFENDERS")
    }
  }

  @Transactional
  fun updatePhysicalAttributes(prisonerNumber: String, request: CorePersonPhysicalAttributesRequest) {
    try {
      val booking = offenderBookingRepository.findLatestOffenderBookingByNomsIdForUpdate(prisonerNumber)
        .orElseThrowNotFound("Prisoner with prisonerNumber %s and existing booking not found", prisonerNumber)

      // Update height and weight on physical attributes
      booking.latestPhysicalAttributes?.let { latestPhysicalAttributes ->
        latestPhysicalAttributes.heightCentimetres = request.height
        latestPhysicalAttributes.heightFeet = request.height?.let { centimetresToFeetAndInches(it).first }
        latestPhysicalAttributes.heightInches = request.height?.let { centimetresToFeetAndInches(it).second }
        latestPhysicalAttributes.weightKgs = request.weight
        latestPhysicalAttributes.weightPounds = request.weight?.let { kilogramsToPounds(it) }
      } ?: booking.offenderPhysicalAttributes.add(
        OffenderPhysicalAttributes(
          id = OffenderPhysicalAttributeId(booking, 1),
          heightCentimetres = request.height,
          weightKgs = request.weight,
        ),
      )

      // Update hair, facial hair, face, build, left eye colour, right eye colour on profile details
      val properties = mapOf(
        HAIR_PROFILE_TYPE to request.hairCode,
        FACIAL_HAIR_PROFILE_TYPE to request.facialHairCode,
        FACE_PROFILE_TYPE to request.faceCode,
        BUILD_PROFILE_TYPE to request.buildCode,
        L_EYE_C_PROFILE_TYPE to request.leftEyeColourCode,
        R_EYE_C_PROFILE_TYPE to request.rightEyeColourCode,
      )

      properties.forEach { (type, code) ->
        val profileType = profileTypeRepository.profileType(type, PHYSICAL_APPEARANCE_PROFILE_CATEGORY).getOrThrow()
        code?.let {
          val profileCode = profileCode(profileType, it)
            ?: throw EntityNotFoundException.withMessage(
              "$type profile code with code %s not found",
              it,
            )
          updateProfileDetailsOfBooking(booking, prisonerNumber, profileType, profileCode.id.code)
        } ?: updateProfileDetailsOfBooking(booking, prisonerNumber, profileType, null)
      }

      // Update shoe size on profile details
      val profileType = profileTypeRepository.profileType(SHOESIZE_PROFILE_TYPE, PHYSICAL_APPEARANCE_PROFILE_CATEGORY).getOrThrow()
      updateProfileDetailsOfBooking(booking, prisonerNumber, profileType, request.shoeSize)

      offenderBookingRepository.save(booking)
    } catch (e: CannotAcquireLockException) {
      throw processLockError(e, prisonerNumber, "OFFENDERS")
    }
  }

  fun getAliases(prisonerNumber: String): List<CorePersonRecordAlias> = offenderRepository.findByNomsId(prisonerNumber)

  fun getAlias(offenderId: Long): CorePersonRecordAlias = offenderRepository.findAliasById(offenderId)
    .orElseThrowNotFound("Alias with offenderId %s not found", "$offenderId")

  @Transactional
  fun createAlias(prisonerNumber: String, request: CreateAlias): CorePersonRecordAlias {
    try {
      val oldWorkingName = offenderRepository.findLinkedToLatestBookingForUpdate(prisonerNumber)
        .orElseThrowNotFound("Prisoner with prisonerNumber %s and existing booking not found", prisonerNumber)

      val newFirstName = request.firstName.trim().uppercase()
      val newLastName = request.lastName.trim().uppercase()
      val newMiddleName1 = request.middleName1?.trim()?.uppercase()
      val newMiddleName2 = request.middleName2?.trim()?.uppercase()

      val newAlias = Offender.builder()
        .nomsId(oldWorkingName.nomsId)
        .rootOffenderId(oldWorkingName.rootOffenderId)
        .aliasOffenderId(oldWorkingName.aliasOffenderId)
        .lastName(newLastName)
        .lastNameKey(newLastName)
        .lastNameAlphaKey(newLastName.take(1))
        .lastNameSoundex(Soundex().soundex(newLastName))
        .firstName(newFirstName)
        .middleName(newMiddleName1)
        .middleName2(newMiddleName2)
        .birthDate(request.dateOfBirth)
        .gender(gender(request.sex)?.getOrThrow())
        .title(title(request.title)?.getOrThrow())
        .ethnicity(ethnicity(request.ethnicity)?.getOrThrow())
        .aliasNameType(nameType(request.nameType)?.getOrThrow())
        .birthPlace(oldWorkingName.birthPlace)
        .birthCountry(oldWorkingName.birthCountry)
        .idSourceCode("SEQ")
        .nameSequence("1234")
        .caseloadType("INST")
        .build()
        .let { offenderRepository.save(it) }

      if (request.isWorkingName) {
        offenderBookingRepository.findLatestOffenderBookingByNomsId(prisonerNumber)
          .orElseThrowNotFound("Prisoner with prisonerNumber %s and existing booking not found", prisonerNumber)
          .also { it.offender = newAlias }

        offenderIdentifierRepository.moveIdentifiersToNewAlias(oldWorkingName.id, newAlias.id)
      }

      return newAlias.toCorePersonRecordAlias(isWorkingName = request.isWorkingName)
    } catch (e: CannotAcquireLockException) {
      throw processLockError(e, prisonerNumber, "OFFENDERS")
    }
  }

  @Transactional
  fun updateAlias(offenderId: Long, request: UpdateAlias): CorePersonRecordAlias {
    try {
      val aliasToUpdate = offenderRepository.findByIdForUpdate(offenderId)
        .orElseThrowNotFound("Alias with offenderId %d not found", offenderId)

      val workingNameAlias = offenderRepository.findLinkedToLatestBooking(aliasToUpdate.nomsId)
        .orElseThrowNotFound("Prisoner with prisonerNumber %s and existing booking not found", aliasToUpdate.nomsId)

      val newFirstName = request.firstName.trim().uppercase()
      val newLastName = request.lastName.trim().uppercase()
      val newMiddleName1 = request.middleName1?.trim()?.uppercase()
      val newMiddleName2 = request.middleName2?.trim()?.uppercase()

      val a = aliasToUpdate.apply {
        firstName = newFirstName
        middleName = newMiddleName1
        middleName2 = newMiddleName2
        lastName = newLastName
        lastNameKey = newLastName
        lastNameAlphaKey = newLastName.take(1)
        lastNameSoundex = Soundex().soundex(newLastName)
        birthDate = request.dateOfBirth
        aliasNameType = nameType(request.nameType)?.getOrThrow()
        title = title(request.title)?.getOrThrow()
        gender = gender(request.sex)?.getOrThrow()
        ethnicity = ethnicity(request.ethnicity)?.getOrThrow()
      }.toCorePersonRecordAlias(isWorkingName = workingNameAlias.id == aliasToUpdate.id)
      return a
    } catch (e: CannotAcquireLockException) {
      throw processAliasLockError(e, offenderId)
    }
  }

  private fun ReferenceCode.toReferenceDataValue() = ReferenceDataValue(
    domain = domain,
    code = code,
    description = description,
  )

  private fun Offender.toCorePersonRecordAlias(isWorkingName: Boolean) = CorePersonRecordAlias(
    prisonerNumber = nomsId,
    offenderId = id,
    isWorkingName = isWorkingName,
    firstName = firstName,
    middleName1 = middleName,
    middleName2 = middleName2,
    lastName = lastName,
    dateOfBirth = birthDate,
    nameType = aliasNameType?.toReferenceDataValue(),
    title = title?.toReferenceDataValue(),
    sex = gender?.toReferenceDataValue(),
    ethnicity = ethnicity?.toReferenceDataValue(),
  )

  @Transactional
  fun createAddress(prisonerNumber: String, request: CreateAddress): AddressDto {
    val offender = offenderRepository.findRootOffenderByNomsId(prisonerNumber)
      .orElseThrowNotFound("Prisoner with prisonerNumber %s and existing booking not found", prisonerNumber)

    val existingAddresses = addressRepository.findByOffenderId(offender.id)

    fun lookupCity(cityCode: String): City = cityRepository.findById(City.pk(cityCode))
      .orElseThrowNotFound("City with city code %s not found", cityCode)

    fun lookupCounty(countyCode: String): County = countyRepository.findById(County.pk(countyCode))
      .orElseThrowNotFound("County with county code %s not found", countyCode)

    fun lookupCountry(countryCode: String): Country = countryRepository.findById(Country.pk(countryCode))
      .orElseThrowNotFound("Country with country code %s not found", countryCode)

    fun lookupAddressUsage(usage: String): AddressUsageType = addressUsageRepository.findById(AddressUsageType.pk(usage))
      .orElseThrowNotFound("Address usage type with code %s not found", usage)

    val newAddressBuilder = OffenderAddress.builder()
      .offender(offender)
      .flat(request.flat)
      .premise(request.premise)
      .street(request.street)
      .locality(request.locality)
      .postalCode(request.postalCode)
      .primaryFlag(if (request.primary == true) "Y" else "N")
      .mailFlag(if (request.mail == true) "Y" else "N")
      .noFixedAddressFlag(if (request.noFixedAddress == true) "Y" else "N")
      .startDate(request.startDate)
      .endDate(request.endDate)
      .country(lookupCountry(request.countryCode))

    if (request.townCode != null) newAddressBuilder.city(lookupCity(request.townCode))
    if (request.countyCode != null) newAddressBuilder.county(lookupCounty(request.countyCode))

    val newAddress = newAddressBuilder.build()

    request.addressUsages.forEach {
      newAddress.addUsage(lookupAddressUsage(it), true)
    }

    if (request.primary == true) {
      existingAddresses.filter { it.primaryFlag == "Y" }.forEach { it.primaryFlag = "N" }
    }

    if (request.mail == true) {
      existingAddresses.filter { it.mailFlag == "Y" }.forEach { it.mailFlag = "N" }
    }

    return AddressTransformer.translate(addressRepository.save(newAddress))
  }

  @Transactional
  fun getCommunicationNeeds(prisonerNumber: String): CorePersonCommunicationNeeds {
    val booking = offenderBookingRepository.findLatestOffenderBookingByNomsId(prisonerNumber)
      .orElseThrowNotFound("Prisoner with prisonerNumber %s and existing booking not found", prisonerNumber)

    val languages = offenderLanguageRepository.findByOffenderBookId(booking.bookingId)

    return CorePersonCommunicationNeeds(
      prisonerNumber,
      languagePreferences = CorePersonLanguagePreferences(
        preferredSpokenLanguage = getFirstPreferredSpokenLanguage(languages)?.referenceCode,
        preferredWrittenLanguage = getFirstPreferredWrittenLanguage(languages)?.referenceCode,
        interpreterRequired = getInterpreterRequired(languages),
      ),
      secondaryLanguages = getSecondaryLanguages(languages),
    )
  }

  @Transactional
  fun createOrUpdateLanguagePreferences(prisonerNumber: String, request: CorePersonLanguagePreferencesRequest) {
    try {
      val booking = offenderBookingRepository.findLatestOffenderBookingByNomsId(prisonerNumber)
        .orElseThrowNotFound("Prisoner with prisonerNumber %s and existing booking not found", prisonerNumber)

      val preferredWrittenLanguageReferenceCode = request.preferredWrittenLanguageCode?.takeIf { it.isNotBlank() }?.let {
        languageCodeRepository.findById(Pk(LANGUAGE_REF_DOMAIN, request.preferredWrittenLanguageCode)).orElseThrow(
          { EntityNotFoundException.withMessage("Preferred written language with code ${request.preferredWrittenLanguageCode} not found") },
        )
      }

      val preferredSpokenLanguageReferenceCode = request.preferredSpokenLanguageCode?.takeIf { it.isNotBlank() }?.let {
        languageCodeRepository.findById(Pk(LANGUAGE_REF_DOMAIN, request.preferredSpokenLanguageCode)).orElseThrow(
          { EntityNotFoundException.withMessage("Preferred spoken language with code ${request.preferredSpokenLanguageCode} not found") },
        )
      }

      val preferredWrittenOffenderLanguage = preferredWrittenLanguageReferenceCode?.let {
        OffenderLanguage.builder()
          .offenderBookId(booking.bookingId)
          .code(preferredWrittenLanguageReferenceCode.code)
          .type(PREF_WRITE_LANGUAGE_TYPE)
          .interpreterRequestedFlag("N")
          .preferredWriteFlag("Y")
          .readSkill("N")
          .writeSkill("N")
          .speakSkill("N")
          .referenceCode(preferredWrittenLanguageReferenceCode)
          .build()
      }

      val preferredSpokenOffenderLanguage = preferredSpokenLanguageReferenceCode?.let {
        OffenderLanguage.builder()
          .offenderBookId(booking.bookingId)
          .code(preferredSpokenLanguageReferenceCode.code)
          .type(PREF_SPEAK_LANGUAGE_TYPE)
          .interpreterRequestedFlag(if (request.interpreterRequired == true) "Y" else "N")
          .preferredWriteFlag("N")
          .readSkill("N")
          .writeSkill("N")
          .speakSkill("N")
          .referenceCode(preferredSpokenLanguageReferenceCode)
          .build()
      }

      val existingPreferredLanguages = offenderLanguageRepository.findByOffenderBookIdForUpdate(booking.bookingId).filter { language ->
        language.type.equals(PREF_SPEAK_LANGUAGE_TYPE, ignoreCase = true) ||
          language.type.equals(PREF_WRITE_LANGUAGE_TYPE, ignoreCase = true)
      }
      val newPreferredLanguages = mutableListOf<OffenderLanguage>().apply {
        preferredWrittenOffenderLanguage?.let { add(it) }
        preferredSpokenOffenderLanguage?.let { add(it) }
      }

      offenderLanguageRepository.deleteAll(existingPreferredLanguages)
      offenderLanguageRepository.saveAll(newPreferredLanguages)
    } catch (e: CannotAcquireLockException) {
      throw processLockError(e, prisonerNumber, "OFFENDER_LANGUAGES")
    }
  }

  @Transactional
  fun addOrUpdateSecondaryLanguage(prisonerNumber: String, request: CorePersonSecondaryLanguageRequest) {
    try {
      val booking = offenderBookingRepository.findLatestOffenderBookingByNomsId(prisonerNumber)
        .orElseThrowNotFound("Prisoner with prisonerNumber %s and existing booking not found", prisonerNumber)

      val languageReferenceCode = languageCodeRepository.findById(Pk(LANGUAGE_REF_DOMAIN, request.language)).orElseThrow {
        EntityNotFoundException.withMessage("Language with code ${request.language} not found")
      }

      val offenderLanguage = offenderLanguageRepository.findByOffenderBookIdAndCodeAndTypeForUpdate(booking.bookingId, languageReferenceCode.code, SEC_LANGUAGE_TYPE).getOrNull()?.apply {
        readSkill = if (request.canRead) "Y" else "N"
        writeSkill = if (request.canWrite) "Y" else "N"
        speakSkill = if (request.canSpeak) "Y" else "N"
      } ?: run {
        OffenderLanguage.builder()
          .offenderBookId(booking.bookingId)
          .code(languageReferenceCode.code)
          .type(SEC_LANGUAGE_TYPE)
          .interpreterRequestedFlag("N")
          .preferredWriteFlag("N")
          .readSkill(if (request.canRead) "Y" else "N")
          .writeSkill(if (request.canWrite) "Y" else "N")
          .speakSkill(if (request.canSpeak) "Y" else "N")
          .referenceCode(languageReferenceCode)
          .build()
      }

      offenderLanguageRepository.save(offenderLanguage)
    } catch (e: CannotAcquireLockException) {
      throw processLockError(e, prisonerNumber, "OFFENDER_LANGUAGES")
    }
  }

  @Transactional
  fun deleteSecondaryLanguage(prisonerNumber: String, languageCode: String) {
    try {
      val booking = offenderBookingRepository.findLatestOffenderBookingByNomsId(prisonerNumber)
        .orElseThrowNotFound("Prisoner with prisonerNumber %s and existing booking not found", prisonerNumber)

      val languageReferenceCode = languageCodeRepository.findById(Pk(LANGUAGE_REF_DOMAIN, languageCode)).orElseThrow {
        EntityNotFoundException.withMessage("Language with code $languageCode not found")
      }

      val offenderLanguage = offenderLanguageRepository.findByOffenderBookIdAndCodeAndTypeForUpdate(booking.bookingId, languageReferenceCode.code, SEC_LANGUAGE_TYPE).orElseThrow {
        EntityNotFoundException.withMessage("Secondary language with code $languageCode not found for [$prisonerNumber]")
      }

      offenderLanguageRepository.delete(offenderLanguage)
    } catch (e: CannotAcquireLockException) {
      throw processLockError(e, prisonerNumber, "OFFENDER_LANGUAGES")
    }
  }

  /*
   * NOTE: Due to NOMIS allowing multiple preferred languages to be added, both NOMIS and existing Prison API functions
   * use sorting by description to determine the 'first' preferred language. We are following the same approach here for
   * consistency during viewing existing records, but the `createOrUpdateLanguagePreferences` function clears all existing
   * preferred languages when adding/updating so going forward there should only ever by one for spoken and one for written.
   */
  private fun getFirstPreferredSpokenLanguage(languages: List<OffenderLanguage>): OffenderLanguage? = languages
    .filter { it.type.equals(PREF_SPEAK_LANGUAGE_TYPE, ignoreCase = true) && it.referenceCode != null }
    .maxByOrNull { it.referenceCode.description }

  private fun getFirstPreferredWrittenLanguage(languages: List<OffenderLanguage>): OffenderLanguage? = languages
    .filter { it.type.equals(PREF_WRITE_LANGUAGE_TYPE, ignoreCase = true) && it.referenceCode != null }
    .maxByOrNull { it.referenceCode.description }

  private fun getInterpreterRequired(languages: List<OffenderLanguage>): Boolean? = languages
    .filter { it.type.equals(PREF_SPEAK_LANGUAGE_TYPE, ignoreCase = true) && it.referenceCode != null }
    .maxByOrNull { it.referenceCode.description }?.interpreterRequestedFlag?.equals("Y", ignoreCase = true)

  private fun getSecondaryLanguages(languages: List<OffenderLanguage>): List<CorePersonSecondaryLanguage> = languages
    .filter { it.type.equals(SEC_LANGUAGE_TYPE, ignoreCase = true) }
    .map {
      CorePersonSecondaryLanguage(
        language = it.referenceCode,
        canRead = it.readSkill.equals("Y", ignoreCase = true),
        canWrite = it.writeSkill.equals("Y", ignoreCase = true),
        canSpeak = it.speakSkill.equals("Y", ignoreCase = true),
      )
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

  private inline fun <reified T> Optional<T>.orElseThrowNotFound(message: String, prisonerNumber: Any) = orElseThrow(EntityNotFoundException.withMessage(message, prisonerNumber))

  private fun country(code: String?): Result<Country>? = code?.takeIf { it.isNotBlank() }?.let {
    countryRepository.findByIdOrNull(Pk(COUNTRY, code))?.let { success(it) }
      ?: failure(EntityNotFoundException.withMessage("Country $code not found"))
  }

  private fun gender(code: String?): Result<Gender>? = code?.takeIf { it.isNotBlank() }?.let {
    genderRepository.findByIdOrNull(Pk(SEX, code))?.let { success(it) }
      ?: failure(EntityNotFoundException.withMessage("Gender $code not found"))
  }

  private fun ethnicity(code: String?): Result<Ethnicity>? = code?.takeIf { it.isNotBlank() }?.let {
    ethnicityRepository.findByIdOrNull(Pk(ETHNICITY, code))?.let { success(it) }
      ?: failure(EntityNotFoundException.withMessage("Ethnicity $code not found"))
  }

  private fun title(code: String?): Result<Title>? = code?.takeIf { it.isNotBlank() }?.let {
    titleRepository.findByIdOrNull(Pk(TITLE, code))?.let { success(it) }
      ?: failure(EntityNotFoundException.withMessage("Title $code not found"))
  }

  private fun nameType(code: String?): Result<NameType>? = code?.takeIf { it.isNotBlank() }?.let {
    nameTypeRepository.findByIdOrNull(Pk(NAME_TYPE, code))?.let { success(it) }
      ?: failure(EntityNotFoundException.withMessage("Name type $code not found"))
  }

  private fun processLockError(e: CannotAcquireLockException, prisonerNumber: String, table: String): Exception {
    log.error("Error getting lock", e)
    return if (true == e.cause?.message?.contains("ORA-30006")) {
      DatabaseRowLockedException("Failed to get $table lock for prisonerNumber=$prisonerNumber")
    } else {
      e
    }
  }

  private fun processAliasLockError(e: CannotAcquireLockException, offenderId: Long): Exception {
    log.error("Error getting lock", e)
    return if (true == e.cause?.message?.contains("ORA-30006")) {
      DatabaseRowLockedException("Failed to get alias lock for offenderId=$offenderId")
    } else {
      e
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
    const val NATIONALITY_PROFILE_TYPE = "NAT"
    const val OTHER_NATIONALITIES_PROFILE_TYPE = "NATIO"
    const val RELIGION_PROFILE_TYPE = "RELF"
    const val SEXUAL_ORIENTATION_PROFILE_TYPE = "SEXO"
    const val SMOKER_PROFILE_TYPE = "SMOKE"
    const val HAIR_PROFILE_TYPE = "HAIR"
    const val FACIAL_HAIR_PROFILE_TYPE = "FACIAL_HAIR"
    const val FACE_PROFILE_TYPE = "FACE"
    const val BUILD_PROFILE_TYPE = "BUILD"
    const val L_EYE_C_PROFILE_TYPE = "L_EYE_C"
    const val R_EYE_C_PROFILE_TYPE = "R_EYE_C"
    const val SHOESIZE_PROFILE_TYPE = "SHOESIZE"
    const val PHYSICAL_APPEARANCE_PROFILE_CATEGORY = "PA"
    val FREE_TEXT_PROFILE_CODES = listOf(OTHER_NATIONALITIES_PROFILE_TYPE, SHOESIZE_PROFILE_TYPE)

    // Communication Needs
    const val PREF_SPEAK_LANGUAGE_TYPE = "PREF_SPEAK"
    const val PREF_WRITE_LANGUAGE_TYPE = "PREF_WRITE"
    const val SEC_LANGUAGE_TYPE = "SEC"
    const val LANGUAGE_REF_DOMAIN = "LANG"
  }
}
