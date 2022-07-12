package uk.gov.justice.hmpps.prison.api.model

import lombok.AllArgsConstructor

@AllArgsConstructor
data class PersonalCareCounterDto(
  val offenderNo: String,
  val size: Int,
)
