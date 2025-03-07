package uk.gov.justice.hmpps.prison.api.model

import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@Schema(description = "Update prisoner's legal name")
data class UpdateWorkingName(
  @Schema(description = "Prisoner's first name", example = "John", requiredMode = REQUIRED)
  @field:Size(max = 35)
  @field:NotBlank
  @field:Pattern(regexp = "^[A-Z|a-z ,.'-]+$", message = "First name is not valid")
  val firstName: String,

  @Schema(description = "Prisoner's middle name.", example = "Middleone")
  @field:Size(max = 35)
  @field:Pattern(regexp = "^[A-Z|a-z ,.'-]+$", message = "Middle name 1 is not valid")
  val middleName1: String? = null,

  @Schema(description = "An additional middle name for the prisoner", example = "Middletwo")
  @field:Size(max = 35)
  @field:Pattern(regexp = "^[A-Z|a-z ,.'-]+$", message = "Middle name 2 is not valid")
  val middleName2: String? = null,

  @Schema(description = "Prisoner's last name", example = "Smith", requiredMode = REQUIRED)
  @field:Size(max = 35)
  @field:NotBlank
  @field:Pattern(regexp = "^[A-Z|a-z ,.'-]+$", message = "Last name is not valid")
  val lastName: String,
)
