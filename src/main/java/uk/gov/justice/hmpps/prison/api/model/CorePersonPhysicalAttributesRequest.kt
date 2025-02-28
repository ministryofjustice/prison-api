package uk.gov.justice.hmpps.prison.api.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Core Person Record Physical Attributes Request. Used to create or update physical attributes.")
data class CorePersonPhysicalAttributesRequest(
  @Schema(description = "Height (in centimetres)")
  val height: Int? = null,

  @Schema(description = "Weight (in kilograms)")
  val weight: Int? = null,

  @Schema(description = "Code for hair type or colour. Note: uses PROFILE_TYPE of `HAIR`", example = "BROWN")
  val hairCode: String? = null,

  @Schema(description = "Code for facial hair type. Note: uses PROFILE_TYPE of `FACIAL_HAIR`", example = "BEARDED")
  val facialHairCode: String? = null,

  @Schema(description = "Code for face shape. Note: uses PROFILE_TYPE of `FACE`", example = "ROUND")
  val faceCode: String? = null,

  @Schema(description = "Code for build. Note: uses PROFILE_TYPE of `BUILD`", example = "MEDIUM")
  val buildCode: String? = null,

  @Schema(description = "Code for left eye colour. Note: uses PROFILE_TYPE of `L_EYE_C`", example = "BLUE")
  val leftEyeColourCode: String? = null,

  @Schema(description = "Code for right eye colour. Note: uses PROFILE_TYPE of `R_EYE_C`", example = "BLUE")
  val rightEyeColourCode: String? = null,

  @Schema(description = "Shoe size")
  val shoeSize: String? = null,
)
