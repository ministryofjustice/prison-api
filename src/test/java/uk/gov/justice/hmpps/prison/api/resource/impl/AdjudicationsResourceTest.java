package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.jdbc.SqlConfig.TransactionMode;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicHearingResult;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicHearingResult.FindingCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OicHearingResult.PleaFindingCode;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = {"/sql/adjudicationHistorySort_init.sql"},
    executionPhase = ExecutionPhase.BEFORE_TEST_METHOD,
    config = @SqlConfig(transactionMode = TransactionMode.ISOLATED))
@Sql(scripts = {"/sql/adjudicationHistorySort_clean.sql"},
    executionPhase = ExecutionPhase.AFTER_TEST_METHOD,
    config = @SqlConfig(transactionMode = TransactionMode.ISOLATED))
public class AdjudicationsResourceTest extends ResourceTest  {

    @Nested
    public class RequestAdjudicationCreationData {

        @Test
        public void returnsExpectedValue() {
            final var token = validToken(List.of("ROLE_MAINTAIN_ADJUDICATIONS"));
            final var body = "A1234AE";

            final var httpEntity = createHttpEntity(token, body);

            final var response = testRestTemplate.exchange(
                "/api/adjudications/adjudication/request-creation-data",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

            // Note we cannot check the adj number as it's value is dependent on other tests
            assertThatJsonFileAndStatus(response, 201, "new_adjudication_request.json");
        }

        @Test
        public void returns404IfInvalidOffenderNo() {
            final var token = validToken(List.of("ROLE_MAINTAIN_ADJUDICATIONS"));
            final var body = "INVALID_OFF_NO";

            final var httpEntity = createHttpEntity(token, body);

            final var response = testRestTemplate.exchange(
                "/api/adjudications/adjudication/request-creation-data",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

            assertThatStatus(response, 404);
        }

        @Test
        public void returns403IfInvalidRole() {
            final var token = validToken(List.of("ROLE_SYSTEM_USER"));
            final var body = "INVALID_OFF_NO";

            final var httpEntity = createHttpEntity(token, body);

            final var response = testRestTemplate.exchange(
                "/api/adjudications/adjudication/request-creation-data",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

            assertThatStatus(response, 403);
        }
    }

    @Nested
    public class CreateAdjudication {

        @Test
        public void returnsExpectedValue() {
            final var token = validToken(List.of("ROLE_MAINTAIN_ADJUDICATIONS"));
            final var body = Map.of(
                "offenderNo", "A1234AE",
                "adjudicationNumber", 1234567,
                "bookingId", -5L,
                "reporterName", "ITAG_USER",
                "reportedDateTime", "2021-01-04T09:12:44",
                "agencyId", "MDI",
                "incidentTime", "2021-01-04T10:12:44",
                "incidentLocationId", -31L,
                "statement", "Example statement");

            final var httpEntity = createHttpEntity(token, body);

            final var response = testRestTemplate.exchange(
                "/api/adjudications/adjudication",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

            assertThatJsonFileAndStatus(response, 201, "new_adjudication.json");
        }

        @Test
        public void returnsExpectedValue_WithOptionalData() {
            final var token = validToken(List.of("ROLE_MAINTAIN_ADJUDICATIONS"));
            final var body = Map.of(
                "offenderNo", "A1234AE",
                "adjudicationNumber", 1234,
                "bookingId", -5L,
                "reporterName", "ITAG_USER",
                "reportedDateTime", "2021-01-04T09:12:44",
                "agencyId", "MDI",
                "incidentTime", "2021-01-04T10:12:44",
                "incidentLocationId", -31L,
                "statement", "Example statement",
                "offenceCodes", List.of("51:8D"));

            final var httpEntity = createHttpEntity(token, body);

            final var response = testRestTemplate.exchange(
                "/api/adjudications/adjudication",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

            assertThatJsonFileAndStatus(response, 201, "new_adjudication_with_optional_data.json");
        }

        @Test
        public void returns400IfInvalidRequest() {
            final var token = validToken(List.of("ROLE_MAINTAIN_ADJUDICATIONS"));
            final var body = Map.of(
                "bookingId", -5L,
                "incidentLocationId", -31L,
                "statement", "Example statement");

            final var httpEntity = createHttpEntity(token, body);

            final var response = testRestTemplate.exchange(
                "/api/adjudications/adjudication",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

            assertThatStatus(response, 400);
        }

        @Test
        public void returns404IfInvalidBooking() {
            final var token = validToken(List.of("ROLE_MAINTAIN_ADJUDICATIONS"));
            final var body = Map.of(
                "offenderNo", "Z1234ZZ",
                "adjudicationNumber", 1234,
                "bookingId", -5L,
                "reporterName", "ITAG_USER",
                "reportedDateTime", "2021-01-04T09:12:44",
                "agencyId", "MDI",
                "incidentTime", "2021-01-04T10:12:44",
                "incidentLocationId", -31L,
                "statement", "Example statement");

            final var httpEntity = createHttpEntity(token, body);

            final var response = testRestTemplate.exchange(
                "/api/adjudications/adjudication",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

            assertThatStatus(response, 404);
        }

        @Test
        public void returns403IfInvalidRole() {
            final var token = validToken(List.of("ROLE_SYSTEM_USER"));
            final var body = Map.of(
                "offenderNo", "Z1234ZZ",
                "adjudicationNumber", 1234,
                "bookingId", -5L,
                "reporterName", "ITAG_USER",
                "reportedDateTime", "2021-01-04T09:12:44",
                "agencyId", "MDI",
                "incidentTime", "2021-01-04T10:12:44",
                "incidentLocationId", -31L,
                "statement", "Example statement");

            final var httpEntity = createHttpEntity(token, body);

            final var response = testRestTemplate.exchange(
                "/api/adjudications/adjudication",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

            assertThatStatus(response, 403);
        }
    }

    @Nested
    public class UpdateAdjudication {

        @Test
        public void returnsExpectedValue() {
            final var token = validToken(List.of("ROLE_MAINTAIN_ADJUDICATIONS"));
            final var body = Map.of(
                "incidentTime", "2021-01-04T10:12:44",
                "incidentLocationId", -31L,
                "statement", "Some Adjusted Comment Text"); // Note that the "Text" is used in free text searches

            final var httpEntity = createHttpEntity(token, body);

            final var response = testRestTemplate.exchange(
                "/api/adjudications/adjudication/-9",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

            assertThatJsonFileAndStatus(response, 201, "update_adjudication.json");
        }

        @Test
        public void returnsExpectedValue_WithOptionalData() {
            final var token = validToken(List.of("ROLE_MAINTAIN_ADJUDICATIONS"));
            final var body = Map.of(
                "incidentTime", "2021-01-04T10:12:44",
                "incidentLocationId", -31L,
                "statement", "Some Adjusted Comment Text",  // Note that the "Text" is used in free text searches
                "offenceCodes", List.of("51:1B"));

            final var httpEntity = createHttpEntity(token, body);

            final var response = testRestTemplate.exchange(
                "/api/adjudications/adjudication/-9",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

            assertThatJsonFileAndStatus(response, 201, "update_adjudication_with_optional_data.json");
        }

        @Test
        public void returns400IfInvalidRequest() {
            final var token = validToken(List.of("ROLE_MAINTAIN_ADJUDICATIONS"));
            final var body = Map.of(
                "incidentLocationId", -31L,
                "statement", "Example statement");

            final var httpEntity = createHttpEntity(token, body);

            final var response = testRestTemplate.exchange(
                "/api/adjudications/adjudication/-5",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

            assertThatStatus(response, 400);
        }

        @Test
        public void returns404IfInvalidAdjudicationNumber() {
            final var token = validToken(List.of("ROLE_MAINTAIN_ADJUDICATIONS"));
            final var body = Map.of(
                "incidentTime", "2021-01-04T10:12:44",
                "incidentLocationId", -31L,
                "statement", "Example statement");

            final var httpEntity = createHttpEntity(token, body);

            final var response = testRestTemplate.exchange(
                "/api/adjudications/adjudication/99",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

            assertThatStatus(response, 404);
        }

        @Test
        public void returns403IfInvalidRole() {
            final var token = validToken(List.of("ROLE_SYSTEM_USER"));
            final var body = Map.of(
                "incidentTime", "2021-01-04T10:12:44",
                "incidentLocationId", -31L,
                "statement", "Example statement");

            final var httpEntity = createHttpEntity(token, body);

            final var response = testRestTemplate.exchange(
                "/api/adjudications/adjudication/-5",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

            assertThatStatus(response, 403);
        }
    }

    @Nested
    public class GetAdjudication {

        @Test
        public void returnsExpectedValue() {
            final var token = validToken(List.of("ROLE_MAINTAIN_ADJUDICATIONS"));

            final var httpEntity = createHttpEntity(token, null);

            final var response = testRestTemplate.exchange(
                "/api/adjudications/adjudication/-5",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

            assertThatJsonFileAndStatus(response, 200, "adjudication_by_number.json");
        }

        @Test
        public void returnsExpectedValue_WithOptionalData() {
            final var token = validToken(List.of("ROLE_MAINTAIN_ADJUDICATIONS"));

            final var httpEntity = createHttpEntity(token, null);

            final var response = testRestTemplate.exchange(
                "/api/adjudications/adjudication/-1",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

            assertThatJsonFileAndStatus(response, 200, "adjudication_by_number_with_optional_data.json");
        }

        @Test
        public void returns404IfInvalidRequest() {
            final var token = validToken(List.of("ROLE_MAINTAIN_ADJUDICATIONS"));

            final var httpEntity = createHttpEntity(token, null);

            final var response = testRestTemplate.exchange(
                "/api/adjudications/adjudication/-199",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

            assertThatStatus(response, 404);
        }

        @Test
        public void returns403IfInvalidRole() {
            final var token = validToken(List.of("ROLE_SYSTEM_USER"));

            final var httpEntity = createHttpEntity(token, null);

            final var response = testRestTemplate.exchange(
                "/api/adjudications/adjudication/-199",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

            assertThatStatus(response, 403);
        }
    }

    @Nested
    public class GetAdjudications {
        @Test
        public void returnsExpectedValue() {
            final var token = validToken(List.of("ROLE_MAINTAIN_ADJUDICATIONS"));
            final var httpEntity = createHttpEntity(token, List.of(-5, -7, -200));

            final var response = testRestTemplate.exchange(
                "/api/adjudications",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

            assertThatJsonFileAndStatus(response, 200, "adjudications_by_numbers.json");
        }

        @Test
        public void returns403IfInvalidRole() {
            final var token = validToken(List.of("ROLE_SYSTEM_USER"));
            final var httpEntity = createHttpEntity(token, List.of(-5, -200));

            final var response = testRestTemplate.exchange(
                "/api/adjudications",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

            assertThatStatus(response, 403);
        }
    }

    @Nested
    public class AdjudicationHearings {

        final List<String> valid = List.of("ROLE_MAINTAIN_ADJUDICATIONS");
        final List<String> invalid = List.of("ROLE_SYSTEM_USER");
        final Map validRequest = Map.of("dateTimeOfHearing","2022-10-24T10:12:44", "hearingLocationId", "-31", "oicHearingType", "GOV_ADULT");
        final Map invalidRequest = Map.of("dateTimeOfHearing","not a date time", "hearingLocationId", "-31", "oicHearingType", "GOV_ADULT");
        final Map invalidLocationRequest = Map.of("dateTimeOfHearing","2022-10-24T10:12:44", "hearingLocationId", "1", "oicHearingType", "GOV_ADULT");
        final Map invalidTypeRequest = Map.of("dateTimeOfHearing","2022-10-24T10:12:44", "hearingLocationId", "-31", "oicHearingType", "WRONG");


        @Test
        public void createHearingReturns403ForInvalidRoles () {
            webTestClient.post()
                .uri("/api/adjudications/adjudication/-9/hearing")
                .headers(setAuthorisation(invalid))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(validRequest)
                .exchange()
                .expectStatus().isForbidden();
        }
        @Test
        public void createHearingReturns404DueToNoAdjudication() {
            webTestClient.post()
                .uri( "/api/adjudications/adjudication/99/hearing")
                .headers(setAuthorisation(valid))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(validRequest)
                .exchange()
                .expectStatus().isNotFound();
        }

        @Test
        public void createHearingReturns400() {
            webTestClient.post()
                .uri("/api/adjudications/adjudication/-9/hearing")
                .headers(setAuthorisation(valid))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
        }

        @Test
        public void createHearingReturns400forOicHearingType() {
            webTestClient.post()
                .uri("/api/adjudications/adjudication/-9/hearing")
                .headers(setAuthorisation(valid))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(invalidTypeRequest)
                .exchange()
                .expectStatus().isBadRequest();
        }

        @Test
        public void createHearingReturns400forLocationId() {
            webTestClient.post()
                .uri("/api/adjudications/adjudication/-9/hearing")
                .headers(setAuthorisation(valid))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(invalidLocationRequest)
                .exchange()
                .expectStatus().isBadRequest();
        }

        @Test
        public void createHearing() {
            webTestClient.post()
                .uri("/api/adjudications/adjudication/-9/hearing")
                .headers(setAuthorisation(valid))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(validRequest)
                .exchange()
                .expectStatus().isCreated();
        }

        @Test
        public void amendHearingReturns403ForInvalidRoles () {
            webTestClient.put()
                .uri("/api/adjudications/adjudication/-9/hearing/-4")
                .headers(setAuthorisation(invalid))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(validRequest)
                .exchange()
                .expectStatus().isForbidden();
        }
        @Test
        public void amendHearingReturns404DueToNoAdjudication() {
            webTestClient.put()
                .uri( "/api/adjudications/adjudication/99/hearing/-4")
                .headers(setAuthorisation(valid))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(validRequest)
                .exchange()
                .expectStatus().isNotFound();
        }

        @Test
        public void amendHearingReturns400() {
            webTestClient.put()
                .uri("/api/adjudications/adjudication/-9/hearing/-4")
                .headers(setAuthorisation(valid))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
        }

        @Test
        public void amendHearingReturns400forOicHearingType() {
            webTestClient.put()
                .uri("/api/adjudications/adjudication/-9/hearing/-4")
                .headers(setAuthorisation(valid))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(invalidTypeRequest)
                .exchange()
                .expectStatus().isBadRequest();
        }

        @Test
        public void amendHearingReturns400forLocationId() {
            webTestClient.put()
                .uri("/api/adjudications/adjudication/-9/hearing/-4")
                .headers(setAuthorisation(valid))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(invalidLocationRequest)
                .exchange()
                .expectStatus().isBadRequest();
        }

        @Test
        public void amendHearingInvalidRequestAsHearingDoesNotBelongToAdjudication() {
            webTestClient.put()
                .uri("/api/adjudications/adjudication/-5/hearing/-4")
                .headers(setAuthorisation(valid))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(validRequest)
                .exchange()
                .expectStatus().isBadRequest();
        }

        @Test
        public void amendHearing() {
            webTestClient.put()
                .uri("/api/adjudications/adjudication/-9/hearing/-4")
                .headers(setAuthorisation(valid))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(validRequest)
                .exchange()
                .expectStatus().isOk();
        }


        @Test
        public void deleteHearingReturns403DueToInvalidRoles () {
            webTestClient.delete()
                .uri("/api/adjudications/adjudication/-9/hearing/1")
                .headers(setAuthorisation(invalid))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isForbidden();
        }

        @Test
        public void deleteHearingReturns404DueToNoAdjudication() {
            webTestClient.delete()
                .uri("/api/adjudications/adjudication/99/hearing/1")
                .headers(setAuthorisation(valid))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isNotFound();
        }

        @Test
        public void deleteHearingReturns404DueToNoHearing() {
            webTestClient.delete()
                .uri("/api/adjudications/adjudication/-9/hearing/2")
                .headers(setAuthorisation(valid))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isNotFound();
        }

        @Test
        public void deleteHearingInvalidRequestAsHearingDoesNotBelongToAdjudication() {
            webTestClient.delete()
                .uri("/api/adjudications/adjudication/-5/hearing/-4")
                .headers(setAuthorisation(valid))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isBadRequest();
        }

        @Test
        public void deleteHearing() {
            webTestClient.delete()
                .uri("/api/adjudications/adjudication/-9/hearing/-4")
                .headers(setAuthorisation(valid))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isOk();
        }
    }

    @Nested
    public class CreateHearingResult {

        final List<String> valid = List.of("ROLE_MAINTAIN_ADJUDICATIONS");
        final List<String> invalid = List.of("ROLE_SYSTEM_USER");

        final Map invalidRequest = Map.of("pleaFindingCode", PleaFindingCode.GUILTY, "findingCode", FindingCode.NOT_PROCEED, "adjudicator", "TWRIGHT");
        final Map validRequest = Map.of("pleaFindingCode", PleaFindingCode.GUILTY, "findingCode", FindingCode.NOT_PROCEED, "adjudicator", "ITAG_USER");

        @Test
        public void createHearingResultReturns403ForInvalidRoles () {
            createHearingResult(invalid, validRequest, -9L, -1L)
                .expectStatus().isForbidden();
        }

        @Test
        public void createHearingResultReturns404DueToNoAdjudication() {
            createHearingResult(valid, validRequest, 99L, -1L)
                .expectStatus().isNotFound();
        }

        @Test
        public void createHearingResultReturns404DueToNoHearing() {
            createHearingResult(valid, validRequest, -9L, 2L)
                .expectStatus().isNotFound();
        }

        @Test
        public void createHearingResultReturns404DueToNoAdjudicatorOnFile() {
            createHearingResult(valid, invalidRequest, -9L, -4L)
                .expectStatus().isNotFound();
        }

        @Test
        public void createHearingResultReturns400DueToHearingNotBeingAssociatedWithAdjudication() {
            createHearingResult(valid, validRequest, -5L, -4L)
                .expectStatus().isBadRequest();
        }

        @Test
        public void createHearingResultReturns400DueToHearingResultPresent() {
            createHearingResult(valid, validRequest, -7L, -1L)
                .expectStatus().isBadRequest();
        }

        @Test
        @Transactional
        public void createHearingResultReturnsSuccess() {
            createHearingResult(valid, validRequest, -3001L, -3004L)
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.findingCode").isEqualTo(FindingCode.NOT_PROCEED.name())
                .jsonPath("$.pleaFindingCode").isEqualTo(PleaFindingCode.GUILTY.name());

            OicHearingResult oicHearingResult = entityManager.find(OicHearingResult.class, new OicHearingResult.PK(-3004L, 1L));
            assertThat(FindingCode.NOT_PROCEED).isEqualTo(oicHearingResult.getFindingCode());
            assertThat(PleaFindingCode.GUILTY).isEqualTo(oicHearingResult.getPleaFindingCode());
        }

        private ResponseSpec createHearingResult(List<String> headers, Map payload, Long adjudicationNumber, Long hearingId) {
            return webTestClient.post()
                .uri("/api/adjudications/adjudication/"+adjudicationNumber+"/hearing/"+hearingId+"/result")
                .headers(setAuthorisation(headers))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(payload)
                .exchange();
        }
    }

    @Nested
    public class AmendHearingResult {
        final List<String> valid = List.of("ROLE_MAINTAIN_ADJUDICATIONS");
        final List<String> invalid = List.of("ROLE_SYSTEM_USER");

        final Map validRequest = Map.of("pleaFindingCode", PleaFindingCode.GUILTY, "findingCode", FindingCode.NOT_PROCEED, "adjudicator", "ITAG_USER");
        final Map invalidRequest = Map.of("pleaFindingCode", PleaFindingCode.GUILTY, "findingCode", FindingCode.NOT_PROCEED, "adjudicator", "TWRIGHT");

        @Test
        public void amendHearingResultReturns403ForInvalidRoles () {
            amendHearingResult(invalid, validRequest, -9L, -1L)
                .expectStatus().isForbidden();
        }

        @Test
        public void amendHearingResultReturns404DueToNoAdjudication() {
            amendHearingResult(valid, validRequest, 99L, -1L)
                .expectStatus().isNotFound();
        }

        @Test
        public void amendHearingResultReturns404DueToNoHearing() {
            amendHearingResult(valid, validRequest, -9L, 2L)
                .expectStatus().isNotFound();
        }

        @Test
        public void amendHearingResultReturns404DueToNoAdjudicatorOnFile() {
            amendHearingResult(valid, invalidRequest, -9L, -4L)
                .expectStatus().isNotFound();
        }

        @Test
        public void amendHearingResultReturns400DueToHearingNotBeingAssociatedWithAdjudication() {
            amendHearingResult(valid, validRequest, -5L, -4L)
                .expectStatus().isBadRequest();
        }

        @Test
        public void amendHearingResultReturns404DueToNoHearingResultPresent() {
            amendHearingResult(valid, validRequest, -9L, -4L)
                .expectStatus().isNotFound();
        }

        @Test
        @Transactional
        public void amendHearingResultReturnsSuccess() {
            amendHearingResult(valid, validRequest, -3001L, -3001L)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.findingCode").isEqualTo(FindingCode.NOT_PROCEED.name())
                .jsonPath("$.pleaFindingCode").isEqualTo(PleaFindingCode.GUILTY.name());

            OicHearingResult oicHearingResult = entityManager.find(OicHearingResult.class, new OicHearingResult.PK(-3001L, 1L));
            assertThat(FindingCode.NOT_PROCEED).isEqualTo(oicHearingResult.getFindingCode());
            assertThat(PleaFindingCode.GUILTY).isEqualTo(oicHearingResult.getPleaFindingCode());
        }

        private ResponseSpec amendHearingResult(List<String> headers, Map payload, Long adjudicationNumber, Long hearingId) {
            return webTestClient.put()
                .uri("/api/adjudications/adjudication/"+adjudicationNumber+"/hearing/"+hearingId+"/result")
                .headers(setAuthorisation(headers))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(payload)
                .exchange();
        }
    }

    @Nested
    public class DeleteHearingResult {

        final List<String> valid = List.of("ROLE_MAINTAIN_ADJUDICATIONS");
        final List<String> invalid = List.of("ROLE_SYSTEM_USER");

        @Test
        public void deleteHearingResultReturns403ForInvalidRoles() {
            deleteHearingResult(invalid, -9L, -1L)
                .expectStatus().isForbidden();
        }

        @Test
        public void deleteHearingResultReturns404DueToNoAdjudication() {
            deleteHearingResult(valid, 99L, -1L)
                .expectStatus().isNotFound();
        }

        @Test
        public void deleteHearingResultReturns404DueToNoHearing() {
            deleteHearingResult(valid, -9L, 2L)
                .expectStatus().isNotFound();
        }

        @Test
        public void deleteHearingResultReturns404DueToNoAdjudicatorOnFile() {
            deleteHearingResult(valid, -9L, -4L)
                .expectStatus().isNotFound();
        }

        @Test
        public void deleteHearingResultReturns400DueToHearingNotBeingAssociatedWithAdjudication() {
            deleteHearingResult(valid, -5L, -4L)
                .expectStatus().isBadRequest();
        }

        @Test
        public void deleteHearingResultReturns404DueToNoHearingResultPresent() {
            deleteHearingResult(valid, -9L, -4L)
                .expectStatus().isNotFound();
        }

        @Test
        public void deleteHearingResultReturnsSuccess_WithoutSanctions() {
            deleteHearingResult(valid, -3001L, -3001L)
                .expectStatus().isOk();
        }

        // TODO test delete HearingResult with sanctions

        private ResponseSpec deleteHearingResult(List<String> headers, Long adjudicationNumber, Long hearingId) {
            return webTestClient.delete()
                .uri("/api/adjudications/adjudication/"+adjudicationNumber+"/hearing/"+hearingId+"/result")
                .headers(setAuthorisation(headers))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .exchange();
        }
    }

    @Nested
    public class CreateSanctions {

    }

}
