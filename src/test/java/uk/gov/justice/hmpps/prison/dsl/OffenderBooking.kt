package uk.gov.justice.hmpps.prison.dsl

import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.prison.api.model.RequestForCourtTransferIn
import uk.gov.justice.hmpps.prison.api.model.RequestForNewBooking
import uk.gov.justice.hmpps.prison.api.model.RequestForTemporaryAbsenceArrival
import uk.gov.justice.hmpps.prison.api.model.RequestToRecall
import uk.gov.justice.hmpps.prison.api.model.RequestToReleasePrisoner
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferIn
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferOut
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferOutToCourt
import uk.gov.justice.hmpps.prison.api.model.RequestToTransferOutToTemporaryAbsence
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderIndividualSchedule
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTeamAssignment
import uk.gov.justice.hmpps.prison.repository.jpa.model.Team
import uk.gov.justice.hmpps.prison.service.PrisonerTransferService
import uk.gov.justice.hmpps.prison.service.enteringandleaving.BookingIntoPrisonService
import uk.gov.justice.hmpps.prison.service.enteringandleaving.ReleasePrisonerService
import uk.gov.justice.hmpps.prison.service.enteringandleaving.TransferIntoPrisonService
import java.time.LocalDateTime

@DslMarker
annotation class BookingDslMarker

@DslMarker
annotation class MovementActionDslMarker

@NomisDataDslMarker
interface BookingDsl {
  @MovementActionDslMarker
  fun release(
    releaseTime: LocalDateTime = LocalDateTime.now().minusHours(1),
    movementReasonCode: String = "CR",
    commentText: String = "Conditional release",
  )

  @MovementActionDslMarker
  fun recall(
    prisonId: String = "MDI",
    recallTime: LocalDateTime = LocalDateTime.now().minusMinutes(30),
    movementReasonCode: String = "24",
    commentText: String = "Recalled",
  )

  @MovementActionDslMarker
  fun temporaryAbsenceRelease(
    releaseTime: LocalDateTime = LocalDateTime.now().minusHours(1),
    movementReasonCode: String = "C3",
    commentText: String = "Day release",
    toLocation: String = "18248",
    shouldReleaseBed: Boolean = false,
  )

  @MovementActionDslMarker
  fun temporaryAbsenceReturn(
    prisonId: String = "MDI",
    returnTime: LocalDateTime = LocalDateTime.now().minusMinutes(30),
    movementReasonCode: String = "C3",
    commentText: String = "Day release",
  )

  @MovementActionDslMarker
  fun sendToCourt(
    releaseTime: LocalDateTime = LocalDateTime.now().minusHours(1),
    movementReasonCode: String = "19",
    commentText: String = "Court appearance",
    toLocation: String = "COURT1",
    shouldReleaseBed: Boolean = false,
    courtEventId: Long? = null,
  )

  @MovementActionDslMarker
  fun returnFromCourt(
    prisonId: String = "MDI",
    returnTime: LocalDateTime = LocalDateTime.now().minusMinutes(30),
    movementReasonCode: String = "19",
    commentText: String = "Court appearance",
  )

  @MovementActionDslMarker
  fun transferOut(
    prisonId: String = "MDI",
    transferTime: LocalDateTime = LocalDateTime.now().minusHours(1),
    movementReasonCode: String = "NOTR",
    commentText: String = "Transfer",
  )

  @MovementActionDslMarker
  fun transferIn(
    receiveTime: LocalDateTime = LocalDateTime.now().minusMinutes(30),
    movementReasonCode: String = "CA",
    commentText: String = "Transfer",
  )

  @VisitBalanceDslMarker
  fun visitBalance(
    voBalance: Int = 4,
    pvoBalance: Int = 2,
  )

  @CourtCaseDslMarker
  fun courtCase(
    courtId: String = "COURT1",
    dsl: CourtCaseDsl.() -> Unit = {},
  )

  @TemporaryAbsenceScheduleDslMarker
  fun scheduleTemporaryAbsence(
    startTime: LocalDateTime = LocalDateTime.now().plusDays(1),
    toAddressId: Long = -22,
  ): OffenderIndividualSchedule

