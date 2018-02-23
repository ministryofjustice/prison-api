package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.Assessment;
import net.syscon.elite.test.EliteClientException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.assertNotNull;

public class BookingAssessmentSteps extends CommonSteps {
    private static final String API_BOOKING_PREFIX = API_PREFIX + "bookings/";

    private Assessment assessment;
    private List<Assessment> assessments;

    public void getAssessmentByCode(Long bookingId, String assessmentCode) {
        doSingleResultApiCall(API_BOOKING_PREFIX + bookingId + "/assessment/" + assessmentCode);
    }

    private void doSingleResultApiCall(String url) {
        init();
        try {
            ResponseEntity<Assessment> response = restTemplate.exchange(url, HttpMethod.GET,
                    createEntity(), new ParameterizedTypeReference<Assessment>() {});
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assessment = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }
    
    private List<Assessment> doMultipleResultApiCall(String url) {
        init();
        try {
            ResponseEntity<List<Assessment>> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            createEntity(),
                            new ParameterizedTypeReference<List<Assessment>>() {});

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            buildResourceData(response);
            return response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
            return null;
        }
    }

    @Override
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
        verifyLocalDate(assessment.getNextReviewDate(), nextReviewDate);
    }

    public void getAssessmentsByCode(String bookingIdList, String assessmentCode) {
        final String query = "?bookingId=" + bookingIdList.replace(",", "&bookingId=");
        assessments = doMultipleResultApiCall(API_BOOKING_PREFIX + "/assessments/" + assessmentCode + query);
    }

    public void verifyMultipleAssessments() {
        assertThat(assessments).asList()
                .extracting("bookingId", "classification", "assessmentCode", "cellSharingAlertFlag", "nextReviewDate")
                .contains(tuple(-1L, "High", "CSR", true, LocalDate.of(2018, Month.JUNE, 1)),
                        tuple(-2L, null, "CSR", true, LocalDate.of(2018, Month.JUNE, 2)),
                        tuple(-3L, "Low", "CSR", true, LocalDate.of(2018, Month.JUNE, 3)),
                        tuple(-4L, "Medium", "CSR", true, LocalDate.of(2018, Month.JUNE, 4)),
                        tuple(-5L, "High", "CSR", true, LocalDate.of(2018, Month.JUNE, 5)),
                        tuple(-6L, "Standard", "CSR", true, LocalDate.of(2018, Month.JUNE, 6)));
    }
}
