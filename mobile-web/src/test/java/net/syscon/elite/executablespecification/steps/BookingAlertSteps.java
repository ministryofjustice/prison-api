package net.syscon.elite.executablespecification.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import net.syscon.elite.api.model.Alert;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * BDD step implementations for Booking alias feature.
 */
public class BookingAlertSteps extends CommonSteps {
    private static final String API_REQUEST_BASE_URL = API_PREFIX + "bookings/{bookingId}/alerts";

    private List<Alert> alerts;
    private Alert alert;

    @Step("Retrieve alerts for offender booking")
    public void getAlerts(Long bookingId) {
        doListApiCall(bookingId, null);
    }

    public void verifyNumber(int number) {
        assertEquals(number, alerts.size());
    }

    @Step("Verify returned offender alias first names")
    public void verifyCodeList(String codes) {
        verifyPropertyValues(alerts, Alert::getAlertCode, codes);
    }

    private void doListApiCall(Long bookingId, String query) {
        init();
        String requestUrl = API_REQUEST_BASE_URL + StringUtils.trimToEmpty(query);
        try {
            ResponseEntity<List<Alert>> response = restTemplate.exchange(requestUrl, HttpMethod.GET, createEntity(),
                    new ParameterizedTypeReference<List<Alert>>() {
                    }, bookingId.toString());
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            alerts = response.getBody();
            buildResourceData(response, "alerts");
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void doSingleResultApiCall(String url) {
        init();
        try {
            ResponseEntity<Alert> response = restTemplate.exchange(url, HttpMethod.GET, createEntity(null, null),
                    new ParameterizedTypeReference<Alert>() {
                    });
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            alert = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    protected void init() {
        super.init();
        alerts = null;
        alert = null;
    }

    public void getAlert(Long bookingId, Long alertId) {
        doSingleResultApiCall(API_REQUEST_BASE_URL + '/' + alertId);
    }

    public void verifyAlertField(String field, String value) {
        verifyField(alert, field, value);
    }
}
