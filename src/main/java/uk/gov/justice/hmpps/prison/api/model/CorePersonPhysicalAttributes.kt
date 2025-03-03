package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileCode

@JsonInclude(NON_NULL)
@Schema(description = "Core Person Record Physical Attributes")
data class CorePersonPhysicalAttributes(
  @Schema(description = "Height (in centimetres)")
  val height: Int? = null,

  @Schema(description = "Weight (in kilograms)")
  val weight: Int? = null,

  @Schema(description = "Hair type or colour. Note: uses PROFILE_TYPE of `HAIR`", example = "BROWN")
  val hair: ProfileCode? = null,

  @Schema(description = "Facial hair type. Note: uses PROFILE_TYPE of `FACIAL_HAIR`", example = "BEARDED")
  val facialHair: ProfileCode? = null,

  @Schema(description = "Face shape. Note: uses PROFILE_TYPE of `FACE`", example = "ROUND")
  val face: ProfileCode? = null,

  @Schema(description = "Build. Note: uses PROFILE_TYPE of `BUILD`", example = "MEDIUM")
  val build: ProfileCode? = null,

  @Schema(description = "Left eye colour. Note: uses PROFILE_TYPE of `L_EYE_C`", example = "BLUE")
  val leftEyeColour: ProfileCode? = null,

  @Schema(description = "Right eye colour. Note: uses PROFILE_TYPE of `R_EYE_C`", example = "BLUE")
  val rightEyeColour: ProfileCode? = null,

  @Schema(description = "Shoe size", example = "9")
  val shoeSize: String? = null,
)
