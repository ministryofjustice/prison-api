package net.syscon.elite.executableSpecification.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import net.syscon.elite.api.model.Account;
import net.syscon.elite.test.EliteClientException;

/**
 * BDD step implementations for Reference Domains service.
 */
public class FinanceSteps extends CommonSteps {
    private static final String API_BOOKING_PREFIX = API_PREFIX + "bookings/";

    private Account result;

    public void getAccount(Long bookingId) {
        doSingleResultApiCall(API_BOOKING_PREFIX + bookingId + "/balances");
    }

    public void verifyField(String fieldName, String expected) throws ReflectiveOperationException {
        final String actual = BeanUtilsBean.getInstance().getProperty(result, fieldName);
        if (StringUtils.isBlank(expected)) {
            assertNull(actual);
        } else {
            assertEquals(expected, actual);
        }
    }

    private void doSingleResultApiCall(String url) {
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

    public void getNonexistentAccount() {
        doSingleResultApiCall(API_BOOKING_PREFIX + "-100000000001/balances");
    }
}
