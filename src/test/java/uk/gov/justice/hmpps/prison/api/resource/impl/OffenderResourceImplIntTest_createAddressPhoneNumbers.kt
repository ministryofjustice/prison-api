@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE

class OffenderResourceImplIntTest_createAddressPhoneNumbers : ResourceTest() {

  @Test
  fun `Create - should return the created phone numbers for the address`() {
    webTestClient.post().uri(CREATE_URL).bodyValue(VALID_PHONE_NUMBERS)
      .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
      .header("Content-Type", APPLICATION_JSON_VALUE).exchange().expectStatus().isOk.expectBody()
      .jsonPath("$.length()").isEqualTo(2)
      .jsonPath("[0].phoneId").isNotEmpty()
      .jsonPath("[0].type").isEqualTo("BUS")
      .jsonPath("[0].number").isEqualTo("12345 678 901")
      .jsonPath("[0].ext").isEqualTo("123")
      .jsonPath("[1].number").isEqualTo("12345 678 902")
  }

  @Test
  fun `Create - should return 403 without the write role`() {
    webTestClient.post().uri(CREATE_URL).bodyValue(VALID_PHONE_NUMBERS)
      .headers(setClientAuthorisation(listOf("EXAMPLE_ROLE")))
      .header("Content-Type", APPLICATION_JSON_VALUE).exchange().expectStatus().isForbidden
  }

  @Test
  fun `Create - should validate each phone number`() {
    webTestClient.post().uri(CREATE_URL).bodyValue("""[{"phoneNumberType":"BUS"}]""")
      .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
      .header("Content-Type", APPLICATION_JSON_VALUE).exchange().expectStatus().isBadRequest
  }

  @Test
  fun `Create - should return 404 when offender or linked address cannot be found`() {
    val headers = setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW"))

    webTestClient.post().uri("/api/offenders/unknown/addresses/-10/phone-numbers").bodyValue(VALID_PHONE_NUMBERS)
      .headers(headers).header("Content-Type", APPLICATION_JSON_VALUE).exchange().expectStatus().isNotFound

    webTestClient.post().uri("/api/offenders/$PRISONER_NUMBER/addresses/-999/phone-numbers")
      .bodyValue(VALID_PHONE_NUMBERS)
      .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
      .header("Content-Type", APPLICATION_JSON_VALUE).exchange().expectStatus().isNotFound

    webTestClient.post().uri("/api/offenders/$PRISONER_NUMBER/addresses/-14/phone-numbers")
      .bodyValue(VALID_PHONE_NUMBERS)
      .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
      .header("Content-Type", APPLICATION_JSON_VALUE).exchange().expectStatus().isNotFound
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AI"
    const val CREATE_URL = "/api/offenders/$PRISONER_NUMBER/addresses/-10/phone-numbers"
    const val VALID_PHONE_NUMBERS =
      """
        [
          {"phoneNumberType":"BUS", "phoneNumber":"12345 678 901", "extension":"123"},
          {"phoneNumberType":"HOME", "phoneNumber":"12345 678 902"}
        ]
      """
  }
}
