package uk.gov.justice.hmpps.prison.api.model

data class CaseLoadDto(
  val caseLoadId: String?,
  val description: String?,
  val type: String?,
  val caseloadFunction: String?,
  val currentlyActive: Boolean = false,
) {
  fun toCaseLoad() = CaseLoad(
    this.caseLoadId,
    this.description,
    this.type,
    this.caseloadFunction,
    this.currentlyActive,
  )
}
