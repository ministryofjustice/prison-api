package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_CLASS
import org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_CLASS
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED

class CourtResourceTest : ResourceTest() {

  @Nested
  @DisplayName("GET /api/court/{bookingId}/next-court-event")
  @Sql(
    scripts = ["/sql/create_future_court_events.sql"],
    executionPhase = BEFORE_TEST_CLASS,
    config = SqlConfig(transactionMode = ISOLATED),
  )
  @Sql(
    scripts = ["/sql/delete_future_court_events.sql"],
    executionPhase = AFTER_TEST_CLASS,
    config = SqlConfig(transactionMode = ISOLATED),
  )
  inner class NextCourtEvent {
    @Test
    fun `Retrieve next court event with everything populated`() {
      webTestClient.get()
        .uri("/api/court/${BOOKING_ID_MINUS_FIVE}/next-court-event")
        .headers(setAuthorisation(listOf("RELEASE_DATES_CALCULATOR")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.eventId").isEqualTo("-20195")
        .jsonPath("$.comments").isEqualTo("Next Court Event!")
        .jsonPath("$.caseReference").isEqualTo("TS9951A")
        .jsonPath("$.courtLocation").isEqualTo("HMP LEEDS")
        .jsonPath("$.courtEventType").isEqualTo("Court Appearance")
    }

    @Test
    fun `Retrieve next court event with minimal data populated`() {
      webTestClient.get()
        .uri("/api/court/${BOOKING_ID_MINUS_SIX}/next-court-event")
        .headers(setAuthorisation(listOf("RELEASE_DATES_CALCULATOR")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.eventId").isEqualTo("-30195")
        .jsonPath("$.comments").isEmpty
        .jsonPath("$.caseReference").isEmpty
        .jsonPath("$.courtLocation").isEqualTo("HMP LEEDS")
        .jsonPath("$.courtEventType").isEqualTo("Court Appearance")
    }
  }

  @Nested
  @DisplayName("GET /api/court/{bookingId}/count-active-cases")
  @Sql(
    scripts = ["/sql/create_active_and_inactive_court_case.sql"],
    executionPhase = BEFORE_TEST_CLASS,
    config = SqlConfig(transactionMode = ISOLATED),
  )
  @Sql(
    scripts = ["/sql/delete_future_court_events.sql"],
    executionPhase = AFTER_TEST_CLASS,
    config = SqlConfig(transactionMode = ISOLATED),
  )
  inner class ActiveCourtCasesCount {
    @Test
    fun `Count court cases`() {
      webTestClient.get()
        .uri("/api/court/${BOOKING_ID_MINUS_FIVE}/count-active-cases")
        .headers(setAuthorisation(listOf("RELEASE_DATES_CALCULATOR")))
        .exchange()
        .expectStatus().isOk
        .expectBody(Int::class.java).isEqualTo(2)
    }

  }

  private companion object {
    const val BOOKING_ID_MINUS_FIVE = -5
    const val BOOKING_ID_MINUS_SIX = -6
  }
}
