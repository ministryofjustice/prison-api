package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.time.LocalDateTime

/**
 * Visit summary
 */
@ApiModel(description = "Visit summary")
data class VisitSummary(
  @ApiModelProperty(value = "Date and time at which next scheduled (i.e. not cancelled) event starts (if any)")
  @JsonProperty("startDateTime")
  val startDateTime: LocalDateTime?,

  @ApiModelProperty(required = true, value = "Whether the prisoner has any visits (previous or next)")
  @JsonProperty("hasVisits")
  val hasVisits: Boolean = false,
)
