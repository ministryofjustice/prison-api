package uk.gov.justice.hmpps.prison.repository.jpa.repository

import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender

fun OffenderRepository.findRootOffenderByNomsIdOrNull(nomsId: String): Offender? =
  findRootOffenderByNomsId(nomsId).orElse(null)
