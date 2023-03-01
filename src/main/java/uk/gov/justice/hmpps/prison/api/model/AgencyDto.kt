package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDate

data class AgencyDto(
  val agencyId: String?,
  val description: String?,
  val longDescription: String?,
  val agencyType: String?,
  val active: Boolean = true,
  val courtType: String?,
  val deactivationDate: LocalDate?,
) {
  fun toAgency() = Agency(
    this.agencyId,
    this.description,
    this.longDescription,
    this.agencyType,
    this.active,
    this.courtType,
    this.deactivationDate,
    null,
    null,
    null,
  )
}
