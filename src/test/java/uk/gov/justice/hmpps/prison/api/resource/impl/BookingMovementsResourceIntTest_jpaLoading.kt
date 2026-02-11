@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookingMovementsResourceIntTest_jpaLoading : ResourceTest() {
  private lateinit var token: String

  @BeforeEach
  fun setup() {
    token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER)
  }

  @Test
  fun retrieveOffenderTransactionHistory_LazyLoads() {
    val request = createHttpEntity(token, null)

    val response: ResponseEntity<String> = testRestTemplate.exchange(
      "/api/offenders/{offenderNo}/damage-obligations",
      HttpMethod.GET,
      request,
      object : ParameterizedTypeReference<String>() {
      },
      "A1234AA",
    )

    assertThatStatus(response, 200)
  }
}
