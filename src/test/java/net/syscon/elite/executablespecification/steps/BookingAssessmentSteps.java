package net.syscon.elite.executablespecification.steps;

import com.google.common.collect.ImmutableList;
import net.syscon.elite.api.model.Assessment;
import net.syscon.elite.api.model.CategorisationDetail;
import net.syscon.elite.api.model.OffenderCategorise;
import net.syscon.elite.api.support.CategorisationStatus;
import net.syscon.elite.test.EliteClientException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.assertNotNull;
import static org.springframework.http.HttpMethod.POST;

public class BookingAssessmentSteps extends CommonSteps {
    private static final String API_BOOKING_PREFIX = API_PREFIX + "bookings/";
    private static final String API_ASSESSMENTS_PREFIX = API_PREFIX + "offender-assessments/";

    private Assessment assessment;
    private List<Assessment> assessments;
    private List<OffenderCategorise> uncategorised;
    private ResponseEntity createUpdateResponse;

    public void getAssessmentByCode(Long bookingId, String assessmentCode) {
        doSingleResultApiCall(API_BOOKING_PREFIX + bookingId + "/assessment/" + assessmentCode);
    }

    public void getAssessments(Long bookingId) {
        doListResultApiCall(API_BOOKING_PREFIX + bookingId + "/assessments");
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

    private List<Assessment> doMultipleResultApiCallWithPost(String url, List<String> offenderNoBody) {
        init();
        try {
            ResponseEntity<List<Assessment>> response =
                    restTemplate.exchange(
                            url,
                            POST,
                            createEntity(offenderNoBody),
                            new ParameterizedTypeReference<List<Assessment>>() {});

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            buildResourceData(response);
            return response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
            return null;
        }
    }

    private void doListResultApiCall(String url) {
        init();
        try {
            ResponseEntity<List<Assessment>> response = restTemplate.exchange(url, HttpMethod.GET,
                    createEntity(null, null), new ParameterizedTypeReference<List<Assessment>>() {});
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assessment = response.getBody().isEmpty() ? null : response.getBody().get(0);
            buildResourceData(response);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void doUncategorisedApiCall(String agencyId) {
        init();
        try {
            ResponseEntity<List<OffenderCategorise>> response = restTemplate.exchange(API_ASSESSMENTS_PREFIX + "category/{agencyId}/uncategorised", HttpMethod.GET,
                    createEntity(), new ParameterizedTypeReference<List<OffenderCategorise>>() {}, agencyId);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            uncategorised = response.getBody();
            buildResourceData(response);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void doCreateCategorisationApiCall(Long bookingId, String category, String committee) {
        init();
        try {
            createUpdateResponse =
                    restTemplate.exchange(
                            API_ASSESSMENTS_PREFIX  + "category/categorise",
                            POST,
                            createEntity(CategorisationDetail.builder().bookingId(bookingId).category(category).committee(committee).build()), ResponseEntity.class);


        } catch (EliteClientException ex) {
            createUpdateResponse = null;
            setErrorResponse(ex.getErrorResponse());
        }
    }

    @Override
    protected void init() {
        super.init();
        assessment = null;
        assessments = null;
        uncategorised = null;
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

    public void getAssessmentsByCode(String offenderList, String assessmentCode, boolean latestOnly) {
        final String query = "?offenderNo=" + offenderList.replace(",", "&offenderNo=") + "&latestOnly=" + latestOnly;
        assessments = doMultipleResultApiCall(API_ASSESSMENTS_PREFIX + assessmentCode + query);
    }

    public void getAssessmentsByCodeUsingPost(String offenders, String assessmentCode) {
        List<String> offenderList = StringUtils.isNotBlank(offenders) ? ImmutableList.copyOf(offenders.split(",")) : Collections.emptyList();
        assessments = doMultipleResultApiCallWithPost(API_ASSESSMENTS_PREFIX + assessmentCode, offenderList);
    }

    public void getCsrasUsingPost(String offenders) {
        List<String> offenderList = StringUtils.isNotBlank(offenders) ? ImmutableList.copyOf(offenders.split(",")) : Collections.emptyList();
        assessments = doMultipleResultApiCallWithPost(API_ASSESSMENTS_PREFIX + "csra/list", offenderList);
    }

    public void verifyMultipleAssessments() {
        verifyNoError();
        assertThat(assessments).asList()
                .extracting("bookingId", "offenderNo", "classification", "assessmentCode", "cellSharingAlertFlag", "nextReviewDate")
                .contains(tuple(-1L, "A1234AA", "High", "CSR", true, LocalDate.of(2018, Month.JUNE, 1)),
                        tuple(-2L, "A1234AB", null, "CSR", true, LocalDate.of(2018, Month.JUNE, 2)),
                        tuple(-3L, "A1234AC", "Low", "CSR", true, LocalDate.of(2018, Month.JUNE, 3)),
                        tuple(-4L, "A1234AD", "Medium", "CSR", true, LocalDate.of(2018, Month.JUNE, 4)),
                        tuple(-5L, "A1234AE", "High", "CSR", true, LocalDate.of(2018, Month.JUNE, 5)),
                        tuple(-6L, "A1234AF", "Standard", "CSR", true, LocalDate.of(2018, Month.JUNE, 6)));
    }

    public void verifyMultipleCategoryAssessments() {
        verifyNoError();
        assertThat(assessments).asList()
                .extracting("bookingId", "offenderNo", "classification", "assessmentCode", "nextReviewDate")
                .containsExactlyInAnyOrder(tuple(-6L, "A1234AF", "Cat C", "CATEGORY", LocalDate.of(2018, Month.JUNE, 7)),
                        tuple(-48L, "A1234AF", "Cat A", "CATEGORY", LocalDate.of(2016, Month.JUNE, 8)));
    }

    public void getUncategorisedOffenders(String agencyId) {
        doUncategorisedApiCall(agencyId);
    }

    public void verifyUncategorisedOffenders(int size) {
        verifyNoError();
        assertThat(uncategorised).asList().hasSize(size);
    }

    public void verifyCategorisedPendingApproval(long bookingId) {
        verifyNoError();
        assertThat(uncategorised).extracting("bookingId", "status").contains(tuple(Long.valueOf(bookingId), CategorisationStatus.AWAITING_APPROVAL));
    }

    public void createCategorisation(long bookingId, String category, String committee) {
        doCreateCategorisationApiCall(bookingId, category, committee);
    }
}
