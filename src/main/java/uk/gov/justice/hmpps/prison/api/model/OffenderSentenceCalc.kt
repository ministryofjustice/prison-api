package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED

/**
 * Offender Sentence Detail
 */
@Schema(description = "Offender Sentence Calculation")
@JsonInclude(JsonInclude.Include.NON_NULL)
open class OffenderSentenceCalc<S : BaseSentenceCalcDates>(
  @Schema(requiredMode = REQUIRED, description = "Offender booking id.", example = "12341321")
  val bookingId: Long,

  @Schema(requiredMode = REQUIRED, description = "Offender Unique Reference", example = "A1000AA")
  val offenderNo: String,

  @Schema(requiredMode = REQUIRED, description = "First Name", example = "John")
  val firstName: String,

  @Schema(requiredMode = REQUIRED, description = "Last Name", example = "Smith")
  val lastName: String,

  @Schema(requiredMode = REQUIRED, description = "Agency Id", example = "LEI")
  val agencyLocationId: String,

  @Schema(
    requiredMode = NOT_REQUIRED,
    description = "Is this the most recent active booking",
    example = "true",
  )
  val mostRecentActiveBooking: Boolean? = null,

  @Schema(description = "Offender Sentence Detail Information")
  val sentenceDetail: S,
)
