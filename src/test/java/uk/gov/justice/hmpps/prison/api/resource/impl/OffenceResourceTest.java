package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.hmpps.prison.api.model.HOCodeDto;
import uk.gov.justice.hmpps.prison.api.model.StatuteDto;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;

import static java.lang.String.format;

public class OffenceResourceTest extends ResourceTest {

    @Nested
    @DisplayName("Tests for all the GET end points")
    public class GeneralOffencesTests {
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
    }

    @Nested
    @DisplayName("Tests creation of HO Codes")
    public class CreateHOCodesTests {
        @Test
        public void testWriteHoCode() {
            final var token = authTokenHelper.getToken(AuthToken.OFFENCE_MAINTAINER);

            final var hoCodeDto = HOCodeDto
                .builder()
                .code("123/45")
                .description("123/45")
                .build();
            final var httpEntity = createHttpEntity(token, hoCodeDto);

            final var response = postRequest(httpEntity, "/api/offences/ho-code");

            assertThatStatus(response, 201);
        }

        @Test
        public void testDuplicateWriteHoCode() {
            final var token = authTokenHelper.getToken(AuthToken.OFFENCE_MAINTAINER);

            final var hoCodeDto = HOCodeDto
                .builder()
                .code("123/99")
                .description("123/99")
                .build();
            final var httpEntity = createHttpEntity(token, hoCodeDto);

            final var response = postRequest(httpEntity, "/api/offences/ho-code");
            assertThatStatus(response, 201);
            final var duplicateResponse = postRequest(httpEntity, "/api/offences/ho-code");
            assertThatStatus(duplicateResponse, 409);
        }
    }

    @Nested
    @DisplayName("Tests creation of Statutes")
    public class CreateStatutesTests {
        @Test
        public void testWriteStatute() {
            final var token = authTokenHelper.getToken(AuthToken.OFFENCE_MAINTAINER);

            final var statuteDto = StatuteDto
                .builder()
                .code("123/45")
                .description("123/45")
                .legislatingBodyCode("UK")
                .build();
            final var httpEntity = createHttpEntity(token, statuteDto);

            final var response = postRequest(httpEntity, "/api/offences/statute");

            assertThatStatus(response, 201);
        }

        @Test
        public void testDuplicateWriteStatute() {
            final var token = authTokenHelper.getToken(AuthToken.OFFENCE_MAINTAINER);

            final var statuteDto = StatuteDto
                .builder()
                .code("123/99")
                .description("123/99")
                .legislatingBodyCode("UK")
                .build();
            final var httpEntity = createHttpEntity(token, statuteDto);

            final var response = postRequest(httpEntity, "/api/offences/statute");
            assertThatStatus(response, 201);
            final var duplicateResponse = postRequest(httpEntity, "/api/offences/statute");
            assertThatStatus(duplicateResponse, 409);
        }
    }

    private ResponseEntity<String> postRequest(HttpEntity<?> httpEntity, String url) {
        return testRestTemplate.exchange(
            url,
            HttpMethod.POST,
            httpEntity,
            new ParameterizedTypeReference<>() {
            });
    }
}
