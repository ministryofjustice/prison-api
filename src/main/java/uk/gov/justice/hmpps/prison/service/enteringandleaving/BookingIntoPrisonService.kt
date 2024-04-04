package uk.gov.justice.hmpps.prison.service.enteringandleaving

import jakarta.persistence.EntityManager
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.InmateDetail
import uk.gov.justice.hmpps.prison.api.model.RequestForNewBooking
import uk.gov.justice.hmpps.prison.api.model.RequestToRecall
import uk.gov.justice.hmpps.prison.exception.CustomErrorCodes
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocationType
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.ImprisonmentStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImprisonmentStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileCode
import uk.gov.justice.hmpps.prison.repository.jpa.model.ProfileType
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CopyTableRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ImprisonmentStatusRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ProfileCodeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ProfileTypeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.findByIdAndDeactivationDateIsNullOrNull
import uk.gov.justice.hmpps.prison.repository.jpa.repository.findByIdAndTypeAndActiveAndDeactivationDateIsNullOrNull
import uk.gov.justice.hmpps.prison.repository.jpa.repository.findByOffenderNomsIdAndBookingSequenceOrNull
import uk.gov.justice.hmpps.prison.repository.jpa.repository.findByStatusAndActiveOrNull
import uk.gov.justice.hmpps.prison.repository.jpa.repository.findByTypeAndCategoryAndActiveOrNull
import uk.gov.justice.hmpps.prison.repository.jpa.repository.findOneByDescriptionAndAgencyIdOrNull
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade
import uk.gov.justice.hmpps.prison.service.BadRequestException
import uk.gov.justice.hmpps.prison.service.ConflictingRequestException
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import uk.gov.justice.hmpps.prison.service.createbooking.CopyPreviousBookingService
import uk.gov.justice.hmpps.prison.service.transformers.OffenderTransformer
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

