package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Offender Email Address Create Request
 **/
@Schema(description = "Offender Email Address Create Request")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class OffenderEmailAddressCreateRequest(
  @Schema(description = "The email address", example = "foo@bar.example", requiredMode = Schema.RequiredMode.REQUIRED)
  @field:NotBlank
  @field:Size(max = 240)
  val emailAddress: String,
)
