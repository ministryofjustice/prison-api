@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.reactive.server.returnResult

@WithMockUser
class AgencyResource_prisonsIntTest : ResourceTest() {

  @Nested
  @DisplayName("GET /agencies/prisons")
  inner class GetAllPrisonContacts {

    @Test
    fun `requires authorization`() {
      webTestClient.get()
        .uri("/api/agencies/prisons")
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `will get the prisons`() {
      webTestClient.get()
        .uri("/api/agencies/prisons")
        .headers(setAuthorisation(listOf("ROLE_MAINTAIN_HEALTH_PROBLEMS")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("\$.length()").value<Int> { assertThat(it).isGreaterThan(2) }
        .jsonPath("\$[*].agencyId").value<List<String>> { assertThat(it).contains("BMI", "BXI", "TRO") }
        .jsonPath("\$[?(@.agencyId == 'BMI')].description").isEqualTo("Birmingham")
    }

    @Test
    fun `will output same as the INST endpoint`() {
      val prisonsResponse = webTestClient.get()
        .uri("/api/agencies/prisons")
        .headers(setAuthorisation(listOf()))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .returnResult<String>().responseBody.blockFirst()!!

      val agenciesResponse = webTestClient.get()
        .uri("/api/agencies/type/INST")
        .headers(setAuthorisation(listOf()))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .returnResult<String>().responseBody.blockFirst()!!

      assertThat(prisonsResponse).isEqualTo(agenciesResponse)
    }
  }
}
