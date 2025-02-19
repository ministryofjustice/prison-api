package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import java.time.LocalDateTime

@Schema(description = "Distinguishing Mark")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class DistinguishingMark(
  @Schema(description = "The sequence id of the distinguishing mark", requiredMode = RequiredMode.REQUIRED)
  val id: Int,

  @Schema(description = "The id of the booking associated with the mark", requiredMode = RequiredMode.REQUIRED)
  val bookingId: Long,

  @Schema(description = "Offender Unique Reference", example = "A1234AA", requiredMode = RequiredMode.REQUIRED)
  val offenderNo: String,

  @Schema(description = "The body part the mark is on", requiredMode = RequiredMode.REQUIRED)
  val bodyPart: String,

  @Schema(description = "The type of distinguishing mark (e.g. tattoo, scar)", requiredMode = RequiredMode.REQUIRED)
  val markType: String,

  @Schema(description = "The side of the body part the mark is on", requiredMode = RequiredMode.NOT_REQUIRED)
  val side: String? = null,

  @Schema(
    description = "The orientation of the mark on the body part (e.g. Centre, Low, Upper)",
    requiredMode = RequiredMode.NOT_REQUIRED,
  )
  val partOrientation: String? = null,

  @Schema(description = "Comment about the distinguishing mark", requiredMode = RequiredMode.NOT_REQUIRED)
  val comment: String? = null,

  @Schema(description = "The date and time the data was created", requiredMode = RequiredMode.REQUIRED)
  val createdAt: LocalDateTime? = null,

  @Schema(
    description = "Username of the user that created the mark",
    example = "USER1",
    requiredMode = RequiredMode.REQUIRED,
  )
  val createdBy: String? = null,

  @Schema(description = "List of images associated with this distinguishing mark")
  val photographUuids: List<DistinguishingMarkImageDetail> = listOf(),
)

data class DistinguishingMarkImageDetail(
  @Schema(description = "The image id")
  val id: Long,

  @Schema(description = "True if this image is the latest one associated with a mark")
  val latest: Boolean = false,
)
