package uk.gov.justice.hmpps.prison.api.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size
import org.jetbrains.annotations.NotNull

@Schema(description = "Request object for creating or updating an identifying mark for a prisoner.")
data class IdentifyingMarkDetails(

  @Schema(
    description = "Code representing the type of distinguishing mark. Reference code from the MARK_TYPE domain.",
    example = "SCAR",
  )
  @NotNull("Mark type is required.")
  val markType: String,

  @Schema(
    description = "Code representing the part of body the distinguishing mark is on. Reference code from the BODY_PART domain.",
    example = "HEAD",
  )
  @NotNull("Body part is required.")
  val bodyPart: String,

  @Schema(
    description = "Code representing the side of the body part the mark is on. Reference code from the SIDE domain.",
    example = "SIDE_R",
  )
  val side: String?,

  @Schema(
    description = "Code representing the orientation of the mark on the body part. Reference code from the PART_ORIENT domain.",
    example = "PART_ORIENT_CENTR",
  )
  val partOrientation: String?,

  @Schema(
    description = "Comment about the distinguishing mark.",
    example = "Long healed scar from an old fight",
  )
  @Size(max = 240, message = "Comment text must be a maximum of 240 characters")
  val comment: String?,
)
