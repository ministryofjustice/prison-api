package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Movement DTO")
data class BookingMovement(
  @Schema(description = "Sequence number")
  val sequence: Int?,

  @Schema(description = "Agency travelling from")
  val fromAgency: String?,

  @Schema(description = "Agency travelling to")
  val toAgency: String?,

  @Schema(
    description = "ADM (admission), CRT (court), REL (release), TAP (temporary absence) or TRN (transfer)",
    allowableValues = ["ADM", "CRT", "REL", "TAP", "TRN"],
  )
  val movementType: String?,

  @Schema(description = "IN or OUT")
  val directionCode: String?,

  @Schema(description = "Movement timestamp")
  val movementDateTime: LocalDateTime?,

  @Schema(description = "Code of movement reason")
  val movementReasonCode: String?,

  @Schema(description = "DB create timestamp")
  val createdDateTime: LocalDateTime?,

  @Schema(description = "DB modify timestamp")
  val modifiedDateTime: LocalDateTime?,
)
