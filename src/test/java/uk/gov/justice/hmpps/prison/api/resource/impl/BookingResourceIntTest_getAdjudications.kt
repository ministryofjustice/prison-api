@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("GET /api/bookings/{bookingId}/adjudications")
class BookingResourceIntTest_getAdjudications : ResourceTest() {

  @Test
  fun `returns 401 without an auth token`() {
    webTestClient.get().uri("/api/bookings/-36/adjudications")
      .exchange().expectStatus().isUnauthorized
  }

  @Test
  fun `should return 403 if no override role`() {
    webTestClient.get().uri("/api/bookings/-36/adjudications")
      .headers(setClientAuthorisation(listOf())).exchange().expectStatus().isForbidden
  }

  @Test
  fun `returns 403 if has override role ROLE_SYSTEM_USER`() {
    webTestClient.get().uri("/api/bookings/-36/adjudications")
      .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER"))).exchange().expectStatus().isForbidden
  }

  @Test
  fun `returns 200 if has override role ROLE_GLOBAL_SEARCH`() {
    webTestClient.get().uri("/api/bookings/-36/adjudications")
      .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH"))).exchange().expectStatus().isOk
  }

  @Test
  fun `returns 200 if has override role ROLE_VIEW_PRISONER_DATA`() {
    webTestClient.get().uri("/api/bookings/-36/adjudications")
      .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA"))).exchange().expectStatus().isOk
  }

  @Test
  fun `returns 200 if has override role ROLE_VIEW_ADJUDICATIONS`() {
    webTestClient.get().uri("/api/bookings/-2/adjudications")
      .headers(setClientAuthorisation(listOf("ROLE_VIEW_ADJUDICATIONS"))).exchange().expectStatus().isOk
  }

  @Test
  fun `returns 404 if no booking exists`() {
    webTestClient.get().uri("/api/bookings/-99999/adjudications")
      .headers(setAuthorisation(listOf())).exchange().expectStatus().isNotFound
      .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -99999 not found.")
  }

  @Test
  fun `returns 404 if no adjudication exists`() {
    webTestClient.get().uri("/api/bookings/-16/adjudications")
      .headers(setAuthorisation(listOf())).exchange().expectStatus().isNotFound
      .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -16 not found.")
  }

  @Test
  fun `returns 404 if not in user caseload`() {
    webTestClient.get().uri("/api/bookings/-2/adjudications")
      .headers(setAuthorisation("WAI_USER", listOf())).exchange().expectStatus().isNotFound
      .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -2 not found.")
  }

  @Test
  fun `returns 200 if in user caseload`() {
    webTestClient.get().uri("/api/bookings/-2/adjudications")
      .headers(setAuthorisation(listOf())).exchange().expectStatus().isOk
  }

  @Test
  fun `returns adjudication summary data`() {
    webTestClient.get().uri("/api/bookings/-2/adjudications?awardCutoffDate=2016-09-01&adjudicationCutoffDate=2016-09-01")
      .headers(setAuthorisation(listOf())).exchange()
      .expectStatus().isOk.expectBody()
      .jsonPath("adjudicationCount").isEqualTo(1)
      .jsonPath("awards.length()").isEqualTo(1)
      .jsonPath("awards[0].sanctionCode").isEqualTo("ADA")
      .jsonPath("awards[0].sanctionCodeDescription").isEqualTo("Additional Days Added")
      .jsonPath("awards[0].days").isEqualTo(11)
      .jsonPath("awards[0].effectiveDate").isEqualTo("2016-11-09")
      .jsonPath("awards[0].months").doesNotExist()
      .jsonPath("awards[0].limit").doesNotExist()
      .jsonPath("awards[0].comment").doesNotExist()
  }

  @Test
  fun `returns no awards if they don't exists for offender`() {
    webTestClient.get().uri("/api/bookings/-4/adjudications")
      .headers(setAuthorisation(listOf()))
      .exchange().expectStatus().isOk.expectBody()
      .jsonPath("adjudicationCount").isEqualTo(0)
      .jsonPath("awards.length()").isEqualTo(0)
  }

  @Test
  fun `returns no awards for offender when cut off dates set`() {
    webTestClient.get().uri("/api/bookings/-7/adjudications?awardCutoffDate=2013-01-01&adjudicationCutoffDate=2013-01-01")
      .headers(setAuthorisation(listOf()))
      .exchange().expectStatus().isOk.expectBody()
      .jsonPath("adjudicationCount").isEqualTo(0)
      .jsonPath("awards.length()").isEqualTo(0)
  }

  @Test
  fun `multiple awards in adjudication summary are returned for offender`() {
    webTestClient.get().uri("/api/bookings/-8/adjudications?awardCutoffDate=2017-08-20&adjudicationCutoffDate=2017-08-20")
      .headers(setAuthorisation(listOf())).exchange()
      .expectStatus().isOk.expectBody()
      .jsonPath("adjudicationCount").isEqualTo(1)
      .jsonPath("awards.length()").isEqualTo(4)
      .jsonPath("awards[*].sanctionCode").value<List<String>> { assertThat(it).containsExactly("FORFEIT", "CC", "STOP_PCT", "FORFEIT") }
      .jsonPath("awards[*].days").value<List<Int>> { assertThat(it).containsExactly(17, 7, 21, 19) }
      .jsonPath("awards[*].months").value<List<Int>> { assertThat(it).containsExactly(2) }
      .jsonPath("awards[*].limit").value<List<Double>> { assertThat(it).containsExactly(50.0) }
      .jsonPath("awards[*].comment").value<List<String>> { assertThat(it).containsExactly("loc", "tv") }
      .jsonPath("awards[*].effectiveDate").value<List<String>> { assertThat(it).containsOnly("2017-11-13") }
  }

  @Test
  fun `older awards expired and are not returned`() {
    webTestClient.get().uri("/api/bookings/-5/adjudications?awardCutoffDate=2017-12-04&adjudicationCutoffDate=2017-12-04")
      .headers(setAuthorisation(listOf())).exchange()
      .expectStatus().isOk.expectBody()
      .jsonPath("adjudicationCount").isEqualTo(2)
      .jsonPath("awards.length()").isEqualTo(3)
  }

  @Test
  fun `different cutoff dates set returns correct adjudication and awards counts`() {
    webTestClient.get().uri("/api/bookings/-5/adjudications?awardCutoffDate=2017-12-07&adjudicationCutoffDate=2017-11-07")
      .headers(setAuthorisation(listOf())).exchange()
      .expectStatus().isOk.expectBody()
      .jsonPath("adjudicationCount").isEqualTo(3)
      .jsonPath("awards.length()").isEqualTo(2)
  }
}
