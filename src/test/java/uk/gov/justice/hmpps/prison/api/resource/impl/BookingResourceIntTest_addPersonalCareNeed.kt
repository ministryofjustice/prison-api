@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.hamcrest.core.StringContains.containsString
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderHealthProblemRepository
import java.time.LocalDate.parse

@WithMockUser
class BookingResourceIntTest_addPersonalCareNeed : ResourceTest() {

  @Autowired
  lateinit var offenderHealthProblemRepository: OffenderHealthProblemRepository

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
            """.trimIndent(),
          ),
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
    fun `will create the offender health problems due to role ROLE_MAINTAIN_HEALTH_PROBLEMS on client`() {
      webTestClient.post()
        .uri("/api/bookings/-2/personal-care-needs")
        .headers(setClientAuthorisation(listOf("ROLE_MAINTAIN_HEALTH_PROBLEMS")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .accept(MediaType.APPLICATION_JSON)
        .body(
          BodyInserters.fromValue(
            """
              {                  
                "problemCode": "ACCU18",
                "commentText": "Offender had an a x-ray",
                "startDate": "2023-06-20",
                "endDate": null,
                "problemStatus": "ON"
               }
            """.trimIndent(),
          ),
        )
        .exchange()
        .expectStatus().isCreated

      // tidy up
      val offenderHealthProblems = offenderHealthProblemRepository.findAllByOffenderBookingOffenderNomsIdInAndOffenderBookingBookingSequenceAndProblemTypeCodeAndStartDateBetween(
        listOf("A1234AB"),
        1,
        "MATSTAT",
        parse("2023-06-20"),
        parse("2023-06-20"),
      )
      offenderHealthProblemRepository.delete(offenderHealthProblems[0])
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
            """.trimIndent(),
          ),
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
            """.trimIndent(),
          ),
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
