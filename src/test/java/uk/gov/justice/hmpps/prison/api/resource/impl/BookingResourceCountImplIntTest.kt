package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.Sql.ExecutionPhase
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters

class BookingResourceCountImplIntTest : ResourceTest() {
  @Autowired
  protected lateinit var webTestClient: WebTestClient

  @Sql(scripts = ["/sql/addingHealthProblems_init.sql"], executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, config = SqlConfig(transactionMode = TransactionMode.ISOLATED))
  @Sql(scripts = ["/sql/addingHealthProblems_clean.sql"], executionPhase = ExecutionPhase.AFTER_TEST_METHOD, config = SqlConfig(transactionMode = TransactionMode.ISOLATED))
  @Test
  fun countPersonalCareNeedsForOffenders() {
    webTestClient.post()
      .uri("/api/bookings/offenderNo/personal-care-needs/count?type=DISAB&fromStartDate=2010-01-01&toStartDate=2011-01-01")
      .header("Content-Type", APPLICATION_JSON_VALUE)
      .headers(setAuthorisation(mutableListOf("ITAG_USER")))
      .accept(APPLICATION_JSON)
      .body(
        BodyInserters.fromValue("[\"A1234AA\",\"A1234AD\"]"),
      ).exchange()
      .expectStatus().isOk()
      .expectBody(String::class.java).isEqualTo("[{\"offenderNo\":\"A1234AA\",\"size\":4},{\"offenderNo\":\"A1234AD\",\"size\":1}]")
  }

  @Test
  fun countPersonalCareNeedsForOffenders_missingProblemType() {
    webTestClient.post()
      .uri("/api/bookings/offenderNo/personal-care-needs/count?fromStartDate=2010-01-01&toStartDate=2011-01-01")
      .header("Content-Type", APPLICATION_JSON_VALUE)
      .headers(setAuthorisation(mutableListOf("ITAG_USER")))
      .accept(APPLICATION_JSON)
      .body(
        BodyInserters.fromValue("[\"A1234AA\",\"A1234AD\"]"),
      ).exchange()
      .expectStatus().isBadRequest()
  }

  @Test
  fun countPersonalCareNeedsForOffenders_emptyBody() {
    webTestClient.post()
      .uri("/api/bookings/offenderNo/personal-care-needs/count?type=DISAB&fromStartDate=2010-01-01&toStartDate=2011-01-01")
      .header("Content-Type", APPLICATION_JSON_VALUE)
      .headers(setAuthorisation(mutableListOf("ITAG_USER")))
      .accept(APPLICATION_JSON)
      .body(
        BodyInserters.fromValue(""),
      ).exchange()
      .expectStatus().isBadRequest()
  }

  @Test
  fun `returns 401 without an auth token`() {
    webTestClient.post().uri("/api/bookings/offenderNo/personal-care-needs/count?type=DISAB&fromStartDate=2010-01-01&toStartDate=2011-01-01")
      .header("Content-Type", APPLICATION_JSON_VALUE)
      .accept(APPLICATION_JSON)
      .bodyValue("[ \"A1234AA\" ]")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  @Disabled("this test fails - code/role update needed")
  fun `returns 403 when client has no override role`() {
    webTestClient.post().uri("/api/bookings/offenderNo/personal-care-needs/count?type=DISAB&fromStartDate=2010-01-01&toStartDate=2011-01-01")
      .headers(setClientAuthorisation(listOf()))
      .header("Content-Type", APPLICATION_JSON_VALUE)
      .accept(APPLICATION_JSON)
      .bodyValue("[ \"A1234AA\" ]")
      .exchange()
      .expectStatus().isForbidden
  }
}
