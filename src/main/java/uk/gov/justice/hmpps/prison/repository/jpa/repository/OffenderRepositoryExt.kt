package uk.gov.justice.hmpps.prison.repository.jpa.repository

import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender

fun OffenderRepository.findOffenderByNomsIdOrNull(nomsId: String): Offender? =
  findOffenderByNomsId(nomsId).orElse(null)
