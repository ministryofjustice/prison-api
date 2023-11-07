package uk.gov.justice.hmpps.prison.executablespecification.steps;

import net.serenitybdd.annotations.Step;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.justice.hmpps.prison.api.model.Account;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Reference Domains service.
 */
public class FinanceSteps extends CommonSteps {
    private static final String API_BOOKING_PREFIX = API_PREFIX + "bookings/";

    private Account result;

    @Step("Get offender account balances")
    public void getAccount(final Long bookingId) {
        doSingleResultApiCall(API_BOOKING_PREFIX + bookingId + "/balances");
    }

    private void doSingleResultApiCall(final String url) {
        init();
        try {
            final var response = restTemplate.exchange(url, HttpMethod.GET, createEntity(null, null),
                    new ParameterizedTypeReference<Account>() {
                    });
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            result = response.getBody();
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    @Override
    protected void init() {
        super.init();
        result = null;
    }

    public void verifyField(final String field, final String expectedValue) throws ReflectiveOperationException {
        super.verifyField(result, field, expectedValue);
    }
}
