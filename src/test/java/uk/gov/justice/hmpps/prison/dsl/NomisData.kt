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
  private val offenderBuilder: OffenderBuilder,
  private val teamBuilder: TeamBuilder,
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
    NomisData(offenderBuilder, teamBuilder).apply(dsl)
  }

  fun deletePrisoner(offenderNo: String) {
    offenderBuilder.deletePrisoner(offenderNo)
  }
}

class NomisData(
  private val offenderBuilder: OffenderBuilder,
  private val teamBuilder: TeamBuilder,
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
  ): OffenderId = offenderBuilder.build(
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
      offenderBuilder.apply(dsl)
    }

  @TeamDslMarker
  override fun team(
    code: String,
    description: String,
    areaCode: String,
    categoryCode: String,
    agencyId: String,
    dsl: TeamDsl.() -> Unit,
  ): Team = teamBuilder.build(
    code = code,
    description = description,
    areaCode = areaCode,
    categoryCode = categoryCode,
    agencyId = agencyId,
  )
    .also {
      teamBuilder.apply(dsl)
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

  @TeamDslMarker
  fun team(
    code: String = UUID.randomUUID().toString().takeLast(20),
    description: String = UUID.randomUUID().toString().takeLast(40),
    areaCode: String = "LON",
    categoryCode: String = "MANAGE",
    agencyId: String = "MDI",
    dsl: TeamDsl.() -> Unit = {},
  ): Team
}

@DslMarker
annotation class NomisDataDslMarker
