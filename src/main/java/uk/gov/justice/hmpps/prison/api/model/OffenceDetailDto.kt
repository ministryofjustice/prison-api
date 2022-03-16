package uk.gov.justice.hmpps.prison.api.model

data class OffenceDetailDto(
  val bookingId: Long?,
  val offenceDescription: String?,
  val offenceCode: String?,
  val statuteCode: String?,
) {
  fun toOffenceDetail() = OffenceDetail(
    this.bookingId,
    this.offenceDescription,
    this.offenceCode,
    this.statuteCode,
  )
}
