package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDate
data class OffenderOutDto(
  var offenderNo: String? = null,
  var bookingId: Long? = null,
  var dateOfBirth: LocalDate? = null,
  var firstName: String? = null,
  var lastName: String? = null,
  var location: String? = null,
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
