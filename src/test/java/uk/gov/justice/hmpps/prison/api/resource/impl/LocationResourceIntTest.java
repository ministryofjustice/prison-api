package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.hmpps.prison.api.model.Location;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class LocationResourceIntTest extends ResourceTest {
    private static final String LOCATION_URL = "/api/locations/{locationId}";
    private static final long ACTIVE_LOCATION_ID = -1L;
    private static final long INACTIVE_LOCATION_ID = -31L;


    @Test
    public void testGetLocation_found() {
        final var response = getLocation(ACTIVE_LOCATION_ID, "");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getLocationId()).isEqualTo(-1);
    }

    @Test
    public void testGetLocation_Inactive_location_not_found() {
        final var response = getLocation(INACTIVE_LOCATION_ID, "");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testGetLocation_Inactive_location_included_and_found() {
        final var response = getLocation(INACTIVE_LOCATION_ID, "?includeInactive=True");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getLocationId()).isEqualTo(-31);
    }

    @Test
    public void testGetLocation_Bad_query_parameter_value_Bad_request() {
        final var response = getLocation(INACTIVE_LOCATION_ID, "?includeInactive=Nope");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testGetLocation_No_query_parameter_value_Not_Found() {
        final var response = getLocation(INACTIVE_LOCATION_ID, "?includeInactive");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    ResponseEntity<Location> getLocation(long locationId, String queryString) {
        return testRestTemplate.exchange(
            LOCATION_URL + queryString,
            HttpMethod.GET,
            createHttpEntityWithBearerAuthorisation(
                "ITAG_USER",
                List.of(),
                Map.of(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            ),
            Location.class,
            locationId);
    }
}
