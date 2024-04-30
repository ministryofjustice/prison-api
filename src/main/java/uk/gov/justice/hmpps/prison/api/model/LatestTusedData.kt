package uk.gov.justice.hmpps.prison.api.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "Latest TUSED dates and associated info for offender")
data class LatestTusedData(
  val latestTused: LocalDate?,
  val latestOverrideTused: LocalDate?,
  val comment: String?,
  val offenderNo: String?,
)
