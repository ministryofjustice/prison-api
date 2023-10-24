package uk.gov.justice.hmpps.prison.service.courtdates

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.courtdates.CourtDateCharge
import uk.gov.justice.hmpps.prison.api.model.courtdates.CourtDateResult
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventChargeRepository
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess

@Service
@Transactional(readOnly = true)
class CourtDateService(
  private val courtEventChargeRepository: CourtEventChargeRepository,
) {

  fun getCourtDateResults(offenderNo: String): List<CourtDateResult> {
    return courtEventChargeRepository.findByOffender(offenderNo).map {
      val event = it.eventAndCharge.courtEvent
      val charge = it.eventAndCharge.offenderCharge
      CourtDateResult(
        event.id,
        event.eventDate,
        it.resultCodeOne?.code,
        it.resultCodeOne?.description,
        it.resultCodeOne?.dispositionCode,
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
        charge.offenderBooking.bookNumber,
      )
    }
      .sortedBy { it.date }
  }
}
