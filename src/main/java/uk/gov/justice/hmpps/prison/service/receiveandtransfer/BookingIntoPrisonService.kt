package uk.gov.justice.hmpps.prison.service.receiveandtransfer

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
import uk.gov.justice.hmpps.prison.repository.jpa.repository.findOffenderByNomsIdOrNull
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
  private val externalMovementTransferService: ExternalMovementTransferService,
  private val bookNumberGenerationService: BookNumberGenerationService,
  private val offenderBookingRepository: OffenderBookingRepository,
  private val entityManager: EntityManager,
  private val bedAssignmentTransferService: BedAssignmentTransferService,
  private val copyTableRepository: CopyTableRepository,
  private val copyPreviousBookingService: CopyPreviousBookingService,
  private val profileTypeRepository: ProfileTypeRepository,
  private val profileCodeRepository: ProfileCodeRepository,
  private val trustAccountService: TrustAccountService,
  private val caseNoteTransferService: CaseNoteTransferService,
  staffUserAccountRepository: StaffUserAccountRepository,
  authenticationFacade: AuthenticationFacade,
) : StaffAwareTransferService(
  staffUserAccountRepository = staffUserAccountRepository,
  authenticationFacade = authenticationFacade,
) {
  fun newBooking(prisonerIdentifier: String, requestForNewBooking: RequestForNewBooking): InmateDetail {
    return newBooking(offender(prisonerIdentifier).getOrThrow(), requestForNewBooking)
  }
  fun newBooking(offender: Offender, requestForNewBooking: RequestForNewBooking): InmateDetail {
    // ensure that we can get a select for update on the bookings before we start
    val bookings = offenderBookingRepository.findAllByOffenderNomsIdForUpdate(offender.nomsId)

    val previousBooking: OffenderBooking? = previousInactiveBooking(offender).getOrThrow()
    val imprisonmentStatus: ImprisonmentStatus =
      imprisonmentStatus(requestForNewBooking.imprisonmentStatus).getOrThrow()
    val prison = prison(requestForNewBooking.prisonId).getOrThrow()
    val cellOrReception = cellOrReceptionCode(
      requestForNewBooking.prisonId,
      requestForNewBooking.cellLocation,
    ).let { cellOrLocationWithSpace(it, prison).getOrThrow() }
    val receiveTime =
      externalMovementTransferService.getReceiveDateTime(requestForNewBooking.bookingInTime, previousBooking)
        .getOrThrow()
    val bookNumber: String = bookNumberGenerationService.generateBookNumber()
    val staff = getLoggedInStaff().getOrThrow().staff

    // now increment the sequence on each booking
    bookings.forEach(OffenderBooking::incBookingSequence)

    return offenderBookingRepository.save(
      OffenderBooking()
        .withBookingBeginDate(receiveTime)
        .withBookNumber(bookNumber)
        .withOffender(offender)
        .withLocation(prison)
        .withAssignedLivingUnit(cellOrReception)
        .withDisclosureFlag("N")
        .withInOutStatus(MovementDirection.IN.name)
        .withActive(true)
        .withBookingStatus("O")
        .withYouthAdultCode("N")
        .withAssignedStaff(staff)
        .withCreateLocation(prison)
        .withBookingType("INST")
        .withRootOffender(offender.rootOffender)
        .withAdmissionReason("NCO")
        .withBookingSequence(1)
        .withStatusReason("${MovementType.ADM.code}-${requestForNewBooking.movementReasonCode}"),
    ).also { newBooking ->
      entityManager.flush()
      val fromLocation = fromLocation(requestForNewBooking.fromLocationId).getOrThrow()

      externalMovementTransferService.updateMovementsForNewOrRecalledBooking(
        booking = newBooking,
        movementReasonCode = requestForNewBooking.movementReasonCode,
        fromLocation = fromLocation,
        prison = prison,
        receiveDateTime = receiveTime,
        commentText = "New Booking",
      ).also { movement ->
        bedAssignmentTransferService.createBedHistory(newBooking, cellOrReception, receiveTime, MovementType.ADM.code)
        previousBooking?.copyKeyDataFromPreviousBooking(newBooking, movement).also {
          // status needs resetting since copy would set to previous value
          newBooking.resetYouthStatus(requestForNewBooking.isYouthOffender)
        }
        trustAccountService.createTrustAccount(newBooking, fromLocation, movement)
        newBooking.setImprisonmentStatus(
          OffenderImprisonmentStatus()
            .withAgyLocId(prison.id)
            .withImprisonmentStatus(imprisonmentStatus),
          receiveTime,
        )
        caseNoteTransferService.createGenerateAdmissionNote(newBooking, movement)
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
        externalMovementTransferService.getReceiveDateTime(recallTime, booking)
          .getOrThrow()

      imprisonmentStatus?.let { imprisonmentStatus(imprisonmentStatus).getOrThrow() }
        ?.also {
          booking.setImprisonmentStatus(
            OffenderImprisonmentStatus()
              .withAgyLocId(prison.id)
              .withImprisonmentStatus(it),
            receiveTime,
          )
        }

      externalMovementTransferService.updateMovementsForNewOrRecalledBooking(
        booking = booking,
        movementReasonCode = movementReasonCode,
        fromLocation = fromLocation,
        prison = prison,
        receiveDateTime = receiveTime,
        commentText = "Recall",
      ).also { movement ->
        bedAssignmentTransferService.createBedHistory(booking, cellOrReception, receiveTime, MovementType.ADM.code)
        booking.resetYouthStatus(isYouthOffender)
        booking.statusReason = "${movement.movementType.code}-$movementReasonCode"
        trustAccountService.createTrustAccount(booking, fromLocation, movement)
        caseNoteTransferService.createGenerateAdmissionNote(booking, movement)
      }
      return offenderTransformer.transform(booking)
    }
  }

  private fun offender(prisonerIdentifier: String): Result<Offender> =
    offenderRepository.findOffenderByNomsIdOrNull(prisonerIdentifier)?.let { success(it) }
      ?: failure(EntityNotFoundException.withMessage("No prisoner found for prisoner number $prisonerIdentifier"))

  private fun previousInactiveBooking(prisonerIdentifier: String): Result<OffenderBooking> =
    offenderBookingRepository.findByOffenderNomsIdAndBookingSequenceOrNull(prisonerIdentifier, 1)?.inActiveOut()
      ?: failure(EntityNotFoundException.withMessage("No bookings found for prisoner number $prisonerIdentifier"))

  private fun previousInactiveBooking(offender: Offender): Result<OffenderBooking?> =
    offender.latestBookingOrNull?.inActiveOut() ?: success(null)

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
        it.takeIf { it.hasSpace(true) }?.let { internalLocation -> success(internalLocation) } ?: failure(
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

private val Offender.latestBookingOrNull: OffenderBooking?
  get() = this.latestBooking.orElse(null)

private fun OffenderBooking.inActiveOut(): Result<OffenderBooking> {
  if (this.isActive) {
    return failure(BadRequestException("Prisoner is currently active"))
  } else if (!this.isOut) {
    return failure(BadRequestException("Prisoner is not currently OUT"))
  }
  return success(this)
}
