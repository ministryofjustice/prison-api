package uk.gov.justice.hmpps.prison.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.RequestToRecall
import uk.gov.justice.hmpps.prison.api.model.RequestToReleasePrisoner
import uk.gov.justice.hmpps.prison.api.resource.UpdatePrisonerDetails
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.findOffenderByNomsIdOrNull
import uk.gov.justice.hmpps.prison.service.enteringandleaving.BookingIntoPrisonService
import uk.gov.justice.hmpps.prison.service.enteringandleaving.ReleasePrisonerService

@Service
class SmokeTestHelperService(
  private val inmateService: InmateService,
  private val releasePrisonerService: ReleasePrisonerService,
  private val bookingIntoPrisonService: BookingIntoPrisonService,
  private val offenderRepository: OffenderRepository,
) {
  companion object {
    const val SMOKE_TEST_PRISON_ID = "LEI"
  }

  @Transactional
  fun updatePrisonerDetails(offenderNo: String, prisonerDetails: UpdatePrisonerDetails) {
    offenderRepository.findOffenderByNomsIdOrNull(offenderNo)?.apply {
      firstName = prisonerDetails.firstName.uppercase()
      lastName = prisonerDetails.lastName.uppercase()
      offenderRepository.save(this)
    } ?: throw EntityNotFoundException.withMessage("Offender $offenderNo not found")
  }

  @Transactional
  fun offenderStatusSetup(offenderNo: String) {
    val offender = inmateService.findOffender(offenderNo, false, false)
    when (offender.inOutStatus) {
      "OUT" -> recallPrisoner(offenderNo)
      "TRN" -> throw IllegalStateException("The offender should have status 'IN' but has status '${offender.inOutStatus}' for agencyId ${offender.agencyId}, unable to recall into $SMOKE_TEST_PRISON_ID Prison")
    }
  }

  @Transactional
  fun releasePrisoner(offenderNo: String) {
    val requestToReleasePrisoner = RequestToReleasePrisoner.builder()
      .commentText("Prisoner was released as part of smoke test")
      .movementReasonCode("CR")
      .build()
    releasePrisonerService.releasePrisoner(offenderNo, requestToReleasePrisoner)
  }

  @Transactional
  fun recallPrisoner(offenderNo: String) {
    val requestToRecall = RequestToRecall.builder()
      .prisonId(SMOKE_TEST_PRISON_ID)
      .movementReasonCode("24")
      .imprisonmentStatus("CUR_ORA")
      .build()
    bookingIntoPrisonService.recallPrisoner(offenderNo, requestToRecall)
  }
}
