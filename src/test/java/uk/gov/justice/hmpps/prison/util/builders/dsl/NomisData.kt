package uk.gov.justice.hmpps.prison.util.builders.dsl

import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.prison.util.builders.randomName
import java.time.LocalDate

@Component
@Transactional
class NomisDataBuilder(
  private val offenderBuilderFactory: OffenderBuilderFactory? = null,
) {
  fun build(dsl: NomisData.() -> Unit) = NomisData(
    offenderBuilderFactory,
  ).apply(dsl)
}

class NomisData(
  private val offenderBuilderFactory: OffenderBuilderFactory? = null,
) : NomisDataDsl {

  @OffenderDslMarker
  override fun offender(
    pncNumber: String?,
    croNumber: String?,
    lastName: String,
    firstName: String,
    middleName1: String?,
    middleName2: String?,
    birthDate: LocalDate,
    genderCode: String,
    ethnicity: String?,
    dsl: OffenderDsl.() -> Unit,
  ): OffenderId =
    offenderBuilderFactory!!.builder()
      .let { builder ->
        builder.build(
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
          .also {
            builder.apply(dsl)
          }
      }
}

@NomisDataDslMarker
interface NomisDataDsl {
  @OffenderDslMarker
  fun offender(
    pncNumber: String? = null,
    croNumber: String? = null,
    lastName: String = "NTHANDA",
    firstName: String = randomName(),
    middleName1: String? = null,
    middleName2: String? = null,
    birthDate: LocalDate = LocalDate.of(1965, 7, 19),
    genderCode: String = "F",
    ethnicity: String? = null,
    dsl: OffenderDsl.() -> Unit = {},
  ): OffenderId
}

@DslMarker
annotation class NomisDataDslMarker
