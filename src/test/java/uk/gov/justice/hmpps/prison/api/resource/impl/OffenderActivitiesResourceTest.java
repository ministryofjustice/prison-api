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

    final String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Nested
    class GetActivitiesHistoryTest {
        @Test
        public void successfulRequest_returnsCorrectData() {
            final var entity = createHttpEntity(validToken(), null);

            final var response = testRestTemplate.exchange(
                "/api/offender-activities/A1234AC/activities-history?earliestEndDate=2021-01-01",
                HttpMethod.GET,
                entity,
                String.class);

            assertThatJsonFileAndStatus(response, HttpStatus.OK.value(), "offender-activities-work-history.json");
            // Check end date separately as it uses sysdate
            final var jsonContent = getBodyAsJsonContent(response);
            final var expectedDate = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
            assertThat(jsonContent).extractingJsonPathStringValue("$.content[4].endDate").isEqualTo(expectedDate);
        }

        @Test
        public void badRequest_NoEndDateParameter() {
            final var entity = createHttpEntity(validToken(), null);

            final var response = testRestTemplate.exchange(
                "/api/offender-activities/1234/activities-history",
                HttpMethod.GET,
                entity,
                String.class);

            assertThatStatus(response, HttpStatus.BAD_REQUEST.value());
            assertThatJson(response.getBody()).node("userMessage").asString().contains("Required request parameter 'earliestEndDate' for method parameter type LocalDate is not present");
        }

        @Test
        public void successfulRequest_page() {
            final var entity = createHttpEntity(validToken(), null);

            final var response = testRestTemplate.exchange(
                "/api/offender-activities/A1234AC/activities-history?earliestEndDate=2021-01-01&page=1&size=2",
                HttpMethod.GET,
                entity,
                String.class);

            final var jsonContent = getBodyAsJsonContent(response);
            assertThat(jsonContent).extractingJsonPathArrayValue("$.content").hasSize(2);
            assertThat(jsonContent).extractingJsonPathStringValue("$.content[0].description").isEqualTo("Address Testing");
            assertThat(jsonContent).extractingJsonPathStringValue("$.content[1].description").isEqualTo("Substance misuse course");
            assertThat(jsonContent).extractingJsonPathNumberValue("$.totalPages").isEqualTo(3);
            assertThat(jsonContent).extractingJsonPathNumberValue("$.totalElements").isEqualTo(5);
        }
    }

    @Nested
    class GetHistoricalAttendancesTest {
        @Test
        public void successfulRequest_returnsCorrectDataPage_0() {
            final var entity = createHttpEntity(validToken(), null);

            final var response = testRestTemplate.exchange(
                "/api/offender-activities/A1234AB/attendance-history?fromDate=2017-01-01&toDate=" + today + "&page=0&size=2&sort=eventId,desc",
                HttpMethod.GET,
                entity,
                String.class);

            assertThatJsonFileAndStatus(response, HttpStatus.OK.value(), "offender-attendence-history-0.json");
        }

        @Test
        public void successfulRequest_returnsCorrectDataPage_1() {
            final var entity = createHttpEntity(validToken(), null);

            final var response = testRestTemplate.exchange(
                "/api/offender-activities/A1234AB/attendance-history?fromDate=2017-01-01&toDate=" + today + "&page=1&size=2&sort=eventId,desc",
                HttpMethod.GET,
                entity,
                String.class);

            assertThatJsonFileAndStatus(response, HttpStatus.OK.value(), "offender-attendence-history-1.json");
        }

        @Test
        public void successfulRequest_returnsCorrectData_Outcome() {
            final var entity = createHttpEntity(validToken(), null);

            final var response = testRestTemplate.exchange(
                "/api/offender-activities/A1234AB/attendance-history?fromDate=2017-01-01&toDate=" + today + "&outcome=UNACAB&page=0&size=10&sort=eventId,desc",
                HttpMethod.GET,
                entity,
                String.class);

            assertThatStatus(response, HttpStatus.OK.value());
            final var jsonContent = getBodyAsJsonContent(response);
            assertThat(jsonContent).extractingJsonPathArrayValue("$.content").hasSize(1);
            assertThat(jsonContent).extractingJsonPathStringValue("$.content[0].eventDate").isEqualTo("2017-09-13");
            assertThat(jsonContent).extractingJsonPathStringValue("$.content[0].outcome").isEqualTo("UNACAB");
        }

        @Test
        public void badRequest_NoFromDateParameter() {
            final var entity = createHttpEntity(validToken(), null);

            final var response = testRestTemplate.exchange(
                "/api/offender-activities/1234/attendance-history?toDate=2017-01-01",
                HttpMethod.GET,
                entity,
                String.class);

            assertThatStatus(response, HttpStatus.BAD_REQUEST.value());
            assertThatJson(response.getBody()).node("userMessage").asString().isEqualTo("Required request parameter 'fromDate' for method parameter type LocalDate is not present");
        }

        @Test
        public void badRequest_NoToDateParameter() {
            final var entity = createHttpEntity(validToken(), null);

            final var response = testRestTemplate.exchange(
                "/api/offender-activities/1234/attendance-history?fromDate=2017-01-01",
                HttpMethod.GET,
                entity,
                String.class);

            assertThatStatus(response, HttpStatus.BAD_REQUEST.value());
            assertThatJson(response.getBody()).node("userMessage").asString().isEqualTo("Required request parameter 'toDate' for method parameter type LocalDate is not present");
        }
    }
}
