package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken

class PrisonersResourceTest : ResourceTest() {
  @Test
  fun testCanFindMultiplePrisonersUsingPost() {
    val token = authTokenHelper.getToken(AuthToken.GLOBAL_SEARCH)
    val httpEntity = createHttpEntity(token, "{ \"offenderNos\": [ \"A1234AC\", \"A1234AA\" ] }")
    val response = testRestTemplate.exchange(
      "/api/prisoners",
      HttpMethod.POST,
      httpEntity,
      object : ParameterizedTypeReference<String?>() {},
    )
    assertThatJsonFileAndStatus(response, HttpStatus.OK, "prisoners_multiple.json")
  }

  @Test
  fun testCanFindMultiplePrisonersAndFilterByMoreThanOneCriteria() {
    val token = authTokenHelper.getToken(AuthToken.GLOBAL_SEARCH)
    val httpEntity = createHttpEntity(token, "{ \"offenderNos\": [ \"A1181MV\", \"A1234AC\", \"A1234AA\" ], \"lastName\": \"BATES\" }")
    val response = testRestTemplate.exchange(
      "/api/prisoners",
      HttpMethod.POST,
      httpEntity,
      object : ParameterizedTypeReference<String?>() {},
    )
    assertThatJsonFileAndStatus(response, HttpStatus.OK, "prisoners_single.json")
  }

  @Test
  fun testCanReturnPrisonerInformationAtLocation() {
    val token = authTokenHelper.getToken(AuthToken.GLOBAL_SEARCH)
    val httpEntity = createHttpEntity(token, null, mapOf("Page-Limit" to "30"))
    val response = testRestTemplate.exchange(
      "/api/prisoners/at-location/LEI",
      HttpMethod.GET,
      httpEntity,
      object : ParameterizedTypeReference<String?>() {},
    )
    assertThatJsonFileAndStatus(response, HttpStatus.OK, "prisoners_information.json")
  }

  @Test
  fun testCanReturnPrisonerInformationByEstablishmentWithSort() {
    val token = authTokenHelper.getToken(AuthToken.GLOBAL_SEARCH)
    val httpEntity = createHttpEntity(token, null, emptyMap())
    val response = testRestTemplate.exchange(
      "/api/prisoners/by-establishment/LEI?size=5&page=2&sort=lastName,asc&sort=givenName1,asc",
      HttpMethod.GET,
      httpEntity,
      object : ParameterizedTypeReference<String?>() {},
    )
    assertThatJsonFileAndStatus(response, HttpStatus.OK, "prisoners_information_paged.json")
  }

  @Test
  fun testCanReturnPrisonerInformationByNomsId() {
    val token = authTokenHelper.getToken(AuthToken.VIEW_PRISONER_DATA)
    val httpEntity = createHttpEntity(token, null, emptyMap())
    val response = testRestTemplate.exchange(
      "/api/prisoners/A1234AA/full-status",
      HttpMethod.GET,
      httpEntity,
      object : ParameterizedTypeReference<String?>() {},
    )
    assertThatJsonFileAndStatus(response, HttpStatus.OK, "prisoners_information_A1234AA.json")
  }

  @Test
  fun testCanReturnPrisonerInformationByNomsIdWhenReleased() {
    val token = authTokenHelper.getToken(AuthToken.VIEW_PRISONER_DATA)
    val httpEntity = createHttpEntity(token, null, emptyMap())
    val response = testRestTemplate.exchange(
      "/api/prisoners/Z0023ZZ/full-status",
      HttpMethod.GET,
      httpEntity,
      object : ParameterizedTypeReference<String?>() {},
    )
    assertThatJsonFileAndStatus(response, HttpStatus.OK, "prisoners_information_Z0023ZZ.json")
  }

  @Test
  fun testReturn404WhenOffenderNotFound() {
    val token = authTokenHelper.getToken(AuthToken.VIEW_PRISONER_DATA)
    val httpEntity = createHttpEntity(token, null, emptyMap())
    val response = testRestTemplate.exchange(
      "/api/prisoners/X1111XX/full-status",
      HttpMethod.GET,
      httpEntity,
      object : ParameterizedTypeReference<String?>() {},
    )
    assertThatStatus(response, HttpStatus.NOT_FOUND)
  }

  @Test
  fun testReturn404WhenDoesNotHavePrivs() {
    val token = authTokenHelper.getToken(AuthToken.NO_CASELOAD_USER)
    val httpEntity = createHttpEntity(token, null, emptyMap())
    val response = testRestTemplate.exchange(
      "/api/prisoners/A1234AA/full-status",
      HttpMethod.GET,
      httpEntity,
      object : ParameterizedTypeReference<String?>() {},
    )
    assertThatStatus(response, HttpStatus.NOT_FOUND)
  }
}
