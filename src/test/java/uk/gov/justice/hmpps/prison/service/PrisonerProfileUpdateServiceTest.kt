package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.dao.CannotAcquireLockException
import uk.gov.justice.hmpps.prison.api.model.CorePersonLanguagePreferencesRequest
import uk.gov.justice.hmpps.prison.api.model.CorePersonPhysicalAttributes
import uk.gov.justice.hmpps.prison.api.model.CorePersonPhysicalAttributesRequest
import uk.gov.justice.hmpps.prison.api.model.CorePersonRecordAlias
import uk.gov.justice.hmpps.prison.api.model.CorePersonSecondaryLanguageRequest
import uk.gov.justice.hmpps.prison.api.model.CreateAlias
import uk.gov.justice.hmpps.prison.api.model.ReferenceDataValue
import uk.gov.justice.hmpps.prison.api.model.UpdateAlias
import uk.gov.justice.hmpps.prison.api.model.UpdateReligion
import uk.gov.justice.hmpps.prison.api.model.UpdateSmokerStatus
import uk.gov.justice.hmpps.prison.exception.DatabaseRowLockedException
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country.COUNTRY
import uk.gov.justice.hmpps.prison.repository.jpa.model.Ethnicity
import uk.gov.justice.hmpps.prison.repository.jpa.model.Ethnicity.ETHNICITY
import uk.gov.justice.hmpps.prison.repository.jpa.model.Gender
import uk.gov.justice.hmpps.prison.repository.jpa.model.LanguageReferenceCode
import uk.gov.justice.hmpps.prison.repository.jpa.model.NameType
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBelief
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderLanguage
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderPhysicalAttributeId
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderPhysicalAttributes
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProfileDetail
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileCode
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileType
import uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode.Pk
import uk.gov.justice.hmpps.prison.repository.jpa.model.StaffUserAccount
import uk.gov.justice.hmpps.prison.repository.jpa.model.Title
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBeliefRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderLanguageRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderProfileDetailRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ProfileCodeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ProfileTypeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository
import java.time.LocalDate
import java.util.Optional

class PrisonerProfileUpdateServiceTest {
  private val offenderRepository: OffenderRepository = mock()
  private val titleRepository: ReferenceCodeRepository<Title> = mock()
  private val genderRepository: ReferenceCodeRepository<Gender> = mock()
  private val ethnicityRepository: ReferenceCodeRepository<Ethnicity> = mock()
  private val nameTypeRepository: ReferenceCodeRepository<NameType> = mock()
  private val countryRepository: ReferenceCodeRepository<Country> = mock()
  private val profileTypeRepository: ProfileTypeRepository = mock()
  private val profileCodeRepository: ProfileCodeRepository = mock()
  private val profileDetailRepository: OffenderProfileDetailRepository = mock()
  private val offenderBookingRepository: OffenderBookingRepository = mock()
  private val offenderBeliefRepository: OffenderBeliefRepository = mock()
  private val staffUserAccountRepository: StaffUserAccountRepository = mock()
  private val offenderLanguageRepository: OffenderLanguageRepository = mock()
  private val languageCodeRepository: ReferenceCodeRepository<LanguageReferenceCode> = mock()
  private val offender: Offender = mock()
  private val booking: OffenderBooking = mock()
  private val offenderProfileDetail: OffenderProfileDetail = mock()
  private val aliasCaptor = argumentCaptor<Offender>()

  private val prisonerProfileUpdateService: PrisonerProfileUpdateService =
    PrisonerProfileUpdateService(
      offenderRepository,
      titleRepository,
      genderRepository,
      ethnicityRepository,
      nameTypeRepository,
      countryRepository,
      profileTypeRepository,
      profileCodeRepository,
      profileDetailRepository,
      offenderBookingRepository,
      offenderBeliefRepository,
      staffUserAccountRepository,
      offenderLanguageRepository,
      languageCodeRepository,
    )

  @Nested
  inner class UpdateBirthPlaceOfCurrentAlias {
    @Test
    internal fun `throws exception when there isn't a booking for the offender`() {
      whenever(offenderRepository.findLinkedToLatestBookingForUpdate(PRISONER_NUMBER)).thenReturn(Optional.empty())

      assertThatThrownBy { prisonerProfileUpdateService.updateBirthPlaceOfCurrentAlias(PRISONER_NUMBER, BIRTH_PLACE) }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessage("Prisoner with prisonerNumber A1234AA and existing booking not found")
    }

    @ParameterizedTest
    @ValueSource(strings = ["SHEFFIELD", "sheFFieLD"])
    internal fun `updates birth place`(birthplace: String) {
      whenever(offenderRepository.findLinkedToLatestBookingForUpdate(PRISONER_NUMBER))
        .thenReturn(Optional.of(offender))

      prisonerProfileUpdateService.updateBirthPlaceOfCurrentAlias(PRISONER_NUMBER, birthplace)

      verify(offender).birthPlace = BIRTH_PLACE
    }

    @ParameterizedTest
    @MethodSource("uk.gov.justice.hmpps.prison.service.PrisonerProfileUpdateServiceTest#nullOrBlankStrings")
    internal fun `updates null or blank birth place to null`(birthPlace: String?) {
      whenever(offenderRepository.findLinkedToLatestBookingForUpdate(PRISONER_NUMBER))
        .thenReturn(Optional.of(offender))

      prisonerProfileUpdateService.updateBirthPlaceOfCurrentAlias(PRISONER_NUMBER, birthPlace)

      verify(offender).birthPlace = null
    }

    @Test
    internal fun `throws DatabaseRowLockedException when database row lock times out`() {
      whenever(offenderRepository.findLinkedToLatestBookingForUpdate(PRISONER_NUMBER))
        .thenThrow(CannotAcquireLockException("", Exception("ORA-30006")))

      assertThatThrownBy { prisonerProfileUpdateService.updateBirthPlaceOfCurrentAlias(PRISONER_NUMBER, BIRTH_PLACE) }
        .isInstanceOf(DatabaseRowLockedException::class.java)
    }
  }

  @Nested
  inner class UpdateBirthCountryOfCurrentAlias {
    @Test
    internal fun `throws exception when there isn't a booking for the offender`() {
      whenever(offenderRepository.findLinkedToLatestBookingForUpdate(PRISONER_NUMBER)).thenReturn(Optional.empty())

      assertThatThrownBy {
        prisonerProfileUpdateService.updateBirthCountryOfCurrentAlias(
          PRISONER_NUMBER,
          BIRTH_COUNTRY_CODE,
        )
      }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessage("Prisoner with prisonerNumber A1234AA and existing booking not found")
    }

    @Test
    internal fun `updates birth country`() {
      whenever(offenderRepository.findLinkedToLatestBookingForUpdate(PRISONER_NUMBER))
        .thenReturn(Optional.of(offender))
      whenever(countryRepository.findById(Pk(COUNTRY, BIRTH_COUNTRY_CODE)))
        .thenReturn(Optional.of(BIRTH_COUNTRY))

      prisonerProfileUpdateService.updateBirthCountryOfCurrentAlias(PRISONER_NUMBER, BIRTH_COUNTRY_CODE)

      verify(offender).birthCountry = BIRTH_COUNTRY
    }

    @ParameterizedTest
    @MethodSource("uk.gov.justice.hmpps.prison.service.PrisonerProfileUpdateServiceTest#nullOrBlankStrings")
    internal fun `updates null or blank birth country to null`(birthCountryCode: String?) {
      whenever(offenderRepository.findLinkedToLatestBookingForUpdate(PRISONER_NUMBER))
        .thenReturn(Optional.of(offender))

      prisonerProfileUpdateService.updateBirthPlaceOfCurrentAlias(PRISONER_NUMBER, birthCountryCode)

      verify(offender).birthPlace = null
    }

    @Test
    internal fun `throws DatabaseRowLockedException when database row lock times out`() {
      whenever(offenderRepository.findLinkedToLatestBookingForUpdate(PRISONER_NUMBER))
        .thenThrow(CannotAcquireLockException("", Exception("ORA-30006")))

      assertThatThrownBy {
        prisonerProfileUpdateService.updateBirthCountryOfCurrentAlias(
          PRISONER_NUMBER,
          BIRTH_COUNTRY_CODE,
        )
      }
        .isInstanceOf(DatabaseRowLockedException::class.java)
    }
  }

