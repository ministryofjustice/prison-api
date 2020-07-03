package uk.gov.justice.hmpps.prison.executablespecification.steps;

import net.thucydides.core.annotations.Step;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.api.model.IepLevelAndComment;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class AddIepLevelSteps extends CommonSteps {

    private static final String ADD_IEP_LEVEL_URL = API_PREFIX + "bookings/{bookingId}/iepLevels";

    private long bookingId;
    private IepLevelAndComment iepLevelAndComment;
    private Integer httpStatus;
    private ErrorResponse errorResponse;

    @Step("set Booking id")
    public void setBookingId(long bookingId) {
        reset();
        this.bookingId = bookingId;
    }

    @Step("set IEP Level and comment")
    public void setIepLevelAndComment(String iepLevel, String comment) {
        iepLevelAndComment = IepLevelAndComment.builder().iepLevel(iepLevel).comment(comment).build();
    }

    @Step("Add IEP Level")
    public void addIepLevel() {
        try {
            ResponseEntity<Void> responseEntity = restTemplate.postForEntity(
                    ADD_IEP_LEVEL_URL,
                    createEntity(iepLevelAndComment),
                    Void.class,
                    Map.of("bookingId", bookingId));
            httpStatus = responseEntity.getStatusCodeValue();
        } catch (final RestClientException e) {
            extractHttpStatusCode(e);
        } catch (final PrisonApiClientException e) {
            errorResponse = e.getErrorResponse();
            httpStatus = errorResponse.getStatus();
        } catch (final Throwable t) {
            fail("Unexpected outcome from Http PUT request", t);
        }
    }

    @Step("Assert HTTP Response status code")
    public void assertHttpResponseStatusCode(int expectedHttpStatusCode) {
        assertThat(httpStatus).isEqualTo(expectedHttpStatusCode);
    }

    private void extractHttpStatusCode(final RestClientException e) {
        if (e instanceof HttpStatusCodeException) {
            httpStatus = ((HttpStatusCodeException) e).getRawStatusCode();
        }
    }

    private void reset() {
        iepLevelAndComment = null;
        httpStatus = null;
        errorResponse = null;
    }

    public void assertErrorMessageContains(String expectedMessage) {
        assertThat(errorResponse).describedAs("Error Response").isNotNull();
        assertThat(errorResponse.getUserMessage()).contains(expectedMessage);
    }
}
