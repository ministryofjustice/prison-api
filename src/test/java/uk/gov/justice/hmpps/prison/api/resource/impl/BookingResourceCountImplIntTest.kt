package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.Sql.ExecutionPhase
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode

class BookingResourceCountImplIntTest : ResourceTest() {
  @Sql(scripts = ["/sql/addingHealthProblems_init.sql"], executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, config = SqlConfig(transactionMode = TransactionMode.ISOLATED))
  @Sql(scripts = ["/sql/addingHealthProblems_clean.sql"], executionPhase = ExecutionPhase.AFTER_TEST_METHOD, config = SqlConfig(transactionMode = TransactionMode.ISOLATED))
  @Test
  fun countPersonalCareNeedsForOffenders() {
    webTestClient.post()
      .uri("/api/bookings/offenderNo/personal-care-needs/count?type=DISAB&fromStartDate=2010-01-01&toStartDate=2011-01-01")
      .header("Content-Type", APPLICATION_JSON_VALUE)
      .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
      .accept(APPLICATION_JSON)
      .bodyValue("""["A1234AA","A1234AD"]""")
      .exchange()
      .expectStatus().isOk()
      .expectBody().json("""[{"offenderNo":"A1234AA","size":4},{"offenderNo":"A1234AD","size":1}]""")
  }

  @Test
  fun countPersonalCareNeedsForOffendersMissingProblemType() {
    webTestClient.post()
      .uri("/api/bookings/offenderNo/personal-care-needs/count?fromStartDate=2010-01-01&toStartDate=2011-01-01")
      .header("Content-Type", APPLICATION_JSON_VALUE)
      .headers(setAuthorisation(listOf()))
      .accept(APPLICATION_JSON)
      .bodyValue("""["A1234AA","A1234AD"]""")
      .exchange()
      .expectStatus().isBadRequest()
      .expectBody()
      .jsonPath("userMessage").isEqualTo("Required request parameter 'type' for method parameter type String is not present")
  }

  @Test
  fun countPersonalCareNeedsForOffendersMissingFromStartDate() {
    webTestClient.post()
      .uri("/api/bookings/offenderNo/personal-care-needs/count?type=DISAB&toStartDate=2011-01-01")
      .header("Content-Type", APPLICATION_JSON_VALUE)
      .headers(setAuthorisation(listOf()))
      .accept(APPLICATION_JSON)
      .bodyValue("""["A1234AA","A1234AD"]""")
      .exchange()
      .expectStatus().isBadRequest()
      .expectBody()
      .jsonPath("userMessage").isEqualTo("Required request parameter 'fromStartDate' for method parameter type LocalDate is not present")
  }

  @Test
  fun countPersonalCareNeedsForOffendersMissingToStartDate() {
    webTestClient.post()
      .uri("/api/bookings/offenderNo/personal-care-needs/count?type=DISAB&fromStartDate=2011-01-01")
      .header("Content-Type", APPLICATION_JSON_VALUE)
      .headers(setAuthorisation(listOf()))
      .accept(APPLICATION_JSON)
      .bodyValue("""["A1234AA","A1234AD"]""")
      .exchange()
      .expectStatus().isBadRequest()
      .expectBody()
      .jsonPath("userMessage").isEqualTo("Required request parameter 'toStartDate' for method parameter type LocalDate is not present")
  }

  @Test
  fun countPersonalCareNeedsForOffenders_emptyBody() {
    webTestClient.post()
      .uri("/api/bookings/offenderNo/personal-care-needs/count?type=DISAB&fromStartDate=2010-01-01&toStartDate=2011-01-01")
      .header("Content-Type", APPLICATION_JSON_VALUE)
      .headers(setAuthorisation(listOf()))
      .accept(APPLICATION_JSON)
      .bodyValue("")
      .exchange()
      .expectStatus().isBadRequest()
  }

  @Test
  fun `returns 401 without an auth token`() {
    webTestClient.post().uri("/api/bookings/offenderNo/personal-care-needs/count?type=DISAB&fromStartDate=2010-01-01&toStartDate=2011-01-01")
      .header("Content-Type", APPLICATION_JSON_VALUE)
      .bodyValue("""[ "A1234AA" ]""")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `returns 403 when client has no override role`() {
    webTestClient.post().uri("/api/bookings/offenderNo/personal-care-needs/count?type=DISAB&fromStartDate=2010-01-01&toStartDate=2011-01-01")
      .headers(setClientAuthorisation(listOf()))
      .header("Content-Type", APPLICATION_JSON_VALUE)
      .accept(APPLICATION_JSON)
      .bodyValue("""[ "A1234AA" ]""")
      .exchange()
      .expectStatus().isForbidden
  }
}
