package uk.gov.justice.hmpps.prison.api.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size
import uk.gov.justice.hmpps.prison.service.validation.MaximumTextSize
import java.time.LocalDate

@Schema(description = "Update to prisoner religion")
data class UpdateReligion(
  @Schema(description = "The religion code", example = "ZORO", required = true)
  val religion: String,
  @Schema(description = "Reason for the religion change", example = "Some information")
  @Size(max = 4000)
  @MaximumTextSize
  val comment: String?,
  @Schema(description = "The date the religious belief is valid from (in YYYY-MM-DD format)")
  val effectiveFromDate: LocalDate?,
  @Schema(description = "Has the religious belief been verified?", example = "false", defaultValue = "false")
  val verified: Boolean,
)
