package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class CourtDateResourceImplIntTest : ResourceTest() {
  @Test
  fun courtDateResults() {
    val requestEntity =
      createHttpEntityWithBearerAuthorisation("ITAG_USER", mutableListOf("ROLE_VIEW_PRISON_DATA"), null)

    val responseEntity: ResponseEntity<String> = testRestTemplate
      .exchange(
        "/api/court-date-results/Z0020XY",
        HttpMethod.GET,
        requestEntity,
        String::class.java,
      )

    assertThatJsonFileAndStatus(responseEntity, HttpStatus.OK.value(), "court-date-results.json")
  }

  @Test
  fun courtDateResultsByCharge() {
    val requestEntity =
      createHttpEntityWithBearerAuthorisation("ITAG_USER", mutableListOf("ROLE_VIEW_PRISON_DATA"), null)

    val responseEntity: ResponseEntity<String> = testRestTemplate
      .exchange(
        "/api/court-date-results/by-charge/Z0020XY",
        HttpMethod.GET,
        requestEntity,
        String::class.java,
      )

    assertThatJsonFileAndStatus(responseEntity, HttpStatus.OK.value(), "court-date-results-by-charge.json")
  }
}
