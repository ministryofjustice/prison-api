package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod.GET
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.GLOBAL_SEARCH
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.NORMAL_USER
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.SYSTEM_USER_READ_WRITE
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA

class EducationResourceTest : ResourceTest() {
  private val offenderNumber = "G8346GA"

  @Test
  fun testShouldNotBeAbleToAccessInformation() {
    val token = authTokenHelper.getToken(NORMAL_USER)
    val httpEntity = createHttpEntity(token, null)
    val response = testRestTemplate.exchange(
      "/api/education/prisoner/{offenderNo}",
      GET,
      httpEntity,
      object : ParameterizedTypeReference<String>() {},
      offenderNumber,
    )
    assertThat(response.statusCode.value()).isEqualTo(403)
  }

  @Test
  fun testShouldNotBeAbleToAccessInformationAsASystemUser() {
    val token = authTokenHelper.getToken(SYSTEM_USER_READ_WRITE)
    val httpEntity = createHttpEntity(token, null)
    val response = testRestTemplate.exchange(
      "/api/education/prisoner/{offenderNo}",
      GET,
      httpEntity,
      object : ParameterizedTypeReference<String>() {},
      offenderNumber,
    )
    assertThat(response.statusCode.value()).isEqualTo(403)
  }

  @Test
  fun testShouldBeAbleToAccessInformationAsAGlobalSearchUser() {
    val token = authTokenHelper.getToken(GLOBAL_SEARCH)
    val httpEntity = createHttpEntity(token, null)
    val response = testRestTemplate.exchange(
      "/api/education/prisoner/{offenderNo}",
      GET,
      httpEntity,
      object : ParameterizedTypeReference<String>() {},
      offenderNumber,
    )
    assertThatJsonFileAndStatus(response, 200, "paged_offender_educations.json")
  }

  @Test
  fun testShouldBeAbleToAccessInformationAsAViewPrisonerDataUser() {
    val token = authTokenHelper.getToken(VIEW_PRISONER_DATA)
    val httpEntity = createHttpEntity(token, null)
    val response = testRestTemplate.exchange(
      "/api/education/prisoner/{offenderNo}",
      GET,
      httpEntity,
      object : ParameterizedTypeReference<String>() {},
      offenderNumber,
    )
    assertThatJsonFileAndStatus(response, 200, "paged_offender_educations.json")
  }

  @Test
  fun testShouldReturn404ForANonExistentOffender() {
    val token = authTokenHelper.getToken(VIEW_PRISONER_DATA)
    val httpEntity = createHttpEntity(token, null)
    val response = testRestTemplate.exchange(
      "/api/education/prisoner/{offenderNo}",
      GET,
      httpEntity,
      object : ParameterizedTypeReference<String>() {},
      "non_existent_nomisid",
    )
    assertThat(response.statusCode.value()).isEqualTo(404)
  }

  @Test
  fun testShouldNotBeAbleToAccessInformation_POST() {
    val token = authTokenHelper.getToken(NORMAL_USER)
    val httpEntity = createHttpEntity(token, setOf(offenderNumber))
    val response = testRestTemplate.postForEntity("/api/education/prisoners", httpEntity, String::class.java)
    assertThat(response.statusCode.value()).isEqualTo(403)
  }

  @Test
  fun testShouldNotBeAbleToAccessInformationAsASystemUser_POST() {
    val token = authTokenHelper.getToken(SYSTEM_USER_READ_WRITE)
    val httpEntity = createHttpEntity(token, setOf(offenderNumber))
    val response = testRestTemplate.postForEntity("/api/education/prisoners", httpEntity, String::class.java)
    assertThat(response.statusCode.value()).isEqualTo(403)
  }

  @Test
  fun testShouldBeAbleToAccessInformationAsAGlobalSearchUser_POST() {
    val token = authTokenHelper.getToken(GLOBAL_SEARCH)
    val httpEntity = createHttpEntity(token, setOf(offenderNumber))
    val response = testRestTemplate.postForEntity("/api/education/prisoners", httpEntity, String::class.java)
    assertThatJsonFileAndStatus(response, 200, "offender_educations.json")
  }

  @Test
  fun testShouldBeAbleToAccessInformationAsAViewPrisonerDataUser_POST() {
    val token = authTokenHelper.getToken(VIEW_PRISONER_DATA)
    val httpEntity = createHttpEntity(token, setOf(offenderNumber))
    val response = testRestTemplate.postForEntity("/api/education/prisoners", httpEntity, String::class.java)
    assertThatJsonFileAndStatus(response, 200, "offender_educations.json")
  }
}
