package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDate
data class OffenderInReceptionDto(
  val offenderNo: String?,
  val bookingId: Long?,
  val dateOfBirth: LocalDate?,
  val firstName: String?,
  val lastName: String?,
) {
  fun toOffenderInReception() = OffenderInReception(
    this.offenderNo,
    this.bookingId,
    this.dateOfBirth,
    this.firstName,
    this.lastName,
  )
}
