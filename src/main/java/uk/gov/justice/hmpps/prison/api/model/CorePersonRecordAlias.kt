package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@Schema(description = "Core Person Record Alias - DTO for use in returning alias data for the Core Person Record proxy")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CorePersonRecordAlias(

  @Schema(description = "Prisoner number", example = "A1234AA")
  val prisonerNumber: String,

  @Schema(description = "Offender ID", example = "543548")
  val offenderId: Long,

  @Schema(description = "Boolean flag to indicate if the alias is a working name", example = "true")
  val isWorkingName: Boolean,

  @Schema(description = "First name", example = "John")
  val firstName: String,

  @Schema(description = "Middle name", example = "Middleone")
  val middleName: String? = null,

  @Schema(description = "Last name", example = "Smith")
  val lastName: String,

  @Schema(description = "Date of birth", example = "1980-02-28")
  val dateOfBirth: LocalDate,

  @Schema(description = "Name type")
  val nameType: ReferenceDataValue? = null,

  @Schema(description = "Title")
  val title: ReferenceDataValue? = null,

  @Schema(description = "Sex")
  val sex: ReferenceDataValue? = null,

  @Schema(description = "Ethnicity")
  val ethnicity: ReferenceDataValue? = null,
)