  @TeamAssignmentDslMarker
  fun teamAssignment(
    teamToAssign: Team,
    functionTypeCode: String = "AUTO_TRN",
  ): OffenderTeamAssignment
}

@Component
class BookingBuilderRepository(
  private val releasePrisonerService: ReleasePrisonerService,
  private val transferIntoPrisonService: TransferIntoPrisonService,
  private val prisonerTransferService: PrisonerTransferService,
  private val bookingIntoPrisonService: BookingIntoPrisonService,
) {
  fun save(
    offenderId: OffenderId,
    prisonId: String,
    bookingInTime: LocalDateTime = LocalDateTime.now().minusDays(1),
    fromLocationId: String?,
    movementReasonCode: String,
    cellLocation: String?,
    imprisonmentStatus: String,
    iepLevel: String?,
    iepLevelComment: String,
    voBalance: Int?,
    pvoBalance: Int?,
    youthOffender: Boolean,
  ): OffenderBookingId = bookingIntoPrisonService.newBooking(
    offenderId.offenderNo,
    RequestForNewBooking
      .builder()
      .bookingInTime(bookingInTime)
      .cellLocation(cellLocation)
      .fromLocationId(fromLocationId)
      .imprisonmentStatus(imprisonmentStatus)
      .movementReasonCode(movementReasonCode)
      .prisonId(prisonId)
      .youthOffender(youthOffender)
      .build(),
  ).let { OffenderBookingId(offenderNo = offenderId.offenderNo, it.bookingId) }

  fun release(
    offenderNo: String,
    releaseTime: LocalDateTime,
    movementReasonCode: String,
    commentText: String,
  ) {
    releasePrisonerService.releasePrisoner(
      offenderNo,
      RequestToReleasePrisoner
        .builder()
        .releaseTime(releaseTime)
        .movementReasonCode(movementReasonCode)
        .commentText(commentText)
        .build(),
    )
  }

  fun recall(
    offenderNo: String,
    prisonId: String,
    recallTime: LocalDateTime,
    movementReasonCode: String,
    commentText: String,
  ) {
    bookingIntoPrisonService.recallPrisoner(
      offenderNo,
      RequestToRecall
        .builder()
        .recallTime(recallTime)
        .movementReasonCode(movementReasonCode)
        .prisonId(prisonId)
        .build(),
    )
  }

  fun sendToCourt(
    offenderNo: String,
    releaseTime: LocalDateTime,
    movementReasonCode: String,
    commentText: String,
    toLocation: String,
    shouldReleaseBed: Boolean,
    courtEventId: Long?,
  ) {
    prisonerTransferService.transferOutPrisonerToCourt(
      offenderNo,
      RequestToTransferOutToCourt
        .builder()
        .movementTime(releaseTime)
        .commentText(commentText)
        .toLocation(toLocation)
        .shouldReleaseBed(shouldReleaseBed)
        .courtEventId(courtEventId)
        .transferReasonCode(movementReasonCode)
        .build(),
    )
  }

  fun returnFromCourt(
    offenderNo: String,
    prisonId: String,
    returnTime: LocalDateTime,
    movementReasonCode: String,
    commentText: String,
  ) {
    transferIntoPrisonService.transferInViaCourt(
      offenderNo,
      RequestForCourtTransferIn.builder()
        .agencyId(prisonId)
        .dateTime(returnTime)
        .movementReasonCode(movementReasonCode)
        .commentText(commentText)
        .build(),
    )
  }

  fun temporaryAbsenceRelease(
    offenderNo: String,
    releaseTime: LocalDateTime,
    movementReasonCode: String,
    commentText: String,
    toLocation: String,
    shouldReleaseBed: Boolean,
  ) {
    prisonerTransferService.transferOutPrisonerToTemporaryAbsence(
      offenderNo,
      RequestToTransferOutToTemporaryAbsence.builder()
        .toCity(toLocation)
        .movementTime(releaseTime)
        .transferReasonCode(movementReasonCode)
        .commentText(commentText)
        .shouldReleaseBed(shouldReleaseBed)
        .scheduleEventId(null)
        .build(),
    )
  }

  fun temporaryAbsenceReturn(
    offenderNo: String,
    prisonId: String,
    returnTime: LocalDateTime,
    movementReasonCode: String,
    commentText: String,
  ) {
    transferIntoPrisonService.transferInAfterTemporaryAbsence(
      offenderNo,
      RequestForTemporaryAbsenceArrival.builder()
        .agencyId(prisonId)
        .dateTime(returnTime)
        .movementReasonCode(movementReasonCode)
        .commentText(commentText)
        .build(),
    )
  }

  fun transferOut(
    offenderNo: String,
    prisonId: String,
    transferTime: LocalDateTime,
    movementReasonCode: String,
    commentText: String,
  ) {
    prisonerTransferService.transferOutPrisoner(
      offenderNo,
      RequestToTransferOut.builder()
        .toLocation(prisonId)
        .movementTime(transferTime)
        .transferReasonCode(movementReasonCode)
        .commentText(commentText)
        .escortType("PECS")
        .build(),

    )
  }

  fun transferIn(
    offenderNo: String,
    receiveTime: LocalDateTime,
    movementReasonCode: String,
    commentText: String,
  ) {
    transferIntoPrisonService.transferInFromPrison(
      offenderNo,
      RequestToTransferIn.builder()
        .receiveTime(receiveTime)
        .commentText(commentText)
        .build(),
    )
  }
}

