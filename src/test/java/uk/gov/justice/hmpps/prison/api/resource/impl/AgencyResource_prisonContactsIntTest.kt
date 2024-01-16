@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser

@WithMockUser
class AgencyResource_prisonContactsIntTest : ResourceTest() {

  @Nested
  @DisplayName("GET /agencies/prison")
  inner class GetAllPrisonContacts {

    @Test
    fun `requires authorization`() {
      webTestClient.get()
        .uri("/api/agencies/prison")
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `will get the prison contacts`() {
      webTestClient.get()
        .uri("/api/agencies/prison")
        .headers(setAuthorisation(listOf("ROLE_MAINTAIN_HEALTH_PROBLEMS")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("\$.length()").value<Int> { assertThat(it).isGreaterThan(2) }
        .jsonPath("\$[*].agencyId").value<List<String>> { assertThat(it).contains("BMI", "BXI", "TRO") }
        .jsonPath("\$[*].agencyId").value<List<String>> { assertThat(it).contains("BMI", "BXI", "TRO") }
        .jsonPath("\$[?(@.agencyId == 'BMI')].formattedDescription").isEqualTo("Birmingham")
        .jsonPath("\$[?(@.agencyId == 'BMI')].phones[*].number").value<List<String>> {
          assertThat(it).containsExactly("0114 2345345")
        }
        .jsonPath("\$[?(@.agencyId == 'BMI')].addresses[*].postalCode").value<List<String>> {
          assertThat(it).containsExactlyInAnyOrder("BM1 23V", "BM1 23V")
        }
        .jsonPath("\$[?(@.agencyId == 'BMI')].addresses[*].street").value<List<String>> {
          assertThat(it).containsExactlyInAnyOrder("Not Primary Address", "BMI Place")
        }
        .jsonPath("\$[?(@.agencyId == 'BMI')].addresses[*].premise").value<List<String>> {
          assertThat(it).containsExactlyInAnyOrder("Birmingham HMP", "Birmingham HMP")
        }
    }
  }
}