@Service
@Transactional
class BookingIntoPrisonService(
  private val offenderRepository: OffenderRepository,
  private val offenderTransformer: OffenderTransformer,
  private val agencyLocationRepository: AgencyLocationRepository,
  private val imprisonmentStatusRepository: ImprisonmentStatusRepository,
  private val agencyLocationTypeRepository: ReferenceCodeRepository<AgencyLocationType>,
  private val agencyInternalLocationRepository: AgencyInternalLocationRepository,
  private val externalMovementService: ExternalMovementService,
  private val bookNumberGenerationService: BookNumberGenerationService,
  private val offenderBookingRepository: OffenderBookingRepository,
  private val entityManager: EntityManager,
  private val bedAssignmentMovementService: BedAssignmentMovementService,
  private val copyTableRepository: CopyTableRepository,
  private val copyPreviousBookingService: CopyPreviousBookingService,
  private val profileTypeRepository: ProfileTypeRepository,
  private val profileCodeRepository: ProfileCodeRepository,
  private val trustAccountService: TrustAccountService,
  private val caseNoteMovementService: CaseNoteMovementService,
  staffUserAccountRepository: StaffUserAccountRepository,
  authenticationFacade: AuthenticationFacade,
) : StaffAwareMovementService(
  staffUserAccountRepository = staffUserAccountRepository,
  authenticationFacade = authenticationFacade,
) {
  fun newBooking(prisonerIdentifier: String, requestForNewBooking: RequestForNewBooking): InmateDetail {
    // grab a lock on the offender and their bookings to ensure that no-one else can add a booking to the offender
    // during our transaction.  Other processes / threads will hang waiting for this to complete.

    // The current offender alias is linked to the booking with sequence 1
    val (offender, offenderBooking) = offenderBookingRepository.findLatestOffenderBookingByNomsIdForUpdate(prisonerIdentifier)
      .map { Pair(it.offender, it) }
      // if there are no bookings then get the root offender instead
      .orElseGet { Pair(rootOffenderForUpdate(prisonerIdentifier).getOrThrow(), null) }

    return newBookingWithoutUpdateLock(offender, offenderBooking, requestForNewBooking)
  }

  fun newBookingWithoutUpdateLock(offender: Offender, previousBooking: OffenderBooking?, requestForNewBooking: RequestForNewBooking): InmateDetail {
    previousInactiveBooking(previousBooking).getOrThrow()
    val imprisonmentStatus: ImprisonmentStatus =
      imprisonmentStatus(requestForNewBooking.imprisonmentStatus).getOrThrow()
    val prison = prison(requestForNewBooking.prisonId).getOrThrow()
    val cellOrReception = cellOrReceptionCode(
      requestForNewBooking.prisonId,
      requestForNewBooking.cellLocation,
    ).let { cellOrLocationWithSpace(it, prison).getOrThrow() }
    val receiveTime =
      externalMovementService.getMovementDateTime(requestForNewBooking.bookingInTime, previousBooking)
        .getOrThrow()
    val bookNumber: String = bookNumberGenerationService.generateBookNumber()
    val staff = getLoggedInStaff().getOrThrow().staff

    // now increment the sequence on each booking - bookings are linked via the root offender
    offender.rootOffender.bookings.forEach(OffenderBooking::incBookingSequence)

    return offenderBookingRepository.save(
      OffenderBooking.builder()
        .bookingBeginDate(receiveTime)
        .bookNumber(bookNumber)
        .offender(offender)
        .location(prison)
        .assignedLivingUnit(cellOrReception)
        .disclosureFlag("N")
        .inOutStatus(MovementDirection.IN.name)
        .active(true)
        .bookingStatus("O")
        .youthAdultCode("N")
        .assignedStaff(staff)
        .createLocation(prison)
        .bookingType("INST")
        .rootOffender(offender.rootOffender)
        .admissionReason("NCO")
        .bookingSequence(1)
        .statusReason("${MovementType.ADM.code}-${requestForNewBooking.movementReasonCode}")
        .build(),
    ).also { newBooking ->
      entityManager.flush()
      val fromLocation = fromLocation(requestForNewBooking.fromLocationId).getOrThrow()

      externalMovementService.updateMovementsForNewOrRecalledBooking(
        booking = newBooking,
        movementReasonCode = requestForNewBooking.movementReasonCode,
        fromLocation = fromLocation,
        prison = prison,
        receiveDateTime = receiveTime,
        commentText = "New Booking",
      ).also { movement ->
        bedAssignmentMovementService.createBedHistory(newBooking, cellOrReception, receiveTime, MovementType.ADM.code)
        previousBooking?.copyKeyDataFromPreviousBooking(newBooking, movement).also {
          // status needs resetting since copy would set to previous value
          newBooking.resetYouthStatus(requestForNewBooking.isYouthOffender)
        }
        trustAccountService.createTrustAccount(newBooking, fromLocation, movement)
        newBooking.setImprisonmentStatus(
          OffenderImprisonmentStatus.builder()
            .agyLocId(prison.id)
            .imprisonmentStatus(imprisonmentStatus)
            .build(),
          receiveTime,
        )
        caseNoteMovementService.createGenerateAdmissionNote(newBooking, movement)
      }
    }.let { offenderTransformer.transform(it) }
  }

  fun recallPrisoner(prisonerIdentifier: String, requestToRecall: RequestToRecall): InmateDetail {
    val booking = previousInactiveBooking(prisonerIdentifier).getOrThrow()
    with(requestToRecall) {
      val prison = prison(prisonId).getOrThrow()
      val cellOrReception =
        cellOrReceptionCode(prisonId, cellLocation).let { cellOrLocationWithSpace(it, prison).getOrThrow() }

      booking.inOutStatus = MovementDirection.IN.name
      booking.isActive = true
      booking.bookingStatus = "O"
      booking.assignedLivingUnit = cellOrReception
      booking.bookingEndDate = null
      booking.location = prison

      val fromLocation = fromLocation(fromLocationId).getOrThrow()
      val receiveTime =
        externalMovementService.getMovementDateTime(recallTime, booking)
          .getOrThrow()

      imprisonmentStatus?.let { imprisonmentStatus(imprisonmentStatus).getOrThrow() }
        ?.also {
          booking.setImprisonmentStatus(
            OffenderImprisonmentStatus.builder()
              .agyLocId(prison.id)
              .imprisonmentStatus(it)
              .build(),
            receiveTime,
          )
        }

      externalMovementService.updateMovementsForNewOrRecalledBooking(
        booking = booking,
        movementReasonCode = movementReasonCode,
        fromLocation = fromLocation,
        prison = prison,
        receiveDateTime = receiveTime,
        commentText = "Recall",
      ).also { movement ->
        bedAssignmentMovementService.createBedHistory(booking, cellOrReception, receiveTime, MovementType.ADM.code)
        booking.resetYouthStatus(isYouthOffender)
        booking.statusReason = "${movement.movementType.code}-$movementReasonCode"
        trustAccountService.createTrustAccount(booking, fromLocation, movement)
        caseNoteMovementService.createGenerateAdmissionNote(booking, movement)
      }
      return offenderTransformer.transform(booking)
    }
  }

  private fun rootOffenderForUpdate(prisonerIdentifier: String): Result<Offender> =
    offenderRepository.findRootOffenderByNomsIdForUpdate(prisonerIdentifier).map { success(it) }
      .orElse(failure(EntityNotFoundException.withMessage("No prisoner found for prisoner number $prisonerIdentifier")))

  private fun previousInactiveBooking(prisonerIdentifier: String): Result<OffenderBooking> =
    offenderBookingRepository.findByOffenderNomsIdAndBookingSequenceOrNull(prisonerIdentifier, 1)?.inActiveOut()
      ?: failure(EntityNotFoundException.withMessage("No bookings found for prisoner number $prisonerIdentifier"))

  private fun previousInactiveBooking(offenderBooking: OffenderBooking?): Result<OffenderBooking?> =
    offenderBooking?.inActiveOut() ?: success(null)

  private fun fromLocation(location: String?): Result<AgencyLocation> = location?.takeIf { it.isNotBlank() }?.let {
    agencyLocationRepository.findByIdAndDeactivationDateIsNullOrNull(it)?.let { location -> success(location) }
      ?: failure(EntityNotFoundException.withMessage("$it is not a valid from location"))
  } ?: agencyLocationRepository.findByIdOrNull(AgencyLocation.OUT)?.let { success(it) } ?: failure(
    EntityNotFoundException.withMessage("${AgencyLocation.OUT} is not a valid from location"),
  )

  private fun imprisonmentStatus(imprisonmentStatus: String): Result<ImprisonmentStatus> =
    imprisonmentStatusRepository.findByStatusAndActiveOrNull(imprisonmentStatus, true)?.let { success(it) } ?: failure(
      EntityNotFoundException.withMessage("No imprisonment status $imprisonmentStatus found"),
    )

  private fun prison(prisonId: String): Result<AgencyLocation> =
    agencyLocationTypeRepository.findByIdOrNull(AgencyLocationType.INST)?.let {
      agencyLocationRepository.findByIdAndTypeAndActiveAndDeactivationDateIsNullOrNull(prisonId, it, true)
        ?.let { prison -> success(prison) } ?: failure(
        EntityNotFoundException.withMessage("$prisonId prison not found"),
      )
    } ?: failure(
      EntityNotFoundException.withMessage("Not Found"),
    )

  private fun cellOrReceptionCode(prisonId: String, cellLocation: String?) =
    cellLocation?.takeIf { it.isNotBlank() } ?: "$prisonId-RECP"

  private fun cellOrLocationWithSpace(
    internalLocationDescription: String,
    prison: AgencyLocation,
  ): Result<AgencyInternalLocation> =
    agencyInternalLocationRepository.findOneByDescriptionAndAgencyIdOrNull(internalLocationDescription, prison.id)
      ?.let {
        it.takeIf { it.hasSpace() }?.let { internalLocation -> success(internalLocation) } ?: failure(
          ConflictingRequestException.withMessage(
            "The cell $internalLocationDescription does not have any available capacity",
            CustomErrorCodes.NO_CELL_CAPACITY,
          ),
        )
      } ?: failure(EntityNotFoundException.withMessage("$internalLocationDescription cell location not found"))

  private fun OffenderBooking?.copyKeyDataFromPreviousBooking(newBooking: OffenderBooking, movement: ExternalMovement) {
    this?.takeIf { copyTableRepository.shouldCopyForAdmission() }?.run {
      copyPreviousBookingService.copyKeyDataFromPreviousBooking(newBooking, this, movement)
      entityManager.flush()
      // booking needs reloading since booking has been amended by copyPreviousBookingService
      entityManager.refresh(newBooking)
    }
  }

  private fun OffenderBooking.resetYouthStatus(isYouthOffender: Boolean) {
    val profileType: ProfileType = profileTypeRepository.youthProfile().getOrThrow()
    val profileCode: ProfileCode =
      profileCodeRepository.profile(profileType, if (isYouthOffender) "Y" else "N").getOrThrow()
    this.add(profileType, profileCode)
  }

  private fun ProfileTypeRepository.youthProfile(): Result<ProfileType> =
    this.findByTypeAndCategoryAndActiveOrNull("YOUTH", "PI", true)?.let { success(it) } ?: failure(
      EntityNotFoundException.withId("YOUTH"),
    )

  private fun ProfileCodeRepository.profile(type: ProfileType, code: String): Result<ProfileCode> =
    this.findByIdOrNull(ProfileCode.PK(type, code))?.let { success(it) } ?: failure(
      EntityNotFoundException.withMessage("Profile Code for YOUTH and $code not found"),
    )
}

private fun CopyTableRepository.shouldCopyForAdmission(): Boolean =
  findByOperationCodeAndMovementTypeAndActiveAndExpiryDateIsNull(
    "COP",
    MovementType.ADM.code,
    true,
  ).isNotEmpty()

private fun OffenderBooking.inActiveOut(): Result<OffenderBooking> {
  if (this.isActive) {
    return failure(BadRequestException("Prisoner is currently active"))
  } else if (!this.isOut) {
    return failure(BadRequestException("Prisoner is not currently OUT"))
  }
  return success(this)
}
