package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.AdjudicationSearchRequest;

import java.util.List;
import java.util.Map;

public class AdjudicationsResourceTest extends ResourceTest  {

    @Nested
    public class CreateAdjudication {

        @Test
        public void returnsExpectedValue() {
            final var token = validToken(List.of("ROLE_MAINTAIN_ADJUDICATIONS"));
            final var body = Map.of(
                "offenderNo", "A1234AE",
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
            final var httpEntity = createHttpEntity(token, List.of(-5, -200));

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
    public class SearchAdjudications {
        @Test
        public void returnsExpectedValue() {
            final var token = validToken(List.of("ROLE_MAINTAIN_ADJUDICATIONS"));
            final var body = Map.of(
                "agencyLocationId", "LEI",
                "adjudicationIdsMask", List.of(-5, -3001));
            final var httpEntity = createHttpEntity(token, body);
            final var response = testRestTemplate.exchange(
                "/api/adjudications/search",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

            assertThatJsonFileAndStatus(response, 200, "adjudications_by_search.json");
        }

        @Test
        public void returns403IfInvalidRole() {
            final var token = validToken(List.of("ROLE_SYSTEM_USER"));
            final var httpEntity = createHttpEntity(token, null);

            final var response = testRestTemplate.exchange(
                "/api/adjudications/search",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

            assertThatStatus(response, 403);
        }
    }
}
