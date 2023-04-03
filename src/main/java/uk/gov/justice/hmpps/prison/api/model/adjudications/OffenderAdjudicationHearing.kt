package uk.gov.justice.hmpps.prison.api.model.adjudications

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "Represents an adjudication hearing at the offender level.")
data class OffenderAdjudicationHearing(
  val agencyId: String,

  @Schema(description = "Display Prisoner Number (UK is NOMS ID)")
  val offenderNo: String,

  @Schema(description = "OIC Hearing ID", example = "1985937")
  val hearingId: Long,

  @Schema(description = "Hearing Type", example = "Governor's Hearing Adult")
  val hearingType: String?,

  @Schema(description = "Hearing Time", example = "2017-03-17T08:30:00")
  val startTime: LocalDateTime?,

  @Schema(description = "The internal location id of the hearing", example = "789448")
  val internalLocationId: Long,

  @Schema(description = "The internal location description of the hearing", example = "PVI-RES-MCASU-ADJUD")
  val internalLocationDescription: String?,

  @Schema(description = "The status of the hearing, SCH, COMP or EXP", example = "COMP")
  val eventStatus: String?,
)