  @Nested
  inner class UpdateEthnicityOfCurrentAlias {
    @Test
    internal fun `throws exception when there isn't a booking for the offender`() {
      whenever(offenderRepository.findLinkedToLatestBookingForUpdate(PRISONER_NUMBER)).thenReturn(Optional.empty())

      assertThatThrownBy {
        prisonerProfileUpdateService.updateEthnicityOfCurrentAlias(
          PRISONER_NUMBER,
          PRISONER_ETHNICITY_CODE,
        )
      }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessage("Prisoner with prisonerNumber A1234AA and existing booking not found")
    }

    @Test
    internal fun `updates ethnicity`() {
      whenever(offenderRepository.findLinkedToLatestBookingForUpdate(PRISONER_NUMBER))
        .thenReturn(Optional.of(offender))
      whenever(ethnicityRepository.findById(Pk(ETHNICITY, PRISONER_ETHNICITY_CODE)))
        .thenReturn(Optional.of(PRISONER_ETHNICITY))

      prisonerProfileUpdateService.updateEthnicityOfCurrentAlias(
        PRISONER_NUMBER,
        PRISONER_ETHNICITY_CODE,
      )

      verify(offender).ethnicity = PRISONER_ETHNICITY
    }

    @ParameterizedTest
    @MethodSource("uk.gov.justice.hmpps.prison.service.PrisonerProfileUpdateServiceTest#nullOrBlankStrings")
    internal fun `updates null or blank ethnicity to null`(ethnicityCode: String?) {
      whenever(offenderRepository.findLinkedToLatestBookingForUpdate(PRISONER_NUMBER))
        .thenReturn(Optional.of(offender))

      prisonerProfileUpdateService.updateEthnicityOfCurrentAlias(PRISONER_NUMBER, ethnicityCode)

      verify(offender).ethnicity = null
    }

    @Test
    internal fun `throws DatabaseRowLockedException when database row lock times out`() {
      whenever(offenderRepository.findLinkedToLatestBookingForUpdate(PRISONER_NUMBER))
        .thenThrow(CannotAcquireLockException("", Exception("ORA-30006")))

      assertThatThrownBy {
        prisonerProfileUpdateService.updateEthnicityOfCurrentAlias(
          PRISONER_NUMBER,
          PRISONER_ETHNICITY_CODE,
        )
      }
        .isInstanceOf(DatabaseRowLockedException::class.java)
    }
  }

  @Nested
  inner class UpdateNationalityOfLatestBooking {

    @BeforeEach
    internal fun setUp() {
      whenever(profileTypeRepository.findByTypeAndCategory(eq(NATIONALITY_PROFILE_TYPE_CODE), any()))
        .thenReturn(Optional.of(NATIONALITY_PROFILE_TYPE))
      whenever(profileTypeRepository.findByTypeAndCategory(eq(OTHER_NATIONALITIES_PROFILE_TYPE_CODE), any()))
        .thenReturn(Optional.of(OTHER_NATIONALITIES_PROFILE_TYPE))
      whenever(profileCodeRepository.findById(ProfileCode.PK(NATIONALITY_PROFILE_TYPE, BRITISH_NATIONALITY_CODE)))
        .thenReturn(Optional.of(BRITISH_NATIONALITY))
    }

    @ParameterizedTest
    @ValueSource(strings = ["BRIT", "BriT"])
    internal fun `updates nationality`(nationalityCode: String) {
      val otherNationalitiesProfileDetail: OffenderProfileDetail = mock()
      whenever(profileDetailRepository.findLinkedToLatestBookingForUpdate(PRISONER_NUMBER, NATIONALITY_PROFILE_TYPE)).thenReturn(
        Optional.of(offenderProfileDetail),
      )
      whenever(profileDetailRepository.findLinkedToLatestBookingForUpdate(PRISONER_NUMBER, OTHER_NATIONALITIES_PROFILE_TYPE)).thenReturn(
        Optional.of(otherNationalitiesProfileDetail),
      )
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER)).thenReturn(
        Optional.of(
          booking,
        ),
      )
      whenever(booking.profileDetails).thenReturn(mutableListOf(offenderProfileDetail))
      whenever(offenderProfileDetail.code).thenReturn(FRENCH_NATIONALITY)
      whenever(otherNationalitiesProfileDetail.profileCode).thenReturn("Original nationalities")

