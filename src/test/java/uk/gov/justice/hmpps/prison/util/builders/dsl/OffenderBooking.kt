package uk.gov.justice.hmpps.prison.util.builders.dsl

import uk.gov.justice.hmpps.prison.util.builders.OffenderBookingBuilder
import uk.gov.justice.hmpps.prison.util.builders.OffenderBookingReleaseBuilder
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
}

data class OffenderBookingId(val offenderNo: String, val bookingId: Long)
