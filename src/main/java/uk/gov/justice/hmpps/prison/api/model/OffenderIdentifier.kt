package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

@Schema(description = "Offender Identifier")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class OffenderIdentifier(
  @param:Schema(description = "Type of offender identifier", example = "PNC")
  val type: String,

  @param:Schema(description = "The value of the offender identifier", example = "1231/XX/121")
  val value: String,

  @param:Schema(description = "The offender number for this identifier", example = "A1234AB")
  val offenderNo: String,

  @param:Schema(description = "The booking ID for this identifier", example = "1231223")
  val bookingId: Long? = null,

  @param:Schema(description = "Issuing Authority Information", example = "Important Auth")
  val issuedAuthorityText: String? = null,

  @param:Schema(description = "Date of issue", example = "2018-01-21")
  val issuedDate: LocalDate? = null,

  @param:Schema(description = "Related caseload type", example = "GENERAL")
  val caseloadType: String? = null,

  @param:Schema(description = "Creation date and time", example = "2018-01-21T15:00:00")
  val whenCreated: LocalDateTime,

  @param:Schema(description = "Offender ID", example = "234547")
  val offenderId: Long,

  @param:Schema(description = "Root Offender ID", example = "654321")
  val rootOffenderId: Long,

  @param:Schema(description = "Identifier sequence", example = "123456")
  val offenderIdSeq: Long,
)
