package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.Assessment;
import net.syscon.elite.test.EliteClientException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

public class BookingAssessmentSteps extends CommonSteps {
    private static final String API_BOOKING_PREFIX = API_PREFIX + "bookings/";

    private Assessment assessment;

    public void getAssessmentByCode(Long bookingId, String assessmentCode) {
        doSingleResultApiCall(API_BOOKING_PREFIX + bookingId + "/assessment/" + assessmentCode);
    }

    private void doSingleResultApiCall(String url) {
        init();
        try {
            ResponseEntity<Assessment> response = restTemplate.exchange(url, HttpMethod.GET,
                    createEntity(null, null), new ParameterizedTypeReference<Assessment>() {});
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assessment = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    protected void init() {
        super.init();
        assessment = null;
    }

    public void verifyField(String field, String value) throws ReflectiveOperationException {
        assertNotNull(assessment);
        super.verifyField(assessment, field, value);
    }

    public void verifyCsra(boolean csra) {
        assertThat(assessment.getCellSharingAlertFlag()).isEqualTo(csra);
    }

    public void verifyNextReviewDate(String nextReviewDate) {
        verifyLocalDate(assessment.getNextReviewDate(),nextReviewDate);
    }
}
