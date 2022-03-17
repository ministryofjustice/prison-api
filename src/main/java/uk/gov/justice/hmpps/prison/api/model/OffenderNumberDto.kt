package uk.gov.justice.hmpps.prison.api.model

data class OffenderNumberDto(
  val offenderNumber: String?,
) {
  fun toOffenderNumber() = OffenderNumber(
    this.offenderNumber,
  )
}
