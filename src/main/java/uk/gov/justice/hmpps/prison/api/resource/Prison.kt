package uk.gov.justice.hmpps.prison.api.resource

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(NON_NULL)
data class Prison(
  @Schema(required = true, description = "Agency identifier.", example = "MDI")
  val agencyId: String,
  @Schema(required = true, description = "Agency description.", example = "Moorland (HMP & YOI)")
  val description: String,
  @Schema(description = "Long description of the agency", example = "Moorland (HMP & YOI)")
  val longDescription: String?,
  @Schema(
    required = true,
    description = "Agency type.  Reference domain is AGY_LOC_TYPE.  Will be INST for a prison.",
    example = "INST",
    allowableValues = ["CRC", "POLSTN", "INST", "COMM", "APPR", "CRT", "POLICE", "IMDC", "TRN", "OUT", "YOT", "SCH", "STC", "HOST", "AIRPORT", "HSHOSP", "HOSPITAL", "PECS", "PAR", "PNP", "PSY"],
  )
  val agencyType: String,
  @Schema(required = true, description = "Indicates the Agency is active", example = "true")
  val active: Boolean,
)
