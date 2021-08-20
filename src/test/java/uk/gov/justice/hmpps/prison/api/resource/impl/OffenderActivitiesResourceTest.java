package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

public class OffenderActivitiesResourceTest extends ResourceTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Nested
    class GetCurrentWork {
        @Test
        public void successfulRequest_returnsCorrectData() {
            final var entity = createHttpEntity(validToken(), null);

            final var response = testRestTemplate.exchange(
                "/api/offender-activities/A1234AC/current-work",
                HttpMethod.GET,
                entity,
                String.class);

            assertThatJsonFileAndStatus(response, HttpStatus.OK.value(), "offender-activities-current-work.json");
        }

        @Test
        public void badRequest_InvalidOffenderNos() {
            final var entity = createHttpEntity(validToken(), null);

            final var response = testRestTemplate.exchange(
                "/api/offender-activities/1234/current-work",
                HttpMethod.GET,
                entity,
                String.class);

            assertThatStatus(response, HttpStatus.NOT_FOUND.value());
            assertThatJson(response.getBody()).node("userMessage").asString().contains("Resource with id [1234] not found.");
        }
    }

    @Nested
    class GetWorkHistory {
        @Test
        public void successfulRequest_returnsCorrectData() {
            final var entity = createHttpEntity(validToken(), null);

            final var response = testRestTemplate.exchange(
                "/api/offender-activities/A1234AC/work-history?earliestEndDate=2021-01-01",
                HttpMethod.GET,
                entity,
                String.class);

            assertThatJsonFileAndStatus(response, HttpStatus.OK.value(), "offender-activities-work-history.json");
            // Check end date separately as it uses sysdate
            final var jsonContent = getBodyAsJsonContent(response);
            final var expectedDate = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
            assertThat(jsonContent).extractingJsonPathStringValue("$.workActivities[4].endDate").isEqualTo(expectedDate);
        }

        @Test
        public void successfulRequest_paginates() {
            final var entity = createHttpEntity(validToken(), null);

            final var response = testRestTemplate.exchange(
                "/api/offender-activities/A1234AC/work-history?earliestEndDate=2021-01-01&page=1&size=1",
                HttpMethod.GET,
                entity,
                String.class);

            final var jsonContent = getBodyAsJsonContent(response);
            assertThat(jsonContent).extractingJsonPathArrayValue("$.workActivities").hasSize(1);
            assertThat(jsonContent).extractingJsonPathStringValue("$.workActivities[0].description").isEqualTo("Weeding");
        }

        @Test
        public void badRequest_NoEndDateParameter() {
            final var entity = createHttpEntity(validToken(), null);

            final var response = testRestTemplate.exchange(
                "/api/offender-activities/1234/work-history",
                HttpMethod.GET,
                entity,
                String.class);

            assertThatStatus(response, HttpStatus.BAD_REQUEST.value());
            assertThatJson(response.getBody()).node("userMessage").asString().contains("Required request parameter 'earliestEndDate' for method parameter type LocalDate is not present");
        }
    }
}
