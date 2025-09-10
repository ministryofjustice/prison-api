package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.hmpps.prison.api.model.CaseNoteTypeSummaryRequest

class CaseNoteResourceIntTest : ResourceTest() {
  @Nested
  @DisplayName("GET /case-notes/usage")
  inner class UsageGet {

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("api/case-notes/usage")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 when does not have override role`() {
      webTestClient.get().uri("api/case-notes/usage")
        .headers(setClientAuthorisation(emptyList()))
        .exchange()
        .expectStatus().isForbidden
    }
  }

  @Nested
  @DisplayName("POST /case-notes/usage-by-types")
  inner class UsageByTypes {

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.post().uri("api/case-notes/usage-by-types")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 when does not have override role`() {
      webTestClient.post().uri("api/case-notes/usage-by-types")
        .headers(setClientAuthorisation(emptyList()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .body(BodyInserters.fromValue(CaseNoteTypeSummaryRequest.builder().build()))
        .exchange()
        .expectStatus().isForbidden
    }
  }
}
