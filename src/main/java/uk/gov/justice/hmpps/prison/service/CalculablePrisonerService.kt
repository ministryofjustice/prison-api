package uk.gov.justice.hmpps.prison.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.calculation.CalculablePrisoner
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingId
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository

@Service
@Transactional(readOnly = true)
class CalculablePrisonerService(
  private val agencyLocationRepository: AgencyLocationRepository,
  private val offenderBookingRepository: OffenderBookingRepository,
) {
  fun getCalculablePrisonerEnvelopeByEstablishment(
    caseLoad: String,
    pageNumber: Int,
    pageSize: Int,
  ): Page<CalculablePrisoner> {
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
      activeBookings.map { determineCalculablePrisoner(it) }.sortedBy { it.bookingId }
    return PageImpl(calculableSentenceEnvelopes, pageRequest, bookingIds.totalElements)
  }

  private fun determineCalculablePrisoner(offenderBooking: OffenderBooking): CalculablePrisoner = CalculablePrisoner(
    offenderBooking.offender.nomsId,
    offenderBooking.bookingId,
  )
}
