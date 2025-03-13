package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Reference Data Value - basic summary of a reference data code selected as the value for a field")
@JsonInclude(NON_NULL)
data class ReferenceDataValue(
  @Schema(description = "Domain", example = "ETHNICITY")
  val domain: String?,

  @Schema(description = "Code", example = "W1")
  val code: String,

  @Schema(description = "Description of the reference data code", example = "White: Eng./Welsh/Scot./N.Irish/British")
  val description: String,
)
