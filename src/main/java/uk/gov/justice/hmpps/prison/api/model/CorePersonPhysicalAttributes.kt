package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(NON_NULL)
@Schema(description = "Core Person Record Physical Attributes")
data class CorePersonPhysicalAttributes(
  @Schema(description = "Height (in centimetres)")
  val height: Int? = null,

  @Schema(description = "Weight (in kilograms)")
  val weight: Int? = null,

  @Schema(description = "Code for hair type or colour. Note: uses PROFILE_TYPE of `HAIR`", example = "BROWN")
  val hairCode: String? = null,

  @Schema(description = "Description for hair type or colour")
  val hairDescription: String? = null,

  @Schema(description = "Code for facial hair type. Note: uses PROFILE_TYPE of `FACIAL_HAIR`", example = "BEARDED")
  val facialHairCode: String? = null,

  @Schema(description = "Description for facial hair type")
  val facialHairDescription: String? = null,

  @Schema(description = "Code for face shape. Note: uses PROFILE_TYPE of `FACE`", example = "ROUND")
  val faceCode: String? = null,

  @Schema(description = "Description for face shape")
  val faceDescription: String? = null,

  @Schema(description = "Code for build. Note: uses PROFILE_TYPE of `BUILD`", example = "MEDIUM")
  val buildCode: String? = null,

  @Schema(description = "Description for build")
  val buildDescription: String? = null,

  @Schema(description = "Code for left eye colour. Note: uses PROFILE_TYPE of `L_EYE_C`", example = "BLUE")
  val leftEyeColourCode: String? = null,

  @Schema(description = "Description for left eye colour")
  val leftEyeColourDescription: String? = null,

  @Schema(description = "Code for right eye colour. Note: uses PROFILE_TYPE of `R_EYE_C`", example = "BLUE")
  val rightEyeColourCode: String? = null,

  @Schema(description = "Description for right eye colour")
  val rightEyeColourDescription: String? = null,

  @Schema(description = "Shoe size", example = "9")
  val shoeSize: String? = null,
)
