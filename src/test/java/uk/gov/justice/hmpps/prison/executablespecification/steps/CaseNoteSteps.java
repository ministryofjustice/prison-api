package uk.gov.justice.hmpps.prison.executablespecification.steps;

import com.google.common.base.Splitter;
import lombok.Data;
import net.thucydides.core.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.justice.hmpps.prison.api.model.CaseNote;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteCount;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteStaffUsage;
import uk.gov.justice.hmpps.prison.api.model.CaseNoteUsage;
import uk.gov.justice.hmpps.prison.api.model.NewCaseNote;
import uk.gov.justice.hmpps.prison.api.model.UpdateCaseNote;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CaseNoteFilter;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Case Note domain.
 */
public class CaseNoteSteps extends CommonSteps {
    private static final String API_REQUEST_BASE_URL = API_PREFIX + "bookings/{bookingId}/caseNotes";
    private static final String API_REQUEST_FOR_CASENOTE = API_REQUEST_BASE_URL + "/{caseNoteId}";
    private static final String API_REQUEST_FOR_CASENOTE_COUNT = API_REQUEST_BASE_URL + "/{type}/{subType}/count";
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

    private CaseNote caseNote;
    private NewCaseNote pendingCaseNote;
    private List<CaseNote> caseNotes;
    private CaseNoteCount caseNoteCount;
    private List<CaseNoteUsage> caseNoteUsageList;
    private CaseNoteUsage caseNoteUsage;
    private List<CaseNoteStaffUsage> caseNoteStaffUsageList;
    private CaseNoteStaffUsage caseNoteStaffUsage;

    @Value("${api.caseNote.sourceCode:AUTO}")
    private String caseNoteSource;
    private CaseNoteFilter caseNoteFilter;

    @Step("Initialisation")
    public void init() {
        super.init();

        caseNote = null;
        pendingCaseNote = null;
        caseNoteFilter = CaseNoteFilter.builder().build();
        caseNoteCount = null;
    }

    @Step("Verify case note")
    public void verify() {
        assertThat(caseNote).isNotNull();
        assertThat(caseNote.getCaseNoteId()).isGreaterThan(0);
        assertThat(caseNote.getType()).isEqualTo(pendingCaseNote.getType());
        assertThat(caseNote.getSubType()).isEqualTo(pendingCaseNote.getSubType());
        assertThat(caseNote.getText()).isEqualTo(pendingCaseNote.getText());
        assertThat(caseNote.getOccurrenceDateTime()).isEqualTo(pendingCaseNote.getOccurrenceDateTime());
        assertThat(caseNote.getCreationDateTime()).isNotNull();
    }

    @Step("Verify case note not created")
    public void verifyNotCreated() {
        assertThat(caseNote).isNull();
    }

    @Step("Verify case note source")
    public void verifyCaseNoteSource() {
        assertThat(caseNote.getSource()).isEqualTo(caseNoteSource);
    }

    @Step("Create case note")
    public CaseNote createCaseNote(final Long bookingId, final NewCaseNote newCaseNote) {
        pendingCaseNote = newCaseNote;
        final var caseNoteId = dispatchCreateRequest(bookingId, newCaseNote);

        if (caseNoteId != null) {
            dispatchGetRequest(bookingId, caseNoteId);
        } else {
            caseNote = null;
        }

        return caseNote;
    }

    @Step("Get case notes")
    public void getCaseNotes(final Long bookingId) {
        dispatchQueryRequest(bookingId);
    }

