package uk.gov.justice.hmpps.prison.api.model.courtdates

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "Represents the outcome for a court date of a given charge")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CourtDateOutcome(

  @Schema(description = "The ID of this court date")
  val id: Long,

  @Schema(description = "The date of the court result")
  val date: LocalDate?,

  @Schema(description = "The result code of the court date")
  val resultCode: String?,

  @Schema(description = "The result description of the court date")
  val resultDescription: String?,

  @Schema(description = "The disposition code of the result of the court date")
  val resultDispositionCode: String?,

)
