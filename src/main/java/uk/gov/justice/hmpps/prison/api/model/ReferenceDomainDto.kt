package uk.gov.justice.hmpps.prison.api.model

data class ReferenceDomainDto(
  val domain: String?,
  val description: String?,
  val domainStatus: String?,
  val ownerCode: String?,
  val applnCode: String?,
  val parentDomain: String?,
) {
  fun toReferenceDomain() = ReferenceDomain(
    this.domain,
    this.description,
    this.domainStatus,
    this.ownerCode,
    this.applnCode,
    this.parentDomain,
  )
}
