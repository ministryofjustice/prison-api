package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;

import java.util.Map;

public class PrisonersResourceTest extends ResourceTest {
    @Test
    public void testCanFindMultiplePrisonersUsingPost() {
        final var token = authTokenHelper.getToken(AuthToken.GLOBAL_SEARCH);

        final var httpEntity = createHttpEntity(token, "{ \"offenderNos\": [ \"A1234AC\", \"A1234AA\" ] }");

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
    public void testCanReturnPrisonerInformationByNomsId() {
        final var token = authTokenHelper.getToken(AuthToken.VIEW_PRISONER_DATA);

        final var httpEntity = createHttpEntity(token, null, Map.of());

        final var response = testRestTemplate.exchange(
                "/api/prisoners/A1234AA/full-status",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThatJsonFileAndStatus(response, 200, "prisoners_information_A1234AA.json");
    }

    @Test
    public void testCanReturnPrisonerInformationByNomsIdWhenReleased() {
        final var token = authTokenHelper.getToken(AuthToken.VIEW_PRISONER_DATA);

        final var httpEntity = createHttpEntity(token, null, Map.of());

        final var response = testRestTemplate.exchange(
                "/api/prisoners/Z0023ZZ/full-status",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThatJsonFileAndStatus(response, 200, "prisoners_information_Z0023ZZ.json");
    }

    @Test
    public void testReturn404WhenOffenderNotFound() {
        final var token = authTokenHelper.getToken(AuthToken.VIEW_PRISONER_DATA);

        final var httpEntity = createHttpEntity(token, null, Map.of());

        final var response = testRestTemplate.exchange(
                "/api/prisoners/X1111XX/full-status",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThatStatus(response, HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void testReturn404WhenDoesNotHavePrivs() {
        final var token = authTokenHelper.getToken(AuthToken.NO_CASELOAD_USER);

        final var httpEntity = createHttpEntity(token, null, Map.of());

        final var response = testRestTemplate.exchange(
                "/api/prisoners/A1234AA/full-status",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThatStatus(response, HttpStatus.NOT_FOUND.value());
    }

}
