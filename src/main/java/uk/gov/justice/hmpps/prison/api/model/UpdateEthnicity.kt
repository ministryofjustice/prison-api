package uk.gov.justice.hmpps.prison.api.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Update to prisoner ethnicity")
data class UpdateEthnicity(
  @Schema(description = "A code representing the prisoner's ethnicity (from ETHNICITY reference domain).", example = "W1", required = true, nullable = true)
  val ethnicity: String?,
)
