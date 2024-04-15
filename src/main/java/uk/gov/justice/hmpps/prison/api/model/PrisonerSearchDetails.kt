package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "Prisoner details required by Prisoner Search")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class PrisonerSearchDetails(

  @Schema(description = "Prisoner Number a.k.a NOMS number, offender number", example = "A1234AA")
  val offenderNo: String,

  @Schema(description = "Booking Id of the active booking", example = "432132")
  val bookingId: Long? = null,

  @Schema(description = "Booking Number of the active booking")
  val bookingNo: String? = null,

  @Schema(description = "First Name")
  val firstName: String,

  @Schema(description = "Middle Name(s)")
  val middleName: String? = null,

  @Schema(description = "Last Name")
  val lastName: String,

  @Schema(description = "Date of Birth of prisoner", example = "1970-03-15")
  val dateOfBirth: LocalDate,

  @Schema(description = "Prison ID", example = "MDI")
  val agencyId: String? = null,

  @Schema(description = "List of alert details for the active booking")
  val alerts: List<Alert>? = null,

  @Schema(description = "Cell or location of the prisoner")
  val assignedLivingUnit: AssignedLivingUnit? = null,

  @Schema(description = "Religion of the prisoner")
  var religion: String? = null,

  @Schema(description = "A set of physical attributes")
  val physicalAttributes: PhysicalAttributes? = null,

  @Schema(description = "List of physical characteristics")
  val physicalCharacteristics: List<PhysicalCharacteristic>? = null,

  @Schema(description = "List of profile information")
  val profileInformation: List<ProfileInformation>? = null,

  @Schema(description = "List of physical marks")
  val physicalMarks: List<PhysicalMark>? = null,

  @Schema(description = "CSRA (Latest assessment with cellSharing=true from list of assessments)")
  val csra: String? = null,

  @Schema(description = "Category code (from list of assessments)")
  val categoryCode: String? = null,

  @Schema(description = "In/Out Status", example = "IN, OUT, TRN")
  val inOutStatus: String? = null,

  @Schema(description = "Prisoner Identifiers")
  val identifiers: List<OffenderIdentifier>? = null,

  @Schema(description = "Sentence Detail")
  val sentenceDetail: SentenceCalcDates? = null,

  @Schema(description = "Most serious offence")
  val mostSeriousOffenceDescription: String? = null,

  @Schema(description = "Currently serving an indeterminate sentence?")
  val indeterminateSentence: Boolean? = null,

  @Schema(description = "Aliases")
  val aliases: List<Alias>? = null,

  @Schema(description = "Status of prisoner", example = "ACTIVE IN, ACTIVE OUT, INACTIVE OUT, INACTIVE TRN")
  val status: String? = null,

  @Schema(
    description = "Last Movement Type Code of prisoner. Note: Reference Data from MOVE_TYPE Domain",
    example = "TAP, CRT, TRN, ADM, REL",
  )
  val lastMovementTypeCode: String? = null,

  @Schema(description = "Last Movement Reason of prisoner. Note: Reference Data from MOVE_RSN Domain", example = "CA")
  val lastMovementReasonCode: String? = null,

  @Schema(description = "Legal Status", example = "REMAND")
  val legalStatus: LegalStatus? = null,

  @Schema(description = "Recall", example = "true")
  val recall: Boolean? = null,

  @Schema(description = "The prisoner's imprisonment status", example = "LIFE")
  val imprisonmentStatus: String? = null,

  @Schema(description = "The prisoner's imprisonment status description", example = "Serving Life Imprisonment")
  val imprisonmentStatusDescription: String? = null,

  @Schema(description = "Date prisoner was received into the prison.", example = "1980-01-01")
  val receptionDate: LocalDate? = null,

  @Schema(description = "current prison or outside with last movement information.", example = "Outside - released from Leeds")
  val locationDescription: String? = null,

  @Schema(description = "the current prison id or the last prison before release", example = "MDI")
  val latestLocationId: String? = null,
)
