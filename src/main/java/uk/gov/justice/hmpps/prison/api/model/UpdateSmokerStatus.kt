package uk.gov.justice.hmpps.prison.api.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Update to prisoner religion")
data class UpdateSmokerStatus(
  @Schema(
    description = "The smoker status code ('Y' for 'Yes', 'N' for 'No', 'V' for 'Vaper/NRT Only')",
    example = "Y",
    allowableValues = ["Y", "N", "V"],
    required = true,
    nullable = true,
  )
  val smokerStatus: String?,
)
