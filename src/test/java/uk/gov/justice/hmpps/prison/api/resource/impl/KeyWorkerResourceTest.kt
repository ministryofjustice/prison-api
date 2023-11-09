package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters

class KeyWorkerResourceTest : ResourceTest() {

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
        .headers(setAuthorisation("RO_USER", listOf(""))).exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Resource with id [LEI] not found.")
    }

    @Test
    fun `returns 404 if not in user caseload`() {
      webTestClient.get().uri("/api/key-worker/LEI/available")
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

  @Nested
  @DisplayName("POST /api/key-worker/{agencyId}/current-allocations")
  inner class CurrentAllocations {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.post().uri("/api/key-worker/LEI/current-allocations")
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `Request for key worker allocations for multiple staff Ids`() {
      webTestClient.post().uri("/api/key-worker/LEI/current-allocations")
        .headers(setAuthorisation("ITAG_USER", listOf("KEY_WORKER")))
        .body(BodyInserters.fromValue(listOf(-5, -4)))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("[*].offenderNo").value<List<Int>> { assertThat(it).hasSize(5) }
    }
  }

  @Nested
  @DisplayName("POST /api/key-worker/offenders/allocationHistory")
  inner class OffendersAllocationHistory {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.post().uri("/api/key-worker/offenders/allocationHistory")
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `Request for key worker allocation history for multiple offender Nos`() {
      webTestClient.post().uri("/api/key-worker/offenders/allocationHistory")
        .headers(setAuthorisation("ITAG_USER", listOf("KEY_WORKER")))
        .body(BodyInserters.fromValue(listOf("A9876RS","A5576RS","A1176RS","A1234AP")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("[*].offenderNo").value<List<Int>> { assertThat(it).hasSize(5) }
    }
    /*
    Scenario: Request for key worker allocations for multiple staff Ids
    When a key worker allocations request is made with staff ids "-5,-4" and agency "LEI"
     private void callPostApiForAllocations(final String url, final List<?> lists, final String agencyId) {
        try {
            final var response =
                    restTemplate.exchange(
                            "/key-worker/{agencyId}/current-allocations";,
                            HttpMethod.POST,
                            createEntity(lists, null),
                            new ParameterizedTypeReference<List<KeyWorkerAllocationDetail>>() {
                            }, agencyId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            allocationsList = response.getBody();

        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }
    Then the key worker has 5 allocations
    assertThat(allocationsList).hasSize(expectedAllocationCount);

  Scenario: Request for key worker allocation history for multiple offender Nos
    When a key worker allocation history request is made with nomis ids "A9876RS,A5576RS,A1176RS,A1234AP"
     private void callPostApiForAllocationHistory(final String url, final List<?> lists) {
        try {
            final var response =
                    restTemplate.exchange(
                            "key-worker/offenders/allocationHistory";,
                            HttpMethod.POST,
                            createEntity(lists, null),
                            new ParameterizedTypeReference<List<OffenderKeyWorker>>() {
                            });

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            allocationHistoryList = response.getBody();

        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }
    Then the key worker has 5 allocation history entries
            assertThat(allocationHistoryList).hasSize(expectedAllocationCount);
     */
  }
}
