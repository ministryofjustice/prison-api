package uk.gov.justice.hmpps.prison.executablespecification.steps;

import com.google.common.base.Splitter;
import lombok.Data;
import net.serenitybdd.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.justice.hmpps.prison.api.model.CaseNote;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteStaffUsage;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteStaffUsageRequest;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteUsage;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Case Note domain.
 */
public class CaseNoteSteps extends CommonSteps {
    private static final String API_REQUEST_FOR_CASENOTE_USAGE = API_PREFIX + "case-notes/usage";
    private static final String API_REQUEST_FOR_CASENOTE_SUMMARY = API_PREFIX + "case-notes/summary";
    private static final String API_REQUEST_FOR_CASENOTE_STAFF_USAGE = API_PREFIX + "case-notes/staff-usage";
    private static final String FROM_DATE_QUERY_PARAM_PREFIX = "&fromDate=";
    private static final String TO_DATE_QUERY_PARAM_PREFIX = "&toDate=";
    private static final String OFFENDER_NOS_QUERY_PARAM_PREFIX = "&offenderNo=";
    private static final String STAFF_IDS_QUERY_PARAM_PREFIX = "&staffId=";
    private static final String CASENOTE_TYPE_QUERY_PARAM_PREFIX = "&type=";
    private static final String CASENOTE_SUBTYPE_QUERY_PARAM_PREFIX = "&subType=";
    private static final String CASENOTE_STAFF_ID_QUERY_PARAM_PREFIX = "&staffId=";
    private static final String CASENOTE_AGENCY_ID_QUERY_PARAM_PREFIX = "&agencyId=";

    private List<CaseNoteUsage> caseNoteUsageList;
    private CaseNoteUsage caseNoteUsage;
    private List<CaseNoteStaffUsage> caseNoteStaffUsageList;
    private CaseNoteStaffUsage caseNoteStaffUsage;

    @Step("Initialisation")
    public void init() {
        super.init();
    }

    @Step("Get case note usage")
    public void getCaseNoteUsage(final String offenderNos, final String staffId, final String agencyId, final String type, final String subType, final String fromDate, final String toDate) {
        dispatchGetCaseNoteUsageRequest(offenderNos, staffId, agencyId, type, subType, fromDate, toDate);
    }

    @Step("Get case note summary by booking id")
    public void getCaseNoteUsageByBookingId(final String bookingIds, final String type, final String subType, final String fromDate, final String toDate) {
        dispatchGetCaseNoteUsageByBookingIdRequest(bookingIds, type, subType, fromDate, toDate);
    }

    @Step("Get case note staff usage")
    public void getCaseNoteStaffUsage(final String staffIds, final String type, final String subType, final String fromDate, final String toDate) {
        dispatchGetCaseNoteStaffUsageRequest(staffIds, type, subType, fromDate, toDate);
    }

    @Step("Verify case note usage response property value")
    public void verifyCaseNoteUsagePropertyValue(final String propertyName, final String expectedValue) throws Exception {
        verifyPropertyValue(caseNoteUsage, propertyName, expectedValue);
    }

    @Step("Verify case note staff usage response property value")
    public void verifyCaseNoteStaffUsagePropertyValue(final String propertyName, final String expectedValue) throws Exception {
        verifyPropertyValue(caseNoteStaffUsage, propertyName, expectedValue);
    }

    @Step("Verify case note usage size")
    public void verifyCaseNoteUsageSize(final int size) {
        assertThat(caseNoteUsageList).hasSize(size);
    }

