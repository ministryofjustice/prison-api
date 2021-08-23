package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

public class WIPOffenderActivitiesResourceTest extends ResourceTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Nested
    class HistoricalAttendances {
        @Test
        @Transactional
        public void successfulRequest_returnsCorrectData() {
            final var entity = createHttpEntity(validToken(), null);

            final var response = testRestTemplate.exchange(
                // TODO - Dates are dependent on SYSDATE!!
                "/api/offender-activities/A1234AC/activity-history?fromDate=2010-01-01&toDate=2030-01-01",
                HttpMethod.GET,
                entity,
                String.class);

            final var jsonContent = getBodyAsJsonContent(response);
            // TODO - Long? assertThat(jsonContent).extractingJsonPathStringValue("$.content[0].bookingId").isEqualTo("-3");
            assertThat(jsonContent).extractingJsonPathStringValue("$[0].eventDate").isEqualTo("2017-09-11");
            assertThat(jsonContent).extractingJsonPathStringValue("$[0].outcome").isEqualTo("ACCABS");
        }
    }
}
