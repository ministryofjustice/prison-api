package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.justice.hmpps.prison.api.model.Location;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class LocationResourceIntTest extends ResourceTest {

    @Test
    public void testGetLocation_found() {

        final var response = testRestTemplate.exchange(
            "/api/locations/-1",
            HttpMethod.GET,
            createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)),
            Location.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getLocationId()).isEqualTo(-1);
    }

    @Test
    public void testGetLocation_Inactive_not_found() {

        final var response = testRestTemplate.exchange(
            "/api/locations/-31",
            HttpMethod.GET,
            createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)),
            Location.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testGetLocation_Inactive_included_and_found() {

        final var response = testRestTemplate.exchange(
            "/api/locations/-31?includeInactive=Yes",
            HttpMethod.GET,
            createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)),
            Location.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getLocationId()).isEqualTo(-31);
    }

    @Test
    public void testGetLocation_Bad_query_parameter_Bad_request() {

        final var response = testRestTemplate.exchange(
            "/api/locations/-31?includeInactive=Meh",
            HttpMethod.GET,
            createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of(), Map.of(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)),
            Location.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
