package uk.gov.justice.hmpps.prison.service.transformers

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.hmpps.prison.api.model.Agency
import uk.gov.justice.hmpps.prison.api.model.CourtCase
import uk.gov.justice.hmpps.prison.api.model.CourtHearing
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent
import uk.gov.justice.hmpps.prison.repository.jpa.model.LegalCaseType
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCourtCase
import java.time.LocalDate
import java.time.LocalTime

class CourtCaseTransformerTest {
  private lateinit var offenderCourtCase: OffenderCourtCase
  private lateinit var courtLocation: AgencyLocation

  @BeforeEach
  fun setup() {
    val booking = OffenderBooking.builder()
      .bookingId(-1L)
      .location(
        AgencyLocation.builder()
          .id("LEI")
          .active(true)
          .type(AgencyLocationType("INST"))
          .description("Leeds")
          .build(),
      ).build()

    courtLocation = AgencyLocation.builder()
      .id("MDI")
      .active(true)
      .type(AgencyLocationType.COURT_TYPE)
      .description("Moorland")
      .build()

    offenderCourtCase = OffenderCourtCase.builder()
      .id(-1L)
      .caseSeq(-2)
      .beginDate(LocalDate.EPOCH)
      .agencyLocation(courtLocation)
      .legalCaseType(LEGAL_CASE_TYPE)
      .caseInfoPrefix("CIP")
      .caseInfoNumber("CIN20177010")
      .caseStatus(CASE_STATUS)
      .courtEvents(
        listOf(
          CourtEvent.builder()
            .id(-1L)
            .offenderBooking(booking)
            .eventDate(LocalDate.EPOCH)
            .startTime(LocalDate.EPOCH.atStartOfDay())
            .courtLocation(courtLocation)
            .build(),
        ),
      )
      .offenderBooking(booking)
      .build()
  }

  @Test
  fun transform() {
    val transformed = CourtCaseTransformer.transform(offenderCourtCase)

    assertThat(transformed).isEqualTo(
      CourtCase.builder()
        .id(-1L)
        .caseSeq(-2)
        .beginDate(LocalDate.EPOCH)
        .caseInfoPrefix("CIP")
        .caseInfoNumber("CIN20177010")
        .agency(
          Agency.builder()
            .agencyId(courtLocation.id)
            .agencyType(courtLocation.type.code)
            .description(courtLocation.description)
            .active(true)
            .build(),
        )
        .caseStatus(CASE_STATUS.description)
        .caseType(LEGAL_CASE_TYPE.description)
        .courtHearings(
          listOf(
            CourtHearing.builder()
              .id(-1L)
              .dateTime(LocalDate.EPOCH.atTime(LocalTime.MIDNIGHT))
              .location(
                Agency.builder()
                  .agencyId(courtLocation.id)
                  .description(courtLocation.description)
                  .agencyType("CRT")
                  .active(true)
                  .build(),
              )
              .build(),
          ),
        )
        .build(),
    )
  }

  companion object {
    private val LEGAL_CASE_TYPE = LegalCaseType("A", "Adult")
    private val CASE_STATUS = CaseStatus("A", "Active")
  }
}
