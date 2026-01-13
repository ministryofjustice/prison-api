package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Schema(description = "Summary of an offender counted as Establishment Roll - In")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class OffenderIn(
  @Schema(description = "Display Prisoner Number", required = true)
  val offenderNo: String? = null,

  @Schema(description = "Booking ID", required = true)
  val bookingId: Long? = null,

  @Schema(description = "Date of birth", required = true)
  val dateOfBirth: LocalDate? = null,

  @Schema(description = "First name", required = true)
  val firstName: String? = null,

  @Schema(description = "Middle name", required = false)
  val middleName: String? = null,

  @Schema(description = "Display Prisoner Number", required = true)
  val lastName: String,

  @Schema(description = "Id for Agency travelling from", required = false)
  val fromAgencyId: String? = null,

  @Schema(description = "Description for Agency travelling from", required = false)
  val fromAgencyDescription: String? = null,

  @Schema(description = "Id for Agency travelling to", required = false)
  val toAgencyId: String? = null,

  @Schema(description = "Description for Agency travelling to", required = false)
  val toAgencyDescription: String? = null,

  @Schema(description = "From city", required = false)
  val fromCity: String? = null,

  @Schema(description = "City offender was sent to", required = false)
  val toCity: String? = null,

  @Schema(description = "Movement time", required = true)
  val movementTime: LocalTime,

  @Schema(description = "Movement date time", required = true, example = "2021-07-16T12:34:56")
  val movementDateTime: LocalDateTime? = null,

  @Schema(description = "Description of the offender's (internal) location", required = false)
  val location: String? = null,

  @Schema(description = "Type of movement", example = "TAP", required = true)
  val movementType: String,

  @Schema(description = "Reason for movement in", required = false)
  val movementReasonDescription: String? = null,

  @Schema(description = "From address", required = false)
  val fromAddress: String? = null,
)
