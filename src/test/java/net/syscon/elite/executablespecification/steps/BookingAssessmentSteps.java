package net.syscon.elite.executablespecification.steps;

import com.google.common.collect.ImmutableList;
import net.syscon.elite.api.model.Assessment;
import net.syscon.elite.api.model.CategorisationDetail;
import net.syscon.elite.api.model.CategoryApprovalDetail;
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
import static org.springframework.http.HttpMethod.PUT;

public class BookingAssessmentSteps extends CommonSteps {
    private static final String API_BOOKING_PREFIX = API_PREFIX + "bookings/";
    private static final String API_ASSESSMENTS_PREFIX = API_PREFIX + "offender-assessments/";

    private Assessment assessment;
    private List<Assessment> assessments;
    private List<OffenderCategorise> offenderCatList;
    private ResponseEntity createUpdateResponse;

    public void getAssessmentByCode(final Long bookingId, final String assessmentCode) {
        doSingleResultApiCall(API_BOOKING_PREFIX + bookingId + "/assessment/" + assessmentCode);
    }

    public void getAssessments(final Long bookingId) {
        doListResultApiCall(API_BOOKING_PREFIX + bookingId + "/assessments");
    }

    private void doSingleResultApiCall(final String url) {
        init();
        try {
            final var response = restTemplate.exchange(url, HttpMethod.GET,
                    createEntity(), new ParameterizedTypeReference<Assessment>() {});
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assessment = response.getBody();
        } catch (final EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private List<Assessment> doMultipleResultApiCall(final String url) {
        init();
        try {
            final var response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            createEntity(),
                            new ParameterizedTypeReference<List<Assessment>>() {});

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            buildResourceData(response);
            return response.getBody();
        } catch (final EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
            return null;
        }
    }

    private List<Assessment> doMultipleResultApiCallWithPost(final String url, final List<String> offenderNoBody) {
        init();
        try {
            final var response =
                    restTemplate.exchange(
                            url,
                            POST,
                            createEntity(offenderNoBody),
                            new ParameterizedTypeReference<List<Assessment>>() {});

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            buildResourceData(response);
            return response.getBody();
        } catch (final EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
            return null;
        }
    }

    private void doListResultApiCall(final String url) {
        init();
        try {
            final var response = restTemplate.exchange(url, HttpMethod.GET,
                    createEntity(null, null), new ParameterizedTypeReference<List<Assessment>>() {});
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assessment = response.getBody().isEmpty() ? null : response.getBody().get(0);
            buildResourceData(response);
        } catch (final EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void doUncategorisedApiCall(final String agencyId) {
        init();
        try {
            final var response = restTemplate.exchange(API_ASSESSMENTS_PREFIX + "category/{agencyId}/uncategorised", HttpMethod.GET,
                    createEntity(), new ParameterizedTypeReference<List<OffenderCategorise>>() {}, agencyId);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            offenderCatList = response.getBody();
            buildResourceData(response);
        } catch (final EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void doGetCategorisedApiCall(final String agencyId, final String fromDate) {
        init();
        try {
            final var url = API_ASSESSMENTS_PREFIX + "category/{agencyId}/categorised" + (StringUtils.isNotBlank(fromDate) ? "?fromDate=" + fromDate : "");
            final var response = restTemplate.exchange(url, HttpMethod.GET,
                    createEntity(), new ParameterizedTypeReference<List<OffenderCategorise>>() {}, agencyId);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            offenderCatList = response.getBody();
            buildResourceData(response);
        } catch (final EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void doCreateCategorisationApiCall(final Long bookingId, final String category, final String committee) {
        init();
        try {
            createUpdateResponse =
                    restTemplate.exchange(
                            API_ASSESSMENTS_PREFIX + "category/categorise",
                            POST,
                            createEntity(CategorisationDetail.builder().bookingId(bookingId).category(category).committee(committee).build()), ResponseEntity.class);

        } catch (final EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void doApproveCategorisationApiCall(final Long bookingId, final String category, final LocalDate date, final String comment) {
        init();
        try {
            createUpdateResponse =
                    restTemplate.exchange(
                            API_ASSESSMENTS_PREFIX + "category/approve",
                            PUT,
                            createEntity(CategoryApprovalDetail.builder()
                                    .bookingId(bookingId)
                                    .category(category)
                                    .evaluationDate(date)
                                    .reviewSupLevelText(comment)
                                    .build()), ResponseEntity.class);

        } catch (final EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    @Override
    protected void init() {
        super.init();
        assessment = null;
        assessments = null;
        offenderCatList = null;
        createUpdateResponse = null;
    }

    public void verifyField(final String field, final String value) throws ReflectiveOperationException {
        assertNotNull(assessment);
        super.verifyField(assessment, field, value);
    }

    public void verifyCsra(final boolean csra) {
        assertThat(assessment.getCellSharingAlertFlag()).isEqualTo(csra);
    }

    public void verifyNextReviewDate(final String nextReviewDate) {
        verifyLocalDate(assessment.getNextReviewDate(), nextReviewDate);
    }

    public void getAssessmentsByCode(final String offenderList, final String assessmentCode, final boolean latestOnly) {
        final var query = "?offenderNo=" + offenderList.replace(",", "&offenderNo=") + "&latestOnly=" + latestOnly;
        assessments = doMultipleResultApiCall(API_ASSESSMENTS_PREFIX + assessmentCode + query);
    }

    public void getAssessmentsByCodeUsingPost(final String offenders, final String assessmentCode) {
        final List<String> offenderList = StringUtils.isNotBlank(offenders) ? ImmutableList.copyOf(offenders.split(",")) : Collections.emptyList();
        assessments = doMultipleResultApiCallWithPost(API_ASSESSMENTS_PREFIX + assessmentCode, offenderList);
    }

    public void getCsrasUsingPost(final String offenders) {
        final List<String> offenderList = StringUtils.isNotBlank(offenders) ? ImmutableList.copyOf(offenders.split(",")) : Collections.emptyList();
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

    public void getUncategorisedOffenders(final String agencyId) {
        doUncategorisedApiCall(agencyId);
    }

    public void verifyOffenderCategoryListSize(final int size) {
        verifyNoError();
        assertThat(offenderCatList).asList().hasSize(size);
    }

    public void verifyCategorisedPendingApproval(final long bookingId) {
        verifyNoError();
        assertThat(offenderCatList).extracting("bookingId", "status").contains(tuple(Long.valueOf(bookingId), CategorisationStatus.AWAITING_APPROVAL));
    }

    public void verifyCategorisedNotPresent(final long bookingId) {
        verifyNoError();
        assertThat(offenderCatList).asList().noneSatisfy(c -> {
            assertThat(((OffenderCategorise) c).getBookingId()).isEqualTo(bookingId);
        });
    }

    public void createCategorisation(final Long bookingId, final String category, final String committee) {
        doCreateCategorisationApiCall(bookingId, category, committee);
    }

    public void approveCategorisation(final Long bookingId, final String category, final LocalDate date, final String comment) {
        doApproveCategorisationApiCall(bookingId, category, date, comment);
    }

    public void getCategorisedOffenders(final String agencyId, final String fromDateString) {
        doGetCategorisedApiCall(agencyId, fromDateString);
    }
}
