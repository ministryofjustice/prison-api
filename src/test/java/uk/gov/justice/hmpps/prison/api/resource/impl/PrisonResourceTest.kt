package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions
import org.assertj.core.api.AssertionsForClassTypes
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.test.context.support.WithMockUser
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken

@WithMockUser
class PrisonResourceTest : ResourceTest() {

  @Test
  fun testPrisonBookingSummaryReturnsResponseWithValidUser() {
    val token = authTokenHelper.getToken(AuthToken.RELEASE_DATE_MANUAL_COMPARER)

    val response = testRestTemplate.exchange(
      "/api/prison/LEI/booking/latest/sentence-summary",
      HttpMethod.GET,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<String?>() {},
    )
    AssertionsForClassTypes.assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    Assertions.assertThat(response.body).contains("courtSentences")
  }

  @Test
  fun testPrisonBookingSummaryReturnsUnauthorisedWithUnauthorisedUser() {
    val token = authTokenHelper.getToken(AuthToken.NORMAL_USER)
    val response = testRestTemplate.exchange(
      "/api/prison/LEI/booking/latest/sentence-summary",
      HttpMethod.GET,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<String?>() {},
    )
    AssertionsForClassTypes.assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
  }
}