    private void dispatchGetCaseNoteUsageRequest(final String offenderNos, final String staffId, final String agencyId, final String type, final String subType, final String fromDate, final String toDate) {
        init();

        final var queryBuilder = new StringBuilder();

        if (StringUtils.isNotBlank(offenderNos)) {
            final var nos = Arrays.asList(offenderNos.split(","));
            nos.forEach(offenderNo -> queryBuilder.append(OFFENDER_NOS_QUERY_PARAM_PREFIX).append(offenderNo));
        }

        if (StringUtils.isNotBlank(staffId)) {
            queryBuilder.append(CASENOTE_STAFF_ID_QUERY_PARAM_PREFIX).append(staffId);
        }

        if (StringUtils.isNotBlank(agencyId)) {
            queryBuilder.append(CASENOTE_AGENCY_ID_QUERY_PARAM_PREFIX).append(agencyId);
        }

        setQueryParams(type, subType, fromDate, toDate, queryBuilder);

        var urlModifier = "";

        if (!queryBuilder.isEmpty()) {
            urlModifier = "?" + queryBuilder.substring(1);
        }

        final var url = API_REQUEST_FOR_CASENOTE_USAGE + urlModifier;

        try {
            final var response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                createEntity(),
                new ParameterizedTypeReference<List<CaseNoteUsage>>() {
                });

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            caseNoteUsageList = response.getBody();
            caseNoteUsage = caseNoteUsageList.isEmpty() ? null : caseNoteUsageList.get(0);
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchGetCaseNoteUsageByBookingIdRequest(final String bookingIds, final String type, final String subType, final String fromDate, final String toDate) {
        init();

        final var queryBuilder = new StringBuilder();
        Splitter.on(',').split(bookingIds).forEach(bookingId -> queryBuilder.append("&bookingId=").append(bookingId));
        setQueryParams(type, subType, fromDate, toDate, queryBuilder);
        final var urlModifier = "?" + queryBuilder.substring(1);
        final var url = API_REQUEST_FOR_CASENOTE_SUMMARY + urlModifier;

        try {
            final var response = restTemplate.exchange(url, HttpMethod.GET, createEntity(),
                new ParameterizedTypeReference<List<CaseNoteUsage>>() {
                });

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            caseNoteUsageList = response.getBody();
            caseNoteUsage = caseNoteUsageList.isEmpty() ? null : caseNoteUsageList.get(0);
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchGetCaseNoteStaffUsageRequest(final String staffIds, final String type, final String subType, final String fromDate, final String toDate) {
        init();
        final var ids = Arrays.stream(staffIds.split(",")).map(Integer::parseInt).toList();
        final var requestBody = new CaseNoteStaffUsageRequest(
            null,
            ids,
            null,
            StringUtils.isEmpty(fromDate) ? null : LocalDate.parse(fromDate),
            StringUtils.isEmpty(toDate) ? null : LocalDate.parse(toDate),
            StringUtils.trimToNull(type),
            StringUtils.trimToNull(subType)
        );

        try {
            final var response = restTemplate.exchange(
                API_REQUEST_FOR_CASENOTE_STAFF_USAGE,
                HttpMethod.POST,
                createEntity(requestBody),
                new ParameterizedTypeReference<List<CaseNoteStaffUsage>>() {
                });

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            caseNoteStaffUsageList = response.getBody();
            caseNoteStaffUsage = caseNoteStaffUsageList.isEmpty() ? null : caseNoteStaffUsageList.get(0);
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void setQueryParams(final String type, final String subType, final String fromDate, final String toDate, final StringBuilder queryBuilder) {
        if (StringUtils.isNotBlank(type)) {
            queryBuilder.append(CASENOTE_TYPE_QUERY_PARAM_PREFIX).append(type);
        }

        if (StringUtils.isNotBlank(subType)) {
            queryBuilder.append(CASENOTE_SUBTYPE_QUERY_PARAM_PREFIX).append(subType);
        }

        if (StringUtils.isNotBlank(fromDate)) {
            queryBuilder.append(FROM_DATE_QUERY_PARAM_PREFIX).append(fromDate);
        }

        if (StringUtils.isNotBlank(toDate)) {
            queryBuilder.append(TO_DATE_QUERY_PARAM_PREFIX).append(toDate);
        }
    }

}
