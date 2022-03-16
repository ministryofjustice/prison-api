package uk.gov.justice.hmpps.prison.api.model

data class KeyworkerDto(
  val staffId: Long?,
  val firstName: String?,
  val lastName: String?,
  val status: String?,
  val thumbnailId: Long?,
  val numberAllocated: Int?,
) {
  fun toKeyworker() = Keyworker(
    this.staffId,
    this.firstName,
    this.lastName,
    this.status,
    this.thumbnailId,
    this.numberAllocated,
  )
}
