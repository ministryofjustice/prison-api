package uk.gov.justice.hmpps.prison.api.model.imprisonmentstatus

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "Represents an offenders imprisonment status at a point in history")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ImprisonmentStatusHistoryDto(
  @Schema(description = "The imprisonment status")
  val status: String,

  @Schema(description = "The date the status was effective from")
  val effectiveDate: LocalDate,

  @Schema(description = "The agency the status was set by")
  val agencyId: String,
)
