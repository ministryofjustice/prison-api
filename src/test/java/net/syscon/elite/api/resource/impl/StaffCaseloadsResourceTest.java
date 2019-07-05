package net.syscon.elite.api.resource.impl;

import net.syscon.elite.executablespecification.steps.AuthTokenHelper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import static org.assertj.core.api.Assertions.assertThat;

public class StaffCaseloadsResourceTest extends ResourceTest {

    @Autowired
    private AuthTokenHelper authTokenHelper;

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

        assertThat(response.getBody()).isEqualTo("[{\"caseLoadId\":\"BXI\",\"description\":\"Brixton (HMP)\",\"type\":\"INST\",\"caseloadFunction\":\"GENERAL\",\"currentlyActive\":false},{\"caseLoadId\":\"LEI\",\"description\":\"Leeds (HMP)\",\"type\":\"INST\",\"caseloadFunction\":\"GENERAL\",\"currentlyActive\":true},{\"caseLoadId\":\"MDI\",\"description\":\"Moorland Closed (HMP & YOI)\",\"type\":\"INST\",\"caseloadFunction\":\"GENERAL\",\"currentlyActive\":false},{\"caseLoadId\":\"NWEB\",\"description\":\"Nomis-web Application\",\"type\":\"APP\",\"caseloadFunction\":\"GENERAL\",\"currentlyActive\":false},{\"caseLoadId\":\"SYI\",\"description\":\"Shrewsbury (HMP)\",\"type\":\"INST\",\"caseloadFunction\":\"GENERAL\",\"currentlyActive\":false},{\"caseLoadId\":\"WAI\",\"description\":\"The Weare (HMP)\",\"type\":\"INST\",\"caseloadFunction\":\"GENERAL\",\"currentlyActive\":false}]");
    }
}
