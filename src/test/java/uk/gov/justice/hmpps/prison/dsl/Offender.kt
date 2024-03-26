package uk.gov.justice.hmpps.prison.dsl

import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.prison.api.model.RequestToCreate
import uk.gov.justice.hmpps.prison.repository.OffenderDeletionRepository
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import uk.gov.justice.hmpps.prison.service.enteringandleaving.PrisonerCreationService
import uk.gov.justice.hmpps.prison.util.builders.randomName
import java.time.LocalDate
import java.time.LocalDateTime

@NomisDataDslMarker
interface OffenderDsl {
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

  fun alias(
    lastName: String = "ALIAS",
    firstName: String = randomName(),
    birthDate: LocalDate = LocalDate.of(1990, 8, 20),
  ): AliasId

  fun address(
    premise: String = "18",
    street: String = "High Street",
    locality: String = "City center",
    cityCode: String = "1357",
    postalCode: String = "S1 3GG",
    countryCode: String = "ENG",
    primary: Boolean = true,
    pafValidated: Boolean = false,
    mail: Boolean = false,
    noFixedAddress: Boolean = false,
  ): AddressId
}

@Component
class OffenderBuilderRepository(
  private val prisonerCreationService: PrisonerCreationService,
  private val offenderDeletionRepository: OffenderDeletionRepository,
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
  ): OffenderId =
    prisonerCreationService.createPrisoner(
      RequestToCreate
        .builder()
        .pncNumber(pncNumber)
        .croNumber(croNumber)
        .lastName(lastName)
        .firstName(firstName)
        .middleName1(middleName1)
        .middleName2(middleName2)
        .dateOfBirth(birthDate)
        .gender(genderCode)
        .ethnicity(ethnicity)
        .build(),
    ).let {
      OffenderId(it.offenderNo)
    }

  fun deletePrisoner(offenderNo: String) {
    kotlin.runCatching {
      offenderDeletionRepository.deleteAllOffenderDataIncludingBaseRecord(offenderNo)
    }.onFailure {
      when (it) {
        is EntityNotFoundException -> println("Ignoring delete of a prisoner that does not exist")
        else -> throw it
      }
    }
  }
}

@Component
class OffenderBuilderFactory(
  private val bookingBuilderFactory: BookingBuilderFactory,
  private val aliasBuilderFactory: AliasBuilderFactory,
  private val repository: OffenderBuilderRepository,
  private val offenderAddressBuilderFactory: OffenderAddressBuilderFactory,
) {

  fun builder(): OffenderBuilder {
    return OffenderBuilder(repository, bookingBuilderFactory, aliasBuilderFactory, offenderAddressBuilderFactory)
  }

  fun deletePrisoner(offenderNo: String) {
    repository.deletePrisoner(offenderNo)
  }
}

class OffenderBuilder(
  private val repository: OffenderBuilderRepository,
  private val bookingBuilderFactory: BookingBuilderFactory,
  private val aliasBuilderFactory: AliasBuilderFactory,
  private val offenderAddressBuilderFactory: OffenderAddressBuilderFactory,
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

  override fun alias(lastName: String, firstName: String, birthDate: LocalDate) = aliasBuilderFactory.builder().build(
    offenderId = offenderId,
    lastName = lastName,
    firstName = firstName,
    birthDate = birthDate,
  )

  override fun address(
    premise: String,
    street: String,
    locality: String,
    cityCode: String,
    postalCode: String,
    countryCode: String,
    primary: Boolean,
    pafValidated: Boolean,
    mail: Boolean,
    noFixedAddress: Boolean,
  ) = offenderAddressBuilderFactory.builder().build(
    offenderId = offenderId,
    premise = premise,
    street = street,
    locality = locality,
    cityCode = cityCode,
    postalCode = postalCode,
    countryCode = countryCode,
    primary = primary,
    pafValidated = pafValidated,
    mail = mail,
    noFixedAddress = noFixedAddress,
  )
}

data class OffenderId(val offenderNo: String)
