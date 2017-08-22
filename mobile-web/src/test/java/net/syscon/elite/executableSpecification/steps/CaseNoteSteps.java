package net.syscon.elite.executableSpecification.steps;

import net.syscon.elite.web.api.model.CaseNote;
import net.syscon.elite.web.api.model.CaseNotes;
import net.syscon.elite.web.api.model.NewCaseNote;
import net.syscon.elite.web.api.model.UpdateCaseNote;
import net.thucydides.core.annotations.Step;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Case Note domain.
 */
public class CaseNoteSteps extends CommonSteps {
    private static final String API_REQUEST_BASE_URL = API_PREFIX + "booking/{bookingId}/caseNotes";
    private static final String API_REQUEST_FOR_CASENOTE = API_REQUEST_BASE_URL + "/{caseNoteId}";

    private CaseNote caseNote;
    private CaseNotes caseNotes;
    private NewCaseNote pendingCaseNote;
    private String caseNoteFilter;

    @Value("${api.caseNote.sourceCode:AUTO}")
    private String caseNoteSource;

    @Step("Initialisation")
    public void init() {
        caseNote = null;
        caseNotes = null;
        pendingCaseNote = null;
        caseNoteFilter = "";
    }

    @Step("Verify case note")
    public void verify() {
        assertThat(caseNote).isNotNull();
        assertThat(caseNote.getCaseNoteId()).isGreaterThan(0);
        assertThat(caseNote.getType()).isEqualTo(pendingCaseNote.getType());
        assertThat(caseNote.getSubType()).isEqualTo(pendingCaseNote.getSubType());
        assertThat(caseNote.getText()).isEqualTo(pendingCaseNote.getText());
        assertThat(caseNote.getOccurrenceDateTime()).isEqualTo(pendingCaseNote.getOccurrenceDateTime());
        assertThat(caseNote.getCreationDateTime()).isNotEmpty();
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
    public CaseNote createCaseNote(Long bookingId, NewCaseNote newCaseNote, boolean creationExpected) {
        pendingCaseNote = newCaseNote;
        Long caseNoteId = dispatchCreateRequest(bookingId, newCaseNote, creationExpected);

        if (creationExpected) {
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

    @Step("Verify case note types")
    public void verifyCaseNoteTypes(String caseNoteTypes) {
        verifyPropertyValues(caseNotes.getCaseNotes(), CaseNote::getType, caseNoteTypes);
    }

    @Step("Verify case note sub types")
    public void verifyCaseNoteSubTypes(String caseNoteSubTypes) {
        verifyPropertyValues(caseNotes.getCaseNotes(), CaseNote::getSubType, caseNoteSubTypes);
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
            if (StringUtils.isNotBlank(caseNoteFilter)) {
                caseNoteFilter += ",and:";
            }

            caseNoteFilter += String.format("occurrenceDateTime:gteq:'%s'", dateFrom);
        }
    }

    @Step("Apply date to filter")
    public void applyDateToFilter(String dateTo) {
        if (StringUtils.isNotBlank(dateTo)) {
            if (StringUtils.isNotBlank(caseNoteFilter)) {
                caseNoteFilter += ",and:";
            }

            caseNoteFilter += String.format("occurrenceDateTime:lteq:'%s'", dateTo);
        }
    }

    private Long dispatchCreateRequest(Long bookingId, NewCaseNote caseNote, boolean creationExpected) {
        ResponseEntity<CaseNote> response = restTemplate.exchange(API_REQUEST_BASE_URL, HttpMethod.POST, createEntity(caseNote),
                CaseNote.class, bookingId);

        if (creationExpected) {
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        return response.getBody().getCaseNoteId();
    }

    private void dispatchGetRequest(Long bookingId, Long caseNoteId) {
        ResponseEntity<CaseNote> response = restTemplate.exchange(API_REQUEST_FOR_CASENOTE, HttpMethod.GET, createEntity(),
                CaseNote.class, bookingId, caseNoteId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        caseNote = response.getBody();
    }

    private void dispatchUpdateRequest(Long bookingId, Long caseNoteId, UpdateCaseNote caseNote) {
        ResponseEntity<CaseNote> response = restTemplate.exchange(API_REQUEST_FOR_CASENOTE, HttpMethod.PUT, createEntity(caseNote),
                CaseNote.class, bookingId, caseNoteId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    private void dispatchQueryRequest(Long bookingId) {
        caseNotes = null;

        String queryUrl = API_REQUEST_BASE_URL + buildQueryParam(caseNoteFilter);

        ResponseEntity<CaseNotes> response =
                restTemplate.exchange(queryUrl, HttpMethod.GET, createEntity(), CaseNotes.class, bookingId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        caseNotes = response.getBody();

        setResourceMetaData(caseNotes.getCaseNotes(), caseNotes.getPageMetaData());
    }
}
