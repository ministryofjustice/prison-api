package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

public class OffenderActivitiesResourceTest extends ResourceTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    public void testGetCurrentWorkActivities() {
        final var entity = createHttpEntity(validToken(), null);

        final var response = testRestTemplate.exchange(
            "/api/offender-activities/A1234AC/current-work",
            HttpMethod.GET,
            entity,
            String.class);

        assertThatJsonFileAndStatus(response, HttpStatus.OK.value(), "offender-activities-current-work.json");
    }

    @Test
    public void testGetCurrentWorkActivities_InvalidOffenderNos() {
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
