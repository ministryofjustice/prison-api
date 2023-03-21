package uk.gov.justice.hmpps.prison.api.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Used for deactivating/reactivating an offence. A deactivated offence is not selectable in NOMIS")
data class OffenceActivationDto(
  val offenceCode: String,
  val statuteCode: String,
  val activationFlag: Boolean,
)
