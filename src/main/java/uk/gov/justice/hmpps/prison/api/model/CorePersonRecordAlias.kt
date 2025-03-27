package uk.gov.justice.hmpps.prison.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.hmpps.prison.repository.jpa.model.Ethnicity.ETHNICITY
import uk.gov.justice.hmpps.prison.repository.jpa.model.Gender.SEX
import uk.gov.justice.hmpps.prison.repository.jpa.model.NameType.NAME_TYPE
import uk.gov.justice.hmpps.prison.repository.jpa.model.Title.TITLE
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

  @Schema(description = "Middle name 1", example = "Middleone")
  val middleName1: String? = null,

  @Schema(description = "Middle name 2", example = "Middleone")
  val middleName2: String? = null,

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
) {
  constructor(
    prisonerNumber: String,
    offenderId: Long,
    workingNameBookingId: Long?,
    firstName: String,
    middleName1: String?,
    middleName2: String?,
    lastName: String,
    dateOfBirth: LocalDate,
    nameTypeCode: String?,
    nameTypeDescription: String?,
    titleCode: String?,
    titleDescription: String?,
    sexCode: String?,
    sexDescription: String?,
    ethnicityCode: String?,
    ethnicityDescription: String?,
  ) : this(
    prisonerNumber,
    offenderId,
    isWorkingName = workingNameBookingId != null,
    firstName,
    middleName1,
    middleName2,
    lastName,
    dateOfBirth,
    nameType = referenceDataValue(NAME_TYPE, nameTypeCode, nameTypeDescription),
    title = referenceDataValue(TITLE, titleCode, titleDescription),
    sex = referenceDataValue(SEX, sexCode, sexDescription),
    ethnicity = referenceDataValue(ETHNICITY, ethnicityCode, ethnicityDescription),
  )
}

private fun referenceDataValue(domain: String, code: String?, description: String?) = if (code == null || description == null) null else ReferenceDataValue(domain, code, description)
