package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("GET /api/incidents/{incidentId}")
class PersonResourceTest : ResourceTest() {

  @Nested
  @DisplayName("GET /api/persons/{personId}/identifiers")
  inner class Identifiers {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/persons/-1/identifiers")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 when client has no authorised role`() {
      webTestClient.get().uri("/api/persons/-1/identifiers")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `Retrieve identifiers for a personId that does not exist`() {
      webTestClient.get().uri("/api/persons/1000/identifiers")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PROFILES")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("[]")
    }

    @Test
    fun `Retrieve the identifiers for a personId having multiple identifiers`() {
      webTestClient.get().uri("/api/persons/-1/identifiers")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PROFILES")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("""[{
          "identifierType": "EXTERNAL_REL",
          "identifierValue": "DELIUS_1_2"
        }, {
          "identifierType": "DL",
          "identifierValue": "NCDON805157PJ9FR"
        },
        {
          "identifierType": "PASS",
          "identifierValue": "PB1575411"
        },
        {
          "identifierType": "MERGED",
          "identifierValue": "A1408CM"
        },
        {
          "identifierType": "CRO",
          "identifierValue": "135196/95W"
        }]""")
    }
  }

  @Nested
  @DisplayName("GET /api/persons/{personId}/addresses")
  inner class Addresses {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/persons/-1/addresses")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 when client has no authorised role`() {
      webTestClient.get().uri("/api/persons/-1/addresses")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns success when client has authorised role`() {
      webTestClient.get().uri("/api/persons/-1/addresses")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_CONTACTS")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("[]")
    }
  }

  @Nested
  @DisplayName("GET /api/persons/{personId}/phones")
  inner class Phones {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/persons/-1/phones")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 when client has no authorised role`() {
      webTestClient.get().uri("/api/persons/-1/phones")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns success when client has authorised role`() {
      webTestClient.get().uri("/api/persons/-1/phones")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_CONTACTS")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("[]")
    }
  }

  @Nested
  @DisplayName("GET /api/persons/{personId}/email")
  inner class Email {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/persons/-1/emails")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 when client has no authorised role`() {
      webTestClient.get().uri("/api/persons/-1/emails")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns success when client has authorised role`() {
      webTestClient.get().uri("/api/persons/-1/emails")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_CONTACTS")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("[]")
    }
  }
}
