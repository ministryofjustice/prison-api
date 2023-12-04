package uk.gov.justice.hmpps.prison.util.builders.dsl

import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.hmpps.prison.service.DataLoaderRepository
import uk.gov.justice.hmpps.prison.util.JwtAuthenticationHelper
import uk.gov.justice.hmpps.prison.util.builders.OffenderBookingBuilder
import java.time.LocalDateTime

@DslMarker
annotation class BookingDslMarker

@NomisDataDslMarker
interface BookingDsl

@Component
class BookingBuilderRepository(
  private val webTestClient: WebTestClient,
  private val jwtAuthenticationHelper: JwtAuthenticationHelper,
  private val dataLoader: DataLoaderRepository,
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
    webTestClient = webTestClient,
    jwtAuthenticationHelper = jwtAuthenticationHelper,
    offenderNo = offenderId.nomsId,
    dataLoader = dataLoader,
  ).let { OffenderBookingId(it.bookingId) }
}

@Component
class BookingBuilderFactory(
  private val repository: BookingBuilderRepository,
) {
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
    prisonId: String = "MDI",
    bookingInTime: LocalDateTime = LocalDateTime.now().minusDays(1),
    fromLocationId: String? = null,
    movementReasonCode: String = "N",
    cellLocation: String? = null,
    imprisonmentStatus: String = "SENT03",
    iepLevel: String? = null,
    iepLevelComment: String = "iep level comment",
    voBalance: Int? = null,
    pvoBalance: Int? = null,
    youthOffender: Boolean = false,
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
