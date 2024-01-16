@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookingResourceIntTest_caseNotes : ResourceTest() {

  @Nested
  @DisplayName("GET /api/bookings/{bookingId}/caseNotes/{type}/{subType}/count")
  inner class CaseNoteCount {

    @Test
    fun `returns 403 if has override ROLE_SYSTEM_USER`() {
      webTestClient.get()
        .uri("/api/bookings/-16/caseNotes/CHAP/FAMMAR/count")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 200 if has override ROLE_VIEW_CASE_NOTES`() {
      webTestClient.get()
        .uri("/api/bookings/-16/caseNotes/CHAP/FAMMAR/count")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_CASE_NOTES")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns 403 as ROLE_BANANAS is not override role`() {
      webTestClient.get()
        .uri("/api/bookings/-16/caseNotes/CHAP/FAMMAR/count")
        .headers(setClientAuthorisation(listOf("ROLE_BANANAS")))
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Client not authorised to access booking with id -16.")
    }

    @Test
    fun `Case note count is requested for booking that is not part of any of logged on staff user's caseloads`() {
      webTestClient.get()
        .uri("/api/bookings/-16/caseNotes/CHAP/FAMMAR/count")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -16 not found.")
    }

    @Test
    fun `Case note count is requested for existing booking, using a fromDate later than toDate`() {
      webTestClient.get()
        .uri("/api/bookings/-1/caseNotes/CHAP/FAMMAR/count?fromDate=2017-09-18&toDate=2017-09-12")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isBadRequest
        .expectBody().jsonPath("userMessage").isEqualTo("Invalid date range: toDate is before fromDate.")
    }

    @Test
    fun `Case note count is 0 when no date params passed into request - uses last 3 months`() {
      webTestClient.get()
        .uri("/api/bookings/-1/caseNotes/CHAP/FAMMAR/count")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("count").isEqualTo(0)
    }

    @Test
    fun `Case note count successfully received for same from and to date`() {
      webTestClient.get()
        .uri("/api/bookings/-1/caseNotes/CHAP/FAMMAR/count?fromDate=2000-01-01&toDate=2020-01-01")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("count").isEqualTo(1)
        .jsonPath("bookingId").isEqualTo(-1)
        .jsonPath("type").isEqualTo("CHAP")
        .jsonPath("subType").isEqualTo("FAMMAR")
        .jsonPath("fromDate").isEqualTo("2000-01-01")
        .jsonPath("toDate").isEqualTo("2020-01-01")
    }

    @Test
    fun `Case note count of 0 received for same from and to date`() {
      webTestClient.get()
        .uri("/api/bookings/-2/caseNotes/CHAP/FAMMAR/count?fromDate=2000-01-01&toDate=2020-01-01")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("count").isEqualTo(0)
    }

    @Test
    fun `Case note count of 8 received for same from and to date`() {
      webTestClient.get()
        .uri("/api/bookings/-3/caseNotes/OBSERVE/OBS_GEN/count?fromDate=2000-01-01&toDate=2020-01-01")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("count").isEqualTo(8)
    }

    @Test
    fun `Case note count of 0 received for different from and to date`() {
      webTestClient.get()
        .uri("/api/bookings/-2/caseNotes/APP/OUTCOME/count?fromDate=2022-01-01&toDate=2023-01-01")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("count").isEqualTo(0)
    }

    @Test
    fun `Case note count of 8 received for different from and to date`() {
      webTestClient.get()
        .uri("/api/bookings/-2/caseNotes/APP/OUTCOME/count?fromDate=2000-01-01&toDate=2020-01-01")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("count").isEqualTo(1)
    }

    @Test
    fun `Case note count received when no to date passed into request`() {
      webTestClient.get()
        .uri("/api/bookings/-3/caseNotes/OBSERVE/OBS_GEN/count?fromDate=2017-08-01")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("count").isEqualTo(2)
    }

    @Test
    fun `Case note count received when no from date passed into request`() {
      webTestClient.get()
        .uri("/api/bookings/-3/caseNotes/OBSERVE/OBS_GEN/count?toDate=2017-08-31")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("count").isEqualTo(6)
    }

    @Test
    fun `Case note count of 4 received when from date includes case notes on that date`() {
      webTestClient.get()
        .uri("/api/bookings/-3/caseNotes/OBSERVE/OBS_GEN/count?fromDate=2017-07-10&toDate=2020-01-01")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("count").isEqualTo(4)
    }

    @Test
    fun `Case note count of 4 received when to date includes case notes one day prior to that date`() {
      webTestClient.get()
        .uri("/api/bookings/-3/caseNotes/OBSERVE/OBS_GEN/count?fromDate=2000-01-02&toDate=2017-07-30")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("count").isEqualTo(6)
    }
  }
}
