package uk.gov.justice.hmpps.prison.api.model.calculation

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.hmpps.prison.api.model.Alert
import java.time.LocalDate

data class Person(

  @Schema(description = "Prisoner Identifier", example = "A1234AA", requiredMode = Schema.RequiredMode.REQUIRED)
  var prisonerNumber: String,

  var dateOfBirth: LocalDate,

  var lastName: String,

  var agencyId: String,

  val alerts: List<Alert>,
)
