package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

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
    public class GetProvenAdjudications {

        @Test
        public void returns403IfInvalidRole() {
            final var token = validToken(List.of("ROLE_DUMMY"));
            final var httpEntity = createHttpEntity(token, List.of(-5, -200));

            final var response = testRestTemplate.exchange(
                "/api/bookings/proven-adjudications",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

            assertThatStatus(response, 403);
        }

        @Test
        public void returnsDataForValidRole() {
            final var token = validToken(List.of("ROLE_VIEW_ADJUDICATIONS"));
            final var httpEntity = createHttpEntity(token, List.of(-5, -200));

            final var response = testRestTemplate.exchange(
                "/api/bookings/proven-adjudications",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

            assertThatStatus(response, 200);
        }

        @Test
        public void returnsValidData() {
            final var token = validToken(List.of("ROLE_VIEW_ADJUDICATIONS"));
            final var httpEntity = createHttpEntity(token, List.of(-5,-8));

            final var response = testRestTemplate.exchange(
                "/api/bookings/proven-adjudications?adjudicationCutoffDate=2017-09-13",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

            assertThatJsonFileAndStatus(response, 200, "proven_adjudications.json");
        }
    }
}
