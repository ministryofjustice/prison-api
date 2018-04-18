package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.CaseNote;
import net.syscon.elite.api.model.CaseNoteCount;
import net.syscon.elite.api.model.NewCaseNote;
import net.syscon.elite.api.model.UpdateCaseNote;
import net.syscon.elite.test.EliteClientException;
import net.thucydides.core.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Case Note domain.
 */
public class CaseNoteSteps extends CommonSteps {
    private static final String API_REQUEST_BASE_URL = API_PREFIX + "bookings/{bookingId}/caseNotes";
    private static final String API_REQUEST_FOR_CASENOTE = API_REQUEST_BASE_URL + "/{caseNoteId}";
    private static final String API_REQUEST_FOR_CASENOTE_COUNT = API_REQUEST_BASE_URL + "/{type}/{subType}/count";
    private static final String FROM_DATE_QUERY_PARAM_PREFIX = "&fromDate=";
    private static final String TO_DATE_QUERY_PARAM_PREFIX = "&toDate=";

    private CaseNote caseNote;
    private NewCaseNote pendingCaseNote;
    private String caseNoteFilter;
    private List<CaseNote> caseNotes;
    private CaseNoteCount caseNoteCount;

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
}
