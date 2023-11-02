package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class OffenderResourceImplIntTest_getOffenderRestrictions : ResourceTest() {
  @Test
  fun shouldReturnListOfActiveOffenderRestrictions() {
    webTestClient.get()
      .uri("/api/offenders/A1234AH/offender-restrictions")
      .headers(setAuthorisation("ITAG_USER", listOf("")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("bookingId").isEqualTo(-8)
      .jsonPath("$.offenderRestrictions.length()").isEqualTo(2)
      .jsonPath("offenderRestrictions[0].restrictionTypeDescription").isEqualTo("Restricted")
      .jsonPath("offenderRestrictions[0].restrictionType").isEqualTo("RESTRICTED")
      .jsonPath("offenderRestrictions[0].startDate").isEqualTo("2001-01-01")
      .jsonPath("offenderRestrictions[0].expiryDate").doesNotExist()
      .jsonPath("offenderRestrictions[0].comment").isEqualTo("Some Comment Text")
      .jsonPath("offenderRestrictions[0].active").isEqualTo("true")
      .jsonPath("offenderRestrictions[1].restrictionTypeDescription").isEqualTo("Child Visitors to be Vetted")
      .jsonPath("offenderRestrictions[1].restrictionType").isEqualTo("CHILD")
      .jsonPath("offenderRestrictions[1].startDate").isEqualTo("2001-01-01")
      .jsonPath("offenderRestrictions[1].expiryDate").doesNotExist()
      .jsonPath("offenderRestrictions[1].comment").isEqualTo("More Comment Text")
      .jsonPath("offenderRestrictions[1].active").isEqualTo("true")
  }

  @Test
  fun shouldReturnListOfAllOffenderRestrictions() {
    webTestClient.get()
      .uri("/api/offenders/A1234AH/offender-restrictions?activeRestrictionsOnly=false")
      .headers(setAuthorisation("ITAG_USER", listOf("")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("bookingId").isEqualTo(-8)
      .jsonPath("$.offenderRestrictions.length()").isEqualTo(3)
      .jsonPath("offenderRestrictions[0].restrictionType").isEqualTo("RESTRICTED")
      .jsonPath("offenderRestrictions[1].restrictionType").isEqualTo("CHILD")
      .jsonPath("offenderRestrictions[2].restrictionType").isEqualTo("BAN")
      .jsonPath("offenderRestrictions[2].expiryDate").isEqualTo("2002-01-01")
  }

  @Test
  fun shouldReturn404WhenOffenderDoesNotExist() {
    webTestClient.get()
      .uri("/api/offenders/AAA444/offender-restrictions")
      .headers(setAuthorisation("ITAG_USER", listOf("")))
      .exchange()
      .expectStatus().isNotFound
      .expectBody()
      .jsonPath("userMessage").isEqualTo("Resource with id [AAA444] not found.")
      .jsonPath("developerMessage").isEqualTo("Resource with id [AAA444] not found.")
  }

  @Nested
  inner class Authorisation {
    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.get()
        .uri("api/offenders/A1234AH/offender-restrictions")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 when client does not have any roles`() {
      webTestClient.get()
        .uri("/api/offenders/A1234AH/offender-restrictions")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Client not authorised to access booking with id -8.")
    }

    @Test
    fun `returns 403 as ROLE_BANANAS is not override role`() {
      webTestClient.get()
        .uri("/api/offenders/A1234AH/offender-restrictions")
        .headers(setClientAuthorisation(listOf("ROLE_BANANAS")))
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Client not authorised to access booking with id -8.")
    }

    @Test
    fun `returns 200 if has override role OFFENDER_RESTRICTIONS`() {
      webTestClient.get()
        .uri("/api/offenders/A1234AH/offender-restrictions")
        .headers(setClientAuthorisation(listOf("ROLE_OFFENDER_RESTRICTIONS")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns 200 if has override role SYSTEM_USER`() {
      webTestClient.get()
        .uri("/api/offenders/A1234AH/offender-restrictions")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns 404 when user does not have any roles`() {
      webTestClient.get()
        .uri("/api/offenders/A1234AH/offender-restrictions")
        .headers(setAuthorisation("UNAUTHORISED_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -8 not found.")
    }

    @Test
    fun `returns 404 as ROLE_BANANAS is not override role`() {
      webTestClient.get()
        .uri("/api/offenders/A1234AH/offender-restrictions")
        .headers(setAuthorisation("UNAUTHORISED_USER", listOf("ROLE_BANANAS")))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -8 not found.")
    }

    @Test
    fun `returns 404 when user does not have offender in their caseload`() {
      webTestClient.get()
        .uri("/api/offenders/A1234AH/offender-restrictions")
        .headers(setAuthorisation("WAI_USER", listOf("")))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -8 not found.")
    }
  }
}
