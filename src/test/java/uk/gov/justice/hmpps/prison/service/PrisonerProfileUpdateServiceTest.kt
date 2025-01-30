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
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.dao.CannotAcquireLockException
import uk.gov.justice.hmpps.prison.api.model.UpdateReligion
import uk.gov.justice.hmpps.prison.exception.DatabaseRowLockedException
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country.COUNTRY
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
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
import java.time.LocalDate
import java.util.Optional

class PrisonerProfileUpdateServiceTest {
  private val offenderRepository: OffenderRepository = mock()
  private val countryRepository: ReferenceCodeRepository<Country> = mock()
  private val profileTypeRepository: ProfileTypeRepository = mock()
  private val profileCodeRepository: ProfileCodeRepository = mock()
  private val profileDetailRepository: OffenderProfileDetailRepository = mock()
  private val offenderBookingRepository: OffenderBookingRepository = mock()
  private val offenderBeliefRepository: OffenderBeliefRepository = mock()
  private val staffUserAccountRepository: StaffUserAccountRepository = mock()
  private val offender: Offender = mock()
  private val booking: OffenderBooking = mock()
  private val offenderProfileDetail: OffenderProfileDetail = mock()

  private val prisonerProfileUpdateService: PrisonerProfileUpdateService =
    PrisonerProfileUpdateService(
      offenderRepository,
      countryRepository,
      profileTypeRepository,
      profileCodeRepository,
      profileDetailRepository,
      offenderBookingRepository,
      offenderBeliefRepository,
      staffUserAccountRepository,
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

  private companion object {
    const val USERNAME = "username"
    const val PRISONER_NUMBER = "A1234AA"
    const val BIRTH_PLACE = "SHEFFIELD"
    const val BIRTH_COUNTRY_CODE = "GBR"
    const val BRITISH_NATIONALITY_CODE = "BRIT"
    const val DRUID_RELIGION_CODE = "DRU"

    val BIRTH_COUNTRY = Country("GBR", "Great Britain")

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

    @JvmStatic
    private fun nullOrBlankStrings() = listOf(null, "", " ", "  ")
  }
}
