package net.syscon.elite.executableSpecification.steps;

import net.syscon.elite.test.EliteClientException;
import net.syscon.elite.v2.api.model.SentenceDetail;
import net.thucydides.core.annotations.Step;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(sentenceDetail.getSentenceStartDate()).isEqualTo(sentenceStartDate);
    }

    @Step("Verify sentence end date")
    public void verifySentenceEndDate(String sentenceEndDate) {
      //  assertThat(sentenceDetail.getSentenceEndDate()).isEqualTo(StringUtils.trimToEmpty(sentenceEndDate));
    }

    @Step("Verify conditional release date")
    public void verifyConditionalReleaseDate(String conditionalReleaseDate) {
      //  assertThat(sentenceDetail.getConditionalReleaseDate()).isEqualTo(StringUtils.trimToEmpty(conditionalReleaseDate));
    }

    @Step("Verify release date")
    public void verifyReleaseDate(String releaseDate) {
        assertThat(sentenceDetail.getReleaseDate()).isEqualTo(releaseDate);
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
