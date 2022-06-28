package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.web.reactive.function.BodyInserters

@WithMockUser
class BookingResourceInt_addPersonalCareNeedTest : ResourceTest() {

  @Nested
  @DisplayName("POST /offender-health-problems")
  inner class CreateOffenderHealthProblem {

    @Test
    fun `requires correct role`() {
      webTestClient.post()
        .uri("/api/bookings/-1/offender-health-problems")
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .headers(setAuthorisation(listOf()))
        .accept(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
                {                  
                  "problemType":"BSCAN",
                  "problemCode": "BSC6.0",
                  "problemDescription": "Offender had an a x-ray",
                  "startDate": "2022-06-20T09:00:00",
                  "endDate": null,
                  "problemStatus": "ON"
                 }
            """.trimIndent()
          )
        )
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `requires authorization`() {
      webTestClient.post()
        .uri("/api/offender-health-problems")
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `will create the offender health problem`() {
      webTestClient.post()
        .uri("/api/bookings/-1/offender-health-problems")
        .headers(setAuthorisation(listOf("ROLE_MAINTAIN_HEALTH_PROBLEMS")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
                {                  
                  "problemType": "DISAB",
                  "problemCode": "D",
                  "problemDescription": "Offender had an a x-ray",
                  "startDate": "2022-06-20",
                  "endDate": null,
                  "problemStatus": "ON"
                 }
            """.trimIndent()
          )
        )
        .exchange()
        .expectStatus().isCreated
        .expectBody()
        .jsonPath("problemType").isEqualTo("DISAB")
        .jsonPath("problemCode").isEqualTo("D")
        .jsonPath("problemDescription").isEqualTo("Offender had an a x-ray")
        .jsonPath("startDate").isEqualTo("2022-06-20")
        .jsonPath("endDate").isEqualTo(null)
        .jsonPath("problemStatus").isEqualTo("ON")
    }
  }
}
