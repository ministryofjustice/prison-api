package uk.gov.justice.hmpps.prison.service.enteringandleaving

import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.InmateDetail
import uk.gov.justice.hmpps.prison.api.model.RequestForNewBooking
import uk.gov.justice.hmpps.prison.api.model.RequestToDischargePrisoner
import uk.gov.justice.hmpps.prison.api.model.RequestToReleasePrisoner
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.service.BadRequestException
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import uk.gov.justice.hmpps.prison.service.PrisonerTransferService
import uk.gov.justice.hmpps.prison.service.transformers.OffenderTransformer
import java.time.LocalDateTime
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success
import kotlin.math.min

@Service
@Transactional
class DischargeToHospitalService(
  private val offenderRepository: OffenderRepository,
  private val offenderBookingRepository: OffenderBookingRepository,
  private val agencyLocationRepository: AgencyLocationRepository,
  private val bookingIntoPrisonService: BookingIntoPrisonService,
  private val externalMovementService: ExternalMovementService,
  private val releasePrisonerService: ReleasePrisonerService,
  private val caseNoteMovementService: CaseNoteMovementService,
  private val transformer: OffenderTransformer,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  fun dischargeToHospital(offenderNo: String, request: RequestToDischargePrisoner): InmateDetail {
    val dischargeTime = request.dischargeTime ?: LocalDateTime.now()
    val toLocation = findHospitalLocation(request.hospitalLocationCode).getOrThrow()
    val offenderBooking = getOrCreateBooking(offenderNo, dischargeTime, request.fromLocationId, request.supportingPrisonId).getOrThrow()
    val lastMovement = offenderBooking.lastMovement.orElse(null)

    return if (!lastMovement.isRelease()) {
      dischargeToHospital(offenderNo, dischargeTime, toLocation)
    } else {
      // this is to support the "Migrate into Restricted Patients" process where an offender was released in NOMIS instead of being moved to hospital in Restricted Patients
      lastMovement.changeToHospitalDischarge(toLocation)
      caseNoteMovementService.createReleaseNote(offenderBooking, lastMovement)
      transformer.transform(offenderBooking)
    }
  }

  private fun findHospitalLocation(hospitalCode: String): Result<AgencyLocation> =
    agencyLocationRepository.findByIdOrNull(hospitalCode)
      ?.let {
        if (it.isHospital) {
          success(it)
        } else {
          failure(EntityNotFoundException.withMessage(String.format("%s is not a hospital", it.description)))
        }
      }
      ?: failure(EntityNotFoundException.withMessage("No $hospitalCode agency found"))

  private fun getOrCreateBooking(offenderNo: String, dischargeTime: LocalDateTime, fromLocation: String?, prison: String?): Result<OffenderBooking> {
    val offender = findOffender(offenderNo).getOrThrow()

    if (offender.bookings.isEmpty()) {
      log.info("Prisoner booking not yet created for $offenderNo, creating a booking in order to discharge to hospital")
      if (fromLocation == null) return failure(BadRequestException("fromLocationId is required when creating a new booking"))
      if (prison == null) return failure(BadRequestException("prison is required when creating a new booking"))
      bookingIntoPrisonService.newBooking(
        offenderNo,
        RequestForNewBooking
          .builder()
          .bookingInTime(dischargeTime)
          .fromLocationId(fromLocation)
          .prisonId(prison)
          .movementReasonCode(MovementReason.AWAIT_REMOVAL_TO_PSY_HOSPITAL.code)
          .imprisonmentStatus(PrisonerTransferService.PSY_HOSP_FROM_PRISON)
          .build(),
      )
    }

    return getOffenderBooking(offenderNo)
  }

  private fun findOffender(offenderNo: String): Result<Offender> =
    offenderRepository.findOffenderByNomsId(offenderNo).orElse(null)
      ?.let { success(it) }
      ?: failure(EntityNotFoundException.withMessage("No prisoner found for prisoner number $offenderNo"))

  private fun getOffenderBooking(offenderNo: String): Result<OffenderBooking> =
    offenderBookingRepository.findByOffenderNomsIdAndBookingSequence(offenderNo, 1)
      .map { success(it) }
      .orElse(failure(EntityNotFoundException.withMessage("No bookings found for prisoner number $offenderNo")))

  private fun ExternalMovement.changeToHospitalDischarge(toLocation: AgencyLocation) {
    val text = (if (commentText == null) "" else "$commentText. ")
      .let { it + "Psychiatric Hospital Discharge to ${toLocation.description}" }
    movementReason = externalMovementService.getMovementReason(MovementReason.DISCHARGE_TO_PSY_HOSPITAL.code).getOrThrow()
    toAgency = toLocation
    commentText = text.substring(0, min(text.length, 240))
    offenderBooking.statusReason = MovementType.REL.code + "-" + MovementReason.DISCHARGE_TO_PSY_HOSPITAL.code
  }

  private fun ExternalMovement?.isRelease() = this?.movementType?.code == MovementType.REL.code

  private fun dischargeToHospital(offenderNo: String, dischargeTime: LocalDateTime, toLocation: AgencyLocation) =
    RequestToReleasePrisoner.builder()
      .commentText("Psychiatric Hospital Discharge to " + toLocation.description)
      .releaseTime(dischargeTime)
      .movementReasonCode(MovementReason.DISCHARGE_TO_PSY_HOSPITAL.code)
      .toLocationCode(toLocation.id)
      .build()
      .let { releasePrisonerService.releasePrisoner(offenderNo, it, mustBeActiveIn = false) }
}
