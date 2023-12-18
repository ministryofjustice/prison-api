@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify

@DisplayName("/api/offenders/{offenderNo}/case-notes/{caseNoteId}")
class OffenderResourceIntTest_getCaseNotes : ResourceTest() {

  @Nested
  inner class Authorisation {
    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.get().uri("/api/offenders/Z00034/case-notes/-11")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Nested
    inner class ClientAccess {
      @Test
      fun `returns 403 when client does not have any roles`() {
        webTestClient.get().uri("/api/offenders/A1234AC/case-notes/-11")
          .headers(setClientAuthorisation(listOf()))
          .exchange()
          .expectStatus().isForbidden
          .expectBody().jsonPath("userMessage").isEqualTo("Client not authorised to access booking with id -3.")
      }

      @Test
      fun `returns 403 as ROLE_BANANAS is not override role`() {
        webTestClient.get().uri("/api/offenders/A1234AC/case-notes/-11")
          .headers(setClientAuthorisation(listOf("ROLE_BANANAS")))
          .exchange()
          .expectStatus().isForbidden
          .expectBody().jsonPath("userMessage").isEqualTo("Client not authorised to access booking with id -3.")
      }

      @Test
      fun `returns 200 if has override ROLE_GLOBAL_SEARCH`() {
        webTestClient.get().uri("/api/offenders/A1234AC/case-notes/-11")
          .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
          .exchange()
          .expectStatus().isOk
      }

      @Test
      fun `returns 200 if has override ROLE_VIEW_CASE_NOTES`() {
        webTestClient.get().uri("/api/offenders/A1234AC/case-notes/-11")
          .headers(setClientAuthorisation(listOf("ROLE_VIEW_CASE_NOTES")))
          .exchange()
          .expectStatus().isOk
      }

      @Test
      fun `returns 200 if has override ROLE_SYSTEM_USER`() {
        webTestClient.get().uri("/api/offenders/A1234AC/case-notes/-11")
          .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
          .exchange()
          .expectStatus().isOk
      }

      @Test
      fun `returns 404 if case note does not exist`() {
        webTestClient.get().uri("/api/offenders/A1234AC/case-notes/-999")
          .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Resource with id [A1234AC] not found.")
      }

      @Test
      fun `invalid client access produces telemetry event`() {
        webTestClient.get().uri("/api/offenders/A1234AC/case-notes/-11")
          .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isForbidden

        verify(telemetryClient).trackEvent(eq("ClientUnauthorisedBookingAccess"), any(), isNull())
      }
    }

    @Nested
    inner class UserAccess {
      @Test
      fun `returns 404 when user does not have any roles`() {
        webTestClient.get().uri("/api/offenders/Z00034/case-notes/-89")
          .headers(setAuthorisation("RO_USER", listOf()))
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Resource with id [Z00034] not found.")
      }

      @Test
      fun `returns 404 as ROLE_BANANAS is not override role`() {
        webTestClient.get().uri("/api/offenders/Z00034/case-notes/-89")
          .headers(setAuthorisation("RO_USER", listOf("ROLE_BANANAS")))
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Resource with id [Z00034] not found.")
      }

      @Test
      fun `returns 200 if has override ROLE_GLOBAL_SEARCH`() {
        webTestClient.get().uri("/api/offenders/A1234AC/case-notes/-11")
          .headers(setAuthorisation("RO_USER", listOf("ROLE_GLOBAL_SEARCH")))
          .exchange()
          .expectStatus().isOk
      }

      @Test
      fun `returns 200 if has override ROLE_VIEW_CASE_NOTES`() {
        webTestClient.get().uri("/api/offenders/A1234AC/case-notes/-11")
          .headers(setAuthorisation("RO_USER", listOf("ROLE_VIEW_CASE_NOTES")))
          .exchange()
          .expectStatus().isOk
      }

      @Test
      fun `returns 404 if case note does not exist`() {
        webTestClient.get().uri("/api/offenders/A1234AC/case-notes/-999")
          .headers(setAuthorisation("ITAG_USER", listOf("")))
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Resource with id [A1234AC] not found.")
      }

      @Test
      fun `returns 200 if has correct caseload`() {
        webTestClient.get().uri("/api/offenders/A1234AC/case-notes/-11")
          .headers(setAuthorisation("ITAG_USER", listOf("")))
          .exchange()
          .expectStatus().isOk
      }

      @Test
      fun `invalid user access produces telemetry event`() {
        webTestClient.get().uri("/api/offenders/A1234AC/case-notes/-11")
          .headers(setAuthorisation("WAI_USER", listOf("")))
          .exchange()
          .expectStatus().isNotFound
          .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -3 not found.")

        verify(telemetryClient).trackEvent(eq("UserUnauthorisedBookingAccess"), any(), isNull())
      }
    }
  }
}
