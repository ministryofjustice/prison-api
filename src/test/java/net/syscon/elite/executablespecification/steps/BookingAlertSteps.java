package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.Alert;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Booking alias feature.
 */
public class BookingAlertSteps extends CommonSteps {
    private static final String API_REQUEST_BASE_URL = API_PREFIX + "bookings/{bookingId}/alerts";
    private static final String API_REQUEST_ALERT_URL = API_PREFIX + "bookings/{bookingId}/alerts/{alertId}";
    private static final String API_REQUEST_ALERT_POST_URL_AGENCY = API_PREFIX + "bookings/offenderNo/{agencyId}/alerts";
    private static final String API_REQUEST_ALERT_POST_URL = API_PREFIX + "bookings/offenderNo/alerts";

    private List<Alert> alerts;
    private Alert alert;

    @Step("Retrieve alerts for offender booking")
    public void getAlerts(Long bookingId) {
        doListApiCall(bookingId);
    }

    @Step("Retrieve alerts for offender nos")
    public void getAlerts(String agencyId, List<String> offenderNos) {
        doPostApiCall(agencyId, offenderNos);
    }

    @Step("Verify returned offender alias first names")
    public void verifyCodeList(String codes) {
        verifyPropertyValues(alerts, Alert::getAlertCode, codes);
    }

    private void doListApiCall(Long bookingId) {
        init();

        try {
            ResponseEntity<List<Alert>> response =
                    restTemplate.exchange(
                            API_REQUEST_BASE_URL,
                            HttpMethod.GET,
                            createEntity(),
                            new ParameterizedTypeReference<List<Alert>>() {},
                            bookingId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            alerts = response.getBody();

            buildResourceData(response);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void doSingleResultApiCall(Long bookingId, Long alertId) {
        init();

        try {
            ResponseEntity<Alert> response =
                    restTemplate.exchange(
                            API_REQUEST_ALERT_URL,
                            HttpMethod.GET,
                            createEntity(null, null),
                            new ParameterizedTypeReference<Alert>() {},
                            bookingId,
                            alertId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            alert = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void doPostApiCall(String agencyId, List<String> offenderNoBody) {
        init();
        try {
            final String POST_URL = agencyId == null ? API_REQUEST_ALERT_POST_URL : API_REQUEST_ALERT_POST_URL_AGENCY;
            ResponseEntity<List<Alert>> response =
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
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    protected void init() {
        super.init();

        alerts = null;
        alert = null;
    }

    @Step("Retrieve specific alert detail for offender booking")
    public void getAlert(Long bookingId, Long alertId) {
        doSingleResultApiCall(bookingId, alertId);
    }

    @Step("Verify value of specific field in alert detail")
    public void verifyAlertField(String field, String value) throws ReflectiveOperationException {
        verifyField(alert, field, value);
    }

    @Step("Verify alert list")
    public void verifyAlerts(List<Alert> expected) {
        assertThat(alerts).asList().containsAll(expected);
    }
}
