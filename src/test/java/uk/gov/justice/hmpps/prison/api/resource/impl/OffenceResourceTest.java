package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.hmpps.prison.api.model.HOCodeDto;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;

import static java.lang.String.format;

public class OffenceResourceTest extends ResourceTest {
    @Test
    public void testCanRetrieveAPageOfOffences() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/offences",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThatJsonFileAndStatus(response, 200, "paged_offences.json");
    }

    @Test
    public void testCanRetrieveAPageOfAllOffences() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/offences/all?size=20",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(response, 200, "paged_all_offences.json");
    }

    @Test
    public void testCanRetrieveAPageOfOffencesByHOCode() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/offences/ho-code?code=823/02",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(response, 200, "paged_ho_code_offences.json");
    }

    @Test
    public void testCanRetrieveAPageOfOffencesByStatuteCode() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/offences/statute?code=RV98",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(response, 200, "paged_statute_offences.json");
    }

    @Test
    public void testCanRetrieveAPageOfOffencesDescription() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
            "/api/offences/search?searchText=vehicle",
            HttpMethod.GET,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });

        assertThatJsonFileAndStatus(response, 200, "paged_searched_for_offences.json");
    }

    @Test
    public void testWriteHoCode() {
        final var token = authTokenHelper.getToken(AuthToken.OFFENCE_MAINTAINER);

        final var body = HOCodeDto
            .builder()
            .code("123/45")
            .description("123/45")
            .build();
        final var httpEntity = createHttpEntity(token, body);

        final var response = createHoCode(httpEntity);

        assertThatStatus(response, 201);
    }

    @Test
    public void testDuplicateWriteHoCode() {
        final var token = authTokenHelper.getToken(AuthToken.OFFENCE_MAINTAINER);

        final var body = HOCodeDto
            .builder()
            .code("123/99")
            .description("123/99")
            .build();
        final var httpEntity = createHttpEntity(token, body);

        final var response = createHoCode(httpEntity);
        assertThatStatus(response, 201);
        final var duplicateResponse = createHoCode(httpEntity);
        assertThatStatus(duplicateResponse, 409);
    }


    private ResponseEntity<String> createHoCode(HttpEntity<?> httpEntity) {
        return testRestTemplate.exchange(
            "/api/offences/ho-code",
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<String>() {
            });
    }
}
