package uk.gov.justice.hmpps.prison.api.model
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

data class AgyPrisonerPayProfileDto(
  val agyLocId: String,
  val autoPayFlag: String,
  val startDate: LocalDate,
  val endDate: LocalDate?,
  val payFrequency: Int?,
  val weeklyAbsenceLimit: Int?,
  val minHalfDayRate: BigDecimal,
  val maxHalfDayRate: BigDecimal,
  val maxPieceWorkRate: BigDecimal,
  val maxBonusRate: BigDecimal,
  val backdateDays: Int?,
  val defaultPayBandCode: String?,
) {
  fun toAgyPrisonerPayProfile() = AgyPrisonerPayProfile(
    this.agyLocId,
    this.startDate,
    this.endDate,
    this.autoPayFlag,
    this.payFrequency,
    this.weeklyAbsenceLimit,
    this.minHalfDayRate.setScale(2, RoundingMode.HALF_UP),
    this.maxHalfDayRate.setScale(2, RoundingMode.HALF_UP),
    this.maxPieceWorkRate.setScale(2, RoundingMode.HALF_UP),
    this.maxBonusRate.setScale(2, RoundingMode.HALF_UP),
    this.backdateDays,
    this.defaultPayBandCode,
  )
}
