@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.InmateDetail
import uk.gov.justice.hmpps.prison.api.resource.UpdatePrisonerDetails
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.service.enteringandleaving.BookingIntoPrisonService
import uk.gov.justice.hmpps.prison.service.enteringandleaving.ReleasePrisonerService
import java.util.Optional

class SmokeTestHelperServiceTest {
  private val inmateService: InmateService = mock()
  private val releasePrisonerService: ReleasePrisonerService = mock()
  private val offenderRepository: OffenderRepository = mock()
  private val bookingIntoPrisonService: BookingIntoPrisonService = mock()
  private val smokeTestHelperService = SmokeTestHelperService(
    inmateService,
    releasePrisonerService,
    bookingIntoPrisonService,
    offenderRepository,
  )

  @Nested
  internal inner class updatePrisonerDetails {
    @Test
    fun notFoundRequest() {
      assertThatThrownBy { smokeTestHelperService.updatePrisonerDetails(SOME_OFFENDER_NO, UpdatePrisonerDetails("joe", "bloggs")) }
        .isInstanceOf(EntityNotFoundException::class.java)
        .hasMessage("Offender $SOME_OFFENDER_NO not found")
    }

    @Test
    fun ok() {
      whenever(offenderRepository.findOffenderByNomsId(any()))
        .thenReturn(
          Optional.of(
            Offender().apply {
              firstName = "Joe"
              lastName = "Bloggs"
            },
          ),
        )

      smokeTestHelperService.updatePrisonerDetails(SOME_OFFENDER_NO, UpdatePrisonerDetails("Fred", "Smith"))
      verify(offenderRepository).save(
        check {
          assertThat(it.firstName).isEqualTo("FRED")
          assertThat(it.lastName).isEqualTo("SMITH")
        },
      )
    }
  }

  @Nested
  internal inner class offenderStatusSetup {
    @Test
    fun notFoundRequest() {
      whenever(inmateService.findOffender(any(), anyBoolean(), anyBoolean()))
        .thenThrow(EntityNotFoundException.withMessage("Offender Not found"))
      assertThatThrownBy { smokeTestHelperService.offenderStatusSetup(SOME_OFFENDER_NO) }
        .isInstanceOf(EntityNotFoundException::class.java)
    }

    @Test
    fun wrongStatus() {
      whenever(inmateService.findOffender(any(), anyBoolean(), anyBoolean()))
        .thenReturn(InmateDetail.builder().inOutStatus("TRN").agencyId("MDI").build())

      assertThatThrownBy { smokeTestHelperService.offenderStatusSetup(SOME_OFFENDER_NO) }
        .isInstanceOf(IllegalStateException::class.java)
        .hasMessage("The offender should have status 'IN' but has status 'TRN' for agencyId MDI, unable to recall into ${SmokeTestHelperService.SMOKE_TEST_PRISON_ID} Prison")

      verifyNoInteractions(bookingIntoPrisonService)
    }

    @Test
    fun okIn() {
      whenever(inmateService.findOffender(any(), anyBoolean(), anyBoolean()))
        .thenReturn(InmateDetail.builder().inOutStatus("IN").agencyId("MDI").build())

      smokeTestHelperService.offenderStatusSetup(SOME_OFFENDER_NO)

      verifyNoInteractions(bookingIntoPrisonService)
    }

    @Test
    fun okOut() {
      whenever(inmateService.findOffender(any(), anyBoolean(), anyBoolean()))
        .thenReturn(InmateDetail.builder().inOutStatus("OUT").agencyId("OUT").build())
      whenever(bookingIntoPrisonService.recallPrisoner(eq(SOME_OFFENDER_NO), any()))
        .thenReturn(InmateDetail.builder().inOutStatus("OUT").agencyId("OUT").build())

      smokeTestHelperService.offenderStatusSetup(SOME_OFFENDER_NO)

      verify(bookingIntoPrisonService).recallPrisoner(eq(SOME_OFFENDER_NO), any())
    }
  }

  companion object {
    private const val SOME_OFFENDER_NO = "A1060AA"
  }
}
