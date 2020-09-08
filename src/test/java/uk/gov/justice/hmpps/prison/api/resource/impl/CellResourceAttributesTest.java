package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;


public class CellResourceAttributesTest extends ResourceTest {

    @Test
    public void returnAllCellAttributes() {
        final var response = makeRequest("-3");

        assertThatJsonFileAndStatus(response, 200, "cell_with_attributes.json");
    }

    @Test
    public void handleCellNotFound() {
        final var response = makeRequest("-991873");

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
    }

    private ResponseEntity<String> makeRequest(final String locationId) {
        final var entity = createHttpEntity(validToken(), null);

        return testRestTemplate.exchange("/api/cell/{cellLocationId}/attributes",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<String>() {},
                locationId
        );
    }
}
