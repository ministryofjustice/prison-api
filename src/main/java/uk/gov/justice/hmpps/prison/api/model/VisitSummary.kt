package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

/**
 * Visit summary
 */
@Schema(description = "Visit summary")
data class VisitSummary(
  @Schema(description = "Date and time at which next scheduled (i.e. not cancelled) event starts (if any)")
  @JsonProperty("startDateTime")
  val startDateTime: LocalDateTime?,

  @Schema(required = true, description = "Whether the prisoner has any visits (previous or next)")
  @JsonProperty("hasVisits")
  val hasVisits: Boolean = false,
)
