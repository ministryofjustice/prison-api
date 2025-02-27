package uk.gov.justice.hmpps.prison.api.model

import org.apache.commons.text.WordUtils
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class OffenderInDto(
  val offenderNo: String?,
  val bookingId: Long?,
  val dateOfBirth: LocalDate?,
  val firstName: String?,
  val middleName: String?,
  val lastName: String?,
  val fromAgencyId: String?,
  val fromAgencyDescription: String?,
  val toAgencyId: String?,
  val toAgencyDescription: String?,
  val movementTime: LocalTime?,
  val movementDateTime: LocalDateTime?,
  val location: String?,
  val movementType: String?,
  val movementReasonDescription: String?,
  val fromAddrPremise: String?,
  val fromAddrStreet: String?,
  val fromAddrLocality: String?,
  val fromAddrCity: String?,
  val fromAddrPostalCode: String?,
) {
  fun toOffenderIn() = OffenderIn(
    offenderNo = this.offenderNo,
    bookingId = this.bookingId,
    dateOfBirth = this.dateOfBirth,
    firstName = WordUtils.capitalizeFully(this.firstName),
    middleName = WordUtils.capitalizeFully(this.middleName),
    lastName = WordUtils.capitalizeFully(this.lastName),
    fromAgencyId = this.fromAgencyId,
    fromAgencyDescription = LocationProcessor.formatLocation(this.fromAgencyDescription),
    toAgencyId = this.toAgencyId,
    toAgencyDescription = LocationProcessor.formatLocation(this.toAgencyDescription),
    toCity = null,
    movementTime = this.movementTime!!,
    movementDateTime = this.movementDateTime,
    location = this.location?.trim(),
    movementType = this.movementType!!,
    movementReasonDescription = this.movementReasonDescription,
    fromAddress = listOfNotNull(
      this.fromAddrPremise,
      this.fromAddrStreet,
      this.fromAddrLocality,
      this.fromAddrCity,
      this.fromAddrPostalCode,
    ).joinToString(", ").ifEmpty { null },
    fromCity = this.fromAddrCity,
  )
}
