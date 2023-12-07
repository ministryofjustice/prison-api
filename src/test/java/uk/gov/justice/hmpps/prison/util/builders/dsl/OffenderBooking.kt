package uk.gov.justice.hmpps.prison.util.builders.dsl

import uk.gov.justice.hmpps.prison.util.builders.OffenderBookingBuilder
import uk.gov.justice.hmpps.prison.util.builders.OffenderBookingCourtTransferBuilder
import uk.gov.justice.hmpps.prison.util.builders.OffenderBookingRecallBuilder
import uk.gov.justice.hmpps.prison.util.builders.OffenderBookingReleaseBuilder
import uk.gov.justice.hmpps.prison.util.builders.OffenderBookingTAPTransferBuilder
import uk.gov.justice.hmpps.prison.util.builders.OffenderBookingTransferBuilder
import uk.gov.justice.hmpps.prison.util.builders.TestDataContext
import java.time.LocalDateTime

@DslMarker
annotation class BookingDslMarker

@NomisDataDslMarker
interface BookingDsl {
  @BookingDslMarker
  fun release(
    releaseTime: LocalDateTime = LocalDateTime.now().minusHours(1),
    movementReasonCode: String = "CR",
    commentText: String = "Conditional release",
  )

  @BookingDslMarker
  fun recall(
    prisonId: String = "MDI",
    recallTime: LocalDateTime = LocalDateTime.now().minusMinutes(30),
    movementReasonCode: String = "24",
    commentText: String = "Recalled",
  )

  @BookingDslMarker
  fun temporaryAbsenceRelease(
    releaseTime: LocalDateTime = LocalDateTime.now().minusHours(1),
    movementReasonCode: String = "C3",
    commentText: String = "Day release",
  )

  @BookingDslMarker
  fun temporaryAbsenceReturn(
    prisonId: String = "MDI",
    returnTime: LocalDateTime = LocalDateTime.now().minusMinutes(30),
    movementReasonCode: String = "C3",
    commentText: String = "Day release",
  )

  @BookingDslMarker
  fun sendToCourt(
    releaseTime: LocalDateTime = LocalDateTime.now().minusHours(1),
    movementReasonCode: String = "19",
    commentText: String = "Court appearance",
  )

  @BookingDslMarker
  fun returnFromCourt(
    prisonId: String = "MDI",
    returnTime: LocalDateTime = LocalDateTime.now().minusMinutes(30),
    movementReasonCode: String = "19",
    commentText: String = "Court appearance",
  )

  @BookingDslMarker
  fun transferOut(
    prisonId: String = "MDI",
    transferTime: LocalDateTime = LocalDateTime.now().minusHours(1),
    movementReasonCode: String = "CA",
    commentText: String = "Transfer",
  )

  @BookingDslMarker
  fun transferIn(
    receiveTime: LocalDateTime = LocalDateTime.now().minusMinutes(30),
    movementReasonCode: String = "CA",
    commentText: String = "Transfer",
  )
}

