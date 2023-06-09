package uk.gov.justice.hmpps.prison.service.courtdates

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.courtdates.CourtDateCharge
import uk.gov.justice.hmpps.prison.api.model.courtdates.CourtDateResult
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventChargeRepository
@Service
@Transactional(readOnly = true)
class CourtDateService(
  private val courtEventChargeRepository: CourtEventChargeRepository,
) {

  fun getCourtDateResults(offenderId: String): List<CourtDateResult> {
    return courtEventChargeRepository.findByOffender(offenderId).map {
      val event = it.eventAndCharge.courtEvent
      val charge = it.eventAndCharge.offenderCharge
      CourtDateResult(
        event.id,
        event.eventDate,
        event.outcomeReasonCode?.code,
        event.outcomeReasonCode?.description,
        event.outcomeReasonCode?.dispositionCode,
        CourtDateCharge(
          charge.id,
          charge.offence.code,
          charge.offence.statute.code,
          charge.offence.description,
          charge.dateOfOffence,
          charge.endDate,
          charge.pleaCode == "G",
          charge.offenderCourtCase.id,
          charge.offenderCourtCase.caseInfoNumber,
          charge.offenderCourtCase.agencyLocation?.description,
          charge.offenderSentenceCharges.firstOrNull()?.offenderSentence?.sequence,
          charge.offenderSentenceCharges.firstOrNull()?.offenderSentence?.courtOrder?.courtDate,
          charge.resultCodeOne?.description,
        ),
        charge.offenderBooking.bookingId,
      )
    }
      .sortedBy { it.date }
  }
}
