package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@JsonInclude(Include.NON_NULL)
@Schema(description = "Create new military record.")
data class CreateMilitaryRecord(
  @Schema(
    description = "Code identifying the war zone where the service took place.",
    example = "AFG",
  )
  val warZoneCode: String? = null,

  @Schema(
    description = "Start date of the military service.",
    example = "2017-06-01",
  )
  val startDate: LocalDate,

  @Schema(
    description = "End date of the military service, if applicable.",
    example = "2019-12-01",
  )
  val endDate: LocalDate? = null,

  @Schema(
    description = "Code indicating the discharge status from the UK military forces.",
    example = "HON",
  )
  val militaryDischargeCode: String? = null,

  @Schema(
    description = "Code identifying the branch of the UK military in which the individual served.",
    example = "ARM",
  )
  val militaryBranchCode: String,

  @Schema(
    description = "Additional notes or details about the military service.",
    example = "Deployed to Afghanistan in support of Operation Herrick.",
  )
  val description: String? = null,

  @Schema(
    description = "Unit number in which the individual served.",
    example = "2nd Battalion, The Royal Anglian Regiment",
  )
  val unitNumber: String? = null,

  @Schema(
    description = "Location where the individual enlisted in the UK military.",
    example = "Windsor, Berkshire",
  )
  val enlistmentLocation: String? = null,

  @Schema(
    description = "Location where the individual was discharged from the UK military.",
    example = "Colchester, Essex",
  )
  val dischargeLocation: String? = null,

  @Schema(
    description = "Flag indicating if the individual was registered for UK selective military service (National Service).",
    example = "false",
  )
  val selectiveServicesFlag: Boolean,

  @Schema(
    description = "Code identifying the individual's military rank in the UK forces.",
    example = "CPL_ARM",
  )
  val militaryRankCode: String? = null,

  @Schema(
    description = "Service number of the individual within the UK military.",
    example = "2345678",
  )
  val serviceNumber: String? = null,

  @Schema(
    description = "Code identifying any disciplinary actions taken against the individual during service.",
    example = "CM",
  )
  val disciplinaryActionCode: String? = null,
)
