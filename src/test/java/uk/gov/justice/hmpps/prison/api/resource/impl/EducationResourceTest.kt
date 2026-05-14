package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.resttestclient.postForEntity
import uk.gov.justice.hmpps.prison.api.resource.impl.AuthTokenHelper.AuthToken.GLOBAL_SEARCH
import uk.gov.justice.hmpps.prison.api.resource.impl.AuthTokenHelper.AuthToken.NORMAL_USER
import uk.gov.justice.hmpps.prison.api.resource.impl.AuthTokenHelper.AuthToken.SYSTEM_USER_READ_WRITE
import uk.gov.justice.hmpps.prison.api.resource.impl.AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA

class EducationResourceTest : ResourceTest() {
  private val offenderNumber = "G8346GA"

  @Test
  fun testShouldNotBeAbleToAccessInformation_POST() {
    val token = authTokenHelper.getToken(NORMAL_USER)
    val httpEntity = createHttpEntity(token, setOf(offenderNumber))
    val response = testRestTemplate.postForEntity<String>("/api/education/prisoners", httpEntity)
    assertThat(response.statusCode.value()).isEqualTo(403)
  }

  @Test
  fun testShouldNotBeAbleToAccessInformationAsASystemUser_POST() {
    val token = authTokenHelper.getToken(SYSTEM_USER_READ_WRITE)
    val httpEntity = createHttpEntity(token, setOf(offenderNumber))
    val response = testRestTemplate.postForEntity<String>("/api/education/prisoners", httpEntity)
    assertThat(response.statusCode.value()).isEqualTo(403)
  }

  @Test
  fun testShouldBeAbleToAccessInformationAsAGlobalSearchUser_POST() {
    val token = authTokenHelper.getToken(GLOBAL_SEARCH)
    val httpEntity = createHttpEntity(token, setOf(offenderNumber))
    val response = testRestTemplate.postForEntity<String>("/api/education/prisoners", httpEntity)
    assertThatJsonFileAndStatus(response, 200, "offender_educations.json")
  }

  @Test
  fun testShouldBeAbleToAccessInformationAsAViewPrisonerDataUser_POST() {
    val token = authTokenHelper.getToken(VIEW_PRISONER_DATA)
    val httpEntity = createHttpEntity(token, setOf(offenderNumber))
    val response = testRestTemplate.postForEntity<String>("/api/education/prisoners", httpEntity)
    assertThatJsonFileAndStatus(response, 200, "offender_educations.json")
  }
}
