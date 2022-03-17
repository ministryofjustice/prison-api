package uk.gov.justice.hmpps.prison.api.model

data class PersonIdentifierDto(
  val identifierType: String?,
  val identifierValue: String?,
) {
  fun toPersonIdentifier() = PersonIdentifier(
    this.identifierType,
    this.identifierValue,
  )
}
