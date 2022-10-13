package uk.gov.justice.hmpps.prison.repository.jpa.repository

import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking

fun OffenderBookingRepository.findByOffenderNomsIdAndBookingSequenceOrNull(
  nomsId: String,
  bookingSequence: Int
): OffenderBooking? = this.findByOffenderNomsIdAndBookingSequence(nomsId, bookingSequence).orElse(null)
