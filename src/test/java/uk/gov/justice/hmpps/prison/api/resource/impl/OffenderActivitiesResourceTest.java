package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

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
            final var entity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_VIEW_ACTIVITIES"), Map.of());

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
            final var entity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_VIEW_ACTIVITIES"), Map.of());

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
            final var entity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_VIEW_ACTIVITIES"), Map.of());

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

        @Test
        public void noAuthorisation() {
            final var entity = createHttpEntity(validToken(), null);

            final var response = testRestTemplate.exchange(
                "/api/offender-activities/A1234AC/activities-history?earliestEndDate=2021-01-01",
                HttpMethod.GET,
                entity,
                String.class);

            assertThatStatus(response, HttpStatus.FORBIDDEN.value());
        }
    }

    @Nested
    class GetHistoricalAttendancesTest {
        @Test
        public void successfulRequest_returnsCorrectDataPage_0() {
            final var entity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_VIEW_ACTIVITIES"), Map.of());

            final var response = testRestTemplate.exchange(
                "/api/offender-activities/A1234AB/attendance-history?fromDate=2017-01-01&toDate=" + today + "&page=0&size=2&sort=eventId,desc",
                HttpMethod.GET,
                entity,
                String.class);

            // Use string rather than file as it needs to contain todays date
            assertThatJsonFileAndStatus(response, HttpStatus.OK.value(), """
                {
                "content": [
                  {
                    "bookingId": -2,
                    "eventDate": "%s",
                    "code": "WOOD",
                    "description": "Woodwork",
                    "activity": "Test Prog 2",
                    "prisonId": "LEI"
                  },
                  {
                    "bookingId": -2,
                    "eventDate": "%s",
                    "code": "SUBS",
                    "description": "Substance misuse course",
                    "comment": "Comment 12",
                    "prisonId": "LEI"
                  }
                ],
                "pageable": {
                  "sort": {
                    "empty": false,
                    "sorted": true,
                    "unsorted": false
                  },
                  "offset": 0,
                  "pageNumber": 0,
                  "pageSize": 2,
                  "unpaged": false,
                  "paged": true
                },
                "last": false,
                "totalPages": 4,
                "totalElements": 7,
                "size": 2,
                "number": 0,
                "sort": {
                  "empty": false,
                  "sorted": true,
                  "unsorted": false
                },
                "first": true,
                "numberOfElements": 2,
                "empty": false
              }""".formatted(today, today));
        }

        @Test
        public void successfulRequest_returnsCorrectDataPage_1() {
            final var entity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_VIEW_ACTIVITIES"), Map.of());

            final var response = testRestTemplate.exchange(
                "/api/offender-activities/A1234AB/attendance-history?fromDate=2017-01-01&toDate=" + today + "&page=1&size=2&sort=eventId,desc",
                HttpMethod.GET,
                entity,
                String.class);

            assertThatJsonFileAndStatus(response, HttpStatus.OK.value(), """
                {
                  "content": [
                    {
                      "bookingId": -2,
                      "eventDate": "2017-09-13",
                      "outcome": "UNACAB",
                      "code": "CC1",
                      "description": "Chapel Cleaner",
                      "prisonId": "LEI"
                    },
                    {
                      "bookingId": -2,
                      "eventDate": "2017-09-14",
                      "outcome": "ACCABS",
                      "code": "WOOD",
                      "description": "Woodwork",
                      "prisonId": "LEI"
                    }
                  ],
                  "pageable": {
                    "sort": {
                      "empty": false,
                      "sorted": true,
                      "unsorted": false
                    },
                    "offset": 2,
                    "pageNumber": 1,
                    "pageSize": 2,
                    "paged": true,
                    "unpaged": false
                  },
                  "last": false,
                  "totalElements": 7,
                  "totalPages": 4,
                  "size": 2,
                  "number": 1,
                  "sort": {
                    "empty": false,
                    "sorted": true,
                    "unsorted": false
                  },
                  "first": false,
                  "numberOfElements": 2,
                  "empty": false
                }""");
        }

        @Test
        public void successfulRequest_returnsCorrectData_Outcome() {
            final var entity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_VIEW_ACTIVITIES"), Map.of());

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
        public void noAuthorisation() {
            final var entity = createHttpEntity(validToken(), null);

            final var response = testRestTemplate.exchange(
                "/api/offender-activities/A1234AB/attendance-history?fromDate=2017-01-01&toDate=2017-01-02",
                HttpMethod.GET,
                entity,
                String.class);

            assertThatStatus(response, HttpStatus.FORBIDDEN.value());
        }

        @Test
        public void badRequest_NoFromDateParameter() {
            final var entity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_VIEW_ACTIVITIES"), Map.of());

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
            final var entity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_VIEW_ACTIVITIES"), Map.of());

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
