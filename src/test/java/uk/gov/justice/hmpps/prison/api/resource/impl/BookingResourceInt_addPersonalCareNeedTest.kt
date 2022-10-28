package uk.gov.justice.hmpps.prison.api.resource.impl

import org.hamcrest.core.StringContains.containsString
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.web.reactive.function.BodyInserters

@WithMockUser
class BookingResourceInt_addPersonalCareNeedTest : ResourceTest() {

  @Nested
  @DisplayName("POST /personal-care-needs")
  inner class CreateOffenderHealthProblem {

    @Test
    fun `requires correct role`() {
      webTestClient.post()
        .uri("/api/bookings/-1/personal-care-needs")
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .headers(setAuthorisation(listOf()))
        .accept(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
                {                  
                  "problemType":"BSCAN",
                  "problemCode": "BSC6.0",
                  "commentText": "Offender had an a x-ray",
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
        .uri("/api/personal-care-needs")
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `will create the offender health problem`() {
      webTestClient.post()
        .uri("/api/bookings/-1/personal-care-needs")
        .headers(setAuthorisation(listOf("ROLE_MAINTAIN_HEALTH_PROBLEMS")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
                {                  
                  "problemCode": "D",
                  "commentText": "Offender had an a x-ray",
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

      webTestClient.get()
        .uri("/api/bookings/-1/personal-care-needs?type=DISAB")
        .headers(setAuthorisation(listOf("ROLE_MAINTAIN_HEALTH_PROBLEMS")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("personalCareNeeds[1].problemType").isEqualTo("DISAB")
        .jsonPath("personalCareNeeds[1].problemCode").isEqualTo("D")
        .jsonPath("personalCareNeeds[1].commentText").isEqualTo("Offender had an a x-ray")
        .jsonPath("personalCareNeeds[1].startDate").isEqualTo("2022-06-20")
        .jsonPath("personalCareNeeds[1].endDate").doesNotExist()
        .jsonPath("personalCareNeeds[1].problemStatus").isEqualTo("ON")
    }

    @Test
    fun `will check for missing inputs values`() {

      webTestClient.post()
        .uri("/api/bookings/-1/personal-care-needs")
        .headers(setAuthorisation(listOf("ROLE_MAINTAIN_HEALTH_PROBLEMS")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
                {}
            """.trimIndent()
          )
        )
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .jsonPath("userMessage").value(containsString("Field: startDate - must not be null"))
        .jsonPath("userMessage").value(containsString("Field: commentText - must not be null"))
        .jsonPath("userMessage").value(containsString("Field: problemStatus - must not be null"))
        .jsonPath("userMessage").value(containsString("Field: problemCode - must not be null"))
    }
  }
}
