package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDate

data class OffenderIdentifierDto(
  val type: String?,
  val value: String?,
  val offenderNo: String?,
  val bookingId: Long?,
  val issuedAuthorityText: String?,
  val issuedDate: LocalDate?,
  val caseloadType: String?,
) {
  fun toOffenderIdentifier() = OffenderIdentifier(
    this.type,
    this.value,
    this.offenderNo,
    this.bookingId,
    this.issuedAuthorityText,
    this.issuedDate,
    this.caseloadType,
  )
}
