package uk.gov.justice.hmpps.prison.api.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class OffenderLatestArrivalDate(
  @Schema(description = "The offender of the offender", example = "A1234AA")
  val offenderNo: String,
  @Schema(description = "The latest arrival date for the offender", example = "2020-12-09")
  val latestArrivalDate: LocalDate,
)
