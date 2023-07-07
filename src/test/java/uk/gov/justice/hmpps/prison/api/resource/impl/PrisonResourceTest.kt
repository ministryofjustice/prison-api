package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import uk.gov.justice.hmpps.prison.api.model.SentenceSummary

class PrisonResourceTest : ResourceTest() {

  @Test
  fun testPrisonBookingSummaryReturnsListResponseReturnsSentenceSummaryList() {
    val establishment = "LEI"

    webTestClient.get()
      .uri("/api/prison/{establishment}/booking/latest/sentence-summary", establishment)
      .headers(
        setAuthorisation(
          listOf("ROLE_RELEASE_DATE_MANUAL_COMPARER"),
        ),
      )
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBodyList(object : ParameterizedTypeReference<SentenceSummary>() {})
  }

  @Test
  fun testPrisonBookingSummaryReturnsUnauthorisedWithUnauthorisedUser() {
    val establishment = "LEI"

    webTestClient.get()
      .uri("/api/prison/{establishment}/booking/latest/sentence-summary", establishment)
      .headers(
        setAuthorisation(
          listOf("ROLE_RELEASE_DATES_CALCULATOR"),
        ),
      )
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isForbidden
  }
}
