package uk.gov.justice.hmpps.prison.service.transformers

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offence
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenceResult
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCharge
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCourtCase
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSentence
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSentenceCharge
import uk.gov.justice.hmpps.prison.repository.jpa.model.Statute
import java.time.LocalDate

class OffenderChargeTransformerTest {
  @Test
  fun `should transform offender charge to offender history detail`() {
    val bookingId = 328L
    val courtCaseId = 439L
    val offenceDate = LocalDate.now().minusMonths(1)
    val endDate = LocalDate.now().minusDays(12)
    val firstCourtDate = LocalDate.now()
    val lastCourtDate = firstCourtDate.plusDays(5)
    val offenceCode = "offence code"
    val offenceDescription = "offence description"
    val statuteCode = "statute code"
    val offenseResult1Description = "offense result 1"
    val offenseResult2Description = "offense result 2"
    val courtEvents = listOf(CourtEvent.builder().eventDate(firstCourtDate).build(), CourtEvent.builder().eventDate(lastCourtDate).build())
    val offence = Offence.builder()
      .code(offenceCode)
      .description(offenceDescription)
      .statute(Statute.builder().code(statuteCode).build())
      .build()
    val offenseResult1 = OffenceResult()
      .withCode("result code 1")
      .withDescription(offenseResult1Description)
      .withConvictionFlag(true)
    val offenseResult2 = OffenceResult()
      .withCode("result code 2")
      .withDescription(offenseResult2Description)
      .withConvictionFlag(false)
    val anOffenderCharge = OffenderCharge.builder()
      .dateOfOffence(offenceDate)
      .endDate(endDate)
      .offenderBooking(OffenderBooking.builder().bookingId(bookingId).build())
      .offenderCourtCase(OffenderCourtCase.builder().id(courtCaseId).courtEvents(courtEvents).build())
      .offence(offence)
      .mostSeriousFlag("Y")
      .resultCodeOne(offenseResult1)
      .resultCodeTwo(offenseResult2)
      .build()

    val offenderHistoryDetails = OffenderChargeTransformer().convert(anOffenderCharge)

    assertThat(offenderHistoryDetails.bookingId).isEqualTo(bookingId)
    assertThat(offenderHistoryDetails.caseId).isEqualTo(courtCaseId)
    assertThat(offenderHistoryDetails.courtDate).isEqualTo(lastCourtDate)
    assertThat(offenderHistoryDetails.mostSerious).isTrue()
    assertThat(offenderHistoryDetails.offenceCode).isEqualTo(offenceCode)
    assertThat(offenderHistoryDetails.offenceDate).isEqualTo(offenceDate)
    assertThat(offenderHistoryDetails.offenceRangeDate).isEqualTo(endDate)
    assertThat(offenderHistoryDetails.offenceDescription).isEqualTo(offenceDescription)
    assertThat(offenderHistoryDetails.primaryResultCode).isEqualTo(offenseResult1.code)
    assertThat(offenderHistoryDetails.primaryResultConviction).isEqualTo(offenseResult1.isConvictionFlag)
    assertThat(offenderHistoryDetails.primaryResultDescription).isEqualTo(offenseResult1.description)
    assertThat(offenderHistoryDetails.secondaryResultCode).isEqualTo(offenseResult2.code)
    assertThat(offenderHistoryDetails.secondaryResultConviction).isEqualTo(offenseResult2.isConvictionFlag)
    assertThat(offenderHistoryDetails.secondaryResultDescription).isEqualTo(offenseResult2.description)
    assertThat(offenderHistoryDetails.statuteCode).isEqualTo(statuteCode)
  }

  @Test
  fun `should set sentence information to null if no associated sentences`() {
    val offenderHistoryDetails = OffenderCharge.builder()
      .offenderBooking(OffenderBooking.builder().bookingId(2L).build())
      .offenderCourtCase(OffenderCourtCase.builder().id(1L).build())
      .offence(Offence.builder().code("code").description("desc").statute(Statute.builder().code("st code").build()).build())
      .build().let {
        OffenderChargeTransformer().convert(it)
      }
    assertThat(offenderHistoryDetails.sentenceStartDate).isNull()
    assertThat(offenderHistoryDetails.primarySentence as Boolean?).isNull()
  }

  @Test
  fun `should set sentence information if there is an associated sentence`() {
    val offenderHistoryDetails = OffenderCharge.builder()
      .offenderBooking(OffenderBooking.builder().bookingId(2L).build())
      .offenderCourtCase(OffenderCourtCase.builder().id(1L).build())
      .offenderSentenceCharges(
        listOf(
          OffenderSentenceCharge.builder().offenderSentence(
            OffenderSentence.builder().sentenceStartDate(LocalDate.parse("2022-01-02")).build(),
          ).build(),
        ),
      )
      .offence(Offence.builder().code("code").description("desc").statute(Statute.builder().code("st code").build()).build())
      .build().let {
        OffenderChargeTransformer().convert(it)
      }
    assertThat(offenderHistoryDetails.sentenceStartDate).isEqualTo(LocalDate.parse("2022-01-02"))
    assertThat(offenderHistoryDetails.primarySentence as Boolean?).isTrue()
  }

  @Test
  fun `should set primary sentence to false if there is an associated consecutive sentence`() {
    val offenderHistoryDetails = OffenderCharge.builder()
      .offenderBooking(OffenderBooking.builder().bookingId(2L).build())
      .offenderCourtCase(OffenderCourtCase.builder().id(1L).build())
      .offenderSentenceCharges(
        listOf(
          OffenderSentenceCharge.builder().offenderSentence(
            OffenderSentence.builder().sentenceStartDate(LocalDate.parse("2022-01-02")).consecutiveToSentenceSequence(3).build(),
          ).build(),
        ),
      )
      .offence(Offence.builder().code("code").description("desc").statute(Statute.builder().code("st code").build()).build())
      .build().let {
        OffenderChargeTransformer().convert(it)
      }
    assertThat(offenderHistoryDetails.sentenceStartDate).isEqualTo(LocalDate.parse("2022-01-02"))
    assertThat(offenderHistoryDetails.primarySentence as Boolean?).isFalse()
  }
}