class BookingBuilderRepository(
  private val testDataContext: TestDataContext,
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
  ): OffenderBookingId = OffenderBookingBuilder(
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
  ).save(
    webTestClient = testDataContext.webTestClient,
    jwtAuthenticationHelper = testDataContext.jwtAuthenticationHelper,
    offenderNo = offenderId.offenderNo,
    dataLoader = testDataContext.dataLoader,
  ).let { OffenderBookingId(offenderNo = offenderId.offenderNo, it.bookingId) }

  fun release(
    offenderNo: String,
    releaseTime: LocalDateTime,
    movementReasonCode: String,
    commentText: String,
  ) {
    OffenderBookingReleaseBuilder(
      offenderNo = offenderNo,
      releaseTime = releaseTime,
      movementReasonCode = movementReasonCode,
      commentText = commentText,
    ).release(
      webTestClient = testDataContext.webTestClient,
      jwtAuthenticationHelper = testDataContext.jwtAuthenticationHelper,
    )
  }
  fun recall(
    offenderNo: String,
    prisonId: String,
    recallTime: LocalDateTime,
    movementReasonCode: String,
    commentText: String,
  ) {
    OffenderBookingRecallBuilder(
      offenderNo = offenderNo,
      prisonId = prisonId,
      recallTime = recallTime,
      movementReasonCode = movementReasonCode,
      commentText = commentText,
    ).recall(
      webTestClient = testDataContext.webTestClient,
      jwtAuthenticationHelper = testDataContext.jwtAuthenticationHelper,
    )
  }

  fun sendToCourt(
    offenderNo: String,
    releaseTime: LocalDateTime,
    movementReasonCode: String,
    commentText: String,
  ) {
    OffenderBookingCourtTransferBuilder(
      offenderNo = offenderNo,
      movementReasonCode = movementReasonCode,
      commentText = commentText,
    ).toCourt(
      webTestClient = testDataContext.webTestClient,
      jwtAuthenticationHelper = testDataContext.jwtAuthenticationHelper,
      releaseTime = releaseTime,
    )
  }
  fun returnFromCourt(
    offenderNo: String,
    prisonId: String,
    returnTime: LocalDateTime,
    movementReasonCode: String,
    commentText: String,
  ) {
    OffenderBookingCourtTransferBuilder(
      offenderNo = offenderNo,
      movementReasonCode = movementReasonCode,
      commentText = commentText,
    ).fromCourt(
      webTestClient = testDataContext.webTestClient,
      jwtAuthenticationHelper = testDataContext.jwtAuthenticationHelper,
      returnTime = returnTime,
      prisonId = prisonId,
    )
  }
  fun temporaryAbsenceRelease(
    offenderNo: String,
    releaseTime: LocalDateTime,
    movementReasonCode: String,
    commentText: String,
  ) {
    OffenderBookingTAPTransferBuilder(
      offenderNo = offenderNo,
      movementReasonCode = movementReasonCode,
      commentText = commentText,
    ).temporaryAbsenceRelease(
      webTestClient = testDataContext.webTestClient,
      jwtAuthenticationHelper = testDataContext.jwtAuthenticationHelper,
      releaseTime = releaseTime,
    )
  }
  fun temporaryAbsenceReturn(
    offenderNo: String,
    prisonId: String,
    returnTime: LocalDateTime,
    movementReasonCode: String,
    commentText: String,
  ) {
    OffenderBookingTAPTransferBuilder(
      offenderNo = offenderNo,
      movementReasonCode = movementReasonCode,
      commentText = commentText,
    ).temporaryAbsenceReturn(
      webTestClient = testDataContext.webTestClient,
      jwtAuthenticationHelper = testDataContext.jwtAuthenticationHelper,
      returnTime = returnTime,
      prisonId = prisonId,
    )
  }

  fun transferOut(
    offenderNo: String,
    prisonId: String,
    transferTime: LocalDateTime,
    movementReasonCode: String,
    commentText: String,
  ) {
    OffenderBookingTransferBuilder(
      offenderNo = offenderNo,
      movementReasonCode = movementReasonCode,
      commentText = commentText,
    ).transferOut(
      webTestClient = testDataContext.webTestClient,
      jwtAuthenticationHelper = testDataContext.jwtAuthenticationHelper,
      prisonId = prisonId,
      releaseTime = transferTime,
    )
  }

  fun transferIn(
    offenderNo: String,
    receiveTime: LocalDateTime,
    movementReasonCode: String,
    commentText: String,
  ) {
    OffenderBookingTransferBuilder(
      offenderNo = offenderNo,
      movementReasonCode = movementReasonCode,
      commentText = commentText,
    ).transferIn(
      webTestClient = testDataContext.webTestClient,
      jwtAuthenticationHelper = testDataContext.jwtAuthenticationHelper,
      returnTime = receiveTime,
    )
  }
}

class BookingBuilderFactory(
  testDataContext: TestDataContext,
) {

  val repository: BookingBuilderRepository = BookingBuilderRepository(
    testDataContext,
  )

  fun builder() = BookingBuilder(
    repository,
  )
}

class BookingBuilder(
  private val repository: BookingBuilderRepository,
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

  override fun temporaryAbsenceRelease(releaseTime: LocalDateTime, movementReasonCode: String, commentText: String) {
    repository.temporaryAbsenceRelease(
      offenderNo = offenderBookingId.offenderNo,
      releaseTime = releaseTime,
      movementReasonCode = movementReasonCode,
      commentText = commentText,
    )
  }

  override fun temporaryAbsenceReturn(prisonId: String, returnTime: LocalDateTime, movementReasonCode: String, commentText: String) {
    repository.temporaryAbsenceReturn(
      offenderNo = offenderBookingId.offenderNo,
      returnTime = returnTime,
      movementReasonCode = movementReasonCode,
      commentText = commentText,
      prisonId = prisonId,
    )
  }

  override fun sendToCourt(releaseTime: LocalDateTime, movementReasonCode: String, commentText: String) {
    repository.sendToCourt(
      offenderNo = offenderBookingId.offenderNo,
      releaseTime = releaseTime,
      movementReasonCode = movementReasonCode,
      commentText = commentText,
    )
  }

  override fun returnFromCourt(prisonId: String, returnTime: LocalDateTime, movementReasonCode: String, commentText: String) {
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
}

data class OffenderBookingId(val offenderNo: String, val bookingId: Long)
