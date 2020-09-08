package uk.gov.justice.hmpps.prison.api.resource;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import uk.gov.justice.hmpps.prison.api.resource.impl.ResourceTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

public class HealthIntTest extends ResourceTest {

    @Test
    public void healthReportsOk() {
        getAndVerifyStatusUp("/health");
    }

    @Test
    public void dbReportsOk() {
        getAndVerifyStatusUp("/health/db");
    }

    @Test
    public void pingReportsOk() {
        getAndVerifyStatusUp("/health/ping");
    }

    @Test
    public void livenessReportsOk() {
        getAndVerifyStatusUp("/health/liveness");
    }

    @Test
    public void readinessReportsOk() {
        getAndVerifyStatusUp("/health/readiness");
    }

    private void getAndVerifyStatusUp(final String url) {
        final var response = testRestTemplate.exchange(
                url,
                HttpMethod.GET,
                RequestEntity.EMPTY,
                new ParameterizedTypeReference<String>() {
                },
                Map.of());

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(getBodyAsJsonContent(response)).extractingJsonPathStringValue("status").isEqualTo("UP");
    }
}
