package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.time.LocalDate

@Schema(description = "Agency prisoner pay profile")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class AgencyPrisonerPayProfile(
  @Schema(description = "Agency identifier", example = "MDI")
  val agencyId: String,

  @Schema(description = "The start date when this pay profile took effect", example = "2022-10-01")
  val startDate: LocalDate,

  @Schema(description = "The end date when this pay profile will stop taking effect", example = "2027-10-01")
  val endDate: LocalDate? = null,

  @Schema(description = "Whether automatic payments are enabled", example = "true")
  val autoPayFlag: Boolean,

  @Schema(description = "The frequency that payroll runs for this agency (usually 1)", example = "1")
  val payFrequency: Int?,

  @Schema(description = "The number of absences that are acceptable within one week", example = "5")
  val weeklyAbsenceLimit: Int?,

  @Schema(description = "The minimum value for a half-day rate", example = "1.25")
  val minHalfDayRate: BigDecimal?,

  @Schema(description = "The maximum value for a half-day rate", example = "5.00")
  val maxHalfDayRate: BigDecimal?,

  @Schema(description = "The maximum value for piece work earnings", example = "6.00")
  val maxPieceWorkRate: BigDecimal?,

  @Schema(description = "The maximum value for a bonus award", example = "3.00")
  val maxBonusRate: BigDecimal?,

  @Schema(description = "The number of days allowed to backdate attendance before it locks.", example = "7")
  val backdateDays: Int?,

  @Schema(description = "The default pay band to use when allocating offenders to paid activities.", example = "1")
  val defaultPayBandCode: String? = null,
)
