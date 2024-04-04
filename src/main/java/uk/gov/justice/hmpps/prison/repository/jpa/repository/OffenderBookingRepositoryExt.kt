package uk.gov.justice.hmpps.prison.repository.jpa.repository

import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import kotlin.jvm.optionals.getOrNull

fun OffenderBookingRepository.findByOffenderNomsIdAndBookingSequenceOrNull(
  nomsId: String,
  bookingSequence: Int,
): OffenderBooking? = this.findByOffenderNomsIdAndBookingSequence(nomsId, bookingSequence).getOrNull()

fun OffenderBookingRepository.findLatestOffenderBookingByNomsIdOrNull(nomsId: String): OffenderBooking? =
  this.findLatestOffenderBookingByNomsId(nomsId).getOrNull()
