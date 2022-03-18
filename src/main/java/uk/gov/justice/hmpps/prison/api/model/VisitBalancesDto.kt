package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDate

data class VisitBalancesDto(
  val remainingVo: Int?,
  val remainingPvo: Int?,
  val latestIepAdjustDate: LocalDate?,
  val latestPrivIepAdjustDate: LocalDate?,
) {
  fun toVisitBalances() = VisitBalances(
    this.remainingVo,
    this.remainingPvo,
    this.latestIepAdjustDate,
    this.latestPrivIepAdjustDate,
  )
}
