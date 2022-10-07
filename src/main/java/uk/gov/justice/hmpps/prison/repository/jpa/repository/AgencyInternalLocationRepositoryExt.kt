package uk.gov.justice.hmpps.prison.repository.jpa.repository

import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation

fun AgencyInternalLocationRepository.findOneByDescriptionAndAgencyIdOrNull(
  description: String,
  agencyId: String
): AgencyInternalLocation? =
  findOneByDescriptionAndAgencyId(description, agencyId).orElse(null)
