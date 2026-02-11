package uk.gov.justice.hmpps.prison.api.resource

import org.junit.jupiter.api.Test
import uk.gov.justice.hmpps.prison.api.resource.impl.ResourceTest

class HealthIntTest : ResourceTest() {
  @Test
  fun healthReportsOk() {
    getAndVerifyStatusUp("/health")
  }

  @Test
  fun dbReportsOk() {
    getAndVerifyStatusUp("/health/db")
  }

  @Test
  fun pingReportsOk() {
    getAndVerifyStatusUp("/health/ping")
  }

  @Test
  fun livenessReportsOk() {
    getAndVerifyStatusUp("/health/liveness")
  }

  @Test
  fun readinessReportsOk() {
    getAndVerifyStatusUp("/health/readiness")
  }

  private fun getAndVerifyStatusUp(url: String) {
    webTestClient.get().uri(url).exchange().expectStatus().isOk
      .expectBody().jsonPath("status").isEqualTo("UP")
  }
}
