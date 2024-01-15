package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.Test

class IdentitiesResourceTest : ResourceTest() {
  @Test
  fun `returns 401 without an auth token`() {
    webTestClient.get().uri("/api/bookings/-4/identifiers")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `should return 403 if does not have override role`() {
    webTestClient.get().uri("/api/bookings/-4/identifiers")
      .headers(setClientAuthorisation(listOf()))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `returns 403 when client has override role ROLE_SYSTEM_USER`() {
    webTestClient.get().uri("/api/bookings/-4/identifiers")
      .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `returns success when client has override role ROLE_GLOBAL_SEARCH`() {
    webTestClient.get().uri("/api/bookings/-4/identifiers")
      .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `returns success when client has override role ROLE_VIEW_PRISONER_DATA`() {
    webTestClient.get().uri("/api/bookings/-4/identifiers")
      .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `returns 404 if not in user caseload`() {
    webTestClient.get().uri("/api/bookings/-4/identifiers")
      .headers(setAuthorisation("WAI_USER", listOf()))
      .exchange()
      .expectStatus().isNotFound
      .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -4 not found.")
  }

  @Test
  fun `returns success if in user caseload`() {
    webTestClient.get().uri("/api/bookings/-4/identifiers")
      .headers(setAuthorisation(listOf()))
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `returns identities by type`() {
    webTestClient.get().uri("/api/bookings/-4/identifiers?type=PNC")
      .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("length()").isEqualTo(1)
      .jsonPath("[0].type").isEqualTo("PNC")
      .jsonPath("[0].value").isEqualTo("1998/1234567L")
      .jsonPath("[0].offenderNo").isEqualTo("A1234AD")
      .jsonPath("[0].bookingId").isEqualTo(-4)
      .jsonPath("[0].issuedDate").isEqualTo("2017-08-28")
      .jsonPath("[0].caseloadType").isEqualTo("INST")
  }

  @Test
  fun `returns identities when no type specified`() {
    webTestClient.get().uri("/api/bookings/-4/identifiers")
      .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("length()").isEqualTo(2)
      .jsonPath("[0].type").isEqualTo("PNC")
      .jsonPath("[0].value").isEqualTo("1998/1234567L")
      .jsonPath("[0].offenderNo").isEqualTo("A1234AD")
      .jsonPath("[0].bookingId").isEqualTo(-4)
      .jsonPath("[0].issuedDate").isEqualTo("2017-08-28")
      .jsonPath("[0].caseloadType").isEqualTo("INST")
      .jsonPath("[1].type").isEqualTo("CRO")
      .jsonPath("[1].value").isEqualTo("CRO144322")
      .jsonPath("[1].offenderNo").isEqualTo("A1234AD")
      .jsonPath("[1].bookingId").isEqualTo(-4)
      .jsonPath("[1].issuedDate").isEqualTo("2019-09-25")
      .jsonPath("[1].caseloadType").isEqualTo("INST")
  }
}
