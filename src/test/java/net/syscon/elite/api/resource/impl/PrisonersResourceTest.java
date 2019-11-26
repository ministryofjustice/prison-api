package net.syscon.elite.api.resource.impl;

import net.syscon.elite.executablespecification.steps.AuthTokenHelper.AuthToken;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import java.util.Map;

public class PrisonersResourceTest extends ResourceTest {
    @Test
    public void testCanFindMultiplePrisonersUsingPost() {
        final var token = authTokenHelper.getToken(AuthToken.GLOBAL_SEARCH);

        final var httpEntity = createHttpEntity(token, "{ \"offenderNos\": [ \"A1181MV\", \"A1234AC\", \"A1234AA\" ] }");

        final var response = testRestTemplate.exchange(
                "/api/prisoners",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThatJsonFileAndStatus(response, 200, "prisoners_multiple.json");
    }

    @Test
    public void testCanFindMulitplePrisonersAndFilterByMoreThanOneCriteria() {
        final var token = authTokenHelper.getToken(AuthToken.GLOBAL_SEARCH);

        final var httpEntity = createHttpEntity(token, "{ \"offenderNos\": [ \"A1181MV\", \"A1234AC\", \"A1234AA\" ], \"lastName\": \"BATES\" }");

        final var response = testRestTemplate.exchange(
                "/api/prisoners",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThatJsonFileAndStatus(response, 200, "prisoners_single.json");
    }

    @Test
    public void testCanReturnPrisonerInformationAtLocation() {
        final var token = authTokenHelper.getToken(AuthToken.GLOBAL_SEARCH);

        final var httpEntity = createHttpEntity(token, null, Map.of("Page-Limit", "100"));

        final var response = testRestTemplate.exchange(
                "/api/prisoners/at-location/LEI",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThatJsonFileAndStatus(response, 200, "prisoners_information.json");
    }

}
