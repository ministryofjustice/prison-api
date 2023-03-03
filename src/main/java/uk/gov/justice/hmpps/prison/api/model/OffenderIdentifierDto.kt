package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDate
import java.time.LocalDateTime

data class OffenderIdentifierDto(
  val type: String?,
  val value: String?,
  val offenderNo: String?,
  val bookingId: Long?,
  val issuedAuthorityText: String?,
  val issuedDate: LocalDate?,
  val caseloadType: String?,
  val whenCreated: LocalDateTime,
) {
  fun toOffenderIdentifier() = OffenderIdentifier(
    this.type,
    this.value,
    this.offenderNo,
    this.bookingId,
    this.issuedAuthorityText,
    this.issuedDate,
    this.caseloadType,
    this.whenCreated,
  )
}
