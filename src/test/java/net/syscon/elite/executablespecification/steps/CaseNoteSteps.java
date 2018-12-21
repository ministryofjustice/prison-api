package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.*;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * BDD step implementations for Case Note domain.
 */
public class CaseNoteSteps extends CommonSteps {
    private static final String API_REQUEST_BASE_URL = API_PREFIX + "bookings/{bookingId}/caseNotes";
    private static final String API_REQUEST_FOR_CASENOTE = API_REQUEST_BASE_URL + "/{caseNoteId}";
    private static final String API_REQUEST_FOR_CASENOTE_COUNT = API_REQUEST_BASE_URL + "/{type}/{subType}/count";
    private static final String API_REQUEST_FOR_CASENOTE_USAGE = API_PREFIX + "case-notes/usage";
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
    private String caseNoteFilter;
    private List<CaseNote> caseNotes;
    private CaseNoteCount caseNoteCount;
    private List<CaseNoteUsage> caseNoteUsageList;
    private CaseNoteUsage caseNoteUsage;
    private List<CaseNoteStaffUsage> caseNoteStaffUsageList;
    private CaseNoteStaffUsage caseNoteStaffUsage;

    @Value("${api.caseNote.sourceCode:AUTO}")
    private String caseNoteSource;
    private String fromDate;
    private String toDate;

