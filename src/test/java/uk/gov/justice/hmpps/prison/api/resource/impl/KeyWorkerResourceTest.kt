package uk.gov.justice.hmpps.prison.api.resource.impl

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.MediaType

class KeyWorkerResourceTest : ResourceTest() {

  @SpyBean
  protected lateinit var telemetryClient: TelemetryClient

  @Nested
  @DisplayName("GET /api/key-worker/{agencyId}/available")
  inner class AvailabileKeyWorkers {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/key-worker/LEI/available")
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 404 as endpoint does not have override role`() {
      webTestClient.get().uri("/api/key-worker/LEI/available")
        .headers(setClientAuthorisation(listOf("")))
        .exchange()
        .expectStatus().isNotFound
      verify(telemetryClient).trackEvent(eq("ClientUnauthorisedAgencyAccess"), any(), isNull())
    }

    @Test
    fun `returns 404 if user has no caseloads`() {
      webTestClient.get().uri("/api/key-worker/LEI/available")
        // RO_USER has no caseloads
        .headers(setAuthorisation("RO_USER", listOf(""))).exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Resource with id [LEI] not found.")
    }

    @Test
    fun `returns 404 if not in user caseload`() {
      webTestClient.get().uri("/api/key-worker/LEI/available")
        // RO_USER has no caseloads
        .headers(setAuthorisation("WAI_USER", listOf(""))).exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Resource with id [LEI] not found.")
    }

    @Test
    fun `returns 404 if agency not found`() {
      webTestClient.get().uri("/api/key-worker/AAI/available")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Resource with id [AAI] not found.")
    }

    @Test
    fun `returns success if  in user caseload`() {
      webTestClient.get().uri("/api/key-worker/LEI/available")
        .headers(setAuthorisation("ITAG_USER", listOf(""))).exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("[*].staffId").value<List<Int>> { assertThat(it).containsExactlyInAnyOrder(-1, -4, -11, -12) }
    }
  }

  @Nested
  @DisplayName("GET /api/key-worker/{agencyId}/allocationHistory")
  inner class AllocationHistory {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/key-worker/LEI/allocationHistory")
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 404 as endpoint does not have override role`() {
      webTestClient.get().uri("/api/key-worker/LEI/allocationHistory")
        .headers(setClientAuthorisation(listOf("")))
        .exchange()
        .expectStatus().isNotFound
      verify(telemetryClient).trackEvent(eq("ClientUnauthorisedAgencyAccess"), any(), isNull())
    }

    @Test
    fun `returns 404 if user has no caseloads`() {
      webTestClient.get().uri("/api/key-worker/LEI/allocationHistory")
        // RO_USER has no caseloads
        .headers(setAuthorisation("RO_USER", listOf(""))).exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Resource with id [LEI] not found.")
    }

    @Test
    fun `returns 404 if not in user caseload`() {
      webTestClient.get().uri("/api/key-worker/LEI/allocationHistory")
        // RO_USER has no caseloads
        .headers(setAuthorisation("WAI_USER", listOf(""))).exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Resource with id [LEI] not found.")
    }

    @Test
    fun `returns 404 if agency not found`() {
      webTestClient.get().uri("/api/key-worker/AAI/allocationHistory")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Resource with id [AAI] not found.")
    }

    @Test
    fun `returns success if  in user caseload`() {
      webTestClient.get().uri("/api/key-worker/LEI/allocationHistory")
        .headers(setAuthorisation("ITAG_USER", listOf(""))).exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("[*].offenderNo").value<List<Int>> { assertThat(it).hasSize(10) }
    }
  }
}
