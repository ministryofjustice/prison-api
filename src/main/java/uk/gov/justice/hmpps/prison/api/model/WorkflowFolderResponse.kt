package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.ALWAYS)
@Schema(description = "A NOMIS workflow folder, including its creation, modification and audit metadata. Nullable fields are returned explicitly as null.")
data class WorkflowFolderResponse(
  @Schema(description = "Workflow code; the folder's primary key.", example = "TASK")
  val workflowCode: String,

  @Schema(description = "Folder description.", requiredMode = REQUIRED)
  val description: String?,

  @Schema(description = "Display sequence; not the export sort order.", requiredMode = REQUIRED)
  val workflowSeq: Int?,

  @Schema(description = "Caseload type.", requiredMode = REQUIRED)
  val caseloadType: String?,

  @Schema(type = "string", format = "local-date-time", description = "Creation timestamp without a timezone.", example = "2020-01-01T10:11:12.123456")
  val createDatetime: LocalDateTime,

  @Schema(description = "User who created the row.")
  val createUserId: String,

  @Schema(type = "string", format = "local-date-time", description = "Modification timestamp without a timezone.", example = "2020-02-02T11:12:13.234567", requiredMode = REQUIRED)
  val modifyDatetime: LocalDateTime?,

  @Schema(description = "User who modified the row.", requiredMode = REQUIRED)
  val modifyUserId: String?,

  @Schema(type = "string", format = "local-date-time", description = "Audit timestamp without a timezone.", example = "2020-03-03T12:13:14.345678", requiredMode = REQUIRED)
  val auditTimestamp: LocalDateTime?,

  @Schema(description = "Audit user ID.", requiredMode = REQUIRED)
  val auditUserId: String?,

  @Schema(description = "Audit module name.", requiredMode = REQUIRED)
  val auditModuleName: String?,

  @Schema(description = "Audit client user ID.", requiredMode = REQUIRED)
  val auditClientUserId: String?,

  @Schema(description = "Audit client IP address.", requiredMode = REQUIRED)
  val auditClientIpAddress: String?,

  @Schema(description = "Audit client workstation name.", requiredMode = REQUIRED)
  val auditClientWorkstationName: String?,

  @Schema(description = "Additional audit information.", requiredMode = REQUIRED)
  val auditAdditionalInfo: String?,
)
