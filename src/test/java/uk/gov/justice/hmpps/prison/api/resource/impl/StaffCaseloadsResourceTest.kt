package uk.gov.justice.hmpps.prison.api.resource.impl

import net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod.GET
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.NORMAL_USER

@DisplayName("/api/staff/{staffId}/caseloads")
class StaffCaseloadsResourceTest : ResourceTest() {

  @Test
  fun testCanRetrieveCaseloadForNonExistentStaffMember() {
    val token = authTokenHelper.getToken(NORMAL_USER)
    val httpEntity = createHttpEntity(token, null)
    val response = testRestTemplate.exchange(
      "/api/staff/{staffId}/caseloads",
      GET,
      httpEntity,
      object : ParameterizedTypeReference<String?>() {},
      10,
    )
    assertThat(response.statusCode.value()).isEqualTo(404)
  }

  @Test
  fun testCanRetrieveCaseloadForAStaffMember() {
    val token = authTokenHelper.getToken(NORMAL_USER)
    val httpEntity = createHttpEntity(token, null)
    val response = testRestTemplate.exchange(
      "/api/staff/{staffId}/caseloads",
      GET,
      httpEntity,
      object : ParameterizedTypeReference<String?>() {},
      -2,
    )
    assertThat(response.statusCode.value()).isEqualTo(200)
    assertThatJson(response.body).isEqualTo(
      "[" +
        "{caseLoadId:\"BXI\",description:\"Brixton (HMP)\",type:\"INST\",caseloadFunction:\"GENERAL\",currentlyActive:false}," +
        "{caseLoadId:\"LEI\",description:\"Leeds (HMP)\",type:\"INST\",caseloadFunction:\"GENERAL\",currentlyActive:true}," +
        "{caseLoadId:\"MDI\",description:\"Moorland Closed (HMP & YOI)\",type:\"INST\",caseloadFunction:\"GENERAL\",currentlyActive:false}," +
        "{caseLoadId:\"NWEB\",description:\"Nomis-web Application\",type:\"APP\",caseloadFunction:\"GENERAL\",currentlyActive:false}," +
        "{caseLoadId:\"RNI\",description:\"Ranby (HMP)\",type:\"INST\",caseloadFunction:\"GENERAL\",currentlyActive:false}," +
        "{caseLoadId:\"SYI\",description:\"Shrewsbury (HMP)\",type:\"INST\",caseloadFunction:\"GENERAL\",currentlyActive:false}," +
        "{caseLoadId:\"WAI\",description:\"The Weare (HMP)\",type:\"INST\",caseloadFunction:\"GENERAL\",currentlyActive:false}]",
    )
  }

  @Test
  fun testCanRetrieveCaseloadForStaffWithNoCaseloads() {
    val token = authTokenHelper.getToken(NORMAL_USER)
    val httpEntity = createHttpEntity(token, null)
    val response = testRestTemplate.exchange(
      "/api/staff/{staffId}/caseloads",
      GET,
      httpEntity,
      object : ParameterizedTypeReference<String?>() {},
      -10,
    )
    assertThat(response.statusCode.value()).isEqualTo(204)
  }
}
