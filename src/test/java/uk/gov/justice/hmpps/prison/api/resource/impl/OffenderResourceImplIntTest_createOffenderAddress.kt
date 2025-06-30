@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.web.reactive.server.WebTestClient

class OffenderResourceImplIntTest_createOffenderAddress : ResourceTest() {

  private fun expectInvalidRequest(requestSpec: WebTestClient.RequestBodyUriSpec, uri: String, body: String) {
    requestSpec.uri(uri).bodyValue(body)
      .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
      .header("Content-Type", APPLICATION_JSON_VALUE).exchange().expectStatus().isBadRequest()
  }

  private fun expectNotFound(requestSpec: WebTestClient.RequestBodyUriSpec, uri: String, body: String) {
    requestSpec.uri(uri).bodyValue(body)
      .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
      .header("Content-Type", APPLICATION_JSON_VALUE).exchange().expectStatus().isNotFound()
  }

  @Nested
  inner class PostOffenderAddress {
    @Test
    fun `Create - should return 401 when user does not even have token`() {
      webTestClient.post().uri(CREATE_URL).bodyValue(VALID_ADDRESS_CREATION_REQUEST).exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `Create - should return 403 if the client does not have any roles`() {
      webTestClient.post().uri(CREATE_URL).bodyValue(VALID_ADDRESS_CREATION_REQUEST)
        .headers(setClientAuthorisation(listOf())).header("Content-Type", APPLICATION_JSON_VALUE).exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `Create - should return 403 if the client does not have the ROLE_PRISON_API__PRISONER_PROFILE__RW role`() {
      webTestClient.post().uri(CREATE_URL).bodyValue(VALID_ADDRESS_CREATION_REQUEST)
        .headers(setClientAuthorisation(listOf("EXAMPLE_ROLE"))).header("Content-Type", APPLICATION_JSON_VALUE)
        .exchange().expectStatus().isForbidden
    }

    @Test
    fun `Create - should return 200 and the created number if the request is valid`() {
      webTestClient.post().uri(CREATE_URL).bodyValue(VALID_ADDRESS_CREATION_REQUEST)
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__PRISONER_PROFILE__RW")))
        .header("Content-Type", APPLICATION_JSON_VALUE).exchange().expectStatus().isOk().expectBody()
        .jsonPath(".addressId").isNotEmpty()
    }

    @Test
    fun `Create - should return 400 when required fields are missing`() {
      expectInvalidRequest(webTestClient.post(), CREATE_URL, INVALID_ADDRESS__EMPTY)
    }

    @Test
    fun `Create - should return 400 with parts of the address that are too long`() {
      expectInvalidRequest(webTestClient.post(), CREATE_URL, INVALID_ADDRESS__FLAT_TOO_LONG)
      expectInvalidRequest(webTestClient.post(), CREATE_URL, INVALID_ADDRESS__PREMISE_TOO_LONG)
      expectInvalidRequest(webTestClient.post(), CREATE_URL, INVALID_ADDRESS__STREET_TOO_LONG)
      expectInvalidRequest(webTestClient.post(), CREATE_URL, INVALID_ADDRESS__LOCALITY_TOO_LONG)
      expectInvalidRequest(webTestClient.post(), CREATE_URL, INVALID_ADDRESS__TOWN_CODE_TOO_LONG)
      expectInvalidRequest(webTestClient.post(), CREATE_URL, INVALID_ADDRESS__COUNTY_CODE_TOO_LONG)
      expectInvalidRequest(webTestClient.post(), CREATE_URL, INVALID_ADDRESS__COUNTRY_CODE_TOO_LONG)
      expectInvalidRequest(webTestClient.post(), CREATE_URL, INVALID_ADDRESS__POSTAL_CODE_TOO_LONG)
    }

    @Test
    fun `Create - should return 404 when prisoner number not found`() {
      expectNotFound(webTestClient.post(), "/api/offenders/unknown/addresses", VALID_ADDRESS_CREATION_REQUEST)
    }

    @Test
    fun `Create - should return 404 when reference codes not found`() {
      expectNotFound(webTestClient.post(), CREATE_URL, INVALID_ADDRESS__TOWN_CODE_INVALID)
      expectNotFound(webTestClient.post(), CREATE_URL, INVALID_ADDRESS__COUNTY_CODE_INVALID)
      expectNotFound(webTestClient.post(), CREATE_URL, INVALID_ADDRESS__COUNTRY_CODE_INVALID)
    }
  }

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val CREATE_URL = "/api/offenders/${PRISONER_NUMBER}/addresses"
    const val REQUIRED_FIELDS = "\"countryCode\": \"ENG\", \"startDate\": \"2021-01-01\", \"addressUsages\": []"

    const val VALID_ADDRESS_CREATION_REQUEST =
      // language=JSON
      """ 
        {
          "flat": "1",
          "premise": "The Building",
          "street": "The Street",
          "locality": "The Locality",
          "townCode": "10001",
          "countyCode": "S.YORKSHIRE",
          "countryCode": "ENG",
          "postalCode": "A1 2BC",
          "primary": true,
          "mail": true,
          "noFixedAddress": true,
          "startDate": "2021-01-01",
          "addressUsages": ["HOME"]
        } 
      """

    const val INVALID_ADDRESS__EMPTY = "{}"
    val INVALID_ADDRESS__FLAT_TOO_LONG = "{\"flat\":\"${"A".repeat(31)}\",$REQUIRED_FIELDS}"
    val INVALID_ADDRESS__PREMISE_TOO_LONG = "{\"premise\":\"${"A".repeat(51)}\",$REQUIRED_FIELDS}"
    val INVALID_ADDRESS__STREET_TOO_LONG = "{\"street\":\"${"A".repeat(161)}\",$REQUIRED_FIELDS}"
    val INVALID_ADDRESS__LOCALITY_TOO_LONG = "{\"locality\":\"${"A".repeat(71)}\",$REQUIRED_FIELDS}"
    val INVALID_ADDRESS__TOWN_CODE_TOO_LONG = "{\"townCode\":\"${"A".repeat(13)}\",$REQUIRED_FIELDS}"
    val INVALID_ADDRESS__COUNTY_CODE_TOO_LONG = "{\"countyCode\":\"${"A".repeat(13)}\",$REQUIRED_FIELDS}"
    val INVALID_ADDRESS__COUNTRY_CODE_TOO_LONG = "{\"countryCode\":\"${"A".repeat(13)}\", \"startDate\": \"2021-01-01\"}"
    val INVALID_ADDRESS__POSTAL_CODE_TOO_LONG = "{\"postalCode\":\"${"A".repeat(13)}\",$REQUIRED_FIELDS}"

    val INVALID_ADDRESS__TOWN_CODE_INVALID = "{\"townCode\":\"INVALID\",$REQUIRED_FIELDS}"
    val INVALID_ADDRESS__COUNTY_CODE_INVALID = "{\"countyCode\":\"INVALID\",$REQUIRED_FIELDS}"
    val INVALID_ADDRESS__COUNTRY_CODE_INVALID = "{\"countryCode\":\"INVALID\", \"startDate\": \"2021-01-01\", \"addressUsages\": []}"
  }
}
