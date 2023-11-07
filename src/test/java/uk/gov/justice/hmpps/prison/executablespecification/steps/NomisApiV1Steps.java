package uk.gov.justice.hmpps.prison.executablespecification.steps;

import net.serenitybdd.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class NomisApiV1Steps extends CommonSteps {

    private static final String API_REQUEST_BASE_URL = API_PREFIX + "v1/offenders/{noms_id}/alerts";

    private Map<String, Object> alerts;

    @Step("Retrieve v1 alerts for noms id")
    public void getAlerts(final String nomsId) {
        doListApiCall(nomsId);
    }

    @Step("Verify v1 alert list")
    public Map<String, Object> getAlerts() {
        return alerts;
    }

    private void doListApiCall(final String nomsId) {
        init();

        try {
            final var response =
                    restTemplate.exchange(
                            API_REQUEST_BASE_URL,
                            HttpMethod.GET,
                            createEntity(),
                            new ParameterizedTypeReference<Map>() {
                            },
                            nomsId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            alerts = response.getBody();
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

}
