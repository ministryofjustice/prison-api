package uk.gov.justice.hmpps.prison.api.model
data class ProfileInformationDto(
  val type: String?,
  val question: String?,
  val resultValue: String?,
) {
  fun toProfileInformation() = ProfileInformation(
    this.type,
    this.question,
    this.resultValue,
  )
}