@Component
class BookingBuilderFactory(
  private val repository: BookingBuilderRepository,
  private val visitBalanceBuilderFactory: VisitBalanceBuilderFactory,
  private val courtCaseBuilderFactory: CourtCaseBuilderFactory,
  private val teamAssignmentBuilderFactory: TeamAssignmentBuilderFactory,
  private val temporaryAbsenceScheduleBuilderFactory: TemporaryAbsenceScheduleBuilderFactory,
) {
  fun builder() =
    BookingBuilder(
      repository,
      visitBalanceBuilderFactory,
      courtCaseBuilderFactory,
      teamAssignmentBuilderFactory,
      temporaryAbsenceScheduleBuilderFactory,
    )
}

class BookingBuilder(
  private val repository: BookingBuilderRepository,
  private val visitBalanceBuilderFactory: VisitBalanceBuilderFactory,
  private val courtCaseBuilderFactory: CourtCaseBuilderFactory,
  private val teamAssignmentBuilderFactory: TeamAssignmentBuilderFactory,
  private val temporaryAbsenceScheduleBuilderFactory: TemporaryAbsenceScheduleBuilderFactory,
) : BookingDsl {

  private lateinit var offenderBookingId: OffenderBookingId

  fun build(
    offenderId: OffenderId,
    prisonId: String,
    bookingInTime: LocalDateTime,
    fromLocationId: String?,
    movementReasonCode: String,
    cellLocation: String?,
    imprisonmentStatus: String,
    iepLevel: String?,
    iepLevelComment: String,
    voBalance: Int?,
    pvoBalance: Int?,
    youthOffender: Boolean,
  ): OffenderBookingId {
    return repository.save(
      offenderId = offenderId,
      prisonId = prisonId,
      bookingInTime = bookingInTime,
      fromLocationId = fromLocationId,
      movementReasonCode = movementReasonCode,
      cellLocation = cellLocation,
      imprisonmentStatus = imprisonmentStatus,
      iepLevel = iepLevel,
      iepLevelComment = iepLevelComment,
      voBalance = voBalance,
      pvoBalance = pvoBalance,
      youthOffender = youthOffender,
    ).also { offenderBookingId = it }
  }

  override fun release(releaseTime: LocalDateTime, movementReasonCode: String, commentText: String) {
    repository.release(
      offenderNo = offenderBookingId.offenderNo,
      releaseTime = releaseTime,
      movementReasonCode = movementReasonCode,
      commentText = commentText,
    )
  }

  override fun recall(prisonId: String, recallTime: LocalDateTime, movementReasonCode: String, commentText: String) {
    repository.recall(
      offenderNo = offenderBookingId.offenderNo,
      recallTime = recallTime,
      prisonId = prisonId,
      movementReasonCode = movementReasonCode,
      commentText = commentText,
    )
  }

  override fun temporaryAbsenceRelease(
    releaseTime: LocalDateTime,
    movementReasonCode: String,
    commentText: String,
    toLocation: String,
    shouldReleaseBed: Boolean,
  ) {
    repository.temporaryAbsenceRelease(
      offenderNo = offenderBookingId.offenderNo,
      releaseTime = releaseTime,
      movementReasonCode = movementReasonCode,
      commentText = commentText,
      toLocation = toLocation,
      shouldReleaseBed = shouldReleaseBed,
    )
  }

  override fun temporaryAbsenceReturn(
    prisonId: String,
    returnTime: LocalDateTime,
    movementReasonCode: String,
    commentText: String,
  ) {
    repository.temporaryAbsenceReturn(
      offenderNo = offenderBookingId.offenderNo,
      returnTime = returnTime,
      movementReasonCode = movementReasonCode,
      commentText = commentText,
      prisonId = prisonId,
    )
  }

  override fun sendToCourt(
    releaseTime: LocalDateTime,
    movementReasonCode: String,
    commentText: String,
    toLocation: String,
    shouldReleaseBed: Boolean,
    courtEventId: Long?,
  ) {
    repository.sendToCourt(
      offenderNo = offenderBookingId.offenderNo,
      releaseTime = releaseTime,
      movementReasonCode = movementReasonCode,
      commentText = commentText,
      toLocation = toLocation,
      shouldReleaseBed = shouldReleaseBed,
      courtEventId = courtEventId,
    )
  }

  override fun returnFromCourt(
    prisonId: String,
    returnTime: LocalDateTime,
    movementReasonCode: String,
    commentText: String,
  ) {
    repository.returnFromCourt(
      offenderNo = offenderBookingId.offenderNo,
      returnTime = returnTime,
      movementReasonCode = movementReasonCode,
      commentText = commentText,
      prisonId = prisonId,
    )
  }

  override fun transferOut(
    prisonId: String,
    transferTime: LocalDateTime,
    movementReasonCode: String,
    commentText: String,
  ) {
    repository.transferOut(
      offenderNo = offenderBookingId.offenderNo,
      prisonId = prisonId,
      transferTime = transferTime,
      movementReasonCode = movementReasonCode,
      commentText = commentText,
    )
  }

  override fun transferIn(
    receiveTime: LocalDateTime,
    movementReasonCode: String,
    commentText: String,
  ) {
    repository.transferIn(
      offenderNo = offenderBookingId.offenderNo,
      receiveTime = receiveTime,
      movementReasonCode = movementReasonCode,
      commentText = commentText,
    )
  }

  override fun visitBalance(voBalance: Int, pvoBalance: Int) {
    visitBalanceBuilderFactory.builder().build(
      offenderBookingId = offenderBookingId,
      voBalance = voBalance,
      pvoBalance = pvoBalance,
    )
  }

  override fun courtCase(
    courtId: String,
    dsl: CourtCaseDsl.() -> Unit,
  ) {
    courtCaseBuilderFactory.builder().let { builder ->
      builder.build(
        offenderBookingId = offenderBookingId,
        courtId = courtId,
      ).also {
        builder.apply(dsl)
      }
    }
  }

  override fun scheduleTemporaryAbsence(startTime: LocalDateTime, toAddressId: Long) =
    temporaryAbsenceScheduleBuilderFactory.builder().build(
      bookingId = offenderBookingId.bookingId,
      startTime = startTime,
      toAddressId = toAddressId,
    )

  override fun teamAssignment(teamToAssign: Team, functionTypeCode: String) =
    teamAssignmentBuilderFactory.builder().build(
      offenderBookingId = offenderBookingId,
      teamToAssign = teamToAssign,
      functionTypeCode = functionTypeCode,
    )
}

data class OffenderBookingId(val offenderNo: String, val bookingId: Long)
