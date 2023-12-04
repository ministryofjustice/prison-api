package uk.gov.justice.hmpps.prison.util.builders.dsl

import uk.gov.justice.hmpps.prison.util.builders.OffenderBookingBuilder
import uk.gov.justice.hmpps.prison.util.builders.TestDataContext
import java.time.LocalDateTime

@DslMarker
annotation class BookingDslMarker

@NomisDataDslMarker
interface BookingDsl

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
    offenderNo = offenderId.nomsId,
    dataLoader = testDataContext.dataLoader,
  ).let { OffenderBookingId(it.bookingId) }
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
}

data class OffenderBookingId(val bookingId: Long)
