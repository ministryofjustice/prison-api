package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.MainSentence;
import net.syscon.elite.test.EliteClientException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

public class BookingSentenceSteps extends CommonSteps {
    private static final String API_BOOKING_PREFIX = API_PREFIX + "bookings/";

    private MainSentence mainSentence;

    public void getMainSentence(Long bookingId) {
        doSingleResultApiCall(API_BOOKING_PREFIX + bookingId + "/mainSentence");
    }

    private void doSingleResultApiCall(String url) {
        init();
        try {
            final ParameterizedTypeReference<MainSentence> ref = new ParameterizedTypeReference<MainSentence>() {
            };
            ResponseEntity<MainSentence> response = restTemplate.exchange(url, HttpMethod.GET, createEntity(null, null), ref);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            mainSentence = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    protected void init() {
        super.init();
        mainSentence = null;
    }

    public void verifyField(String field, String value) throws ReflectiveOperationException {
        assertNotNull(mainSentence);
        super.verifyField(mainSentence, field, value);
    }
    
    public void getNonexistentMainSentence() {
        doSingleResultApiCall(API_BOOKING_PREFIX + "-100000000001/mainSentence");
    }

    public void getMainSentenceInDifferentCaseload() {
        doSingleResultApiCall(API_BOOKING_PREFIX + "-16/mainSentence");
    }
}
