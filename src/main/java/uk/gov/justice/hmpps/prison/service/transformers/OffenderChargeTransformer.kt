package uk.gov.justice.hmpps.prison.service.transformers

import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.prison.api.model.OffenceHistoryDetail
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCharge

@Component
class OffenderChargeTransformer : Converter<OffenderCharge, OffenceHistoryDetail> {
  override fun convert(offenderCharge: OffenderCharge): OffenceHistoryDetail {
    val latestCourtEvent = offenderCharge.offenderCourtCase.courtEvents.maxByOrNull { it.eventDate }

    return OffenceHistoryDetail.builder()
      .bookingId(offenderCharge.offenderBooking.bookingId)
      .caseId(offenderCharge.offenderCourtCase.id)
      .offenceDate(offenderCharge.dateOfOffence)
      .offenceRangeDate(offenderCharge.endDate)
      .offenceDescription(offenderCharge.offence.description)
      .offenceCode(offenderCharge.offence.code)
      .statuteCode(offenderCharge.offence.statute.code)
      .mostSerious("Y" == offenderCharge.mostSeriousFlag)
      .primaryResultCode(offenderCharge.resultCodeOne?.code)
      .secondaryResultCode(offenderCharge.resultCodeTwo?.code)
      .primaryResultDescription(offenderCharge.resultCodeOne?.description)
      .secondaryResultDescription(offenderCharge.resultCodeTwo?.description)
      .primaryResultConviction(offenderCharge.resultCodeOne?.isConvictionFlag)
      .secondaryResultConviction(offenderCharge.resultCodeTwo?.isConvictionFlag)
      .courtDate(latestCourtEvent?.eventDate)
      .build()
  }
}
