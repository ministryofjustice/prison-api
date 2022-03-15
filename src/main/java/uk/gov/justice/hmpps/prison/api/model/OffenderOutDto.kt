package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDate
data class OffenderOutDto(
  val offenderNo: String?,
  val bookingId: Long?,
  val dateOfBirth: LocalDate?,
  val firstName: String?,
  val lastName: String?,
  val location: String?,
) {
  fun toOffenderOut() = OffenderOut(
    this.offenderNo,
    this.bookingId,
    this.dateOfBirth,
    this.firstName,
    this.lastName,
    this.location,
  )
}
