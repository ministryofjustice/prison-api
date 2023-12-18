package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("GET /api/incidents/{incidentId}")
class IncidentsResourceTest : ResourceTest() {

  @Test
  fun `should return 401 when user does not even have token`() {
    webTestClient.get().uri("/api/incidents/-1")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `returns 403 when client has no authorised role`() {
    webTestClient.get().uri("/api/incidents/-4")
      .headers(setClientAuthorisation(listOf()))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `returns success when client has authorised role`() {
    webTestClient.get().uri("/api/incidents/-1")
      .headers(setClientAuthorisation(listOf("ROLE_VIEW_INCIDENTS")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("responses.length()").isEqualTo(19)
      .jsonPath("parties.length()").isEqualTo(6)
      .jsonPath("incidentCaseId").isEqualTo(-1)
      .jsonPath("incidentTitle").isEqualTo("Big Fight")
      .jsonPath("incidentType").isEqualTo("ASSAULT")
  }

  @Test
  fun `returns success when no parties`() {
    webTestClient.get().uri("/api/incidents/-4")
      .headers(setClientAuthorisation(listOf("ROLE_VIEW_INCIDENTS")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("incidentCaseId").isEqualTo(-4)
      .jsonPath("incidentTitle").isEqualTo("Medium sized fight")
      .jsonPath("parties").doesNotExist()
  }
}
