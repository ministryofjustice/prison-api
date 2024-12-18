package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.dao.CannotAcquireLockException
import uk.gov.justice.hmpps.prison.exception.DatabaseRowLockedException
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country.COUNTRY
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
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
import java.util.Optional

class PrisonerProfileUpdateServiceTest {
  private val offenderRepository: OffenderRepository = mock()
  private val countryRepository: ReferenceCodeRepository<Country> = mock()
  private val profileTypeRepository: ProfileTypeRepository = mock()
  private val profileCodeRepository: ProfileCodeRepository = mock()
  private val profileDetailRepository: OffenderProfileDetailRepository = mock()
  private val offenderBookingRepository: OffenderBookingRepository = mock()
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
      whenever(profileTypeRepository.findByTypeAndCategoryAndActive(eq("NAT"), any(), any()))
        .thenReturn(Optional.of(NATIONALITY_PROFILE_TYPE))
      whenever(profileCodeRepository.findById(ProfileCode.PK(NATIONALITY_PROFILE_TYPE, BRITISH_NATIONALITY_CODE)))
        .thenReturn(Optional.of(BRITISH_NATIONALITY))
    }

    @ParameterizedTest
    @ValueSource(strings = ["BRIT", "BriT"])
    internal fun `updates nationality`(nationalityCode: String) {
      whenever(profileDetailRepository.findLinkedToLatestBookingForUpdate(eq(PRISONER_NUMBER), any())).thenReturn(Optional.of(offenderProfileDetail))
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER)).thenReturn(Optional.of(booking))
      whenever(booking.profileDetails).thenReturn(listOf(offenderProfileDetail))
      whenever(offenderProfileDetail.code).thenReturn(FRENCH_NATIONALITY)

      prisonerProfileUpdateService.updateNationalityOfLatestBooking(PRISONER_NUMBER, nationalityCode)

      verify(offenderProfileDetail).setProfileCode(BRITISH_NATIONALITY)
    }

    @Test
    internal fun `adds nationality when missing`() {
      val profileDetails = mutableListOf<OffenderProfileDetail>()

      whenever(profileDetailRepository.findLinkedToLatestBookingForUpdate(eq(PRISONER_NUMBER), any())).thenReturn(Optional.empty())
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER)).thenReturn(Optional.of(booking))
      whenever(booking.profileDetails).thenReturn(profileDetails)

      prisonerProfileUpdateService.updateNationalityOfLatestBooking(PRISONER_NUMBER, BRITISH_NATIONALITY_CODE)

      assertThat(booking.profileDetails).hasSize(1)
      with(booking.profileDetails[0]) {
        assertThat(code).isEqualTo(BRITISH_NATIONALITY)
      }
    }

    @Test
    internal fun `removes nationality`() {
      val profileDetails = mutableListOf(offenderProfileDetail)

      whenever(profileDetailRepository.findLinkedToLatestBookingForUpdate(eq(PRISONER_NUMBER), any())).thenReturn(Optional.of(offenderProfileDetail))
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER)).thenReturn(Optional.of(booking))
      whenever(booking.profileDetails).thenReturn(profileDetails)
      whenever(offenderProfileDetail.code).thenReturn(FRENCH_NATIONALITY)

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
    internal fun `throws exception if there isn't a matching profile type`() {
      whenever(
        profileTypeRepository.findByTypeAndCategoryAndActive(
          eq("NAT"),
          any(),
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
        .hasMessage("Resource with id [NAT] not found.")
    }

    @Test
    internal fun `throws exception if there isn't a matching profile code`() {
      whenever(profileCodeRepository.findById(ProfileCode.PK(NATIONALITY_PROFILE_TYPE, BRITISH_NATIONALITY_CODE)))
        .thenReturn(Optional.empty())

      assertThatThrownBy {
        prisonerProfileUpdateService.updateNationalityOfLatestBooking(
          PRISONER_NUMBER,
          BRITISH_NATIONALITY_CODE,
        )
      }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessage("Profile Code for NAT and BRIT not found")
    }

    @Test
    internal fun `throws DatabaseRowLockedException when database row lock times out`() {
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(PRISONER_NUMBER)).thenReturn(Optional.of(booking))
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

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val BIRTH_PLACE = "SHEFFIELD"
    const val BIRTH_COUNTRY_CODE = "GBR"
    const val BRITISH_NATIONALITY_CODE = "BRIT"

    val BIRTH_COUNTRY = Country("GBR", "Great Britain")

    val NATIONALITY_PROFILE_TYPE =
      ProfileType("NAT", "PI", "Nationality", false, true, "CODE", true, null, null)
    val BRITISH_NATIONALITY =
      ProfileCode(ProfileCode.PK(NATIONALITY_PROFILE_TYPE, BRITISH_NATIONALITY_CODE), "British", true, true, null, null)
    val FRENCH_NATIONALITY =
      ProfileCode(ProfileCode.PK(NATIONALITY_PROFILE_TYPE, "FREN"), "French", true, true, null, null)

    @JvmStatic
    private fun nullOrBlankStrings() = listOf(null, "", " ", "  ")
  }
}
