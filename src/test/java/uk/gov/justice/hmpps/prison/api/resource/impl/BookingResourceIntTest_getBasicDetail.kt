package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE

@DisplayName("POST /api/bookings/offenders")
class BookingResourceIntTest_getBasicDetail : ResourceTest() {

  @Test
  fun `should return 401 when user does not even have token`() {
    webTestClient.post().uri("/api/bookings/offenders")
      .header("Content-Type", APPLICATION_JSON_VALUE)
      .bodyValue("""["A1234AE","A1234AB"]""").exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `should return 400 if no override role and no user in context`() {
    webTestClient.post().uri("/api/bookings/offenders")
      .header("Content-Type", APPLICATION_JSON_VALUE)
      .headers(setClientAuthorisation(listOf()))
      .bodyValue("""["A1234AE","A1234AB"]""").exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `returns 400 when client has override role ROLE_SYSTEM_USER`() {
    webTestClient.post().uri("/api/bookings/offenders")
      .header("Content-Type", APPLICATION_JSON_VALUE)
      .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
      .bodyValue("""["A1234AE","A1234AB"]""").exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `returns success when client has override role ROLE_GLOBAL_SEARCH`() {
    webTestClient.post().uri("/api/bookings/offenders")
      .header("Content-Type", APPLICATION_JSON_VALUE)
      .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
      .bodyValue("""["A1234AE","A1234AB"]""").exchange()
      .expectStatus().isOk
  }

  @Test
  fun `returns success when client has override role ROLE_VIEW_PRISONER_DATA`() {
    webTestClient.post().uri("/api/bookings/offenders")
      .header("Content-Type", APPLICATION_JSON_VALUE)
      .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
      .bodyValue("""["A1234AE","A1234AB"]""").exchange()
      .expectStatus().isOk
  }

  @Test
  fun `returns success when user has caseload for offenders`() {
    webTestClient.post().uri("/api/bookings/offenders")
      .header("Content-Type", APPLICATION_JSON_VALUE)
      .headers(setAuthorisation(listOf()))
      .bodyValue("""["A1234AE","A1234AB"]""").exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("length()").isEqualTo(2)
  }

  @Test
  fun `returns data when user has caseload for offenders`() {
    webTestClient.post().uri("/api/bookings/offenders")
      .header("Content-Type", APPLICATION_JSON_VALUE)
      .headers(setAuthorisation(listOf()))
      .bodyValue("""["A1234AE","A1234AB"]""").exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("length()").isEqualTo(2)
      .jsonPath("[0].offenderNo").isEqualTo("A1234AE")
      .jsonPath("[0].firstName").isEqualTo("Donald")
      .jsonPath("[0].middleName").isEqualTo("Jeffrey Robert")
      .jsonPath("[0].lastName").isEqualTo("Matthews")
      .jsonPath("[0].bookingId").isEqualTo(-5)
      .jsonPath("[0].agencyId").isEqualTo("LEI")
      .jsonPath("[1].offenderNo").isEqualTo("A1234AB")
      .jsonPath("[1].firstName").isEqualTo("Gillian")
      .jsonPath("[1].middleName").isEqualTo("Eve")
      .jsonPath("[1].lastName").isEqualTo("Anderson")
      .jsonPath("[1].bookingId").isEqualTo(-2)
      .jsonPath("[1].agencyId").isEqualTo("LEI")
  }

  @Test
  fun `returns data when has override role and no user in context`() {
    webTestClient.post().uri("/api/bookings/offenders")
      .header("Content-Type", APPLICATION_JSON_VALUE)
      .headers(setAuthorisation(listOf()))
      .bodyValue("""["A1234AE","A1234AB","Z0017ZZ"]""").exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("length()").isEqualTo(3)
  }
}
