package uk.gov.justice.hmpps.prison.util.builders.dsl

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.prison.util.builders.randomName
import java.time.LocalDate

@Component
class NomisDataBuilder(private val offenderBuilderFactory: OffenderBuilderFactory) {
  fun build(dsl: NomisData.() -> Unit) {
    SecurityContextHolder.setContext(
      SecurityContextHolder.createEmptyContext().apply {
        val principal = User("ITAG_USER", "", true, true, true, true, emptyList())
        this.authentication = UsernamePasswordAuthenticationToken.authenticated(
          principal,
          principal.password,
          principal.authorities,
        )
      },
    )
    NomisData(offenderBuilderFactory).apply(dsl)
  }

  fun deletePrisoner(offenderNo: String) {
    offenderBuilderFactory.deletePrisoner(offenderNo)
  }
}

class NomisData(
  private val offenderBuilderFactory: OffenderBuilderFactory,
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
    offenderBuilderFactory.builder()
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
