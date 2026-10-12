@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE

class OffenderResourceImplIntTest_updateAddressPhoneNumbers : ResourceTest() {

  @Test
  fun `Update - should return the updated phone number for the address`() {
    val createdPhones = webTestClient.post().uri(CREATE_URL)
      .bodyValue("""[{"phoneNumberType":"BUS", "phoneNumber":"12345 678 901"}]""")
      .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
      .header("Content-Type", APPLICATION_JSON_VALUE).exchange().expectStatus().isOk.expectBody(String::class.java)
      .returnResult().responseBody!!
    val phoneId = jsonMapper.readTree(createdPhones).get(0).get("phoneId").longValue()

    webTestClient.put().uri("/api/offenders/$PRISONER_NUMBER/addresses/-10/phone-numbers/$phoneId")
      .bodyValue("""{"phoneNumberType":"HOME", "phoneNumber":"020 1234 5678", "extension":"456"}""")
      .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
      .header("Content-Type", APPLICATION_JSON_VALUE).exchange().expectStatus().isOk.expectBody()
      .jsonPath("$.phoneId").isEqualTo(phoneId.toInt())
      .jsonPath("$.type").isEqualTo("HOME")
      .jsonPath("$.number").isEqualTo("020 1234 5678")
      .jsonPath("$.ext").isEqualTo("456")
  }

  @Test
  fun `Update - should require the write role and validate the phone number`() {
    webTestClient.put().uri("/api/offenders/$PRISONER_NUMBER/addresses/-10/phone-numbers/-1")
      .bodyValue("""{"phoneNumberType":"BUS", "phoneNumber":"12345"}""")
      .headers(setClientAuthorisation(listOf("EXAMPLE_ROLE")))
      .header("Content-Type", APPLICATION_JSON_VALUE).exchange().expectStatus().isForbidden

    webTestClient.put().uri("/api/offenders/$PRISONER_NUMBER/addresses/-10/phone-numbers/-1")
      .bodyValue("""{"phoneNumberType":"BUS"}""")
      .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
      .header("Content-Type", APPLICATION_JSON_VALUE).exchange().expectStatus().isBadRequest
  }

  @Test
  fun `Update - should return 404 when offender address or phone link cannot be found`() {
    val headers = setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW"))
    val body = """{"phoneNumberType":"BUS", "phoneNumber":"12345 678 901"}"""
    val createdPhones = webTestClient.post().uri(CREATE_URL)
      .bodyValue("""[{"phoneNumberType":"BUS", "phoneNumber":"12345 678 901"}]""")
      .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
      .header("Content-Type", APPLICATION_JSON_VALUE).exchange().expectStatus().isOk.expectBody(String::class.java)
      .returnResult().responseBody!!
    val phoneId = jsonMapper.readTree(createdPhones).get(0).get("phoneId").longValue()

    webTestClient.put().uri("/api/offenders/unknown/addresses/-10/phone-numbers/-1").bodyValue(body)
      .headers(headers).header("Content-Type", APPLICATION_JSON_VALUE).exchange().expectStatus().isNotFound

    webTestClient.put().uri("/api/offenders/$PRISONER_NUMBER/addresses/-999/phone-numbers/-1").bodyValue(body)
      .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
      .header("Content-Type", APPLICATION_JSON_VALUE).exchange().expectStatus().isNotFound

    webTestClient.put().uri("/api/offenders/$PRISONER_NUMBER/addresses/-10/phone-numbers/-999").bodyValue(body)
      .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
      .header("Content-Type", APPLICATION_JSON_VALUE).exchange().expectStatus().isNotFound

    webTestClient.put().uri("/api/offenders/$PRISONER_NUMBER/addresses/-11/phone-numbers/$phoneId").bodyValue(body)
      .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
      .header("Content-Type", APPLICATION_JSON_VALUE).exchange().expectStatus().isNotFound

    webTestClient.put().uri("/api/offenders/$PRISONER_NUMBER/addresses/-14/phone-numbers/$phoneId").bodyValue(body)
      .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
      .header("Content-Type", APPLICATION_JSON_VALUE).exchange().expectStatus().isNotFound
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AI"
    const val CREATE_URL = "/api/offenders/$PRISONER_NUMBER/addresses/-10/phone-numbers"
  }
}
