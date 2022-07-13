package uk.gov.justice.hmpps.prison.api.model

import io.swagger.v3.oas.annotations.media.Schema
import lombok.AllArgsConstructor

@AllArgsConstructor
data class PersonalCareCounterDto(
  @Schema(description = "Offender number")
  val offenderNo: String,
  @Schema(description = "Number of health problems records in set time")
  val size: Int,
)
