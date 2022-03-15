package uk.gov.justice.hmpps.prison.api.model.adjudications

data class AdjudicationOffenceDto(
  val id: String?,
  val code: String?,
  val description: String?,
) {
  fun toAdjudicationOffence() = AdjudicationOffence(
    this.id,
    this.code,
    this.description,
  )
}
