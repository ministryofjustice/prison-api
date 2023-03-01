package uk.gov.justice.hmpps.prison.repository.jpa.repository

import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType

fun AgencyLocationRepository.findByIdAndDeactivationDateIsNullOrNull(id: String): AgencyLocation? =
  findByIdAndDeactivationDateIsNull(id).orElse(null)

fun AgencyLocationRepository.findByIdAndTypeAndActiveAndDeactivationDateIsNullOrNull(
  id: String,
  type: AgencyLocationType,
  active: Boolean,
): AgencyLocation? = findByIdAndTypeAndActiveAndDeactivationDateIsNull(id, type, active).orElse(null)
