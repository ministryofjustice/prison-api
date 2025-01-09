package uk.gov.justice.hmpps.prison.api.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size

@Schema(description = "Update to prisoner religion")
data class UpdateReligion(
  @Schema(description = "Religion", example = "ZORO", required = true, nullable = true)
  val religion: String?,
  @Schema(description = "Comment", example = "Some information", required = false)
  @Size(max = 4000, message = "Comment text must be a maximum of 4000 characters")
  val comment: String?,
)
