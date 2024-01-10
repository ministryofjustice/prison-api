package uk.gov.justice.hmpps.prison.executablespecification.steps;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.hmpps.prison.api.model.Assessment;
import uk.gov.justice.hmpps.prison.api.model.CategoryApprovalDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenderCategorise;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

public class BookingAssessmentSteps extends CommonSteps {
    private static final String API_ASSESSMENTS_PREFIX = API_PREFIX + "offender-assessments/";

    private List<Assessment> assessments;
    private List<OffenderCategorise> offenderCatList;

    private List<Assessment> doMultipleResultApiCall(final String url) {
        init();
        try {
            final var response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            createEntity(),
                            new ParameterizedTypeReference<List<Assessment>>() {
                            });

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            buildResourceData(response);
            return response.getBody();
        } catch (final PrisonApiClientException ex) {
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
                            new ParameterizedTypeReference<List<Assessment>>() {
                            });

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            buildResourceData(response);
            return response.getBody();
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
            return null;
        }
    }

    private void doGetCategoryApiCall(final String agencyId, final String type, final String date) {
        init();
        try {
            final var url = API_ASSESSMENTS_PREFIX + "category/{agencyId}?type={type}" + (StringUtils.isNotBlank(date) ? "&date=" + date : "");
            final var response = restTemplate.exchange(url, HttpMethod.GET,
                    createEntity(), new ParameterizedTypeReference<List<OffenderCategorise>>() {
                    }, agencyId, type);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            offenderCatList = response.getBody();
            buildResourceData(response);
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void doApproveCategorisationApiCall(final Long bookingId, final String category, final LocalDate date, final String comment) {
        init();
        try {
            restTemplate.exchange(
                API_ASSESSMENTS_PREFIX + "category/approve",
                PUT,
                createEntity(CategoryApprovalDetail.builder()
                        .bookingId(bookingId)
                        .category(category)
                        .evaluationDate(date)
                        .approvedCategoryComment(comment)
                        .reviewCommitteeCode("GOV")
                        .build()), ResponseEntity.class);

        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    @Override
    protected void init() {
        super.init();
        assessments = null;
        offenderCatList = null;
    }

    public void getAssessmentsByCode(final String offenderList, final String assessmentCode, final boolean latestOnly, final boolean activeOnly) {
        final var query = "?offenderNo=" + offenderList.replace(",", "&offenderNo=") + "&latestOnly=" + latestOnly + "&activeOnly=" + activeOnly;
        assessments = doMultipleResultApiCall(API_ASSESSMENTS_PREFIX + assessmentCode + query);
    }

    public void getAssessmentsByCodeUsingPost(final String offenders, final String assessmentCode) {
        final List<String> offenderList = StringUtils.isNotBlank(offenders) ? ImmutableList.copyOf(offenders.split(",")) : Collections.emptyList();
        final var query = "?latestOnly=true&activeOnly=true";
        assessments = doMultipleResultApiCallWithPost(API_ASSESSMENTS_PREFIX + assessmentCode + query, offenderList);
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
                        tuple(-3L, "A1234AC", "Low", "CSR", true, LocalDate.of(2018, Month.JUNE, 3)),
                        tuple(-5L, "A1234AE", "High", "CSR", true, LocalDate.of(2018, Month.JUNE, 5)),
                        tuple(-6L, "A1234AF", "Standard", "CSR", true, LocalDate.of(2018, Month.JUNE, 6)));
    }

    public void verifyMultipleCategoryAssessments() {
        verifyNoError();
        assertThat(assessments).asList()
                .extracting("bookingId", "offenderNo", "classification", "assessmentCode", "nextReviewDate", "assessmentAgencyId", "assessmentDate", "approvalDate")
                .containsExactlyInAnyOrder(tuple(-6L, "A1234AF", "Cat C", "CATEGORY", LocalDate.of(2018, Month.JUNE, 7), null, LocalDate.of(2017, Month.APRIL, 4), null),
                        tuple(-48L, "A1234AF", "Cat A", "CATEGORY", LocalDate.of(2016, Month.AUGUST, 8), "LEI", LocalDate.of(2016, Month.APRIL, 4), LocalDate.of(2016, Month.JULY, 7)),
                        tuple(-48L, "A1234AF", "Cat B", "CATEGORY", LocalDate.of(2018, Month.MAY, 8), "MDI", LocalDate.of(2016, Month.MAY, 4), LocalDate.of(2016, Month.MAY, 9)),
                        tuple(-48L, "A1234AF", "Cat B", "CATEGORY", LocalDate.of(2016, Month.MARCH, 8), "MDI", LocalDate.of(2016, Month.MARCH, 4), LocalDate.of(2016, Month.MARCH, 9)), // INACTIVE categorisation
                        tuple(-5L, "A1234AE", "Unclass", "CATEGORY", LocalDate.of(2016, Month.JUNE, 8), null, LocalDate.of(2016, Month.APRIL, 4), null));
    }

    public void getUncategorisedOffenders(final String agencyId) {
        doGetCategoryApiCall(agencyId, "UNCATEGORISED", null);
    }

    public void verifyOffenderCategoryListSize(final int size) {
        verifyNoError();
        assertThat(offenderCatList).asList().hasSize(size);
    }

    public void verifyOffenderCategoryListNotEmpty() {
        verifyNoError();
        assertThat(offenderCatList).asList().isNotEmpty();
    }

    public void approveCategorisation(final Long bookingId, final String category, final LocalDate date, final String comment) {
        doApproveCategorisationApiCall(bookingId, category, date, comment);
    }

    public void getCategorisedOffenders(final String agencyId, final String fromDateString) {
        doGetCategoryApiCall(agencyId, "CATEGORISED", fromDateString);
    }

    public void getRecategorise(final String agencyId, final String dateString) {
        doGetCategoryApiCall(agencyId, "RECATEGORISATIONS", dateString);
    }
}
