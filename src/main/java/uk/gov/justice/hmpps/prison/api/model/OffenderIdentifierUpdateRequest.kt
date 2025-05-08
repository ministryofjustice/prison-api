package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Offender Identifier Update Request
 **/
@Schema(description = "Offender Identifier Update Request")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class OffenderIdentifierUpdateRequest(
  @Schema(description = "The value of the offender identifier", example = "1231/XX/121", requiredMode = Schema.RequiredMode.REQUIRED)
  @field:NotBlank
  @field:Size(max = 20)
  val identifier: String,

  @Schema(description = "Issuing Authority Information", example = "Important info", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @field:Size(max = 240)
  val issuedAuthorityText: String? = null,
)
