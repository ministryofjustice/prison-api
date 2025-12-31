@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.ClassOrderer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestClassOrder
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.web.reactive.server.WebTestClient

@TestClassOrder(ClassOrderer.ClassName::class)
class OffenderResourceImplIntTest_getOffenderPhoneNumbers : ResourceTest() {

  private fun expectInvalidRequest(requestSpec: WebTestClient.RequestBodyUriSpec, uri: String, body: String) {
    requestSpec.uri(uri).bodyValue(body)
      .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
      .header("Content-Type", APPLICATION_JSON_VALUE).exchange().expectStatus().isBadRequest()
  }

  @Nested
  inner class GetOffenderNumbers {
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
    fun `Read - should return offender phone numbers`() {
      webTestClient.get().uri(GET_URL).headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA"))).exchange()
        .expectBody().jsonPath("$.length()").isEqualTo(2).jsonPath("[0].type").isEqualTo(PHONE_1.TYPE)
        .jsonPath("[0].number").isEqualTo(PHONE_1.NUMBER).jsonPath("[0].phoneId").isEqualTo(PHONE_1.ID)
        .jsonPath("[0].ext").isEqualTo(PHONE_1.EXT).jsonPath("[1].type").isEqualTo(PHONE_2.TYPE).jsonPath("[1].number")
        .isEqualTo(PHONE_2.NUMBER).jsonPath("[1].phoneId").isEqualTo(PHONE_2.ID).jsonPath("[1].ext")
        .isEqualTo(PHONE_2.EXT)
    }
  }

  @Nested
  inner class PostOffenderNumbers {
    @Test
    fun `Create - should return 401 when user does not even have token`() {
      webTestClient.post().uri(CREATE_URL).bodyValue(VALID_PHONE_NUMBER_CREATION_REQUEST).exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `Create - should return 403 if the client does not have any roles`() {
      webTestClient.post().uri(CREATE_URL).bodyValue(VALID_PHONE_NUMBER_CREATION_REQUEST)
        .headers(setClientAuthorisation(listOf())).header("Content-Type", APPLICATION_JSON_VALUE).exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `Create - should return 403 if the client does not have the ROLE_PRISON_API__PRISONER_PROFILE__RW role`() {
      webTestClient.post().uri(CREATE_URL).bodyValue(VALID_PHONE_NUMBER_CREATION_REQUEST)
        .headers(setClientAuthorisation(listOf("EXAMPLE_ROLE"))).header("Content-Type", APPLICATION_JSON_VALUE)
        .exchange().expectStatus().isForbidden
    }

    @Test
    fun `Create - should return 200 and the created number if the request is valid`() {
      webTestClient.post().uri(CREATE_URL).bodyValue(VALID_PHONE_NUMBER_CREATION_REQUEST)
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE).exchange().expectStatus().isOk().expectBody()
        .jsonPath(".phoneId").isNotEmpty().jsonPath(".type").isEqualTo("BUS").jsonPath(".number")
        .isEqualTo("12345 678 902").jsonPath(".ext").isEqualTo("123")
    }

    @Test
    fun `Create - should return 400 with a missing phone number type`() {
      expectInvalidRequest(webTestClient.post(), CREATE_URL, "{ \"phoneNumber\": \"01234 567 890\" }")
    }

    @Test
    fun `Create - should return 400 with an invalid phone number type`() {
      expectInvalidRequest(webTestClient.post(), CREATE_URL, INVALID_PHONE_NUMBER_TYPE_CREATION_REQUEST)
    }

    @Test
    fun `Create - should return 400 with a missing phone number`() {
      expectInvalidRequest(webTestClient.post(), CREATE_URL, "{ \"phoneNumberType\": \"BUS\" }")
    }

    @Test
    fun `Create - should return 400 with an invalid phone number`() {
      expectInvalidRequest(
        webTestClient.post(),
        CREATE_URL,
        "{ \"phoneNumber\": \"11111111111111111111111111111111111111111\" }",
      )
    }
  }

  @Nested
  inner class PutOffenderNumber {
    @Test
    fun `Update - should return 401 when user does not even have token`() {
      webTestClient.put().uri(UPDATE_URL).bodyValue(VALID_PHONE_NUMBER_CREATION_REQUEST).exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `Update - should return 403 if the client does not have any roles`() {
      webTestClient.put().uri(UPDATE_URL).bodyValue(VALID_PHONE_NUMBER_CREATION_REQUEST)
        .headers(setClientAuthorisation(listOf())).header("Content-Type", APPLICATION_JSON_VALUE).exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `Update - should return 403 if the client does not have the ROLE_PRISON_API__PRISONER_PROFILE__RW role`() {
      webTestClient.put().uri(UPDATE_URL).bodyValue(VALID_PHONE_NUMBER_CREATION_REQUEST)
        .headers(setClientAuthorisation(listOf("EXAMPLE_ROLE"))).header("Content-Type", APPLICATION_JSON_VALUE)
        .exchange().expectStatus().isForbidden
    }

    @Test
    fun `Update - should return 200 and updated number if the request is valid`() {
      webTestClient.put().uri(UPDATE_URL).bodyValue(VALID_PHONE_NUMBER_CREATION_REQUEST)
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE).exchange().expectStatus().isOk().expectBody()
        .jsonPath(".phoneId").isNotEmpty().jsonPath(".type").isEqualTo("BUS").jsonPath(".number")
        .isEqualTo("12345 678 902").jsonPath(".ext").isEqualTo("123")
    }

    @Test
    fun `Update - should return 400 with a missing phone number type`() {
      expectInvalidRequest(webTestClient.put(), UPDATE_URL, "{ \"phoneNumber\": \"01234 567 890\" }")
    }

    @Test
    fun `Update - should return 400 with an invalid phone number type`() {
      expectInvalidRequest(webTestClient.put(), UPDATE_URL, INVALID_PHONE_NUMBER_TYPE_CREATION_REQUEST)
    }

    @Test
    fun `Update - should return 400 with a missing phone number`() {
      expectInvalidRequest(webTestClient.put(), UPDATE_URL, "{ \"phoneNumberType\": \"BUS\" }")
    }

    @Test
    fun `Update - should return 400 with an invalid phone number`() {
      expectInvalidRequest(
        webTestClient.put(),
        UPDATE_URL,
        "{ \"phoneNumber\": \"11111111111111111111111111111111111111111\" }",
      )
    }
  }

  private companion object {
    const val PRISONER_NUMBER = "A1238AI"
    const val GET_URL = "/api/offenders/${PRISONER_NUMBER}/phone-numbers"
    const val CREATE_URL = "/api/offenders/${PRISONER_NUMBER}/phone-numbers"
    const val UPDATE_URL = "/api/offenders/${PRISONER_NUMBER}/phone-numbers/-19"

    data object PHONE_1 {
      const val TYPE = "HOME"
      const val NUMBER = "0114 878787"
      const val EXT = "345"
      const val ID = -19
    }

    data object PHONE_2 {
      const val TYPE = "MOB"
      const val NUMBER = "07878 787878"
      const val EXT = "543"
      const val ID = -20
    }

    const val INVALID_PHONE_NUMBER_TYPE_CREATION_REQUEST =
      // language=json
      """
        {
          "phoneNumberType": "EXAMPLE_INVALID_TYPE",
          "phoneNumber": "12345 678 901",
          "extension": "123"
        }
      """

    const val VALID_PHONE_NUMBER_CREATION_REQUEST =
      // language=json
      """
        {
          "phoneNumberType": "BUS",
          "phoneNumber": "12345 678 902",
          "extension": "123"
        }
      """
  }
}
