package uk.gov.justice.hmpps.prison.api.model

import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED
import jakarta.validation.constraints.Size
import java.time.LocalDate

@Schema(description = "Create a prisoner address")
data class CreateAddress(

  @Schema(description = "Flat", example = "3B")
  @field:Size(max = 30)
  val flat: String? = null,

  @Schema(description = "Premise", example = "Liverpool Prison")
  @field:Size(max = 50)
  val premise: String? = null,

  @Schema(description = "Street", example = "Slinn Street")
  @field:Size(max = 160)
  val street: String? = null,

  @Schema(description = "Locality", example = "Brincliffe")
  @field:Size(max = 70)
  val locality: String? = null,

  @Schema(description = "Town/City code. Note: Reference domain is CITY", example = "17743")
  @field:Size(max = 12)
  val townCode: String? = null,

  @Schema(description = "County code. Note: Reference domain is COUNTY", example = "HEREFORD")
  @field:Size(max = 12)
  val countyCode: String? = null,

  @Schema(requiredMode = REQUIRED, description = "Country code. Note: Reference domain is COUNTRY", example = "ENG")
  @field:Size(max = 12)
  val countryCode: String,

  @Schema(description = "Postal code", example = "LI1 5TH")
  @field:Size(max = 12)
  val postalCode: String? = null,

  @Schema(description = "Primary address", example = "true")
  val primary: Boolean? = null,

  @Schema(description = "Mail address", example = "true")
  val mail: Boolean? = null,

  @Schema(description = "No fixed address", example = "false")
  val noFixedAddress: Boolean? = null,

  @Schema(description = "Date address is in use from", example = "2005-05-12")
  val startDate: LocalDate,

  @Schema(description = "Date address is in use to", example = "2005-05-12")
  val endDate: LocalDate? = null,

  @Schema(description = "Address usages", example = "HOME")
  val addressUsages: Collection<String>,
)
