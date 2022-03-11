package uk.gov.justice.hmpps.prison.api.model
import java.time.LocalDate
data class OffenderInReceptionDto(
  var offenderNo: String? = null,
  var bookingId: Long? = null,
  var dateOfBirth: LocalDate? = null,
  var firstName: String? = null,
  var lastName: String? = null,
) {
  fun toOffenderInReception() = OffenderInReception(
    this.offenderNo,
    this.bookingId,
    this.dateOfBirth,
    this.firstName,
    this.lastName,
  )
}
