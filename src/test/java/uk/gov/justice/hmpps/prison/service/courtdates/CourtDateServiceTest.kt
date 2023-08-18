package uk.gov.justice.hmpps.prison.service.courtdates

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.courtdates.CourtDateCharge
import uk.gov.justice.hmpps.prison.api.model.courtdates.CourtDateResult
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEventCharge
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtOrder
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

class CourtDateServiceTest {

  private val courtEventChargeRepository = mock<CourtEventChargeRepository>()
  private val courtDateService = CourtDateService(
    courtEventChargeRepository,
  )

  @Nested
  inner class GetCourtDateResults {
    @Test
    fun `should get dates with empty data`() {
      whenever(courtEventChargeRepository.findByOffender(PRISONER_ID)).thenReturn(listOf())

      val result = courtDateService.getCourtDateResults(PRISONER_ID)

      assertThat(result).isEmpty()
    }

    @Test
    fun `should get dates with only required relationships (minimal data)`() {
      whenever(courtEventChargeRepository.findByOffender(PRISONER_ID)).thenReturn(
        listOf(
          CourtEventCharge(
            OffenderCharge()
              .withId(1)
              .withOffence(
                Offence()
                  .withCode("OFF")
                  .withStatute(Statute().withCode("STAT"))
                  .withDescription("An offence"),
              )
              .withOffenderCourtCase(
                OffenderCourtCase()
                  .withId(3),
              )
              .withOffenderBooking(
                OffenderBooking()
                  .withBookingId(4),
              ),
            CourtEvent()
              .withId(2),
          ),
          CourtEventCharge(
            OffenderCharge()
              .withId(5)
              .withOffence(
                Offence()
                  .withCode("OFF")
                  .withStatute(Statute().withCode("STAT"))
                  .withDescription("An offence"),
              )
              .withOffenderCourtCase(
                OffenderCourtCase()
                  .withId(3),
              )
              .withOffenderBooking(
                OffenderBooking()
                  .withBookingId(4)
                  .withBookNumber("ABC123"),
              ),
            CourtEvent()
              .withId(2),
          ),
        ),
      )

      val result = courtDateService.getCourtDateResults(PRISONER_ID)

      assertThat(result).isEqualTo(
        listOf(
          CourtDateResult(
            id = 2,
            date = null,
            resultCode = null,
            resultDescription = null,
            resultDispositionCode = null,
            bookingId = 4,
            bookNumber = "ABC123",
            charge = CourtDateCharge()
              .withChargeId(1)
              .withOffenceCode("OFF")
              .withOffenceStatue("STAT")
              .withOffenceDescription("An offence")
              .withOffenceDate(null)
              .withOffenceEndDate(null)
              .withGuilty(false)
              .withCourtCaseId(3)
              .withCourtCaseRef(null)
              .withCourtLocation(null)
              .withSentenceSequence(null)
              .withSentenceDate(null)
              .withResultDescription(null),
          ),
          CourtDateResult(
            id = 2,
            date = null,
            resultCode = null,
            resultDescription = null,
            resultDispositionCode = null,
            bookingId = 4,
            bookNumber = "ABC123",
            charge = CourtDateCharge()
              .withChargeId(5)
              .withOffenceCode("OFF")
              .withOffenceStatue("STAT")
              .withOffenceDescription("An offence")
              .withOffenceDate(null)
              .withOffenceEndDate(null)
              .withGuilty(false)
              .withCourtCaseId(3)
              .withCourtCaseRef(null)
              .withCourtLocation(null)
              .withSentenceSequence(null)
              .withSentenceDate(null)
              .withResultDescription(null),
          ),
        ),
      )
    }

    @Test
    fun `should get dates with full data set`() {
      val offenceResult = OffenceResult()
        .withCode("1002")
        .withDescription("Imprisonment")
        .withDispositionCode("F")
      whenever(courtEventChargeRepository.findByOffender(PRISONER_ID)).thenReturn(
        listOf(
          CourtEventCharge(
            OffenderCharge()
              .withId(1)
              .withOffence(
                Offence()
                  .withCode("OFF")
                  .withStatute(Statute().withCode("STAT"))
                  .withDescription("An offence"),
              )
              .withOffenderCourtCase(
                OffenderCourtCase()
                  .withId(3)
                  .withCaseInfoNumber("TS1000")
                  .withAgencyLocation(
                    AgencyLocation()
                      .withDescription("Birmingham Crown Court"),
                  ),
              )
              .withOffenderBooking(
                OffenderBooking()
                  .withBookingId(4)
                  .withBookNumber("ABC123"),
              )
              .withDateOfOffence(LocalDate.of(2021, 1, 1))
              .withEndDate(LocalDate.of(2021, 6, 1))
              .withPleaCode("G")
              .withOffenderSentenceCharges(
                listOf(
                  OffenderSentenceCharge()
                    .withOffenderSentence(
                      OffenderSentence()
                        .withId(OffenderSentence.PK(4, 5))
                        .withCourtOrder(
                          CourtOrder()
                            .withCourtDate(LocalDate.of(2022, 1, 1)),
                        ),
                    ),
                ),
              )
              .withResultCodeOne(offenceResult),
            CourtEvent()
              .withId(2)
              .withEventDate(LocalDate.of(2022, 1, 1))
              .withOutcomeReasonCode(
                offenceResult,
              ),
          ),
        ),
      )

      val result = courtDateService.getCourtDateResults(PRISONER_ID)

      assertThat(result).isEqualTo(
        listOf(
          CourtDateResult(
            id = 2,
            date = LocalDate.of(2022, 1, 1),
            resultCode = "1002",
            resultDescription = "Imprisonment",
            resultDispositionCode = "F",
            bookingId = 4,
            bookNumber = "ABC123",
            charge = CourtDateCharge()
              .withChargeId(1)
              .withOffenceCode("OFF")
              .withOffenceStatue("STAT")
              .withOffenceDescription("An offence")
              .withOffenceDate(LocalDate.of(2021, 1, 1))
              .withOffenceEndDate(LocalDate.of(2021, 6, 1))
              .withGuilty(true)
              .withCourtCaseId(3)
              .withCourtCaseRef("TS1000")
              .withCourtLocation("Birmingham Crown Court")
              .withSentenceSequence(5)
              .withSentenceDate(LocalDate.of(2022, 1, 1))
              .withResultDescription("Imprisonment"),
          ),
        ),
      )
    }
  }

  companion object {
    const val PRISONER_ID = "ASD"
  }
}
