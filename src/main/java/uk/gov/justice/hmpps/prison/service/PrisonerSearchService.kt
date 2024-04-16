package uk.gov.justice.hmpps.prison.service

import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.prison.api.model.PrisonerSearchDetails

@Component
class PrisonerSearchService(private val inmateService: InmateService) {

  fun getPrisonerDetails(offenderNo: String): PrisonerSearchDetails =
    inmateService.findOffender(offenderNo, true, false)
      .let {
        PrisonerSearchDetails(
          offenderNo = it.offenderNo,
          bookingId = it.bookingId,
          bookingNo = it.bookingNo,
          firstName = it.firstName,
          middleName = it.middleName,
          lastName = it.lastName,
          dateOfBirth = it.dateOfBirth,
          agencyId = it.agencyId,
          alerts = it.alerts,
          assignedLivingUnit = it.assignedLivingUnit,
          religion = it.religion,
          physicalAttributes = it.physicalAttributes,
          physicalCharacteristics = it.physicalCharacteristics,
          profileInformation = it.profileInformation,
          physicalMarks = it.physicalMarks,
          csra = it.csra,
          categoryCode = it.categoryCode,
          inOutStatus = it.inOutStatus,
          identifiers = it.identifiers,
          sentenceDetail = it.sentenceDetail,
          mostSeriousOffence = it.offenceHistory?.firstOrNull { it.mostSerious }?.offenceDescription,
          indeterminateSentence = it.sentenceTerms?.any { st -> st.lifeSentence && it.bookingId == st.bookingId },
          aliases = it.aliases,
          status = it.status,
          lastMovementTypeCode = it.lastMovementTypeCode,
          lastMovementReasonCode = it.lastMovementReasonCode,
          legalStatus = it.legalStatus,
          recall = it.recall,
          imprisonmentStatus = it.imprisonmentStatus,
          imprisonmentStatusDescription = it.imprisonmentStatusDescription,
          receptionDate = it.receptionDate,
          locationDescription = it.locationDescription,
          latestLocationId = it.latestLocationId,
        )
      }
}
