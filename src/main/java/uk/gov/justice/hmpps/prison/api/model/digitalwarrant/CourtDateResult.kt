package uk.gov.justice.hmpps.prison.api.model.digitalwarrant

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "Represents a court date and its outcome")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CourtDateResult(

  @Schema(description = "The ID of this court date")
  val id: Long,

  @Schema(description = "The date of the court result")
  val date: LocalDate?,

  @Schema(description = "The result code of the court date")
  val resultCode: String?,

  @Schema(description = "The result description of the court date")
  val resultDescription: String?,

  @Schema(description = "The charge which is the subject of the court date")
  val charge: Charge,

  @Schema(description = "The id of the booking this court date was linked to")
  val bookingId: Long?
)
