package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

@Schema(description = "Prisoner details required by Prisoner Search")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class PrisonerSearchDetails(

  @Schema(description = "Prisoner Number a.k.a NOMS number, offender number", example = "A1234AA")
  val offenderNo: String,

  @Schema(description = "Offender Id", example = "678673")
  val offenderId: Long? = null,

  @Schema(description = "Booking Id of the active booking", example = "432132")
  val bookingId: Long? = null,

  @Schema(description = "Booking Number of the active booking")
  val bookingNo: String? = null,

  @Schema(description = "Title")
  val title: String? = null,

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

  @Schema(description = "Prisoner Identifiers including those from aliases")
  val allIdentifiers: List<OffenderIdentifier>? = null,

  @Schema(description = "Sentence Detail")
  val sentenceDetail: SentenceCalcDates? = null,

  @Schema(description = "Most serious offence")
  val mostSeriousOffence: String? = null,

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

  @Schema(description = "Last Movement time", example = "2024-12-01 12:23:34")
  val lastMovementTime: LocalDateTime? = null,

  @Schema(description = "Time of transfer into the current or most recent prison", example = "2024-12-01 12:23:34")
  val lastAdmissionTime: LocalDateTime? = null,

  @Schema(description = "The previous prison for the prisoner within the current term or booking, if any", example = "PVI")
  val previousPrisonId: String? = null,

  @Schema(description = "The date they left the previous prison", example = "2025-09-15T14:35:00")
  val previousPrisonLeavingDate: LocalDateTime? = null,

  @Schema(description = "Legal Status", example = "REMAND")
  val legalStatus: LegalStatus? = null,

  @Schema(description = "Recall", example = "true")
  val recall: Boolean? = null,

  @Schema(description = "The prisoner's imprisonment status", example = "LIFE")
  val imprisonmentStatus: String? = null,

  @Schema(description = "The prisoner's imprisonment status description", example = "Serving Life Imprisonment")
  val imprisonmentStatusDescription: String? = null,

  @Schema(description = "The prisoner's convicted status", example = "Convicted")
  val convictedStatus: String? = null,

  @Schema(description = "Date prisoner was first received into prison in their current or most recent term.", example = "2023-01-01")
  val receptionDate: LocalDate? = null,

  @Schema(description = "current prison or outside with last movement information.", example = "Outside - released from Leeds")
  val locationDescription: String? = null,

  @Schema(description = "the current agency (prison, hospital, court) id or the last one before release", example = "MDI")
  val latestLocationId: String? = null,

  @Schema(description = "the current prison id or the last i.e. final prison before release. Same as latestLocationId if latestLocationId was a prison", example = "MDI")
  val lastPrisonId: String? = null,

  @Schema(description = "Prisoner Addresses")
  val addresses: List<AddressDto>? = null,

  @Schema(description = "Prisoner Phone Numbers")
  val phones: List<Telephone>? = null,

  @Schema(description = "Prisoner Email Addresses")
  val emailAddresses: List<Email>? = null,

  @Schema(description = "Convicted offences for ALL bookings")
  val allConvictedOffences: List<OffenceHistoryDetail>? = null,

  @Schema(description = "Personal Care Needs. Specific types are selected for the prisoner profile : DISAB, MATSTAT, PHY, PSYCH, SC")
  val personalCareNeeds: List<PersonalCareNeed>? = null,

  @Schema(description = "Languages spoken, read or written")
  val languages: List<OffenderLanguageDto>? = null,

  @Schema(description = "The prisoner's current facial image can be retrieved by plugging this id into the endpoint /api/images/{prisonerNumber}/data?imageId={imageId}", example = "341221")
  val imageId: Long? = null,

  @Schema(description = "true if the prisoner has a military record, i.e. any data in OFFENDER_MILITARY_RECORDS")
  val militaryRecord: Boolean? = null,
)
