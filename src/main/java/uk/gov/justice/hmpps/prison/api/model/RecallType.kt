package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonFormat

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class RecallType(
  val recallName: String,
  val isFixedTermRecall: Boolean = false,
) {
  NONE("NONE"),
  STANDARD_RECALL("STANDARD_RECALL"),
  STANDARD_RECALL_255("STANDARD_RECALL_255"),
  FIXED_TERM_RECALL_14("FIXED_TERM_RECALL_14", isFixedTermRecall = true),
  FIXED_TERM_RECALL_28("FIXED_TERM_RECALL_28", isFixedTermRecall = true),
}
