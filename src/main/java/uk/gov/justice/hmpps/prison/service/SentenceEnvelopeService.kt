package uk.gov.justice.hmpps.prison.service

import org.apache.commons.text.WordUtils
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.BookingAdjustment
import uk.gov.justice.hmpps.prison.api.model.FixedTermRecallDetails
import uk.gov.justice.hmpps.prison.api.model.OffenderFinePaymentDto
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceAndOffences
import uk.gov.justice.hmpps.prison.api.model.SentenceAdjustmentValues
import uk.gov.justice.hmpps.prison.api.model.calculation.CalculableSentenceEnvelope
import uk.gov.justice.hmpps.prison.api.model.calculation.Person
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAlert
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingId
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.service.transformers.OffenderAlertTransformer
import java.util.Objects
import java.util.stream.Collectors

@Service
@Transactional(readOnly = true)
class SentenceEnvelopeService(
  private val agencyLocationRepository: AgencyLocationRepository,
  private val offenderFixedTermRecallService: OffenderFixedTermRecallService,
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
      offenderBookingRepository.findAllByBookingIdIn(bookingIds.stream().map(OffenderBookingId::bookingId).toList())
    val calculableSentenceEnvelopes = activeBookings.stream()
      .map { offenderBooking: OffenderBooking -> determineCalculableSentenceEnvelope(offenderBooking) }
      .toList()
    return PageImpl(calculableSentenceEnvelopes, pageRequest, bookingIds.totalElements)
  }

  fun getCalculableSentenceEnvelopeByOffenderNumbers(offenderNumbers: Set<String?>?): List<CalculableSentenceEnvelope> {
    val bookingIds =
      offenderBookingRepository.findDistinctByActiveTrueAndOffenderNomsIdInAndSentences_statusAndSentences_CalculationType_CalculationTypeNotLikeAndSentences_CalculationType_CategoryNot(
        offenderNumbers,
        "A",
        "%AGG%",
        "LICENCE",
      )
    val activeBookings =
      offenderBookingRepository.findAllByBookingIdIn(bookingIds.stream().map(OffenderBookingId::bookingId).toList())
    return activeBookings.stream()
      .map { offenderBooking: OffenderBooking -> determineCalculableSentenceEnvelope(offenderBooking) }
      .toList()
  }

  private fun determineCalculableSentenceEnvelope(offenderBooking: OffenderBooking): CalculableSentenceEnvelope {
    val person = Person(
      offenderBooking.offender.nomsId,
      offenderBooking.offender.birthDate,
      WordUtils.capitalizeFully(offenderBooking.offender.lastName),
      offenderBooking.location.id,
      offenderBooking.alerts.stream().filter { obj: OffenderAlert -> obj.isActive }
        .map { offenderAlert: OffenderAlert? -> OffenderAlertTransformer.transformForOffender(offenderAlert) }
        .collect(Collectors.toList()),
    )
    val sentenceAdjustments =
      offenderBooking.getSentenceAdjustments().stream().filter { obj: SentenceAdjustmentValues -> obj.isActive }
        .toList()
    val bookingAdjustments =
      offenderBooking.getBookingAdjustments().stream().filter { obj: BookingAdjustment -> obj.isActive }
        .toList()
    val sentences = bookingService.getSentenceAndOffenceDetails(offenderBooking.bookingId).stream()
      .filter { obj: OffenderSentenceAndOffences? -> Objects.nonNull(obj) }
      .filter { sentence: OffenderSentenceAndOffences -> sentence.sentenceStatus == "A" }.toList()
    val containsFine = sentences.stream().anyMatch { obj: OffenderSentenceAndOffences -> obj.isAFine }
    val containsFixedTermRecall =
      sentences.stream().anyMatch { obj: OffenderSentenceAndOffences -> obj.isFixedTermRecallType }
    val offenderFinePaymentDtoList = getFinesIfRequired(containsFine, offenderBooking.bookingId)
    val fixedTermRecallDetails = getFixedTermRecall(containsFixedTermRecall, offenderBooking.bookingId)
    val sentenceCalcDates = bookingService.getBookingSentenceCalcDatesV1_1(offenderBooking.bookingId)
    return CalculableSentenceEnvelope(
      person,
      offenderBooking.bookingId,
      sentences,
      sentenceAdjustments,
      bookingAdjustments,
      offenderFinePaymentDtoList,
      fixedTermRecallDetails,
      sentenceCalcDates,
    )
  }

  private fun getFinesIfRequired(containsFine: Boolean, bookingId: Long): List<OffenderFinePaymentDto> =
    if (containsFine) {
      bookingService.getOffenderFinePayments(bookingId)
    } else {
      emptyList()
    }

  private fun getFixedTermRecall(containsRecall: Boolean, bookingId: Long): FixedTermRecallDetails? =
    if (containsRecall) {
      offenderFixedTermRecallService.getFixedTermRecallDetails(bookingId)
    } else {
      null
    }
}
