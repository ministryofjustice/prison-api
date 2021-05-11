package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;


public class CellResourceHistoryTest extends ResourceTest {
    private final String CELL_LOCATION_ID = "-16";
    private final String AGENCY_ID = "LEI";

    @Test
    public void returnAllBedHistoriesForDateAndAgency() {

        final var assignmentDate = java.time.LocalDate.of(2020, 4, 3);

        final var response = makeRequest(AGENCY_ID, assignmentDate.toString());

        assertThatJsonFileAndStatus(response, 200, "cell-histories-by-date.json");
    }

    @Test
    public void returnAllBedHistoriesForDateRangeOnly() {
        final var fromDateTime = LocalDateTime.of(2000, 10, 16, 10, 10, 10);
        final var toDateTime = LocalDateTime.of(2020, 10, 10, 11, 11, 11);

        final var response = makeRequest(CELL_LOCATION_ID, fromDateTime.toString(), toDateTime.toString());

        assertThatJsonFileAndStatus(response, 200, "cell-histories.json");
    }

    @Test
    public void handleInvalidFromDate() {
        final var response = makeRequest(CELL_LOCATION_ID, "hello", LocalDateTime.now().toString());

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    public void handleInvalidToDate() {
        final var response = makeRequest(CELL_LOCATION_ID, LocalDateTime.now().toString(), "hello");

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    public void handleCellNotFound() {
        final var response = makeRequest("-991873", LocalDateTime.now().toString(), LocalDateTime.now().toString());

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
    }

    private ResponseEntity<String> makeRequest(final String locationId, final String fromDate, final String toDate) {
        final var entity = createHttpEntity(validToken(), null);

        return testRestTemplate.exchange("/api/cell/{cellLocationId}/history?fromDate={fromDate}&toDate={toDate}",
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<String>() {
            },
            locationId,
            fromDate,
            toDate
        );
    }

    private ResponseEntity<String> makeRequest(final String agencyId, final String assignmentDate) {
        final var entity = createHttpEntity(validToken(), null);

        return testRestTemplate.exchange("/api/cell/{agencyId}/history/{assignmentDate}",
            HttpMethod.GET,
            entity,
            new ParameterizedTypeReference<String>() {
            },
            agencyId,
            assignmentDate
        );
    }
}
