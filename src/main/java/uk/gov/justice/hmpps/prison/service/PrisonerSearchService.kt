package uk.gov.justice.hmpps.prison.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.prison.api.model.InmateDetail
import uk.gov.justice.hmpps.prison.api.model.PrisonerSearchDetails
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.service.transformers.OffenderTransformer
import java.util.Optional

@Component
class PrisonerSearchService(
  private val offenderBookingRepository: OffenderBookingRepository,
  private val offenderRepository: OffenderRepository,
  private val offenderTransformer: OffenderTransformer,
  private val inmateService: InmateService,
) {

  @Transactional
  fun getPrisonerDetails(offenderNo: String): PrisonerSearchDetails =
    getInmateDetail(offenderNo)
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
          alerts = it.alerts?.sortedBy { it.alertId },
          assignedLivingUnit = it.assignedLivingUnit,
          religion = it.religion,
          physicalAttributes = it.physicalAttributes,
          physicalCharacteristics = it.physicalCharacteristics,
          profileInformation = it.profileInformation,
          physicalMarks = it.physicalMarks,
          csra = it.csra,
          categoryCode = it.categoryCode,
          inOutStatus = it.inOutStatus,
          identifiers = it.identifiers?.sortedBy { it.whenCreated },
          sentenceDetail = it.sentenceDetail,
          mostSeriousOffence = it.offenceHistory?.filter { off -> off.bookingId == it.bookingId }?.firstOrNull { it.mostSerious }?.offenceDescription,
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

  private fun getInmateDetail(offenderNo: String): InmateDetail {
    val booking = offenderBookingRepository.findLatestOffenderBookingByNomsId(offenderNo).toNullable()
    val offender = booking?.offender ?: offenderRepository.findRootOffenderByNomsId(offenderNo).toNullable()
    if (offender == null) throw EntityNotFoundException.withId(offenderNo)
    return booking
      ?.let { offenderTransformer.transform(it) }
      ?.apply {
        // TODO These need to be modelled in JPA and set by the OffenderTransformer
        physicalAttributes = inmateService.getPhysicalAttributes(bookingId)
        physicalCharacteristics = inmateService.getPhysicalCharacteristics(bookingId)
        physicalMarks = inmateService.getPhysicalMarks(bookingId)
        aliases = inmateService.getAliases(bookingId)
        inmateService.getAllAssessmentsOrdered(bookingId).also {
          csra = it.firstOrNull { it.isCellSharingAlertFlag }?.classification
          categoryCode = it.firstOrNull { "CATEGORY" == it.assessmentCode }?.classificationCode
        }
      }
      ?: let { offenderTransformer.transformWithoutBooking(offender) }
  }
}

private fun <T> Optional<T>.toNullable(): T? = orElse(null)
