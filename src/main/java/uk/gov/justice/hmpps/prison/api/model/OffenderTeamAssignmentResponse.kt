package uk.gov.justice.hmpps.prison.api.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "A prisoner's team assignment, without related booking or team details.")
data class OffenderTeamAssignmentResponse(
  @Schema(description = "Prisoner booking ID.", example = "12345")
  val bookingId: Long,

  @Schema(description = "Function assigned to the team for this booking.", example = "AUTO_TRN")
  val functionType: String,

  @Schema(description = "Assigned team ID.", example = "678")
  val teamId: Long,

  @Schema(description = "Date the team was assigned.", example = "2026-01-15")
  val assignmentDate: LocalDate,
)
