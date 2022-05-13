package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode;
import uk.gov.justice.hmpps.prison.api.model.HOCodeDto;
import uk.gov.justice.hmpps.prison.api.model.OffenceDto;
import uk.gov.justice.hmpps.prison.api.model.StatuteDto;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;

import java.time.LocalDate;

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
        private final String maintainerToken = authTokenHelper.getToken(AuthToken.OFFENCE_MAINTAINER);

        @Test
        public void testWriteHoCode() {
            final var hoCodeDto = HOCodeDto
                .builder()
                .code("123/45")
                .description("123/45")
                .build();
            final var httpEntity = createHttpEntity(maintainerToken, hoCodeDto);

            final var response = postRequest(httpEntity, "/api/offences/ho-code");

            assertThatStatus(response, 201);
        }

        @Test
        public void testDuplicateWriteHoCode() {
            final var hoCodeDto = HOCodeDto
                .builder()
                .code("123/99")
                .description("123/99")
                .build();
            final var httpEntity = createHttpEntity(maintainerToken, hoCodeDto);

            final var response = postRequest(httpEntity, "/api/offences/ho-code");
            assertThatStatus(response, 201);
            final var duplicateResponse = postRequest(httpEntity, "/api/offences/ho-code");
            assertThatStatus(duplicateResponse, 409);
        }
    }

    @Nested
    @DisplayName("Tests creation of Statutes")
    public class CreateStatutesTests {
        private final String maintainerToken = authTokenHelper.getToken(AuthToken.OFFENCE_MAINTAINER);

        @Test
        public void testWriteStatute() {
            final var statuteDto = StatuteDto
                .builder()
                .code("123/45")
                .description("123/45")
                .legislatingBodyCode("UK")
                .build();
            final var httpEntity = createHttpEntity(maintainerToken, statuteDto);

            final var response = postRequest(httpEntity, "/api/offences/statute");

            assertThatStatus(response, 201);
        }

        @Test
        public void testDuplicateWriteStatute() {
            final var statuteDto = StatuteDto
                .builder()
                .code("123/99")
                .description("123/99")
                .legislatingBodyCode("UK")
                .build();
            final var httpEntity = createHttpEntity(maintainerToken, statuteDto);

            final var response = postRequest(httpEntity, "/api/offences/statute");
            assertThatStatus(response, 201);
            final var duplicateResponse = postRequest(httpEntity, "/api/offences/statute");
            assertThatStatus(duplicateResponse, 409);
        }
    }

    @Nested
    @DisplayName("Tests creation and  update of an offence")
    public class CreateOrUpdateOffenceTests {
        private final String maintainerToken = authTokenHelper.getToken(AuthToken.OFFENCE_MAINTAINER);
        private final StatuteDto statuteDto = StatuteDto
            .builder()
            .code("9235")
            .description("9235")
            .legislatingBodyCode("UK")
            .activeFlag("Y")
            .build();

        private final HOCodeDto hoCodeDto = HOCodeDto
            .builder()
            .code("923/99")
            .description("923/99")
            .activeFlag("Y")
            .build();

        private final OffenceDto offenceDto = OffenceDto.builder()
            .code("2XX")
            .statuteCode(statuteDto)
            .hoCode(hoCodeDto)
            .description("2XX Description")
            .severityRanking("58")
            .activeFlag("Y")
            .build();

        @Sql(scripts = {"/sql/clean_offences.sql"},
            executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
            config = @SqlConfig(transactionMode = TransactionMode.ISOLATED))
        @Test
        public void testCreateOffence() {
            final var statuteHttpEntity = createHttpEntity(maintainerToken, statuteDto);
            final var offenceHttpEntity = createHttpEntity(maintainerToken, offenceDto);
            final var hoCodeEntity = createHttpEntity(maintainerToken, hoCodeDto);
            postRequest(statuteHttpEntity, "/api/offences/statute");
            postRequest(hoCodeEntity, "/api/offences/ho-code");

            final var response = postRequest(offenceHttpEntity, "/api/offences/offence");
            assertThatStatus(response, 201);

            final var getResponse = testRestTemplate.exchange(
                "/api/offences/statute?code=9235",
                HttpMethod.GET,
                createHttpEntity(maintainerToken, null),
                new ParameterizedTypeReference<String>() {
                });

            assertThatJsonFileAndStatus(getResponse, 200, "offence_after_create.json");
        }

        @Sql(scripts = {"/sql/clean_offences.sql"},
            executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
            config = @SqlConfig(transactionMode = TransactionMode.ISOLATED))
        @Test
        public void testUpdateOffence() {
            final OffenceDto offenceDto = OffenceDto.builder()
                .code("M5")
                .statuteCode(StatuteDto.builder().code("RC86").build())
                .hoCode(HOCodeDto.builder().code("815/90").build())
                .description("Manslaughter Old UPDATED")
                .severityRanking("700")
                .activeFlag("N")
                .expiryDate(LocalDate.of(2020, 10, 13))
                .build();
            final var offenceHttpEntity = createHttpEntity(maintainerToken, offenceDto);

            final var response = putRequest(offenceHttpEntity);
            assertThatStatus(response, 204);

            final var getResponse = testRestTemplate.exchange(
                "/api/offences/search?searchText=UPDATED",
                HttpMethod.GET,
                createHttpEntity(maintainerToken, null),
                new ParameterizedTypeReference<String>() {
                });

            assertThatJsonFileAndStatus(getResponse, 200, "offence_after_update.json");
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

    private ResponseEntity<String> putRequest(HttpEntity<?> httpEntity) {
        return testRestTemplate.exchange(
            "/api/offences/offence",
            HttpMethod.PUT,
            httpEntity,
            new ParameterizedTypeReference<>() {
            });
    }
}
