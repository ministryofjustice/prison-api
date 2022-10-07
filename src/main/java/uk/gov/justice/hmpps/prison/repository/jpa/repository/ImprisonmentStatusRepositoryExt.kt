package uk.gov.justice.hmpps.prison.repository.jpa.repository

import uk.gov.justice.hmpps.prison.repository.jpa.model.ImprisonmentStatus

fun ImprisonmentStatusRepository.findByStatusAndActiveOrNull(status: String, active: Boolean): ImprisonmentStatus? =
  findByStatusAndActive(status, active).orElse(null)
