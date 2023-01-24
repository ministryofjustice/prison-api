package uk.gov.justice.hmpps.prison.service.digitalwarrant

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.digitalwarrant.Charge
import uk.gov.justice.hmpps.prison.api.model.digitalwarrant.CourtDateResult
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEventCharge
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offence
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenceResult
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCharge
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCourtCase
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSentence
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSentenceCharge
import uk.gov.justice.hmpps.prison.repository.jpa.model.Statute
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventChargeRepository
import java.time.LocalDate


class DigitalWarrantServiceTest {

  private val courtEventChargeRepository = mock<CourtEventChargeRepository>()
  private val digitalWarrantService = DigitalWarrantService(
    mock(),
    mock(),
    mock(),
    mock(),
    mock(),
    mock(),
    mock(),
    mock(),
    mock(),
    mock(),
    mock(),
    mock(),
    mock(),
    mock(),
    mock(),
    mock(),
    courtEventChargeRepository,
    mock(),
    mock(),
    mock(),
  )

  @Nested
  inner class GetCourtDateResults {
    @Test
    fun `should get dates with empty data`() {
      whenever(courtEventChargeRepository.findByOffender(PRISONER_ID)).thenReturn(listOf())

      val result = digitalWarrantService.getCourtDateResults(PRISONER_ID)

      assertThat(result).isEmpty()
    }

    @Test
    fun `should get dates with only required relationships (minimal data)`() {
      whenever(courtEventChargeRepository.findByOffender(PRISONER_ID)).thenReturn(listOf(
        CourtEventCharge(
          OffenderCharge()
            .withId(1)
            .withOffence(
              Offence()
                .withCode("OFF")
                .withStatute(Statute().withCode("STAT"))
            )
            .withOffenderCourtCase(
              OffenderCourtCase()
                .withId(3)
            )
            .withOffenderBooking(
              OffenderBooking()
                .withBookingId(4)
            ),
          CourtEvent()
            .withId(2)
        )
      ))

      val result = digitalWarrantService.getCourtDateResults(PRISONER_ID)

      assertThat(result).isEqualTo(
        listOf(
          CourtDateResult(
            id = 2,
            date = null,
            resultCode = null,
            resultDescription = null,
            bookingId = 4,
            charge = Charge()
              .withChargeId(1)
              .withOffenceCode("OFF")
              .withOffenceStatue("STAT")
              .withOffenceDate(null)
              .withOffenceEndDate(null)
              .withGuilty(false)
              .withCourtCaseId(3)
              .withSentenceSequence(null)
          )
        )
      )
    }

    @Test
    fun `should get dates with full data set`() {
      whenever(courtEventChargeRepository.findByOffender(PRISONER_ID)).thenReturn(listOf(
        CourtEventCharge(
          OffenderCharge()
            .withId(1)
            .withOffence(
              Offence()
                .withCode("OFF")
                .withStatute(Statute().withCode("STAT"))
            )
            .withOffenderCourtCase(
              OffenderCourtCase()
                .withId(3)
            )
            .withOffenderBooking(
              OffenderBooking()
                .withBookingId(4)
            )
            .withDateOfOffence(LocalDate.of(2021, 1,1))
            .withEndDate(LocalDate.of(2021, 6,1))
            .withPleaCode("G")
            .withOffenderSentenceCharges(
              listOf(
                OffenderSentenceCharge()
                  .withOffenderSentence(
                    OffenderSentence()
                      .withId(OffenderSentence.PK(4, 5))
                  )
              )
            ),
          CourtEvent()
            .withId(2)
            .withEventDate(LocalDate.of(2022, 1,1))
            .withOutcomeReasonCode(
              OffenceResult()
                .withCode("1002")
                .withDescription("Imprisonment")
            )
        )
      ))

      val result = digitalWarrantService.getCourtDateResults(PRISONER_ID)

      assertThat(result).isEqualTo(
        listOf(
          CourtDateResult(
            id = 2,
            date = LocalDate.of(2022, 1,1),
            resultCode = "1002",
            resultDescription = "Imprisonment",
            bookingId = 4,
            charge = Charge()
              .withChargeId(1)
              .withOffenceCode("OFF")
              .withOffenceStatue("STAT")
              .withOffenceDate(LocalDate.of(2021, 1,1))
              .withOffenceEndDate(LocalDate.of(2021, 6,1))
              .withGuilty(true)
              .withCourtCaseId(3)
              .withSentenceSequence(5)
          )
        )
      )
    }
  }

  companion object {
    const val PRISONER_ID = "ASD"
  }
}