@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.ClassOrderer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestClassOrder
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.web.reactive.server.WebTestClient

@TestClassOrder(ClassOrderer.ClassName::class)
class OffenderResourceImplIntTest_getOffenderEmailAddresses : ResourceTest() {

  private fun expectInvalidRequest(requestSpec: WebTestClient.RequestBodyUriSpec, uri: String, body: String) {
    requestSpec.uri(uri).bodyValue(body)
      .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
      .header("Content-Type", APPLICATION_JSON_VALUE).exchange().expectStatus().isBadRequest()
  }

  @Nested
  inner class GetOffenderEmails {
    @Test
    fun `Read - should return 401 when user does not even have token`() {
      webTestClient.get().uri(GET_URL).exchange().expectStatus().isUnauthorized
    }

    @Test
    fun `Read - should return 403 if no override role`() {
      webTestClient.get().uri(GET_URL).headers(setClientAuthorisation(listOf())).exchange().expectStatus().isForbidden
    }

    @Test
    fun `Read - should return success when has ROLE_VIEW_PRISONER_DATA override role`() {
      webTestClient.get().uri(GET_URL).headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA"))).exchange()
        .expectStatus().isOk
    }

    @Test
    fun `Read - should return offender email addresses`() {
      webTestClient.get().uri(GET_URL).headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA"))).exchange()
        .expectBody().jsonPath("$.length()").isEqualTo(2).jsonPath("[0].email").isEqualTo(EMAIL_1.EMAIL_ADDRESS)
        .jsonPath("[0].emailAddressId").isEqualTo(EMAIL_1.ID).jsonPath("[1].email")
        .isEqualTo(EMAIL_2.EMAIL_ADDRESS).jsonPath("[1].emailAddressId").isEqualTo(EMAIL_2.ID)
    }
  }

  @Nested
  inner class PostOffenderEmails {
    @Test
    fun `Create - should return 401 when user does not even have token`() {
      webTestClient.post().uri(CREATE_URL).bodyValue(VALID_EMAIL_ADDRESS_CREATION_REQUEST).exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `Create - should return 403 if the client does not have any roles`() {
      webTestClient.post().uri(CREATE_URL).bodyValue(VALID_EMAIL_ADDRESS_CREATION_REQUEST)
        .headers(setClientAuthorisation(listOf())).header("Content-Type", APPLICATION_JSON_VALUE).exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `Create - should return 403 if the client does not have the ROLE_PRISON_API__PRISONER_PROFILE__RW role`() {
      webTestClient.post().uri(CREATE_URL).bodyValue(VALID_EMAIL_ADDRESS_CREATION_REQUEST)
        .headers(setClientAuthorisation(listOf("EXAMPLE_ROLE"))).header("Content-Type", APPLICATION_JSON_VALUE)
        .exchange().expectStatus().isForbidden
    }

    @Test
    fun `Create - should return 200 and the created number if the request is valid`() {
      webTestClient.post().uri(CREATE_URL).bodyValue(VALID_EMAIL_ADDRESS_CREATION_REQUEST)
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE).exchange().expectStatus().isOk().expectBody()
        .jsonPath(".emailAddressId").isNotEmpty().jsonPath(".email").isEqualTo("prisoner@test.com")
    }

    @Test
    fun `Create - should return 400 with a missing email address`() {
      expectInvalidRequest(webTestClient.post(), CREATE_URL, INVALID_EMAIL_ADDRESS_CREATION_REQUEST__EMPTY)
    }

    @Test
    fun `Create - should return 400 with an email address that is too long`() {
      expectInvalidRequest(webTestClient.post(), CREATE_URL, INVALID_EMAIL_ADDRESS_CREATION_REQUEST__TOO_LONG)
    }
  }

  @Nested
  inner class PutOffenderEmail {
    @Test
    fun `Update - should return 401 when user does not even have token`() {
      webTestClient.put().uri(UPDATE_URL).bodyValue(VALID_EMAIL_ADDRESS_CREATION_REQUEST).exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `Update - should return 403 if the client does not have any roles`() {
      webTestClient.put().uri(UPDATE_URL).bodyValue(VALID_EMAIL_ADDRESS_CREATION_REQUEST)
        .headers(setClientAuthorisation(listOf())).header("Content-Type", APPLICATION_JSON_VALUE).exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `Update - should return 403 if the client does not have the ROLE_PRISON_API__PRISONER_PROFILE__RW role`() {
      webTestClient.put().uri(UPDATE_URL).bodyValue(VALID_EMAIL_ADDRESS_CREATION_REQUEST)
        .headers(setClientAuthorisation(listOf("EXAMPLE_ROLE"))).header("Content-Type", APPLICATION_JSON_VALUE)
        .exchange().expectStatus().isForbidden
    }

    @Test
    fun `Update - should return 200 and updated number if the request is valid`() {
      webTestClient.put().uri(UPDATE_URL).bodyValue(VALID_EMAIL_ADDRESS_CREATION_REQUEST)
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE).exchange().expectStatus().isOk().expectBody()
        .jsonPath(".emailAddressId").isNotEmpty().jsonPath(".email").isEqualTo("prisoner@test.com")
    }

    @Test
    fun `Update - should return 400 with an empty email address`() {
      expectInvalidRequest(webTestClient.put(), UPDATE_URL, INVALID_EMAIL_ADDRESS_CREATION_REQUEST__EMPTY)
    }

    @Test
    fun `Update - should return 400 with an email address that is too long`() {
      expectInvalidRequest(webTestClient.put(), UPDATE_URL, INVALID_EMAIL_ADDRESS_CREATION_REQUEST__TOO_LONG)
    }
  }

  private companion object {
    const val PRISONER_NUMBER = "A1238AI"
    const val GET_URL = "/api/offenders/${PRISONER_NUMBER}/email-addresses"
    const val CREATE_URL = "/api/offenders/${PRISONER_NUMBER}/email-addresses"
    const val UPDATE_URL = "/api/offenders/${PRISONER_NUMBER}/email-addresses/-10"

    data object EMAIL_1 {
      const val EMAIL_ADDRESS = "prisoner@home.com"
      const val ID = -10
    }

    data object EMAIL_2 {
      const val EMAIL_ADDRESS = "prisoner@backup.com"
      const val ID = -11
    }

    val EMAIL_ADDRESS_TOO_LONG = "A".repeat(500)
    const val VALID_EMAIL_ADDRESS_CREATION_REQUEST = "{ \"emailAddress\": \"prisoner@test.com\" }"
    const val INVALID_EMAIL_ADDRESS_CREATION_REQUEST__EMPTY = "{ \"emailAddress\": \"\" }"
    val INVALID_EMAIL_ADDRESS_CREATION_REQUEST__TOO_LONG = "{\"emailAddress\": $EMAIL_ADDRESS_TOO_LONG }"
  }
}
