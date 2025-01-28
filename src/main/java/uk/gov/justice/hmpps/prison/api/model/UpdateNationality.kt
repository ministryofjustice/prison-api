package uk.gov.justice.hmpps.prison.api.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size

@Schema(description = "Update to prisoner nationality")
data class UpdateNationality(
  @Schema(description = "The nationality code", example = "BRIT", required = true, nullable = true)
  val nationality: String?,
  @Schema(description = "Other nationalities", example = "Irish", required = true, nullable = true)
  @Size(max = 40, message = "Other nationalities must be a maximum of 40 characters")
  val otherNationalities: String?,
)
