package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.PrisonerDetail;
import net.syscon.elite.api.model.v1.Events;
import net.syscon.elite.executablespecification.steps.AuthTokenHelper;
import net.syscon.elite.executablespecification.steps.AuthTokenHelper.AuthToken;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.core.ResolvableType.forType;

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

}
