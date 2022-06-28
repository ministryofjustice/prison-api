package uk.gov.justice.hmpps.prison.api.resource

import org.junit.jupiter.api.Test
import uk.gov.justice.hmpps.prison.api.resource.impl.ResourceTest

class DatabaseRestoreInfoResourceIntTest : ResourceTest() {
  @Test
  fun testDatabaseRestoreInfo() {
    webTestClient.get()
      .uri("/api/restore-info")
      .exchange()
      .expectStatus().isNotFound
  }
}
