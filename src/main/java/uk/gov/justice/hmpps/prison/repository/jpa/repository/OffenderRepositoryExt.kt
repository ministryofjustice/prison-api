package uk.gov.justice.hmpps.prison.repository.jpa.repository

import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender

fun OffenderRepository.findOffenderWithLatestBookingByNomsIdOrNull(nomsId: String): Offender? =
  findOffenderWithLatestBookingByNomsId(nomsId).orElse(null)
