package uk.gov.justice.hmpps.prison.util.builders.dsl

import uk.gov.justice.hmpps.prison.util.builders.TestDataContext
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

class OffenderBuilderRepository(
  private val testDataContext: TestDataContext,
) {
  fun save(
    pncNumber: String?,
    croNumber: String?,
    lastName: String,
    firstName: String,
    middleName1: String?,
    middleName2: String?,
    birthDate: LocalDate,
    genderCode: String,
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
    bookingBuilders = emptyArray(),
  ).save(testDataContext).let { OffenderId(it.offenderNo) }

  fun deletePrisoner(offenderNo: String) {
    testDataContext.dataLoader.offenderDeletionRepository.deleteAllOffenderDataIncludingBaseRecord(offenderNo)
  }
}

class OffenderBuilderFactory(
  testDataContext: TestDataContext,
) {
  private val bookingBuilderFactory: BookingBuilderFactory = BookingBuilderFactory(testDataContext = testDataContext)
  private val repository: OffenderBuilderRepository = OffenderBuilderRepository(testDataContext = testDataContext)

  fun builder(): OffenderBuilder {
    return OffenderBuilder(repository, bookingBuilderFactory)
  }

  fun deletePrisoner(offenderNo: String) {
    repository.deletePrisoner(offenderNo)
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
  ).also {
    offenderId = it
  }

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

data class OffenderId(val offenderNo: String)
