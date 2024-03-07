package uk.gov.justice.hmpps.prison.service

import org.apache.commons.text.WordUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.calculation.CalculableSentenceEnvelope
import uk.gov.justice.hmpps.prison.api.model.calculation.Person
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderFixedTermRecall
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingId
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderFixedTermRecallRepository
import uk.gov.justice.hmpps.prison.service.transformers.OffenderAlertTransformer

@Service
@Transactional(readOnly = true)
class SentenceEnvelopeService(
  private val agencyLocationRepository: AgencyLocationRepository,
  private val offenderFixedTermRecallRepository: OffenderFixedTermRecallRepository,
  private val offenderBookingRepository: OffenderBookingRepository,
  private val bookingService: BookingService,
) {
  fun getCalculableSentenceEnvelopeByEstablishment(
    caseLoad: String,
    pageNumber: Int,
    pageSize: Int,
  ): Page<CalculableSentenceEnvelope> {
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

  fun getCalculableSentenceEnvelopeByOffenderNumbers(offenderNumbers: Set<String?>): List<CalculableSentenceEnvelope> {
    val bookingIds =
      offenderBookingRepository.findDistinctByActiveTrueAndOffenderNomsIdInAndSentences_statusAndSentences_CalculationType_CalculationTypeNotLikeAndSentences_CalculationType_CategoryNot(
        offenderNumbers,
        "A",
        "%AGG%",
        "LICENCE",
      )
    val activeBookings = offenderBookingRepository.findAllByBookingIdIn(bookingIds.map(OffenderBookingId::bookingId))
    return activeBookings.map { determineCalculableSentenceEnvelope(it) }
  }

  private fun determineCalculableSentenceEnvelope(offenderBooking: OffenderBooking): CalculableSentenceEnvelope {
    val person = Person(
      offenderBooking.offender.nomsId,
      offenderBooking.offender.birthDate,
      WordUtils.capitalizeFully(offenderBooking.offender.lastName),
      offenderBooking.location.id,
      offenderBooking.alerts.filter { it.isActive }
        .map { OffenderAlertTransformer.transformForOffender(it) },
    )
    val sentenceAdjustments = offenderBooking.getSentenceAdjustments().filter { it.isActive }
    val bookingAdjustments = offenderBooking.getBookingAdjustments().filter { it.isActive }
    val bookingId = offenderBooking.bookingId

    val sentences = offenderBooking.sentences.filter {
      it.status == "A" &&
        !it.calculationType.calculationType.contains("AGG") &&
        it.calculationType.category != "LICENCE"
    }.filterNotNull().map { it.sentenceAndOffenceDetail }.sortedBy { it.sentenceSequence }

    val offenderFinePaymentDtoList = sentences.takeIf { sentences.any { it.isAFine } }?.let {
      bookingService.getOffenderFinePayments(bookingId)
    } ?: emptyList()

    val fixedTermRecallDetails = sentences
      .takeIf { sentences.any { it.isFixedTermRecallType } }?.let {
        offenderFixedTermRecallRepository.findById(bookingId)
          .map(OffenderFixedTermRecall::mapToFixedTermRecallDetails)
          .orElseThrow(EntityNotFoundException.withMessage("No fixed term recall found for booking $bookingId"))
      }
    return CalculableSentenceEnvelope(
      person,
      bookingId,
      sentences,
      sentenceAdjustments,
      bookingAdjustments,
      offenderFinePaymentDtoList,
      fixedTermRecallDetails,
      offenderBooking.sentenceCalcDates,
    )
  }
}
