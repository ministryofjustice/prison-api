package uk.gov.justice.hmpps.prison.executablespecification.steps;

import net.serenitybdd.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.justice.hmpps.prison.api.model.Alert;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Booking alias feature.
 */
public class BookingAlertSteps extends CommonSteps {
    private static final String API_REQUEST_BASE_URL = API_PREFIX + "bookings/{bookingId}/alerts/v2";
    private static final String API_REQUEST_ALERT_POST_URL_AGENCY = API_PREFIX + "bookings/offenderNo/{agencyId}/alerts";
    private static final String API_REQUEST_ALERT_POST_URL = API_PREFIX + "bookings/offenderNo/alerts";

    private List<Alert> alerts;

    @Step("Retrieve alerts for offender booking")
    public void getAlerts(final Long bookingId) {
        doListApiCall(bookingId);
    }

    @Step("Retrieve alerts for offender nos")
    public void getAlerts(final String agencyId, final List<String> offenderNos) {
        doPostApiCall(agencyId, offenderNos);
    }

    @Step("Verify returned offender alias first names")
    public void verifyCodeList(final String codes) {
        verifyPropertyValues(alerts, Alert::getAlertCode, codes);
    }

    private void doListApiCall(final Long bookingId) {
        init();

        try {
            final var response =
                    restTemplate.exchange(
                            API_REQUEST_BASE_URL,
                            HttpMethod.GET,
                            createEntity(),
                            new ParameterizedTypeReference<RestResponsePage<Alert>>() {
                            },
                            bookingId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            alerts = Objects.requireNonNull(response.getBody()).getContent();

            buildResourceData(response.getBody());
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void doPostApiCall(final String agencyId, final List<String> offenderNoBody) {
        init();
        try {
            final var POST_URL = agencyId == null ? API_REQUEST_ALERT_POST_URL : API_REQUEST_ALERT_POST_URL_AGENCY;
            final var response =
                    restTemplate.exchange(
                            POST_URL,
                            HttpMethod.POST,
                            createEntity(offenderNoBody),
                            new ParameterizedTypeReference<List<Alert>>() {
                            },
                            agencyId);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            alerts = response.getBody();
            buildResourceData(response);
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    protected void init() {
        super.init();

        alerts = null;
    }

    @Step("Verify alert list")
    public void verifyAlerts(final List<Alert> expected) {
        assertThat(alerts).asList().containsAll(expected);
    }
}
