package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.*
import org.springframework.dao.CannotAcquireLockException
import uk.gov.justice.hmpps.prison.exception.DatabaseRowLockedException
import uk.gov.justice.hmpps.prison.repository.jpa.model.*
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country.COUNTRY
import uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode.Pk
import uk.gov.justice.hmpps.prison.repository.jpa.repository.*
import java.util.*

class PrisonerProfileUpdateServiceTest {
  private val offenderRepository: OffenderRepository = mock()
  private val offenderProfileDetailRepository: OffenderProfileDetailRepository = mock()
  private val countryRepository: ReferenceCodeRepository<Country> = mock()
  private val profileTypeRepository: ProfileTypeRepository = mock()
  private val profileCodeRepository: ProfileCodeRepository = mock()
  private val offender: Offender = mock()
  private val offenderProfileDetail: OffenderProfileDetail = mock()

  private val prisonerProfileUpdateService: PrisonerProfileUpdateService =
    PrisonerProfileUpdateService(
      offenderRepository,
      offenderProfileDetailRepository,
      countryRepository,
      profileTypeRepository,
      profileCodeRepository,
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

    @Test
    internal fun `updates birth place`() {
      whenever(offenderRepository.findLinkedToLatestBookingForUpdate(PRISONER_NUMBER))
        .thenReturn(Optional.of(offender))

      prisonerProfileUpdateService.updateBirthPlaceOfCurrentAlias(PRISONER_NUMBER, BIRTH_PLACE)

      verify(offender).birthPlace = BIRTH_PLACE
    }

    @Test
    internal fun `enforces capitalisation when updating birth place`() {
      whenever(offenderRepository.findLinkedToLatestBookingForUpdate(PRISONER_NUMBER))
        .thenReturn(Optional.of(offender))

      prisonerProfileUpdateService.updateBirthPlaceOfCurrentAlias(PRISONER_NUMBER, "sheFFieLD")

      verify(offender).birthPlace = "SHEFFIELD"
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
  inner class UpdateNationalityOfCurrentBooking {

    @BeforeEach
    internal fun setUp() {
      whenever(profileTypeRepository.findByTypeAndCategoryAndActive(eq("NAT"), any(), any()))
        .thenReturn(Optional.of(NATIONALITY_PROFILE_TYPE))
      whenever(profileCodeRepository.findById(ProfileCode.PK(NATIONALITY_PROFILE_TYPE, BRITISH_NATIONALITY_CODE)))
        .thenReturn(Optional.of(BRITISH_NATIONALITY))
    }

    @Test
    internal fun `updates nationality`() {
      whenever(offenderProfileDetailRepository.findNationalityLinkedToLatestBookingForUpdate(PRISONER_NUMBER))
        .thenReturn(Optional.of(offenderProfileDetail))

      prisonerProfileUpdateService.updateNationalityOfCurrentBooking(PRISONER_NUMBER, BRITISH_NATIONALITY_CODE)

      verify(offenderProfileDetail).code = BRITISH_NATIONALITY
    }

    @Test
    internal fun `enforces capitalisation when updating nationality`() {
      whenever(offenderProfileDetailRepository.findNationalityLinkedToLatestBookingForUpdate(PRISONER_NUMBER))
        .thenReturn(Optional.of(offenderProfileDetail))

      prisonerProfileUpdateService.updateNationalityOfCurrentBooking(PRISONER_NUMBER, "bRiT")

      verify(offenderProfileDetail).code = BRITISH_NATIONALITY
    }

    @Test
    internal fun `removes nationality`() {
      whenever(offenderProfileDetailRepository.findNationalityLinkedToLatestBookingForUpdate(PRISONER_NUMBER))
        .thenReturn(Optional.of(offenderProfileDetail))
      offenderProfileDetail.code = BRITISH_NATIONALITY

      prisonerProfileUpdateService.updateNationalityOfCurrentBooking(PRISONER_NUMBER, null)

      verify(offenderProfileDetail).code = null
    }

    @Test
    internal fun `throws exception when there isn't a booking for the offender`() {
      whenever(offenderProfileDetailRepository.findNationalityLinkedToLatestBookingForUpdate(PRISONER_NUMBER)).thenReturn(
        Optional.empty(),
      )

      assertThatThrownBy {
        prisonerProfileUpdateService.updateNationalityOfCurrentBooking(
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
        prisonerProfileUpdateService.updateNationalityOfCurrentBooking(
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
        prisonerProfileUpdateService.updateNationalityOfCurrentBooking(
          PRISONER_NUMBER,
          BRITISH_NATIONALITY_CODE,
        )
      }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessage("Profile Code for NAT and BRIT not found")
    }

    @Test
    internal fun `throws DatabaseRowLockedException when database row lock times out`() {
      whenever(offenderProfileDetailRepository.findNationalityLinkedToLatestBookingForUpdate(PRISONER_NUMBER))
        .thenThrow(CannotAcquireLockException("", Exception("ORA-30006")))

      assertThatThrownBy {
        prisonerProfileUpdateService.updateNationalityOfCurrentBooking(
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

    @JvmStatic
    private fun nullOrBlankStrings() = listOf(null, "", " ", "  ")
  }
}