    @Step("Get case note count")
    public void getCaseNoteCount(final long bookingId, final String type, final String subType, final String fromDate, final String toDate) {
        dispatchGetCaseNoteCountRequest(bookingId, type, subType, fromDate, toDate);
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

    @Step("Verify case note types")
    public void verifyCaseNoteTypes(final String caseNoteTypes) {
        verifyPropertyValues(caseNotes, CaseNote::getType, caseNoteTypes);
    }

    @Step("Verify case note sub types")
    public void verifyCaseNoteSubTypes(final String caseNoteSubTypes) {
        verifyPropertyValues(caseNotes, CaseNote::getSubType, caseNoteSubTypes);
    }

    @Step("Verify case note count response property value")
    public void verifyCaseNoteCountPropertyValue(final String propertyName, final String expectedValue) throws Exception {
        verifyPropertyValue(caseNoteCount, propertyName, expectedValue);
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

    @Step("Verify case note usage size")
    public void verifyCaseNoteStaffUsageSize(final int size) {
        assertThat(size).isEqualTo(caseNoteStaffUsageList.size());
    }


    @Step("Apply case note type filter")
    public void applyCaseNoteTypeFilter(final String caseNoteType) {
        if (StringUtils.isNotBlank(caseNoteType)) {
            caseNoteFilter = caseNoteFilter.toBuilder().type(caseNoteType).build();
        }
    }

    @Step("Apply case note sub type filter")
    public void applyCaseNoteSubTypeFilter(final String caseNoteSubType) {
        if (StringUtils.isNotBlank(caseNoteSubType)) {
            caseNoteFilter = caseNoteFilter.toBuilder().subType(caseNoteSubType).build();
        }
    }

    @Step("Apply case note agency filter")
    public void applyAgencyFilter(final String agencyId) {
        if (StringUtils.isNotBlank(agencyId)) {
            caseNoteFilter = caseNoteFilter.toBuilder().prisonId(agencyId).build();
        }
    }


    @Step("Apply date from filter")
    public void applyDateFromFilter(final String dateFrom) {
        if (StringUtils.isNotBlank(dateFrom)) {
            caseNoteFilter = caseNoteFilter.toBuilder().startDate(LocalDate.parse(dateFrom)).build();
        }
    }

    @Step("Apply date to filter")
    public void applyDateToFilter(final String dateTo) {
        if (StringUtils.isNotBlank(dateTo)) {
            caseNoteFilter = caseNoteFilter.toBuilder().endDate(LocalDate.parse(dateTo)).build();
        }
    }

    private Long dispatchCreateRequest(final Long bookingId, final NewCaseNote caseNote) {
        Long caseNoteId;

        try {
            final var response = restTemplate.exchange(API_REQUEST_BASE_URL, HttpMethod.POST, createEntity(caseNote),
                    CaseNote.class, bookingId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

            caseNoteId = response.getBody().getCaseNoteId();
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());

            caseNoteId = null;
        }

        return caseNoteId;
    }

    private void dispatchGetRequest(final Long bookingId, final Long caseNoteId) {
        try {
            final var response = restTemplate.exchange(API_REQUEST_FOR_CASENOTE, HttpMethod.GET,
                    createEntity(), CaseNote.class, bookingId, caseNoteId);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            caseNote = response.getBody();
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    @Data
    public static class CaseNoteWrapper {
        private List<CaseNote> content;
    }

    private void dispatchQueryRequest(final Long bookingId) {
        caseNotes = null;

        StringBuilder params = new StringBuilder();

        if (caseNoteFilter.getStartDate() != null) {
            if (params.length() == 0) { params.append("?"); } else { params.append("&"); }
            params.append("from=").append(caseNoteFilter.getStartDate());
        }

        if (caseNoteFilter.getEndDate() != null) {
            if (params.length() == 0) { params.append("?"); } else { params.append("&"); }
            params.append("to=").append(caseNoteFilter.getEndDate());
        }

        if (caseNoteFilter.getType() != null) {
            if (params.length() == 0) { params.append("?"); } else { params.append("&"); }
            params.append("type=").append(caseNoteFilter.getType());
        }

        if (caseNoteFilter.getSubType() != null) {
            if (params.length() == 0) { params.append("?"); } else { params.append("&"); }
            params.append("subType=").append(caseNoteFilter.getSubType());
        }

        if (caseNoteFilter.getPrisonId() != null) {
            if (params.length() == 0) { params.append("?"); } else { params.append("&"); }
            params.append("prisonId=").append(caseNoteFilter.getPrisonId());
        }

        if (params.length() == 0) { params.append("?"); } else { params.append("&"); }
        params.append(getPaginationParams());

        try {
            final var response = restTemplate.exchange(API_REQUEST_BASE_URL + params, HttpMethod.GET,
                    createEntity(null, addPaginationHeaders()), new ParameterizedTypeReference<RestResponsePage<CaseNote>>() {
                }, bookingId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            buildResourceData(response.getBody());
            caseNotes = response.getBody().getContent();
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchGetCaseNoteCountRequest(final Long bookingId, final String type, final String subType, final String fromDate, final String toDate) {
        init();

        var urlModifier = "";

        if (StringUtils.isNotBlank(fromDate)) {
            urlModifier += (FROM_DATE_QUERY_PARAM_PREFIX + fromDate);
        }

        if (StringUtils.isNotBlank(toDate)) {
            urlModifier += (TO_DATE_QUERY_PARAM_PREFIX + toDate);
        }

        if (StringUtils.isNotBlank(urlModifier)) {
            urlModifier = "?" + urlModifier.substring(1);
        }

        final var url = API_REQUEST_FOR_CASENOTE_COUNT + urlModifier;

        try {
            final var response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createEntity(),
                    CaseNoteCount.class,
                    bookingId,
                    type,
                    subType);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            caseNoteCount = response.getBody();
        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
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

        if (queryBuilder.length() > 0) {
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

        final var queryBuilder = new StringBuilder();

        if (StringUtils.isNotBlank(staffIds)) {
            final var ids = Arrays.asList(staffIds.split(","));
            ids.forEach(staffId -> queryBuilder.append(STAFF_IDS_QUERY_PARAM_PREFIX).append(staffId));
        }

        setQueryParams(type, subType, fromDate, toDate, queryBuilder);

        var urlModifier = "";

        if (queryBuilder.length() > 0) {
            urlModifier = "?" + queryBuilder.substring(1);
        }

        final var url = API_REQUEST_FOR_CASENOTE_STAFF_USAGE + urlModifier;

        try {
            final var response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createEntity(),
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
