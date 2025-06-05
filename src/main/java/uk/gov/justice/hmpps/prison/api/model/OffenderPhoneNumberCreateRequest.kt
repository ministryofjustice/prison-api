package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Offender Phone Number Create Request
 **/
@Schema(description = "Offender Phone Number Create Request")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class OffenderPhoneNumberCreateRequest(
  @Schema(description = "Type of offender phone number", example = "BUS", requiredMode = Schema.RequiredMode.REQUIRED)
  @field:NotBlank
  @field:Size(max = 12)
  val phoneNumberType: String,

  @Schema(description = "The phone number", example = "01234 567 890", requiredMode = Schema.RequiredMode.REQUIRED)
  @field:NotBlank
  @field:Size(max = 40)
  val phoneNumber: String,

  @Schema(
    description = "The telephone extension",
    example = "123",
    requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    nullable = true,
  )
  @field:Size(max = 7)
  val extension: String? = null,
)
