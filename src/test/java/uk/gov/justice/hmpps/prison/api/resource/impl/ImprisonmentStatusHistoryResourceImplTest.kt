package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType

class ImprisonmentStatusHistoryResourceImplTest : ResourceTest() {
  @Test
  fun courtDateResults() {
    webTestClient.get()
      .uri("/api/imprisonment-status-history/{offenderNo}", "A1180HL")
      .headers(setAuthorisation(listOf("ROLE_VIEW_PRISON_DATA")))
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectBody()
      .jsonPath("$.length()").isEqualTo(3)
      .jsonPath("[0].status").isEqualTo("FTR_ORA")
      .jsonPath("[0].effectiveDate").isEqualTo("2016-03-29")
      .jsonPath("[0].agencyId").isEqualTo("MDI")
      .jsonPath("[1].status").isEqualTo("TRL")
      .jsonPath("[1].effectiveDate").isEqualTo("2016-03-30")
      .jsonPath("[1].agencyId").isEqualTo("MDI")
      .jsonPath("[2].status").isEqualTo("DPP")
      .jsonPath("[2].effectiveDate").isEqualTo("2016-03-31")
      .jsonPath("[2].agencyId").isEqualTo("MDI")
  }
}
