package uk.gov.justice.hmpps.prison.api.model.digitalwarrant

import java.time.LocalDate

data class CourtDateResult(
  val id: Long,
  val date: LocalDate?,
  val resultCode: String?,
  val resultDescription: String?,
  val charge: Charge?,
  val bookingId: Long?
)
