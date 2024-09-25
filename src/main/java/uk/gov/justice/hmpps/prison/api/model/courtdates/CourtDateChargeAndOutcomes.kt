package uk.gov.justice.hmpps.prison.api.model.courtdates

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "A charge and all its court date outcomes")
@JsonInclude(
  JsonInclude.Include.NON_NULL,
)
data class CourtDateChargeAndOutcomes(
  @Schema(description = "The id of the charge")
  val chargeId: Long? = null,

  @Schema(description = "The offence code of the office in the court case")
  val offenceCode: String? = null,

  @Schema(description = "The offence statute of the office in the court case")
  val offenceStatue: String? = null,

  @Schema(description = "The offence description")
  val offenceDescription: String? = null,

  @Schema(description = "The date of the offence")
  val offenceDate: LocalDate? = null,

  @Schema(description = "The offence end date")
  val offenceEndDate: LocalDate? = null,

  @Schema(description = "Was the verdict guilty or not guilty")
  val guilty: Boolean = false,

  @Schema(description = "The id of the court case")
  val courtCaseId: Long? = null,

  @Schema(description = "Court case reference")
  val courtCaseRef: String? = null,

  @Schema(description = "Court case location")
  val courtLocation: String? = null,

  @Schema(description = "The sequence of the sentence from this charge")
  val sentenceSequence: Int? = null,

  @Schema(description = "The sentence date")
  val sentenceDate: LocalDate? = null,

  @Schema(description = "The sentence type")
  val sentenceType: String? = null,

  @Schema(description = "The result code of the charge")
  val resultCode: String? = null,

  @Schema(description = "The result description of the charge")
  val resultDescription: String? = null,

  @Schema(description = "The disposition code of the result of the charge")
  val resultDispositionCode: String? = null,

  @Schema(description = "The id of the booking this court date was linked to")
  val bookingId: Long,

  @Schema(description = "The user readable ID for a booking")
  val bookNumber: String,

  @Schema(description = "Is this charge active in NOMIS.")
  val active: Boolean,

  @Schema(description = "All the court date outcomes for this charge")
  val outcomes: List<CourtDateOutcome>,
)
