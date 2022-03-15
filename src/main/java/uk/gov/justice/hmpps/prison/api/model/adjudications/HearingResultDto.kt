package uk.gov.justice.hmpps.prison.api.model.adjudications

data class HearingResultDto(
  val oicOffenceCode: String?,
  val offenceType: String?,
  val offenceDescription: String?,
  val plea: String?,
  val finding: String?,
  val oicHearingId: Long,
  val resultSeq: Long?,
) {
  fun toHearingResult() = HearingResult(
    this.oicOffenceCode,
    this.offenceType,
    this.offenceDescription,
    this.plea,
    this.finding,
    null,
    this.oicHearingId,
    this.resultSeq,
  )
}
