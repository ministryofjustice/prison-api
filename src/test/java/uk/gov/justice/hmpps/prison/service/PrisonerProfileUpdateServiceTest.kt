package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.dao.CannotAcquireLockException
import uk.gov.justice.hmpps.prison.exception.DatabaseRowLockedException
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country
import uk.gov.justice.hmpps.prison.repository.jpa.model.Country.COUNTRY
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode.Pk
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import java.util.Optional

class PrisonerProfileUpdateServiceTest {
  private val offenderRepository: OffenderRepository = mock()
  private val countryRepository: ReferenceCodeRepository<Country> = mock()
  private val offender: Offender = mock()

  private val prisonerProfileUpdateService: PrisonerProfileUpdateService =
    PrisonerProfileUpdateService(offenderRepository, countryRepository)

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

      assertThatThrownBy { prisonerProfileUpdateService.updateBirthCountryOfCurrentAlias(PRISONER_NUMBER, BIRTH_COUNTRY_CODE) }
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

      assertThatThrownBy { prisonerProfileUpdateService.updateBirthCountryOfCurrentAlias(PRISONER_NUMBER, BIRTH_COUNTRY_CODE) }
        .isInstanceOf(DatabaseRowLockedException::class.java)
    }
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val BIRTH_PLACE = "SHEFFIELD"
    const val BIRTH_COUNTRY_CODE = "GBR"

    val BIRTH_COUNTRY = Country("GBR", "Great Britain")

    @JvmStatic
    private fun nullOrBlankStrings() = listOf(null, "", " ", "  ")
  }
}
