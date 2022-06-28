package uk.gov.justice.hmpps.prison.api.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import javax.validation.constraints.NotNull

data class OffenderHealthProblems(
  @Schema(description = "Problem type", example = "BSCAN", type = "string", required = true)
  @field:NotNull
  val problemType: String? = null,

  @Schema(description = "Problem code", example = "BSC6.0", type = "string", required = true)
  @field:NotNull
  val problemCode: String? = null,

  @Schema(description = "Description", example = "Intelligence", type = "string", required = true)
  @field:NotNull
  val description: String? = null,

  @Schema(description = "Start date", example = "2022-06-22", type = "string", required = true)
  @field:NotNull
  val startDate: LocalDate? = null,

  @Schema(description = "End date", example = "2022-06-22", type = "string", required = false)
  val endDate: LocalDate? = null,

  @Schema(description = "Problem status", example = "ON", type = "string", required = true)
  @field:NotNull
  val problemStatus: String? = null,

  @Schema(description = "Offender booking Id", example = "123456", type = "Long", required = true)
  @field:NotNull
  val offenderBookingId: Long? = null
)
