package uk.gov.justice.hmpps.prison.api.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.hmpps.prison.api.resource.impl.ResourceTest

class PersonalOfficerResourceTest : ResourceTest() {

  @Test
  fun `should return 401 without a token`() {
    webTestClient.get().uri(PERSONAL_OFFICER_ALLOCATION_HISTORY_URL, "LEI")
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `should return 403 without the correct role`() {
    webTestClient.get().uri(PERSONAL_OFFICER_ALLOCATION_HISTORY_URL, "LEI")
      .headers(setClientAuthorisation(listOf()))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `should return 200 with correct role`() {
    webTestClient.get().uri(PERSONAL_OFFICER_ALLOCATION_HISTORY_URL, "LEI")
      .headers(setAuthorisation("USER", listOf("ROLE_ALLOCATIONS__RO")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("[*].offenderNo").value<List<Int>> { assertThat(it).hasSize(23) }
  }

  companion object {
    private const val PERSONAL_OFFICER_ALLOCATION_HISTORY_URL = "/api/personal-officer/{agencyId}/allocation-history"
  }
}
