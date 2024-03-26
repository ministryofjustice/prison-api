package uk.gov.justice.hmpps.prison.dsl

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.prison.repository.jpa.model.Team
import uk.gov.justice.hmpps.prison.util.builders.randomName
import java.time.LocalDate
import java.util.UUID

@Component
class NomisDataBuilder(
  private val offenderBuilderFactory: OffenderBuilderFactory,
  private val teamBuilderFactory: TeamBuilderFactory,
) {
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
    NomisData(offenderBuilderFactory, teamBuilderFactory).apply(dsl)
  }

  fun deletePrisoner(offenderNo: String) {
    offenderBuilderFactory.deletePrisoner(offenderNo)
  }
}

@NomisDataDslMarker
class NomisData(
  private val offenderBuilderFactory: OffenderBuilderFactory,
  private val teamBuilderFactory: TeamBuilderFactory,
) {
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
    dsl: OffenderBuilder.() -> Unit = {},
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

  fun team(
    code: String = UUID.randomUUID().toString().takeLast(20),
    description: String = UUID.randomUUID().toString().takeLast(40),
    areaCode: String = "LON",
    categoryCode: String = "MANAGE",
    agencyId: String = "MDI",
    dsl: TeamBuilder.() -> Unit = {},
  ): Team =
    teamBuilderFactory.builder()
      .let { builder ->
        builder.build(
          code = code,
          description = description,
          areaCode = areaCode,
          categoryCode = categoryCode,
          agencyId = agencyId,
        )
          .also {
            builder.apply(dsl)
          }
      }
}

@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class NomisDataDslMarker