      prisonerProfileUpdateService.updateNationalityOfLatestBooking(
        PRISONER_NUMBER,
        nationalityCode,
        "Updated nationalities",
      )
      verify(offenderProfileDetail).setProfileCode(BRITISH_NATIONALITY)
      verify(otherNationalitiesProfileDetail).profileCode = "Updated nationalities"
    }

    @Test
    internal fun `adds nationality when missing`() {
      val profileDetails = mutableListOf<OffenderProfileDetail>()

      whenever(profileDetailRepository.findLinkedToLatestBookingForUpdate(eq(PRISONER_NUMBER), any())).thenReturn(
        Optional.empty(),
      )
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER)).thenReturn(
        Optional.of(booking),
      )
      whenever(booking.profileDetails).thenReturn(profileDetails)

      prisonerProfileUpdateService.updateNationalityOfLatestBooking(PRISONER_NUMBER, BRITISH_NATIONALITY_CODE, "Added nationalities")

      assertThat(booking.profileDetails).hasSize(2)
      with(booking.profileDetails[0]) {
        assertThat(code).isEqualTo(BRITISH_NATIONALITY)
      }
      with(booking.profileDetails[1]) {
        assertThat(profileCode).isEqualTo("Added nationalities")
      }
    }

    @Test
    internal fun `removes nationality`() {
      val otherNationalitiesProfileDetail: OffenderProfileDetail = mock()
      val profileDetails = mutableListOf(offenderProfileDetail)

      whenever(profileDetailRepository.findLinkedToLatestBookingForUpdate(PRISONER_NUMBER, NATIONALITY_PROFILE_TYPE)).thenReturn(
        Optional.of(offenderProfileDetail),
      )
      whenever(profileDetailRepository.findLinkedToLatestBookingForUpdate(PRISONER_NUMBER, OTHER_NATIONALITIES_PROFILE_TYPE)).thenReturn(
        Optional.of(otherNationalitiesProfileDetail),
      )
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER)).thenReturn(
        Optional.of(
          booking,
        ),
      )
      whenever(booking.profileDetails).thenReturn(profileDetails)
      whenever(offenderProfileDetail.code).thenReturn(FRENCH_NATIONALITY)
      whenever(otherNationalitiesProfileDetail.profileCode).thenReturn("Existing nationalities")

      prisonerProfileUpdateService.updateNationalityOfLatestBooking(PRISONER_NUMBER, null)

      assertThat(profileDetails).isEmpty()
    }

    @Test
    internal fun `throws exception when there isn't a booking for the offender`() {
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER)).thenReturn(Optional.empty())

      assertThatThrownBy {
        prisonerProfileUpdateService.updateNationalityOfLatestBooking(
          PRISONER_NUMBER,
          BRITISH_NATIONALITY_CODE,
        )
      }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessage("Prisoner with prisonerNumber A1234AA and existing booking not found")
    }

    @Test
    internal fun `throws exception if there isn't a matching NAT profile type`() {
      whenever(
        profileTypeRepository.findByTypeAndCategory(
          eq(NATIONALITY_PROFILE_TYPE_CODE),
          any(),
        ),
      ).thenReturn(Optional.empty())

      assertThatThrownBy {
        prisonerProfileUpdateService.updateNationalityOfLatestBooking(
          PRISONER_NUMBER,
          BRITISH_NATIONALITY_CODE,
        )
      }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessage("Resource with id [${NATIONALITY_PROFILE_TYPE_CODE}] not found.")
    }

    @Test
    internal fun `throws exception if there isn't a matching profile code for nationality`() {
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER)).thenReturn(
        Optional.of(
          booking,
        ),
      )
      whenever(profileCodeRepository.findById(ProfileCode.PK(NATIONALITY_PROFILE_TYPE, BRITISH_NATIONALITY_CODE)))
        .thenReturn(Optional.empty())

      assertThatThrownBy {
        prisonerProfileUpdateService.updateNationalityOfLatestBooking(
          PRISONER_NUMBER,
          BRITISH_NATIONALITY_CODE,
        )
      }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessage("Profile Code for $NATIONALITY_PROFILE_TYPE_CODE and $BRITISH_NATIONALITY_CODE not found")
    }

    @Test
    internal fun `throws DatabaseRowLockedException when database row lock times out`() {
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER)).thenReturn(
        Optional.of(
          booking,
        ),
      )
      whenever(profileDetailRepository.findLinkedToLatestBookingForUpdate(eq(PRISONER_NUMBER), any()))
        .thenThrow(CannotAcquireLockException("", Exception("ORA-30006")))

      assertThatThrownBy {
        prisonerProfileUpdateService.updateNationalityOfLatestBooking(
          PRISONER_NUMBER,
          BRITISH_NATIONALITY_CODE,
        )
      }
        .isInstanceOf(DatabaseRowLockedException::class.java)
    }
  }

  @Nested
  inner class UpdateReligionOfLatestBooking {

    @BeforeEach
    internal fun setUp() {
      val user = StaffUserAccount.builder().username(USERNAME).build()
      whenever(profileTypeRepository.findByTypeAndCategory(eq(RELIGION_PROFILE_TYPE_CODE), any()))
        .thenReturn(Optional.of(RELIGION_PROFILE_TYPE))
      whenever(profileCodeRepository.findById(ProfileCode.PK(RELIGION_PROFILE_TYPE, DRUID_RELIGION_CODE)))
        .thenReturn(Optional.of(DRUID_RELIGION))
      whenever(staffUserAccountRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user))
    }

    @ParameterizedTest
    @ValueSource(strings = ["DRU", "drU"])
    internal fun `updates religion and history`(religionCode: String) {
      val request = UpdateReligion(religionCode, "some comment", LocalDate.now(), true)
      whenever(profileDetailRepository.findLinkedToLatestBookingForUpdate(eq(PRISONER_NUMBER), any())).thenReturn(
        Optional.of(offenderProfileDetail),
      )
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER)).thenReturn(
        Optional.of(
          booking,
        ),
      )
      whenever(booking.profileDetails).thenReturn(listOf(offenderProfileDetail))
      whenever(booking.rootOffender).thenReturn(offender)
      whenever(offender.id).thenReturn(123456L)
      whenever(offenderProfileDetail.code).thenReturn(ZOROASTRIAN_RELIGION)
      val beliefCaptor = ArgumentCaptor.forClass(OffenderBelief::class.java)

      prisonerProfileUpdateService.updateReligionOfLatestBooking(PRISONER_NUMBER, request, USERNAME)

      verify(offenderProfileDetail, never()).setProfileCode(any<ProfileCode>())
      verify(offenderProfileDetail, never()).profileCode = any<String>()
      verify(offenderBeliefRepository).save(beliefCaptor.capture())
      val belief = beliefCaptor.value
      assertThat(belief.booking).isEqualTo(booking)
      assertThat(belief.createdByUser.username).isEqualTo(USERNAME)
      assertThat(belief.beliefCode).isEqualTo(DRUID_RELIGION)
      assertThat(belief.changeReason).isTrue()
      assertThat(belief.comments).isEqualTo("some comment")
      assertThat(belief.startDate).isEqualTo(LocalDate.now())
      assertThat(belief.verified).isTrue()
    }

    @Test
    internal fun `adds religion to history but not profile details when missing, and provides sensible default values for optional fields`() {
      val request = UpdateReligion(DRUID_RELIGION_CODE, null, null, false)
      val profileDetails = mutableListOf<OffenderProfileDetail>()

      whenever(profileDetailRepository.findLinkedToLatestBookingForUpdate(eq(PRISONER_NUMBER), any())).thenReturn(
        Optional.empty(),
      )
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER)).thenReturn(
        Optional.of(
          booking,
        ),
      )
      whenever(booking.profileDetails).thenReturn(profileDetails)
      whenever(booking.rootOffender).thenReturn(offender)
      whenever(offender.id).thenReturn(123456L)
      val beliefCaptor = ArgumentCaptor.forClass(OffenderBelief::class.java)

      prisonerProfileUpdateService.updateReligionOfLatestBooking(PRISONER_NUMBER, request, USERNAME)

      assertThat(booking.profileDetails).hasSize(0)
      verify(offenderBeliefRepository).save(beliefCaptor.capture())
      val belief = beliefCaptor.value
      assertThat(belief.booking).isEqualTo(booking)
      assertThat(belief.createdByUser.username).isEqualTo(USERNAME)
      assertThat(belief.beliefCode).isEqualTo(DRUID_RELIGION)
      assertThat(belief.changeReason).isFalse()
      assertThat(belief.comments).isNull()
      assertThat(belief.verified).isFalse()
      assertThat(belief.startDate).isEqualTo(LocalDate.now())
    }

    @Test
    internal fun `does not update religion or history if the new value matches the existing value`() {
      val request = UpdateReligion(DRUID_RELIGION_CODE, null, null, false)
      val profileDetails = mutableListOf<OffenderProfileDetail>()
      whenever(profileDetailRepository.findLinkedToLatestBookingForUpdate(eq(PRISONER_NUMBER), any())).thenReturn(
        Optional.of(offenderProfileDetail),
      )
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER)).thenReturn(
        Optional.of(
          booking,
        ),
      )
      whenever(booking.profileDetails).thenReturn(profileDetails)
      whenever(booking.rootOffender).thenReturn(offender)
      whenever(offender.id).thenReturn(123456L)
      whenever(offenderProfileDetail.code).thenReturn(DRUID_RELIGION)

      prisonerProfileUpdateService.updateReligionOfLatestBooking(PRISONER_NUMBER, request, USERNAME)

      verify(offenderProfileDetail, never()).setProfileCode(any<ProfileCode>())
      verify(offenderProfileDetail, never()).profileCode = any<String>()
      verify(offenderBeliefRepository, never()).save(any())
    }

    @Test
    internal fun `throws exception when there isn't a booking for the offender`() {
      val request = UpdateReligion(DRUID_RELIGION_CODE, null, null, false)
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER)).thenReturn(Optional.empty())

      assertThatThrownBy {
        prisonerProfileUpdateService.updateReligionOfLatestBooking(
          PRISONER_NUMBER,
          request,
          USERNAME,
        )
      }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessage("Prisoner with prisonerNumber A1234AA and existing booking not found")
    }

    @Test
    internal fun `throws exception if there isn't a matching profile type`() {
      val request = UpdateReligion(DRUID_RELIGION_CODE, null, null, false)
      whenever(
        profileTypeRepository.findByTypeAndCategory(
          eq(RELIGION_PROFILE_TYPE_CODE),
          any(),
        ),
      ).thenReturn(Optional.empty())

      assertThatThrownBy {
        prisonerProfileUpdateService.updateReligionOfLatestBooking(
          PRISONER_NUMBER,
          request,
          USERNAME,
        )
      }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessage("Resource with id [$RELIGION_PROFILE_TYPE_CODE] not found.")
    }

    @Test
    internal fun `throws exception if there isn't a matching profile code`() {
      val request = UpdateReligion(DRUID_RELIGION_CODE, null, null, false)
      whenever(profileCodeRepository.findById(ProfileCode.PK(RELIGION_PROFILE_TYPE, DRUID_RELIGION_CODE)))
        .thenReturn(Optional.empty())

      assertThatThrownBy {
        prisonerProfileUpdateService.updateReligionOfLatestBooking(
          PRISONER_NUMBER,
          request,
          USERNAME,
        )
      }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessage("Profile Code for $RELIGION_PROFILE_TYPE_CODE and $DRUID_RELIGION_CODE not found")
    }

    @Test
    internal fun `throws exception if there isn't a user profile matching the username`() {
      val request = UpdateReligion(DRUID_RELIGION_CODE, null, null, false)
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER)).thenReturn(
        Optional.of(
          booking,
        ),
      )
      whenever(staffUserAccountRepository.findByUsername(USERNAME)).thenReturn(Optional.empty())

      assertThatThrownBy {
        prisonerProfileUpdateService.updateReligionOfLatestBooking(
          PRISONER_NUMBER,
          request,
          USERNAME,
        )
      }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessage("Staff user account with provided username not found")
    }

    @Test
    internal fun `throws DatabaseRowLockedException when database row lock times out`() {
      val request = UpdateReligion(DRUID_RELIGION_CODE, null, null, false)
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER)).thenReturn(
        Optional.of(
          booking,
        ),
      )
      whenever(profileDetailRepository.findLinkedToLatestBookingForUpdate(eq(PRISONER_NUMBER), any()))
        .thenThrow(CannotAcquireLockException("", Exception("ORA-30006")))

      assertThatThrownBy {
        prisonerProfileUpdateService.updateReligionOfLatestBooking(
          PRISONER_NUMBER,
          request,
          USERNAME,
        )
      }
        .isInstanceOf(DatabaseRowLockedException::class.java)
    }
  }

  @Nested
  inner class UpdateSmokerStatusOfLatestBooking {

    @BeforeEach
    internal fun setUp() {
      whenever(profileTypeRepository.findByTypeAndCategory(eq(SMOKER_PROFILE_TYPE_CODE), any()))
        .thenReturn(Optional.of(SMOKER_PROFILE_TYPE))
      whenever(profileCodeRepository.findById(ProfileCode.PK(SMOKER_PROFILE_TYPE, SMOKER_YES_CODE)))
        .thenReturn(Optional.of(SMOKER_YES))
    }

    @ParameterizedTest
    @ValueSource(strings = ["Y", "y"])
    internal fun `updates smoker status`(smokerStatusCode: String) {
      whenever(profileDetailRepository.findLinkedToLatestBookingForUpdate(PRISONER_NUMBER, SMOKER_PROFILE_TYPE)).thenReturn(
        Optional.of(offenderProfileDetail),
      )
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER)).thenReturn(Optional.of(booking))
      whenever(booking.profileDetails).thenReturn(mutableListOf(offenderProfileDetail))
      whenever(offenderProfileDetail.code).thenReturn(SMOKER_NO)

      prisonerProfileUpdateService.updateSmokerStatusOfLatestBooking(
        PRISONER_NUMBER,
        UpdateSmokerStatus(SMOKER_YES_CODE),
      )

      verify(offenderProfileDetail).setProfileCode(SMOKER_YES)
    }

    @Test
    internal fun `adds smoker status when missing`() {
      val profileDetails = mutableListOf<OffenderProfileDetail>()

      whenever(profileDetailRepository.findLinkedToLatestBookingForUpdate(eq(PRISONER_NUMBER), any()))
        .thenReturn(Optional.empty())
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER))
        .thenReturn(Optional.of(booking))
      whenever(booking.profileDetails).thenReturn(profileDetails)

      prisonerProfileUpdateService.updateSmokerStatusOfLatestBooking(PRISONER_NUMBER, UpdateSmokerStatus(SMOKER_YES_CODE))

      assertThat(booking.profileDetails).hasSize(1)
      with(booking.profileDetails[0]) { assertThat(code).isEqualTo(SMOKER_YES) }
    }

    @Test
    internal fun `removes smoker status`() {
      val otherNationalitiesProfileDetail: OffenderProfileDetail = mock()
      val profileDetails = mutableListOf(offenderProfileDetail)

      whenever(profileDetailRepository.findLinkedToLatestBookingForUpdate(PRISONER_NUMBER, SMOKER_PROFILE_TYPE))
        .thenReturn(Optional.of(offenderProfileDetail))
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER))
        .thenReturn(Optional.of(booking))
      whenever(booking.profileDetails).thenReturn(profileDetails)
      whenever(offenderProfileDetail.code).thenReturn(SMOKER_YES)

      prisonerProfileUpdateService.updateSmokerStatusOfLatestBooking(PRISONER_NUMBER, UpdateSmokerStatus(smokerStatus = null))

      assertThat(profileDetails).isEmpty()
    }

    @Test
    internal fun `throws exception when there isn't a booking for the offender`() {
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER))
        .thenReturn(Optional.empty())

      assertThatThrownBy {
        prisonerProfileUpdateService.updateSmokerStatusOfLatestBooking(PRISONER_NUMBER, UpdateSmokerStatus(SMOKER_YES_CODE))
      }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessage("Prisoner with prisonerNumber A1234AA and existing booking not found")
    }

    @Test
    internal fun `throws exception if there isn't a matching SMOKE profile type`() {
      whenever(
        profileTypeRepository.findByTypeAndCategory(
          eq(SMOKER_PROFILE_TYPE_CODE),
          any(),
        ),
      ).thenReturn(Optional.empty())

      assertThatThrownBy {
        prisonerProfileUpdateService.updateSmokerStatusOfLatestBooking(
          PRISONER_NUMBER,
          UpdateSmokerStatus(SMOKER_YES_CODE),
        )
      }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessage("Resource with id [${SMOKER_PROFILE_TYPE_CODE}] not found.")
    }

    @Test
    internal fun `throws exception if there isn't a matching profile code for smoker status`() {
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER))
        .thenReturn(
          Optional.of(booking),
        )
      whenever(profileCodeRepository.findById(ProfileCode.PK(SMOKER_PROFILE_TYPE, SMOKER_YES_CODE)))
        .thenReturn(Optional.empty())

      assertThatThrownBy {
        prisonerProfileUpdateService.updateSmokerStatusOfLatestBooking(
          PRISONER_NUMBER,
          UpdateSmokerStatus(SMOKER_YES_CODE),
        )
      }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessage("Profile Code for ${SMOKER_PROFILE_TYPE_CODE} and ${SMOKER_YES_CODE} not found")
    }

    @Test
    internal fun `throws DatabaseRowLockedException when database row lock times out`() {
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER))
        .thenReturn(Optional.of(booking))
      whenever(profileDetailRepository.findLinkedToLatestBookingForUpdate(eq(PRISONER_NUMBER), any()))
        .thenThrow(CannotAcquireLockException("", Exception("ORA-30006")))

      assertThatThrownBy {
        prisonerProfileUpdateService.updateSmokerStatusOfLatestBooking(
          PRISONER_NUMBER,
          UpdateSmokerStatus(SMOKER_YES_CODE),
        )
      }
        .isInstanceOf(DatabaseRowLockedException::class.java)
    }
  }

  @Nested
  inner class GetPhysicalAttributes {
    @Test
    internal fun `returns physical attributes when booking exists`() {
      val booking = mock<OffenderBooking>()
      val physicalAttributes = OffenderPhysicalAttributes(id = OffenderPhysicalAttributeId(OffenderBooking.builder().bookingId(1).build(), 1), heightCentimetres = 180, weightKgs = 75)
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER))
        .thenReturn(Optional.of(booking))
      whenever(booking.latestPhysicalAttributes).thenReturn(physicalAttributes)

      val result = prisonerProfileUpdateService.getPhysicalAttributes(PRISONER_NUMBER)

      assertThat(result.height).isEqualTo(180)
      assertThat(result.weight).isEqualTo(75)
    }

    @Test
    internal fun `returns empty physical attributes when no attributes exist`() {
      val booking = mock<OffenderBooking>()
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER))
        .thenReturn(Optional.of(booking))
      whenever(booking.latestPhysicalAttributes).thenReturn(null)

      val result = prisonerProfileUpdateService.getPhysicalAttributes(PRISONER_NUMBER)

      assertThat(result).isEqualTo(CorePersonPhysicalAttributes())
    }

    @Test
    internal fun `throws exception when there isn't a booking for the offender`() {
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER))
        .thenReturn(Optional.empty())

      assertThatThrownBy { prisonerProfileUpdateService.getPhysicalAttributes(PRISONER_NUMBER) }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessage("Prisoner with prisonerNumber A1234AA and existing booking not found")
    }
  }

  @Nested
  inner class UpdatePhysicalAttributes {
    private val request = CorePersonPhysicalAttributesRequest(height = 190, weight = 80)

    @BeforeEach
    internal fun setUp() {
      whenever(profileTypeRepository.findByTypeAndCategory(HAIR_PROFILE_TYPE_CODE, PHYSICAL_ATTRIBUTES_PROFILE_CATEGORY_CODE))
        .thenReturn(Optional.of(HAIR_PROFILE_TYPE))
      whenever(profileCodeRepository.findById(ProfileCode.PK(HAIR_PROFILE_TYPE, HAIR_BROWN_CODE)))
        .thenReturn(Optional.of(HAIR_BROWN))

      whenever(profileTypeRepository.findByTypeAndCategory(FACIAL_HAIR_PROFILE_TYPE_CODE, PHYSICAL_ATTRIBUTES_PROFILE_CATEGORY_CODE))
        .thenReturn(Optional.of(FACIAL_HAIR_PROFILE_TYPE))
      whenever(profileCodeRepository.findById(ProfileCode.PK(FACIAL_HAIR_PROFILE_TYPE, FACIAL_HAIR_BEARDED_CODE)))
        .thenReturn(Optional.of(FACIAL_HAIR_BEARDED))

      whenever(profileTypeRepository.findByTypeAndCategory(FACE_PROFILE_TYPE_CODE, PHYSICAL_ATTRIBUTES_PROFILE_CATEGORY_CODE))
        .thenReturn(Optional.of(FACE_PROFILE_TYPE))
      whenever(profileCodeRepository.findById(ProfileCode.PK(FACE_PROFILE_TYPE, FACE_ROUND_CODE)))
        .thenReturn(Optional.of(FACE_ROUND))

      whenever(profileTypeRepository.findByTypeAndCategory(BUILD_PROFILE_TYPE_CODE, PHYSICAL_ATTRIBUTES_PROFILE_CATEGORY_CODE))
        .thenReturn(Optional.of(BUILD_PROFILE_TYPE))
      whenever(profileCodeRepository.findById(ProfileCode.PK(BUILD_PROFILE_TYPE, BUILD_MEDIUM_CODE)))
        .thenReturn(Optional.of(BUILD_MEDIUM))

      whenever(profileTypeRepository.findByTypeAndCategory(LEFT_EYE_COLOUR_PROFILE_TYPE_CODE, PHYSICAL_ATTRIBUTES_PROFILE_CATEGORY_CODE))
        .thenReturn(Optional.of(LEFT_EYE_COLOUR_PROFILE_TYPE))
      whenever(profileCodeRepository.findById(ProfileCode.PK(LEFT_EYE_COLOUR_PROFILE_TYPE, LEFT_EYE_COLOUR_BLUE_CODE)))
        .thenReturn(Optional.of(LEFT_EYE_COLOUR_BLUE))

      whenever(profileTypeRepository.findByTypeAndCategory(RIGHT_EYE_COLOUR_PROFILE_TYPE_CODE, PHYSICAL_ATTRIBUTES_PROFILE_CATEGORY_CODE))
        .thenReturn(Optional.of(RIGHT_EYE_COLOUR_PROFILE_TYPE))
      whenever(profileCodeRepository.findById(ProfileCode.PK(RIGHT_EYE_COLOUR_PROFILE_TYPE, RIGHT_EYE_COLOUR_BLUE_CODE)))
        .thenReturn(Optional.of(RIGHT_EYE_COLOUR_BLUE))

      whenever(profileTypeRepository.findByTypeAndCategory(SHOESIZE_PROFILE_TYPE_CODE, PHYSICAL_ATTRIBUTES_PROFILE_CATEGORY_CODE))
        .thenReturn(Optional.of(SHOESIZE_PROFILE_TYPE))
      whenever(profileCodeRepository.findById(ProfileCode.PK(RIGHT_EYE_COLOUR_PROFILE_TYPE, RIGHT_EYE_COLOUR_BLUE_CODE)))
        .thenReturn(Optional.of(RIGHT_EYE_COLOUR_BLUE))
    }

    @Test
    internal fun `updates physical attributes when booking exists`() {
      val booking = mock<OffenderBooking>()
      val physicalAttributes = OffenderPhysicalAttributes(id = OffenderPhysicalAttributeId(OffenderBooking.builder().bookingId(1).build(), 1), heightCentimetres = 180, weightKgs = 75)
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsIdForUpdate(PRISONER_NUMBER))
        .thenReturn(Optional.of(booking))
      whenever(booking.latestPhysicalAttributes).thenReturn(physicalAttributes)

      prisonerProfileUpdateService.updatePhysicalAttributes(PRISONER_NUMBER, request)

      assertThat(physicalAttributes.heightCentimetres).isEqualTo(190)
      assertThat(physicalAttributes.weightKgs).isEqualTo(80)
      verify(offenderBookingRepository).save(booking)
    }

    @Test
    internal fun `adds physical attributes when none exist`() {
      val booking = mock<OffenderBooking>()
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsIdForUpdate(PRISONER_NUMBER))
        .thenReturn(Optional.of(booking))
      whenever(booking.offenderPhysicalAttributes).thenReturn(mutableListOf())
      whenever(booking.latestPhysicalAttributes).thenReturn(null)

      prisonerProfileUpdateService.updatePhysicalAttributes(PRISONER_NUMBER, request)

      assertThat(booking.offenderPhysicalAttributes).hasSize(1)
      assertThat(booking.offenderPhysicalAttributes[0].heightCentimetres).isEqualTo(190)
      assertThat(booking.offenderPhysicalAttributes[0].weightKgs).isEqualTo(80)
      verify(offenderBookingRepository).save(booking)
    }

    @Test
    internal fun `throws exception when there isn't a booking for the offender`() {
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsIdForUpdate(PRISONER_NUMBER))
        .thenReturn(Optional.empty())

      assertThatThrownBy { prisonerProfileUpdateService.updatePhysicalAttributes(PRISONER_NUMBER, request) }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessage("Prisoner with prisonerNumber A1234AA and existing booking not found")
    }

    @Test
    internal fun `throws DatabaseRowLockedException when database row lock times out`() {
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsIdForUpdate(PRISONER_NUMBER))
        .thenThrow(CannotAcquireLockException("", Exception("ORA-30006")))

      assertThatThrownBy { prisonerProfileUpdateService.updatePhysicalAttributes(PRISONER_NUMBER, request) }
        .isInstanceOf(DatabaseRowLockedException::class.java)
    }
  }

  @Nested
  inner class CreateAndUpdateAlias {

    private lateinit var existingAlias: Offender

    private val expectedResponse = CorePersonRecordAlias(
      offenderId = 0,
      prisonerNumber = PRISONER_NUMBER,
      firstName = FIRST_NAME,
      middleName1 = MIDDLE_NAME_1,
      middleName2 = MIDDLE_NAME_2,
      lastName = LAST_NAME,
      dateOfBirth = BIRTH_DATE,
      nameType = ReferenceDataValue(NAME_TYPE.domain, NAME_TYPE.code, NAME_TYPE.description),
      title = ReferenceDataValue(TITLE.domain, TITLE.code, TITLE.description),
      sex = ReferenceDataValue(GENDER.domain, GENDER.code, GENDER.description),
      ethnicity = ReferenceDataValue(PRISONER_ETHNICITY.domain, PRISONER_ETHNICITY.code, PRISONER_ETHNICITY.description),
      isWorkingName = true,
    )

    @BeforeEach
    internal fun setUp() {
      existingAlias = Offender.builder()
        .id(OFFENDER_ID)
        .rootOffenderId(ROOT_OFFENDER_ID)
        .aliasOffenderId(ALIAS_OFFENDER_ID)
        .firstName("OLD_${FIRST_NAME}")
        .middleName("OLD_${MIDDLE_NAME_1}")
        .middleName2("OLD_${MIDDLE_NAME_2}")
        .lastName("OLD_${LAST_NAME}")
        .lastNameKey("OLD_${LAST_NAME}")
        .lastNameSoundex("OLD_${LAST_NAME_SOUNDEX}")
        .lastNameAlphaKey("OLD_${LAST_NAME_ALPHA}")
        .nomsId(PRISONER_NUMBER)
        .birthCountry(BIRTH_COUNTRY)
        .birthPlace(BIRTH_PLACE)
        .birthDate(BIRTH_DATE.minusDays(1))
        .gender(Gender("F", "Female"))
        .ethnicity(Ethnicity("O1", "Chinese"))
        .title(Title("MS", "Ms"))
        .aliasNameType(NameType("NICK", "Nickname"))
        .build()

      whenever(offenderRepository.findByIdForUpdate(OFFENDER_ID)).thenReturn(Optional.of(existingAlias))
      whenever(offenderRepository.findLinkedToLatestBooking(PRISONER_NUMBER)).thenReturn(Optional.of(existingAlias))
      whenever(offenderRepository.findLinkedToLatestBookingForUpdate(PRISONER_NUMBER)).thenReturn(Optional.of(existingAlias))
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER)).thenReturn(Optional.of(booking))
      whenever(nameTypeRepository.findById(NAME_TYPE.primaryKey)).thenReturn(Optional.of(NAME_TYPE))
      whenever(genderRepository.findById(GENDER.primaryKey)).thenReturn(Optional.of(GENDER))
      whenever(ethnicityRepository.findById(PRISONER_ETHNICITY.primaryKey)).thenReturn(Optional.of(PRISONER_ETHNICITY))
      whenever(titleRepository.findById(TITLE.primaryKey)).thenReturn(Optional.of(TITLE))
      whenever(offenderRepository.save(aliasCaptor.capture()))
        .thenAnswer { aliasCaptor.firstValue.also { it.id = NEW_OFFENDER_ID } }
    }

    @Nested
    inner class CreateAlias {
      private val request = CreateAlias(
        firstName = FIRST_NAME,
        middleName1 = MIDDLE_NAME_1,
        middleName2 = MIDDLE_NAME_2,
        lastName = LAST_NAME,
        dateOfBirth = BIRTH_DATE,
        nameType = NAME_TYPE.code,
        title = TITLE.code,
        sex = GENDER.code,
        ethnicity = PRISONER_ETHNICITY.code,
        isWorkingName = true,
      )

      @Test
      internal fun `can create a non-working name alias`() {
        val alias = prisonerProfileUpdateService.createAlias(PRISONER_NUMBER, request.copy(isWorkingName = false))

        assertNewAliasSaved()
        assertThat(alias).isEqualTo(expectedResponse.copy(offenderId = NEW_OFFENDER_ID, isWorkingName = false))
        verify(booking, never()).offender
      }

      @Test
      internal fun `can create a working name alias`() {
        val alias = prisonerProfileUpdateService.createAlias(PRISONER_NUMBER, request.copy(isWorkingName = true))

        assertNewAliasSaved()
        assertThat(alias).isEqualTo(expectedResponse.copy(offenderId = NEW_OFFENDER_ID, isWorkingName = true))
        verify(booking).offender = aliasCaptor.firstValue
      }

      @Test
      internal fun `throws exception when there isn't an offender`() {
        whenever(offenderRepository.findLinkedToLatestBookingForUpdate(PRISONER_NUMBER))
          .thenReturn(Optional.empty())

        assertThatThrownBy { prisonerProfileUpdateService.createAlias(PRISONER_NUMBER, request) }
          .isInstanceOf(EntityNotFoundException::class.java)
          .hasMessage("Prisoner with prisonerNumber A1234AA and existing booking not found")
      }

      @Test
      internal fun `throws exception when there isn't a booking`() {
        whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER))
          .thenReturn(Optional.empty())

        assertThatThrownBy { prisonerProfileUpdateService.createAlias(PRISONER_NUMBER, request.copy(isWorkingName = true)) }
          .isInstanceOf(EntityNotFoundException::class.java)
          .hasMessage("Prisoner with prisonerNumber A1234AA and existing booking not found")
      }

      @Test
      internal fun `throws exception when title is not valid`() {
        whenever(titleRepository.findById(TITLE.primaryKey)).thenReturn(Optional.empty())

        assertThatThrownBy { prisonerProfileUpdateService.createAlias(PRISONER_NUMBER, request) }
          .isInstanceOf(EntityNotFoundException::class.java)
          .hasMessage("Title MR not found")
      }

      @Test
      internal fun `throws exception when gender is not valid`() {
        whenever(genderRepository.findById(GENDER.primaryKey)).thenReturn(Optional.empty())

        assertThatThrownBy { prisonerProfileUpdateService.createAlias(PRISONER_NUMBER, request) }
          .isInstanceOf(EntityNotFoundException::class.java)
          .hasMessage("Gender M not found")
      }

      @Test
      internal fun `throws exception when ethnicity is not valid`() {
        whenever(ethnicityRepository.findById(PRISONER_ETHNICITY.primaryKey)).thenReturn(Optional.empty())

        assertThatThrownBy { prisonerProfileUpdateService.createAlias(PRISONER_NUMBER, request) }
          .isInstanceOf(EntityNotFoundException::class.java)
          .hasMessage("Ethnicity W1 not found")
      }

      @Test
      internal fun `throws exception when name type is not valid`() {
        whenever(nameTypeRepository.findById(NAME_TYPE.primaryKey)).thenReturn(Optional.empty())

        assertThatThrownBy { prisonerProfileUpdateService.createAlias(PRISONER_NUMBER, request) }
          .isInstanceOf(EntityNotFoundException::class.java)
          .hasMessage("Name type CN not found")
      }

      @Test
      internal fun `throws DatabaseRowLockedException when database row lock times out`() {
        whenever(offenderRepository.findLinkedToLatestBookingForUpdate(PRISONER_NUMBER))
          .thenThrow(CannotAcquireLockException("", Exception("ORA-30006")))

        assertThatThrownBy { prisonerProfileUpdateService.createAlias(PRISONER_NUMBER, request) }
          .isInstanceOf(DatabaseRowLockedException::class.java)
      }

      private fun assertNewAliasSaved() {
        val newAlias = aliasCaptor.firstValue
        assertThat(newAlias.nomsId).isEqualTo(PRISONER_NUMBER)
        assertThat(newAlias.rootOffenderId).isEqualTo(ROOT_OFFENDER_ID)
        assertThat(newAlias.aliasOffenderId).isEqualTo(ALIAS_OFFENDER_ID)
        assertThat(newAlias.lastName).isEqualTo(LAST_NAME)
        assertThat(newAlias.lastNameKey).isEqualTo(LAST_NAME)
        assertThat(newAlias.lastNameAlphaKey).isEqualTo(LAST_NAME_ALPHA)
        assertThat(newAlias.lastNameSoundex).isEqualTo(LAST_NAME_SOUNDEX)
        assertThat(newAlias.firstName).isEqualTo(FIRST_NAME)
        assertThat(newAlias.middleName).isEqualTo(MIDDLE_NAME_1)
        assertThat(newAlias.birthDate).isEqualTo(BIRTH_DATE)
        assertThat(newAlias.gender).isEqualTo(GENDER)
        assertThat(newAlias.title).isEqualTo(TITLE)
        assertThat(newAlias.ethnicity).isEqualTo(PRISONER_ETHNICITY)
        assertThat(newAlias.birthPlace).isEqualTo(BIRTH_PLACE)
        assertThat(newAlias.birthCountry).isEqualTo(BIRTH_COUNTRY)
        assertThat(newAlias.aliasNameType).isEqualTo(NAME_TYPE)
        assertThat(newAlias.idSourceCode).isEqualTo("SEQ")
        assertThat(newAlias.nameSequence).isEqualTo("1234")
        assertThat(newAlias.caseloadType).isEqualTo("INST")
      }
    }

    @Nested
    inner class UpdateAlias {

      private val request = UpdateAlias(
        firstName = FIRST_NAME,
        middleName1 = MIDDLE_NAME_1,
        middleName2 = MIDDLE_NAME_2,
        lastName = LAST_NAME,
        dateOfBirth = BIRTH_DATE,
        nameType = NAME_TYPE.code,
        title = TITLE.code,
        sex = GENDER.code,
        ethnicity = PRISONER_ETHNICITY.code,
      )

      @Test
      internal fun `can update a working-name alias`() {
        whenever(offenderRepository.findLinkedToLatestBooking(PRISONER_NUMBER)).thenReturn(Optional.of(existingAlias))

        val response = prisonerProfileUpdateService.updateAlias(OFFENDER_ID, request)

        assertThat(response).isEqualTo(expectedResponse.copy(offenderId = OFFENDER_ID, isWorkingName = true))
        assertExistingAliasUpdated()
      }

      @Test
      internal fun `can update a non working-name alias`() {
        whenever(offender.id).thenReturn(999L)
        whenever(offenderRepository.findLinkedToLatestBooking(PRISONER_NUMBER)).thenReturn(Optional.of(offender))

        val response = prisonerProfileUpdateService.updateAlias(OFFENDER_ID, request)

        assertThat(response).isEqualTo(expectedResponse.copy(offenderId = OFFENDER_ID, isWorkingName = false))
        assertExistingAliasUpdated()
      }

      @Test
      internal fun `throws exception when there isn't an offender`() {
        whenever(offenderRepository.findByIdForUpdate(OFFENDER_ID))
          .thenReturn(Optional.empty())

        assertThatThrownBy { prisonerProfileUpdateService.updateAlias(OFFENDER_ID, request) }
          .isInstanceOf(EntityNotFoundException::class.java)
          .hasMessage("Alias with offenderId 111111 not found")
      }

      @Test
      internal fun `throws exception when title is not valid`() {
        whenever(titleRepository.findById(TITLE.primaryKey)).thenReturn(Optional.empty())

        assertThatThrownBy { prisonerProfileUpdateService.updateAlias(OFFENDER_ID, request) }
          .isInstanceOf(EntityNotFoundException::class.java)
          .hasMessage("Title MR not found")
      }

      @Test
      internal fun `throws exception when gender is not valid`() {
        whenever(genderRepository.findById(GENDER.primaryKey)).thenReturn(Optional.empty())

        assertThatThrownBy { prisonerProfileUpdateService.updateAlias(OFFENDER_ID, request) }
          .isInstanceOf(EntityNotFoundException::class.java)
          .hasMessage("Gender M not found")
      }

      @Test
      internal fun `throws exception when ethnicity is not valid`() {
        whenever(ethnicityRepository.findById(PRISONER_ETHNICITY.primaryKey)).thenReturn(Optional.empty())

        assertThatThrownBy { prisonerProfileUpdateService.updateAlias(OFFENDER_ID, request) }
          .isInstanceOf(EntityNotFoundException::class.java)
          .hasMessage("Ethnicity W1 not found")
      }

      @Test
      internal fun `throws exception when name type is not valid`() {
        whenever(nameTypeRepository.findById(NAME_TYPE.primaryKey)).thenReturn(Optional.empty())

        assertThatThrownBy { prisonerProfileUpdateService.updateAlias(OFFENDER_ID, request) }
          .isInstanceOf(EntityNotFoundException::class.java)
          .hasMessage("Name type CN not found")
      }

      @Test
      internal fun `throws DatabaseRowLockedException when database row lock times out`() {
        whenever(offenderRepository.findByIdForUpdate(OFFENDER_ID))
          .thenThrow(CannotAcquireLockException("", Exception("ORA-30006")))

        assertThatThrownBy { prisonerProfileUpdateService.updateAlias(OFFENDER_ID, request) }
          .isInstanceOf(DatabaseRowLockedException::class.java)
      }

      private fun assertExistingAliasUpdated() {
        assertThat(existingAlias.firstName).isEqualTo(FIRST_NAME)
        assertThat(existingAlias.middleName).isEqualTo(MIDDLE_NAME_1)
        assertThat(existingAlias.lastName).isEqualTo(LAST_NAME)
        assertThat(existingAlias.lastNameKey).isEqualTo(LAST_NAME)
        assertThat(existingAlias.lastNameAlphaKey).isEqualTo(LAST_NAME_ALPHA)
        assertThat(existingAlias.lastNameSoundex).isEqualTo(LAST_NAME_SOUNDEX)
        assertThat(existingAlias.birthDate).isEqualTo(BIRTH_DATE)
        assertThat(existingAlias.aliasNameType).isEqualTo(NAME_TYPE)
        assertThat(existingAlias.title).isEqualTo(TITLE)
        assertThat(existingAlias.gender).isEqualTo(GENDER)
        assertThat(existingAlias.ethnicity).isEqualTo(PRISONER_ETHNICITY)
      }
    }
  }

  @Nested
  inner class GetCommunicationNeeds {
    @Test
    internal fun `returns communication needs when booking exists`() {
      val booking = mock<OffenderBooking>()
      val languages = listOf(
        OffenderLanguage.builder().type("PREF_SPEAK").referenceCode(LanguageReferenceCode("ENG", "English")).build(),
        OffenderLanguage.builder().type("SEC").referenceCode(LanguageReferenceCode("SPA", "Spanish")).build(),
      )
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER))
        .thenReturn(Optional.of(booking))
      whenever(offenderLanguageRepository.findByOffenderBookId(booking.bookingId)).thenReturn(languages)

      val result = prisonerProfileUpdateService.getCommunicationNeeds(PRISONER_NUMBER)

      assertThat(result.languagePreferences?.preferredSpokenLanguage?.code).isEqualTo("ENG")
      assertThat(result.secondaryLanguages).hasSize(1)
      assertThat(result.secondaryLanguages[0].language.code).isEqualTo("SPA")
    }

    @Test
    internal fun `throws exception when there isn't a booking for the offender`() {
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER))
        .thenReturn(Optional.empty())

      assertThatThrownBy { prisonerProfileUpdateService.getCommunicationNeeds(PRISONER_NUMBER) }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessage("Prisoner with prisonerNumber A1234AA and existing booking not found")
    }
  }

  @Nested
  inner class CreateOrUpdateLanguagePreferences {
    private val request = CorePersonLanguagePreferencesRequest(
      preferredSpokenLanguageCode = "ENG",
      preferredWrittenLanguageCode = "ENG",
      interpreterRequired = true,
    )

    @Test
    internal fun `creates or updates language preferences - empty list`() {
      val booking = mock<OffenderBooking>()
      val languageReferenceCode = LanguageReferenceCode("ENG", "English")
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER))
        .thenReturn(Optional.of(booking))
      whenever(languageCodeRepository.findById(any())).thenReturn(Optional.of(languageReferenceCode))
      whenever(offenderLanguageRepository.findByOffenderBookIdForUpdate(booking.bookingId)).thenReturn(emptyList())

      prisonerProfileUpdateService.createOrUpdateLanguagePreferences(PRISONER_NUMBER, request)

      verify(offenderLanguageRepository).deleteAll(any<List<OffenderLanguage>>())
      verify(offenderLanguageRepository).saveAll(any<List<OffenderLanguage>>())
    }

    @Test
    internal fun `creates or updates language preferences - existing languages`() {
      val booking = mock<OffenderBooking>()
      val languageReferenceCode = LanguageReferenceCode("ENG", "English")
      val offenderLanguages = listOf(
        OffenderLanguage.builder().type("PREF_SPEAK").referenceCode(LanguageReferenceCode("ITA", "Italian")).build(),
        OffenderLanguage.builder().type("PREF_SPEAK").referenceCode(LanguageReferenceCode("SPA", "Spanish")).build(),
      )
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER))
        .thenReturn(Optional.of(booking))
      whenever(languageCodeRepository.findById(any())).thenReturn(Optional.of(languageReferenceCode))
      whenever(offenderLanguageRepository.findByOffenderBookIdForUpdate(booking.bookingId)).thenReturn(offenderLanguages)

      prisonerProfileUpdateService.createOrUpdateLanguagePreferences(PRISONER_NUMBER, request)

      verify(offenderLanguageRepository).deleteAll(offenderLanguages)
      verify(offenderLanguageRepository).saveAll(any<List<OffenderLanguage>>())
    }

    @Test
    internal fun `throws exception when there isn't a booking for the offender`() {
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER))
        .thenReturn(Optional.empty())

      assertThatThrownBy { prisonerProfileUpdateService.createOrUpdateLanguagePreferences(PRISONER_NUMBER, request) }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessage("Prisoner with prisonerNumber A1234AA and existing booking not found")
    }

    @Test
    internal fun `throws DatabaseRowLockedException when database row lock times out`() {
      val languageReferenceCode = LanguageReferenceCode("ENG", "English")
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER))
        .thenReturn(Optional.of(booking))
      whenever(languageCodeRepository.findById(any())).thenReturn(Optional.of(languageReferenceCode))
      whenever(offenderLanguageRepository.findByOffenderBookIdForUpdate(booking.bookingId))
        .thenThrow(CannotAcquireLockException("", Exception("ORA-30006")))

      assertThatThrownBy { prisonerProfileUpdateService.createOrUpdateLanguagePreferences(PRISONER_NUMBER, request) }
        .isInstanceOf(DatabaseRowLockedException::class.java)
    }
  }

  @Nested
  inner class AddOrUpdateSecondaryLanguage {
    private val request = CorePersonSecondaryLanguageRequest(
      language = "SPA",
      canRead = true,
      canWrite = true,
      canSpeak = true,
    )

    @Test
    internal fun `adds secondary language`() {
      val booking = mock<OffenderBooking>()
      val languageReferenceCode = LanguageReferenceCode("SPA", "Spanish")
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER))
        .thenReturn(Optional.of(booking))
      whenever(languageCodeRepository.findById(any())).thenReturn(Optional.of(languageReferenceCode))
      whenever(offenderLanguageRepository.findByOffenderBookIdAndCodeAndTypeForUpdate(booking.bookingId, "SPA", "SEC"))
        .thenReturn(Optional.empty())

      prisonerProfileUpdateService.addOrUpdateSecondaryLanguage(PRISONER_NUMBER, request)

      verify(offenderLanguageRepository).save(any())
    }

    @Test
    internal fun `updates secondary language`() {
      val booking = mock<OffenderBooking>()
      val languageReferenceCode = LanguageReferenceCode("SPA", "Spanish")
      val offenderLanguage = OffenderLanguage.builder().code("SPA").build()
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER))
        .thenReturn(Optional.of(booking))
      whenever(languageCodeRepository.findById(any())).thenReturn(Optional.of(languageReferenceCode))
      whenever(offenderLanguageRepository.findByOffenderBookIdAndCodeAndTypeForUpdate(booking.bookingId, "SPA", "SEC"))
        .thenReturn(Optional.of(offenderLanguage))

      prisonerProfileUpdateService.addOrUpdateSecondaryLanguage(PRISONER_NUMBER, request)

      verify(offenderLanguageRepository).save(any())
    }

    @Test
    internal fun `throws exception when there isn't a booking for the offender`() {
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER))
        .thenReturn(Optional.empty())

      assertThatThrownBy { prisonerProfileUpdateService.addOrUpdateSecondaryLanguage(PRISONER_NUMBER, request) }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessage("Prisoner with prisonerNumber A1234AA and existing booking not found")
    }

    @Test
    internal fun `throws DatabaseRowLockedException when database row lock times out`() {
      val languageReferenceCode = LanguageReferenceCode("SPA", "Spanish")
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER))
        .thenReturn(Optional.of(booking))
      whenever(languageCodeRepository.findById(any())).thenReturn(Optional.of(languageReferenceCode))
      whenever(offenderLanguageRepository.findByOffenderBookIdAndCodeAndTypeForUpdate(booking.bookingId, "SPA", "SEC"))
        .thenThrow(CannotAcquireLockException("", Exception("ORA-30006")))

      assertThatThrownBy { prisonerProfileUpdateService.addOrUpdateSecondaryLanguage(PRISONER_NUMBER, request) }
        .isInstanceOf(DatabaseRowLockedException::class.java)
    }
  }

  @Nested
  inner class DeleteSecondaryLanguage {
    @Test
    internal fun `deletes secondary language`() {
      val booking = mock<OffenderBooking>()
      val languageReferenceCode = LanguageReferenceCode("SPA", "Spanish")
      val offenderLanguage = OffenderLanguage.builder().code("SPA").build()
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER))
        .thenReturn(Optional.of(booking))
      whenever(languageCodeRepository.findById(any())).thenReturn(Optional.of(languageReferenceCode))
      whenever(offenderLanguageRepository.findByOffenderBookIdAndCodeAndTypeForUpdate(booking.bookingId, "SPA", "SEC"))
        .thenReturn(Optional.of(offenderLanguage))

      prisonerProfileUpdateService.deleteSecondaryLanguage(PRISONER_NUMBER, "SPA")

      verify(offenderLanguageRepository).delete(offenderLanguage)
    }

    @Test
    internal fun `throws exception when there isn't a booking for the offender`() {
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER))
        .thenReturn(Optional.empty())

      assertThatThrownBy { prisonerProfileUpdateService.deleteSecondaryLanguage(PRISONER_NUMBER, "SPA") }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessage("Prisoner with prisonerNumber A1234AA and existing booking not found")
    }

    @Test
    internal fun `throws DatabaseRowLockedException when database row lock times out`() {
      val languageReferenceCode = LanguageReferenceCode("SPA", "Spanish")
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER))
        .thenReturn(Optional.of(booking))
      whenever(languageCodeRepository.findById(any())).thenReturn(Optional.of(languageReferenceCode))
      whenever(offenderLanguageRepository.findByOffenderBookIdAndCodeAndTypeForUpdate(booking.bookingId, "SPA", "SEC"))
        .thenThrow(CannotAcquireLockException("", Exception("ORA-30006")))

      assertThatThrownBy { prisonerProfileUpdateService.deleteSecondaryLanguage(PRISONER_NUMBER, "SPA") }
        .isInstanceOf(DatabaseRowLockedException::class.java)
    }
  }

  private companion object {
    const val USERNAME = "username"
    const val PRISONER_NUMBER = "A1234AA"
    const val OFFENDER_ID = 111111L
    const val NEW_OFFENDER_ID = 222222L
    const val BIRTH_PLACE = "SHEFFIELD"
    const val BRITISH_NATIONALITY_CODE = "BRIT"
    const val DRUID_RELIGION_CODE = "DRU"
    const val FIRST_NAME = "JOHN"
    const val MIDDLE_NAME_1 = "MIDDLEONE"
    const val MIDDLE_NAME_2 = "MIDDLETWO"
    const val LAST_NAME = "SMITH"
    const val LAST_NAME_ALPHA = "S"
    const val LAST_NAME_SOUNDEX = "S530"
    const val ROOT_OFFENDER_ID = 123456L
    const val ALIAS_OFFENDER_ID = 654321L

    val BIRTH_DATE = LocalDate.now()

    const val BIRTH_COUNTRY_CODE = "GBR"
    val BIRTH_COUNTRY = Country(BIRTH_COUNTRY_CODE, "Great Britain")

    val GENDER = Gender("M", "Male")

    const val PRISONER_ETHNICITY_CODE = "W1"
    val PRISONER_ETHNICITY = Ethnicity("W1", "White British")

    val TITLE = Title("MR", "Mr.")

    val NAME_TYPE = NameType("CN", "Current Name")

    const val NATIONALITY_PROFILE_TYPE_CODE = "NAT"
    val NATIONALITY_PROFILE_TYPE =
      ProfileType(NATIONALITY_PROFILE_TYPE_CODE, "PI", "Nationality", false, true, "CODE", true, null, null)
    val BRITISH_NATIONALITY =
      ProfileCode(ProfileCode.PK(NATIONALITY_PROFILE_TYPE, BRITISH_NATIONALITY_CODE), "British", true, true, null, null)
    val FRENCH_NATIONALITY =
      ProfileCode(ProfileCode.PK(NATIONALITY_PROFILE_TYPE, "FREN"), "French", true, true, null, null)

    const val OTHER_NATIONALITIES_PROFILE_TYPE_CODE = "NATIO"
    val OTHER_NATIONALITIES_PROFILE_TYPE =
      ProfileType(
        OTHER_NATIONALITIES_PROFILE_TYPE_CODE,
        "PI",
        "Other nationalities",
        false,
        true,
        "TEXT",
        true,
        null,
        null,
      )

    const val RELIGION_PROFILE_TYPE_CODE = "RELF"
    val RELIGION_PROFILE_TYPE =
      ProfileType(RELIGION_PROFILE_TYPE_CODE, "PI", "Religion", false, true, "CODE", true, null, null)
    val DRUID_RELIGION =
      ProfileCode(ProfileCode.PK(RELIGION_PROFILE_TYPE, DRUID_RELIGION_CODE), "Druid", true, true, null, null)
    val ZOROASTRIAN_RELIGION =
      ProfileCode(ProfileCode.PK(RELIGION_PROFILE_TYPE, "ZORO"), "Zoroastrian", true, true, null, null)

    const val SMOKER_PROFILE_TYPE_CODE = "SMOKE"
    val SMOKER_PROFILE_TYPE =
      ProfileType(SMOKER_PROFILE_TYPE_CODE, "PI", "Smoker", false, true, "CODE", true, null, null)
    const val SMOKER_NO_CODE = "N"
    const val SMOKER_YES_CODE = "Y"
    val SMOKER_NO =
      ProfileCode(ProfileCode.PK(SMOKER_PROFILE_TYPE, SMOKER_NO_CODE), "No", true, true, null, null)
    val SMOKER_YES =
      ProfileCode(ProfileCode.PK(SMOKER_PROFILE_TYPE, SMOKER_YES_CODE), "Yes", true, true, null, null)

    const val PHYSICAL_ATTRIBUTES_PROFILE_CATEGORY_CODE = "PA"
    const val HAIR_PROFILE_TYPE_CODE = "HAIR"
    val HAIR_PROFILE_TYPE =
      ProfileType(HAIR_PROFILE_TYPE_CODE, "PA", "Hair", false, true, "CODE", true, null, null)
    const val HAIR_BROWN_CODE = "BROWN"
    val HAIR_BROWN =
      ProfileCode(ProfileCode.PK(HAIR_PROFILE_TYPE, HAIR_BROWN_CODE), "Brown", true, true, null, null)

    const val FACIAL_HAIR_PROFILE_TYPE_CODE = "FACIAL_HAIR"
    val FACIAL_HAIR_PROFILE_TYPE =
      ProfileType(FACIAL_HAIR_PROFILE_TYPE_CODE, "PA", "Facial hair", false, true, "CODE", true, null, null)
    const val FACIAL_HAIR_BEARDED_CODE = "BEARDED"
    val FACIAL_HAIR_BEARDED =
      ProfileCode(ProfileCode.PK(FACIAL_HAIR_PROFILE_TYPE, FACIAL_HAIR_BEARDED_CODE), "Full beard", true, true, null, null)

    const val BUILD_PROFILE_TYPE_CODE = "BUILD"
    val BUILD_PROFILE_TYPE =
      ProfileType(BUILD_PROFILE_TYPE_CODE, "PA", "Build", false, true, "CODE", true, null, null)
    const val BUILD_MEDIUM_CODE = "MEDIUM"
    val BUILD_MEDIUM =
      ProfileCode(ProfileCode.PK(BUILD_PROFILE_TYPE, BUILD_MEDIUM_CODE), "Medium", true, true, null, null)

    const val FACE_PROFILE_TYPE_CODE = "FACE"
    val FACE_PROFILE_TYPE =
      ProfileType(FACE_PROFILE_TYPE_CODE, "PA", "Face", false, true, "CODE", true, null, null)
    const val FACE_ROUND_CODE = "ROUND"
    val FACE_ROUND =
      ProfileCode(ProfileCode.PK(FACE_PROFILE_TYPE, FACE_ROUND_CODE), "Round", true, true, null, null)

    const val LEFT_EYE_COLOUR_PROFILE_TYPE_CODE = "L_EYE_C"
    val LEFT_EYE_COLOUR_PROFILE_TYPE =
      ProfileType(LEFT_EYE_COLOUR_PROFILE_TYPE_CODE, "PA", "Left eye colour", false, true, "CODE", true, null, null)
    const val LEFT_EYE_COLOUR_BLUE_CODE = "BLUE"
    val LEFT_EYE_COLOUR_BLUE =
      ProfileCode(ProfileCode.PK(LEFT_EYE_COLOUR_PROFILE_TYPE, LEFT_EYE_COLOUR_BLUE_CODE), "Blue", true, true, null, null)

    const val RIGHT_EYE_COLOUR_PROFILE_TYPE_CODE = "R_EYE_C"
    val RIGHT_EYE_COLOUR_PROFILE_TYPE =
      ProfileType(RIGHT_EYE_COLOUR_PROFILE_TYPE_CODE, "PA", "Right eye colour", false, true, "CODE", true, null, null)
    const val RIGHT_EYE_COLOUR_BLUE_CODE = "BLUE"
    val RIGHT_EYE_COLOUR_BLUE =
      ProfileCode(ProfileCode.PK(RIGHT_EYE_COLOUR_PROFILE_TYPE, RIGHT_EYE_COLOUR_BLUE_CODE), "Blue", true, true, null, null)

    const val SHOESIZE_PROFILE_TYPE_CODE = "SHOESIZE"
    val SHOESIZE_PROFILE_TYPE =
      ProfileType(SHOESIZE_PROFILE_TYPE_CODE, "PA", "Shoe size", false, true, "TEXT", true, null, null)

    @JvmStatic
    private fun nullOrBlankStrings() = listOf(null, "", " ", "  ")
  }
}
