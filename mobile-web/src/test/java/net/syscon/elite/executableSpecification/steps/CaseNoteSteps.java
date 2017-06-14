package net.syscon.elite.executableSpecification.steps;

import net.syscon.elite.web.api.model.CaseNote;
import net.syscon.elite.web.api.model.UpdateCaseNote;
import net.thucydides.core.annotations.Step;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Case Note domain.
 */
public class CaseNoteSteps extends CommonSteps {
    private CaseNote caseNote;
    private CaseNote pendingCaseNote;

    @Step("Create case note")
    public void create(String type, String subType, String text) {
        pendingCaseNote = new CaseNote();

        pendingCaseNote.setType(type);
        pendingCaseNote.setSubType(subType);
        pendingCaseNote.setText(text);

        caseNote = createCaseNote(pendingCaseNote);
    }

    @Step("Verify case note")
    public void verify() {
        assertThat(caseNote).isNotNull();
        assertThat(caseNote.getCaseNoteId()).isGreaterThan(0);
        assertThat(caseNote.getType()).isEqualTo(pendingCaseNote.getType());
        assertThat(caseNote.getSubType()).isEqualTo(pendingCaseNote.getSubType());
        assertThat(caseNote.getText()).isEqualTo(pendingCaseNote.getText());
        assertThat(caseNote.getCreationDateTime()).isNotEmpty();
    }

    public CaseNote createCaseNote(final CaseNote newCaseNote) {
        ResponseEntity<CaseNote> response = restTemplate.exchange("/api/booking/6000/caseNotes", HttpMethod.POST, createEntity(newCaseNote, null), CaseNote.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return getCaseNote(response.getBody().getCaseNoteId());
    }

    public CaseNote getCaseNote(long caseNoteId) {
        ResponseEntity<CaseNote> response = restTemplate.exchange("/api/booking/6000/caseNotes/" + caseNoteId, HttpMethod.GET, createEntity(null, null), CaseNote.class);
        return response.getBody();
    }

    public CaseNote updateCaseNote(final long caseNoteId, final UpdateCaseNote updatedCaseNote) {
        ResponseEntity<CaseNote> response = restTemplate.exchange("/api/booking/6000/caseNotes/" + caseNoteId, HttpMethod.PUT, createEntity(updatedCaseNote, null), CaseNote.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return getCaseNote(caseNoteId);
    }
}