    @Step("Initialisation")
    public void init() {
        super.init();

        caseNote = null;
        pendingCaseNote = null;
        caseNoteFilter = "";
        fromDate = null;
        toDate = null;
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
    public CaseNote createCaseNote(Long bookingId, NewCaseNote newCaseNote) {
        pendingCaseNote = newCaseNote;
        Long caseNoteId = dispatchCreateRequest(bookingId, newCaseNote);

        if (caseNoteId != null) {
            dispatchGetRequest(bookingId, caseNoteId);
        } else {
            caseNote = null;
        }

        return caseNote;
    }

    @Step("Update case note")
    public CaseNote updateCaseNote(CaseNote originalCaseNote, UpdateCaseNote updatedCaseNote) {
        dispatchUpdateRequest(originalCaseNote.getBookingId(), originalCaseNote.getCaseNoteId(), updatedCaseNote);
        dispatchGetRequest(originalCaseNote.getBookingId(), originalCaseNote.getCaseNoteId());

        return caseNote;
    }

    @Step("Get case notes")
    public void getCaseNotes(Long bookingId) {
        dispatchQueryRequest(bookingId);
    }

    @Step("Get case note")
    public CaseNote getCaseNote(long bookingId, long caseNoteId) {
        dispatchGetRequest(bookingId, caseNoteId);

        return caseNote;
    }

    @Step("Get case note count")
    public void getCaseNoteCount(long bookingId, String type, String subType, String fromDate, String toDate) {
        dispatchGetCaseNoteCountRequest(bookingId, type, subType, fromDate, toDate);
    }

    @Step("Get case note usage")
    public void getCaseNoteUsage(String offenderNos, String staffId, String agencyId, String type, String subType, String fromDate, String toDate) {
        dispatchGetCaseNoteUsageRequest(offenderNos, staffId, agencyId, type, subType, fromDate, toDate);
    }

    @Step("Get case note staff usage")
    public void getCaseNoteStaffUsage(String staffIds, String type, String subType, String fromDate, String toDate) {
        dispatchGetCaseNoteStaffUsageRequest(staffIds, type, subType, fromDate, toDate);
    }

    @Step("Verify case note types")
    public void verifyCaseNoteTypes(String caseNoteTypes) {
        verifyPropertyValues(caseNotes, CaseNote::getType, caseNoteTypes);
    }

    @Step("Verify case note sub types")
    public void verifyCaseNoteSubTypes(String caseNoteSubTypes) {
        verifyPropertyValues(caseNotes, CaseNote::getSubType, caseNoteSubTypes);
    }

    @Step("Verify case note count response property value")
    public void verifyCaseNoteCountPropertyValue(String propertyName, String expectedValue) throws Exception {
        verifyPropertyValue(caseNoteCount, propertyName, expectedValue);
    }

    @Step("Verify case note usage response property value")
    public void verifyCaseNoteUsagePropertyValue(String propertyName, String expectedValue) throws Exception {
        verifyPropertyValue(caseNoteUsage, propertyName, expectedValue);
    }

    @Step("Verify case note staff usage response property value")
    public void verifyCaseNoteStaffUsagePropertyValue(String propertyName, String expectedValue) throws Exception {
        verifyPropertyValue(caseNoteStaffUsage, propertyName, expectedValue);
    }

    @Step("Verify case note usage size")
    public void verifyCaseNoteUsageSize(int size) {
        assertEquals(caseNoteUsageList.size(), size);
    }

    @Step("Verify case note usage size")
    public void verifyCaseNoteStaffUsageSize(int size) {
        assertEquals(caseNoteStaffUsageList.size(), size);
    }


    @Step("Apply case note type filter")
    public void applyCaseNoteTypeFilter(String caseNoteType) {
        if (StringUtils.isNotBlank(caseNoteType)) {
            if (StringUtils.isNotBlank(caseNoteFilter)) {
                caseNoteFilter += ",and:";
            }

            caseNoteFilter += String.format("type:in:'%s'", caseNoteType);
        }
    }

    @Step("Apply case note sub type filter")
    public void applyCaseNoteSubTypeFilter(String caseNoteSubType) {
        if (StringUtils.isNotBlank(caseNoteSubType)) {
            if (StringUtils.isNotBlank(caseNoteFilter)) {
                caseNoteFilter += ",and:";
            }

            caseNoteFilter += String.format("subType:in:'%s'", caseNoteSubType);
        }
    }

    @Step("Apply case note agency filter")
    public void applyAgencyFilter(String agencyId) {
        if (StringUtils.isNotBlank(agencyId)) {
            if (StringUtils.isNotBlank(caseNoteFilter)) {
                caseNoteFilter += ",and:";
            }

            caseNoteFilter += String.format("agencyId:eq:'%s'", agencyId);
        }
    }


    @Step("Apply date from filter")
    public void applyDateFromFilter(String dateFrom) {
        if (StringUtils.isNotBlank(dateFrom)) {
            fromDate = dateFrom;
        }
    }

    @Step("Apply date to filter")
    public void applyDateToFilter(String dateTo) {
        if (StringUtils.isNotBlank(dateTo)) {
            toDate = dateTo;
        }
    }

    private Long dispatchCreateRequest(Long bookingId, NewCaseNote caseNote) {
        Long caseNoteId;

        try {
            ResponseEntity<CaseNote> response = restTemplate.exchange(API_REQUEST_BASE_URL, HttpMethod.POST, createEntity(caseNote),
                    CaseNote.class, bookingId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

            caseNoteId = response.getBody().getCaseNoteId();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());

            caseNoteId = null;
        }

        return caseNoteId;
    }

    private void dispatchGetRequest(Long bookingId, Long caseNoteId) {
        try {
            ResponseEntity<CaseNote> response = restTemplate.exchange(API_REQUEST_FOR_CASENOTE, HttpMethod.GET,
                    createEntity(), CaseNote.class, bookingId, caseNoteId);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            caseNote = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchUpdateRequest(Long bookingId, Long caseNoteId, UpdateCaseNote caseNote) {
        try {
            ResponseEntity<CaseNote> response = restTemplate.exchange(API_REQUEST_FOR_CASENOTE, HttpMethod.PUT,
                    createEntity(caseNote), CaseNote.class, bookingId, caseNoteId);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchQueryRequest(Long bookingId) {
        caseNotes = null;

        String queryUrl = API_REQUEST_BASE_URL + buildQuery(caseNoteFilter);

        if (StringUtils.isNotBlank(fromDate)) {
            queryUrl += "&from=" + fromDate;
        }

        if (StringUtils.isNotBlank(toDate)) {
            queryUrl += "&to=" + toDate;
        }

        try {
            ResponseEntity<List<CaseNote>> response = restTemplate.exchange(queryUrl, HttpMethod.GET,
                    createEntity(null, addPaginationHeaders()), new ParameterizedTypeReference<List<CaseNote>>() {
                    }, bookingId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            buildResourceData(response);
            caseNotes = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchGetCaseNoteCountRequest(Long bookingId, String type, String subType, String fromDate, String toDate) {
        init();

        String urlModifier = "";

        if (StringUtils.isNotBlank(fromDate)) {
            urlModifier += (FROM_DATE_QUERY_PARAM_PREFIX + fromDate);
        }

        if (StringUtils.isNotBlank(toDate)) {
            urlModifier += (TO_DATE_QUERY_PARAM_PREFIX + toDate);
        }

        if (StringUtils.isNotBlank(urlModifier)) {
            urlModifier = "?" + urlModifier.substring(1);
        }

        String url = API_REQUEST_FOR_CASENOTE_COUNT + urlModifier;

        try {
            ResponseEntity<CaseNoteCount> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createEntity(),
                    CaseNoteCount.class,
                    bookingId,
                    type,
                    subType);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            caseNoteCount = response.getBody();
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchGetCaseNoteUsageRequest(String offenderNos, String staffId, String agencyId, String type, String subType, String fromDate, String toDate) {
        init();

        final StringBuilder queryBuilder = new StringBuilder();

        if (StringUtils.isNotBlank(offenderNos)) {
            List<String> nos = Arrays.asList(offenderNos.split(","));
            nos.forEach(offenderNo -> queryBuilder.append(OFFENDER_NOS_QUERY_PARAM_PREFIX).append(offenderNo));
        }

        if (StringUtils.isNotBlank(staffId)) {
            queryBuilder.append(CASENOTE_STAFF_ID_QUERY_PARAM_PREFIX).append(staffId);
        }

        if (StringUtils.isNotBlank(agencyId)) {
            queryBuilder.append(CASENOTE_AGENCY_ID_QUERY_PARAM_PREFIX).append(agencyId);
        }

        setQueryParams(type, subType, fromDate, toDate, queryBuilder);

        String urlModifier = "";

        if (queryBuilder.length() > 0) {
            urlModifier = "?" + queryBuilder.substring(1);
        }

        String url = API_REQUEST_FOR_CASENOTE_USAGE + urlModifier;

        try {
            ResponseEntity<List<CaseNoteUsage>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createEntity(),
                        new ParameterizedTypeReference<List<CaseNoteUsage>>() {});

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            caseNoteUsageList = response.getBody();
            caseNoteUsage = caseNoteUsageList.isEmpty() ? null : caseNoteUsageList.get(0);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void dispatchGetCaseNoteStaffUsageRequest(String staffIds, String type, String subType, String fromDate, String toDate) {
        init();

        final StringBuilder queryBuilder = new StringBuilder();

        if (StringUtils.isNotBlank(staffIds)) {
            List<String> ids = Arrays.asList(staffIds.split(","));
            ids.forEach(staffId -> queryBuilder.append(STAFF_IDS_QUERY_PARAM_PREFIX).append(staffId));
        }

        setQueryParams(type, subType, fromDate, toDate, queryBuilder);

        String urlModifier = "";

        if (queryBuilder.length() > 0) {
            urlModifier = "?" + queryBuilder.substring(1);
        }

        String url = API_REQUEST_FOR_CASENOTE_STAFF_USAGE + urlModifier;

        try {
            ResponseEntity<List<CaseNoteStaffUsage>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    createEntity(),
                    new ParameterizedTypeReference<List<CaseNoteStaffUsage>>() {});

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            caseNoteStaffUsageList = response.getBody();
            caseNoteStaffUsage = caseNoteStaffUsageList.isEmpty() ? null : caseNoteStaffUsageList.get(0);
        } catch (EliteClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    private void setQueryParams(String type, String subType, String fromDate, String toDate, StringBuilder queryBuilder) {
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
