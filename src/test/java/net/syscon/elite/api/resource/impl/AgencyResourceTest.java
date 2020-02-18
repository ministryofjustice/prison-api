package net.syscon.elite.api.resource.impl;

import net.syscon.elite.executablespecification.steps.AuthTokenHelper.AuthToken;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

public class AgencyResourceTest extends ResourceTest {

    @Test
    public void testCanFindAgenciesByType() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/agencies/type/INST",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThatJsonFileAndStatus(response, 200, "agencies_by_type.json");
    }

    @Test
    public void testCanFindAgenciesByTypePlusInactive() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/agencies/type/INST?activeOnly={activeOnly}",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                }, "false");

        assertThatJsonFileAndStatus(response, 200, "inactive_agencies_by_type.json");
    }

}
