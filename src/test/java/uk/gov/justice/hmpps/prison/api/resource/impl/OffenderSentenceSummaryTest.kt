package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("GET /api/offenders/{nomsId}/booking/latest/sentence-summary")
class OffenderSentenceSummaryTest : ResourceTest() {
  @Test
  fun `returns 401 without an auth token`() {
    webTestClient.get().uri("/api/offenders/Z0020ZZ/booking/latest/sentence-summary")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `should return 403 if does not have override role`() {
    webTestClient.get().uri("/api/offenders/Z0020ZZ/booking/latest/sentence-summary")
      .headers(setClientAuthorisation(listOf()))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `returns 403 when client has override role ROLE_SYSTEM_USER`() {
    webTestClient.get().uri("/api/offenders/Z0020ZZ/booking/latest/sentence-summary")
      .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `returns success when client has override role ROLE_VIEW_PRISONER_DATA`() {
    webTestClient.get().uri("/api/offenders/Z0020ZZ/booking/latest/sentence-summary")
      .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `returns success if in user caseload`() {
    webTestClient.get().uri("/api/offenders/A1234AA/booking/latest/sentence-summary")
      .headers(setAuthorisation(listOf()))
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `returns sentence summary data for the offender`() {
    webTestClient.get().uri("/api/offenders/Z0020ZZ/booking/latest/sentence-summary")
      .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json("sentence_summary.json".readFile())
  }

  internal fun String.readFile(): String = this@OffenderSentenceSummaryTest::class.java.getResource(this).readText()
}
