package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import java.time.LocalDate

@Schema(description = "Incident Type Configuration")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class IncidentTypeConfiguration(

  @Schema(
    requiredMode = RequiredMode.REQUIRED,
    description = "Incident type of this configuration",
    example = "ASSAULTS",
  )
  private val incidentType: String,

  @Schema(requiredMode = RequiredMode.REQUIRED, description = "ID internal of this question set for this incident type", example = "123412")
  private val questionnaireId: Long,

  @Schema(
    requiredMode = RequiredMode.REQUIRED,
    description = "List of questions (with answers) for this incident type",
  )
  private val questions: List<IncidentTypeQuestion>,

  @Schema(
    requiredMode = RequiredMode.REQUIRED,
    description = "List of roles that can apply to a prisoner in this incident type",
  )
  private val prisonerRoles: List<IncidentTypePrisonerRole>,

  @Schema(description = "Indicates this incident type is still usable")
  val active: Boolean = true,

  @Schema(description = "Date the incident type was expired")
  var expiryDate: LocalDate? = null,
)
