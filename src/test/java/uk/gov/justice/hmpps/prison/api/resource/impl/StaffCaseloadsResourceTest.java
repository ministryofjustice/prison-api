package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

public class StaffCaseloadsResourceTest extends ResourceTest {
    @Test
    public void testCanRetrieveCaseloadForAStaffMember() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/staff/{staffId}/caseloads",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                },
                -2);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);

        assertThatJson(response.getBody()).isEqualTo("[" +
                "{caseLoadId:\"BXI\",description:\"Brixton (HMP)\",type:\"INST\",caseloadFunction:\"GENERAL\",currentlyActive:false}," +
                "{caseLoadId:\"LEI\",description:\"Leeds (HMP)\",type:\"INST\",caseloadFunction:\"GENERAL\",currentlyActive:true}," +
                "{caseLoadId:\"MDI\",description:\"Moorland Closed (HMP & YOI)\",type:\"INST\",caseloadFunction:\"GENERAL\",currentlyActive:false}," +
                "{caseLoadId:\"NWEB\",description:\"Nomis-web Application\",type:\"APP\",caseloadFunction:\"GENERAL\",currentlyActive:false}," +
                "{caseLoadId:\"RNI\",description:\"Ranby (HMP)\",type:\"INST\",caseloadFunction:\"GENERAL\",currentlyActive:false}," +
                "{caseLoadId:\"SYI\",description:\"Shrewsbury (HMP)\",type:\"INST\",caseloadFunction:\"GENERAL\",currentlyActive:false}," +
                "{caseLoadId:\"WAI\",description:\"The Weare (HMP)\",type:\"INST\",caseloadFunction:\"GENERAL\",currentlyActive:false}]");
    }

    @Test
    public void testCanRetrieveCaseloadForNonExistentStaffMember() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/staff/{staffId}/caseloads",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                },
                10);

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    public void testCanRetrieveCaseloadForStaffWithNoCaseloads() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/staff/{staffId}/caseloads",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                },
                -10);

        assertThat(response.getStatusCodeValue()).isEqualTo(204);
    }

}
