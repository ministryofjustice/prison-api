package uk.gov.justice.hmpps.prison.executablespecification.steps;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.hmpps.prison.api.model.CategoryApprovalDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenderCategorise;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.PUT;

public class BookingAssessmentSteps extends CommonSteps {
    private static final String API_ASSESSMENTS_PREFIX = API_PREFIX + "offender-assessments/";

    private List<OffenderCategorise> offenderCatList;

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
        offenderCatList = null;
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
