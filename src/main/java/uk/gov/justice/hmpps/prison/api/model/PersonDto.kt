package uk.gov.justice.hmpps.prison.api.model

data class PersonDto(
  val personId: Long,
  val lastName: String?,
  val firstName: String?,
) {
  fun toPerson() = Person(
    this.personId,
    this.lastName,
    this.firstName,
  )
}
