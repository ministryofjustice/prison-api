package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import uk.gov.justice.hmpps.prison.api.model.AgencyPrisonerPayProfile
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse
import java.math.BigDecimal
import java.time.LocalDate

class AgencyPrisonerPayProfileIntTest : ResourceTest() {
  @Test
  fun agencyPayProfile_returnsSuccessAndData() {
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf("ROLE_VIEW_PRISON_DATA"), emptyMap())
    val responseEntity = testRestTemplate.exchange(
      "/api/agencies/LEI/pay-profile",
      HttpMethod.GET,
      requestEntity,
      object : ParameterizedTypeReference<AgencyPrisonerPayProfile?>() {},
    )
    assertThatStatus(responseEntity, 200)
    assertThat(responseEntity.body).isInstanceOf(AgencyPrisonerPayProfile::class.java)
    val payProfile = responseEntity.body
    assertThat(payProfile).isNotNull

    with(payProfile!!) {
      assertThat(agencyId).isEqualTo("LEI")
      assertThat(startDate).isEqualTo(LocalDate.of(2020, 10, 1))
      assertThat(endDate).isNull()
      assertThat(autoPayFlag).isTrue
      assertThat(minHalfDayRate).isEqualTo(BigDecimal("1.25"))
      assertThat(maxHalfDayRate).isEqualTo(BigDecimal("5.25"))
      assertThat(maxBonusRate).isEqualTo(BigDecimal("4.00"))
      assertThat(maxPieceWorkRate).isEqualTo(BigDecimal("8.00"))
      assertThat(payFrequency).isEqualTo(1)
      assertThat(backdateDays).isEqualTo(7)
      assertThat(defaultPayBandCode).isEqualTo("1")
      assertThat(weeklyAbsenceLimit).isEqualTo(21)
    }
  }

  @Test
  fun agencyPrisonerPayProfile_returnsAccessDenied() {
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf("ROLE_UNRECOGNISED"), emptyMap())
    val responseEntity = testRestTemplate.exchange(
      "/api/agencies/LEI/pay-profile",
      HttpMethod.GET,
      requestEntity,
      ErrorResponse::class.java,
    )
    assertThatStatus(responseEntity, 403)
    assertThat(responseEntity.body).isInstanceOf(ErrorResponse::class.java)
  }

  @Test
  fun agencyPrisonerPayProfile_returnsNotFound() {
    val requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", listOf("ROLE_VIEW_PRISON_DATA"), emptyMap())
    val responseEntity = testRestTemplate.exchange(
      "/api/agencies/XXX/pay-profile",
      HttpMethod.GET,
      requestEntity,
      ErrorResponse::class.java,
    )
    assertThatStatus(responseEntity, 404)
    assertThat(responseEntity.body).isNotNull
    assertThat(responseEntity.body).isInstanceOf(ErrorResponse::class.java)
    assertThat(responseEntity.body?.userMessage).isEqualTo("Resource with id [XXX] not found.")
  }
}
