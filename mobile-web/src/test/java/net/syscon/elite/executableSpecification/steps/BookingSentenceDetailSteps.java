package net.syscon.elite.executableSpecification.steps;

import net.serenitybdd.core.PendingStepException;
import net.syscon.elite.test.EliteClientException;
import net.syscon.elite.v2.api.model.SentenceDetail;
import net.thucydides.core.annotations.Step;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

/**
 * BDD step implementations for Booking Sentence Detail feature.
 */
public class BookingSentenceDetailSteps extends CommonSteps {
    private static final String BOOKING_SENTENCE_DETAIL_API_URL = V2_API_PREFIX + "bookings/{bookingId}/sentenceDetail";

    private SentenceDetail sentenceDetail;

    @Step("Get booking sentence detail")
    public void getBookingSentenceDetail(Long bookingId) {
        dispatchRequest(bookingId);
    }

    @Step("Verify sentence start date")
    public void verifySentenceStartDate(String sentenceStartDate) {
        throw new PendingStepException("Pending implementation.");
    }

    @Step("Verify sentence end date")
    public void verifySentenceEndDate(String sentenceEndDate) {
        throw new PendingStepException("Pending implementation.");
    }

    private void dispatchRequest(Long bookingId) {
        init();

        ResponseEntity<SentenceDetail> response;

        try {
            response =
                    restTemplate.exchange(
                            BOOKING_SENTENCE_DETAIL_API_URL,
                            HttpMethod.GET,
                            createEntity(),
                            SentenceDetail.class,
                            bookingId);

            sentenceDetail = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }
}
