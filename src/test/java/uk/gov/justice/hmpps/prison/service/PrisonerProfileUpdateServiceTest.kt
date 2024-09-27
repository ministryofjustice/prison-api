package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import java.util.Optional

class PrisonerProfileUpdateServiceTest {
  private val offenderRepository: OffenderRepository = mock()
  private val offenderBookingRepository: OffenderBookingRepository = mock()

  private val prisonerProfileUpdateService: PrisonerProfileUpdateService =
    PrisonerProfileUpdateService(offenderRepository, offenderBookingRepository)

  @Nested
  inner class UpdateBirthPlaceOfCurrentAlias {
    @Test
    internal fun `throws exception when there isn't a booking for the offender`() {
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(any())).thenReturn(Optional.empty())

      assertThatThrownBy { prisonerProfileUpdateService.updateBirthPlaceOfCurrentAlias(PRISONER_NUMBER, BIRTH_PLACE) }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessage("Prisoner with prisonerNumber A1234AA and existing booking not found")
    }

    @Test
    internal fun `updates birth place`() {
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(any()))
        .thenReturn(Optional.of(OffenderBooking()))

      prisonerProfileUpdateService.updateBirthPlaceOfCurrentAlias(PRISONER_NUMBER, BIRTH_PLACE)

      verify(offenderRepository).updateBirthPlaceOfCurrentAlias(PRISONER_NUMBER, BIRTH_PLACE)
    }

    @ParameterizedTest
    @MethodSource("uk.gov.justice.hmpps.prison.service.PrisonerProfileUpdateServiceTest#nullOrBlankBirthPlaces")
    internal fun `updates null or blank birth place to null`(birthPlace: String?) {
      whenever(offenderBookingRepository.findLatestOffenderBookingByNomsId(any()))
        .thenReturn(Optional.of(OffenderBooking()))

      prisonerProfileUpdateService.updateBirthPlaceOfCurrentAlias(PRISONER_NUMBER, birthPlace)

      verify(offenderRepository).updateBirthPlaceOfCurrentAlias(PRISONER_NUMBER, null)
    }
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val BIRTH_PLACE = "SHEFFIELD"

    @JvmStatic
    private fun nullOrBlankBirthPlaces() = listOf(null, "", " ", "  ")
  }
}
