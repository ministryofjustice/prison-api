package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;


public class CellResourceHistoryTest extends ResourceTest {
    private final String SOME_CELL_LOCATION_ID = "-16";

    @Test
    public void returnAllBedHistories() {

        final var fromDate =  LocalDate.of(2000,10,16);
        final var toDate =    LocalDate.of(2020,10,10);

        final var response = makeRequest(SOME_CELL_LOCATION_ID, fromDate.toString(), toDate.toString());

        assertThatJsonFileAndStatus(response, 200, "cell-histories.json");
    }

    @Test
    public void handleInvalidFromDate() {
        final var response = makeRequest(SOME_CELL_LOCATION_ID, "hello", LocalDate.now().toString());

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    public void handleInvalidToDate() {
        final var response = makeRequest(SOME_CELL_LOCATION_ID, LocalDate.now().toString(), "hello");

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    public void handleCellNotFound() {
        final var response = makeRequest("-991873", LocalDate.now().toString(), LocalDate.now().toString());

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
    }

    private ResponseEntity<String> makeRequest(final String locationId, final String fromDate, final String toDate) {
        final var entity = createHttpEntity(validToken(), null);

        return testRestTemplate.exchange("/api/cell/{cellLocationId}/history?fromDate={fromDate}&toDate={toDate}",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<String>() {},
                locationId,
                fromDate,
                toDate
        );
    }
}
