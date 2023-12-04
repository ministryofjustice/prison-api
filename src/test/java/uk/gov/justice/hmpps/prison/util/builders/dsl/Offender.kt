package uk.gov.justice.hmpps.prison.util.builders.dsl

import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.hmpps.prison.service.DataLoaderRepository
import uk.gov.justice.hmpps.prison.util.JwtAuthenticationHelper
import uk.gov.justice.hmpps.prison.util.builders.randomName
import java.time.LocalDate
import java.time.LocalDateTime

@DslMarker
annotation class OffenderDslMarker

@NomisDataDslMarker
interface OffenderDsl {
  @BookingDslMarker
  fun booking(
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
    dsl: BookingDsl.() -> Unit = {},
  ): OffenderBookingId
}

@Component
class OffenderBuilderRepository(
  private val webTestClient: WebTestClient,
  private val jwtAuthenticationHelper: JwtAuthenticationHelper,
  private val dataLoader: DataLoaderRepository,
) {
  fun save(
    pncNumber: String? = null,
    croNumber: String? = null,
    lastName: String = "NTHANDA",
    firstName: String = randomName(),
    middleName1: String? = null,
    middleName2: String? = null,
    birthDate: LocalDate = LocalDate.of(1965, 7, 19),
    genderCode: String = "M",
    ethnicity: String? = null,
  ): OffenderId = uk.gov.justice.hmpps.prison.util.builders.OffenderBuilder(
    pncNumber = pncNumber,
    croNumber = croNumber,
    lastName = lastName,
    firstName = firstName,
    middleName1 = middleName1,
    middleName2 = middleName2,
    birthDate = birthDate,
    genderCode = genderCode,
    ethnicity = ethnicity,
  ).save(
    webTestClient = webTestClient,
    jwtAuthenticationHelper = jwtAuthenticationHelper,
    dataLoader = dataLoader,
  ).let { OffenderId(it.offenderNo) }
}

@Component
class OffenderBuilderFactory(
  private val repository: OffenderBuilderRepository,
  private val bookingBuilderFactory: BookingBuilderFactory,
) {
  fun builder(): OffenderBuilder {
    return OffenderBuilder(repository, bookingBuilderFactory)
  }
}

class OffenderBuilder(
  private val repository: OffenderBuilderRepository,
  private val bookingBuilderFactory: BookingBuilderFactory,
) : OffenderDsl {
  private lateinit var offenderId: OffenderId

  fun build(
    pncNumber: String?,
    croNumber: String?,
    lastName: String,
    firstName: String,
    middleName1: String?,
    middleName2: String?,
    birthDate: LocalDate,
    genderCode: String,
    ethnicity: String?,
  ): OffenderId = repository.save(
    pncNumber = pncNumber,
    croNumber = croNumber,
    lastName = lastName,
    firstName = firstName,
    middleName1 = middleName1,
    middleName2 = middleName2,
    birthDate = birthDate,
    genderCode = genderCode,
    ethnicity = ethnicity,
  )

  override fun booking(
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
    dsl: BookingDsl.() -> Unit,
  ): OffenderBookingId = bookingBuilderFactory.builder()
    .let { builder ->
      builder.build(
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
      )
        .also {
          builder.apply(dsl)
        }
    }
}

data class OffenderId(val nomsId: String)
