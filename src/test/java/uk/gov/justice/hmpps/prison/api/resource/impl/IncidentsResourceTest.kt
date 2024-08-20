package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class IncidentsResourceTest : ResourceTest() {

  @DisplayName("GET /api/incidents/{incidentId}")
  @Nested
  inner class RetrieveIncidentsTest {
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

  @DisplayName("GET /api/incidents/configuration")
  @Nested
  inner class RetrieveIncidentTypeConfigurationTest {

    @Nested
    inner class Security {
      @Test
      fun `should return 401 when user does not even have token`() {
        webTestClient.get().uri("/api/incidents/configuration")
          .exchange()
          .expectStatus().isUnauthorized
      }
    }

    @Nested
    inner class Validation {
      @Test
      fun `returns success when client has a token`() {
        webTestClient.get().uri("/api/incidents/configuration")
          .headers(setClientAuthorisation(listOf()))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("length()").isEqualTo(24)
      }

      @Test
      fun `returns 404 when incorrect incident type selected`() {
        webTestClient.get().uri("/api/incidents/configuration?incident-type=XXXX")
          .headers(setClientAuthorisation(listOf()))
          .exchange()
          .expectStatus().is4xxClientError
      }
    }

    @Nested
    inner class HappyPath {
      @Test
      fun `returns success when individual incident type selected`() {
        webTestClient.get().uri("/api/incidents/configuration?incident-type=ASSAULT")
          .headers(setClientAuthorisation(listOf()))
          .exchange()
          .expectStatus().isOk
          .expectBody().json(
            """
            [
              {
                "incidentType": "ASSAULT",
                "incidentTypeDescription": "ASSAULTS",
                "prisonerRoles": [
                  {
                    "prisonerRole": "FIGHT",
                    "singleRole": false,
                    "active": true
                  },
                  {
                    "prisonerRole": "VICT",
                    "singleRole": false,
                    "active": true
                  }
                ],
                "active": true
              }
            ]
              """,
            false,
          )
      }
    }
  }
}
