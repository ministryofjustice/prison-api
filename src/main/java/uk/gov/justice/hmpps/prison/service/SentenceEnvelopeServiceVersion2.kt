package uk.gov.justice.hmpps.prison.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.calculation.CalculableSentenceEnvelopeVersion2
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingId
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository

@Service
@Transactional(readOnly = true)
class SentenceEnvelopeServiceVersion2(
  private val agencyLocationRepository: AgencyLocationRepository,
  private val offenderBookingRepository: OffenderBookingRepository,
  private val bookingService: BookingService,
) {
  fun getCalculableSentenceEnvelopeByEstablishment(
    caseLoad: String,
    pageNumber: Int,
    pageSize: Int,
  ): Page<CalculableSentenceEnvelopeVersion2> {
    val agencyLocation = agencyLocationRepository.getReferenceById(caseLoad)
    val pageRequest = PageRequest.of(pageNumber, pageSize, Sort.by("bookingId"))
    val bookingIds =
      offenderBookingRepository.findDistinctByActiveTrueAndLocationAndSentences_statusAndSentences_CalculationType_CalculationTypeNotLikeAndSentences_CalculationType_CategoryNot(
        agencyLocation,
        "A",
        "%AGG%",
        "LICENCE",
        pageRequest,
      )
    val activeBookings =
      offenderBookingRepository.findAllByBookingIdIn(bookingIds.map(OffenderBookingId::bookingId).toList())
    val calculableSentenceEnvelopes =
      activeBookings.map { determineCalculableSentenceEnvelope(it) }.sortedBy { it.bookingId }
    return PageImpl(calculableSentenceEnvelopes, pageRequest, bookingIds.totalElements)
  }

  fun getCalculableSentenceEnvelopeByOffenderNumbers(offenderNumbers: Set<String?>): List<CalculableSentenceEnvelopeVersion2> {
    val bookingIds =
      offenderBookingRepository.findDistinctByActiveTrueAndOffenderNomsIdInAndSentences_statusAndSentences_CalculationType_CalculationTypeNotLikeAndSentences_CalculationType_CategoryNot(
        offenderNumbers,
        "A",
        "%AGG%",
        "LICENCE",
      )

    // ensure that the user has access to each of the bookings
    bookingIds.forEach { bookingService.verifyBookingAccess(it.bookingId, "VIEW_PRISONER_DATA") }

    val activeBookings = offenderBookingRepository.findAllByBookingIdIn(bookingIds.map(OffenderBookingId::bookingId))
    return activeBookings.map { determineCalculableSentenceEnvelope(it) }
  }

  private fun determineCalculableSentenceEnvelope(offenderBooking: OffenderBooking): CalculableSentenceEnvelopeVersion2 = CalculableSentenceEnvelopeVersion2(
    offenderBooking.offender.nomsId,
    offenderBooking.bookingId,
  )
}
