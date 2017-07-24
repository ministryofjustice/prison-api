package net.syscon.elite.executableSpecification.steps;

import net.syscon.elite.web.api.model.CaseNote;
import net.syscon.elite.web.api.model.UpdateCaseNote;
import net.thucydides.core.annotations.Step;
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
    private CaseNote pendingCaseNote;

    @Value("${api.caseNote.sourceCode:AUTO}")
    private String caseNoteSource;

    public void create(String type, String subType, String text, String occurrenceDateTime) {
        pendingCaseNote = new CaseNote();

        pendingCaseNote.setType(type);
        pendingCaseNote.setSubType(subType);
        pendingCaseNote.setText(text);
        pendingCaseNote.setOccurrenceDateTime(occurrenceDateTime);

        caseNote = createCaseNote(pendingCaseNote);
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

    @Step("Verify case note source")
    public void verifyCaseNoteSource() {
        assertThat(caseNote.getSource()).isEqualTo(caseNoteSource);
    }

    @Step("Create case note")
    public CaseNote createCaseNote(CaseNote newCaseNote) {
        Long caseNoteId = dispatchCreateRequest(6000L, newCaseNote);

        dispatchGetRequest(6000L, caseNoteId);

        return caseNote;
    }

    @Step("Update case note")
    public CaseNote updateCaseNote(Long caseNoteId, UpdateCaseNote updatedCaseNote) {
        dispatchUpdateRequest(6000L, caseNoteId, updatedCaseNote);
        dispatchGetRequest(6000L, caseNoteId);

        return caseNote;
    }

    private Long dispatchCreateRequest(Long bookingId, CaseNote caseNote) {
        ResponseEntity<CaseNote> response = restTemplate.exchange(API_REQUEST_BASE_URL, HttpMethod.POST, createEntity(caseNote),
                CaseNote.class, bookingId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

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
}
