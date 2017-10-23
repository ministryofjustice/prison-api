package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.Account;
import net.syscon.elite.test.EliteClientException;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Reference Domains service.
 */
public class FinanceSteps extends CommonSteps {
    private static final String API_BOOKING_PREFIX = API_PREFIX + "bookings/";

    private Account result;

    public void getAccount(Long bookingId) {
        doSingleResultApiCall(API_BOOKING_PREFIX + bookingId + "/balances");
    }

    private void doSingleResultApiCall(String url) {
        init();
        try {
            ResponseEntity<Account> response = restTemplate.exchange(url, HttpMethod.GET, createEntity(null, null),
                    new ParameterizedTypeReference<Account>() {
                    });
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            result = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    protected void init() {
        super.init();
        result = null;
    }

    public void verifyField(String field, String value) throws ReflectiveOperationException {
        super.verifyField(result, field, value);
    }

    public void getNonexistentAccount() {
        doSingleResultApiCall(API_BOOKING_PREFIX + "-100000000001/balances");
    }

    public void getAccountInDifferentCaseload() {
        doSingleResultApiCall(API_BOOKING_PREFIX + "-16/balances");
    }
}
