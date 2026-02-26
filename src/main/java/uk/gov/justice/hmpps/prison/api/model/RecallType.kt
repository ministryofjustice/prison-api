package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Describes a recall type.")
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class RecallType(
  @Schema(description = "The recall name.", example = "STANDARD_RECALL_255")
  val recallName: String,

  @Schema(description = "Is this a standard recall.", example = "true")
  val isStandardRecall: Boolean = false,

  @Schema(description = "Is this a fixed term recall.", example = "false")
  val isFixedTermRecall: Boolean = false,
) {
  NONE("NONE"),
  STANDARD_RECALL("STANDARD_RECALL", isStandardRecall = true),
  STANDARD_RECALL_255("STANDARD_RECALL_255", isStandardRecall = true),
  FIXED_TERM_RECALL_14("FIXED_TERM_RECALL_14", isFixedTermRecall = true),
  FIXED_TERM_RECALL_28("FIXED_TERM_RECALL_28", isFixedTermRecall = true),
  FIXED_TERM_RECALL_56("FIXED_TERM_RECALL_56", isFixedTermRecall = true),
}
