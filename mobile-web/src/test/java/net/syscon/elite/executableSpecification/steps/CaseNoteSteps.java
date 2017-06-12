package net.syscon.elite.executableSpecification.steps;

import net.syscon.elite.web.api.model.CaseNote;
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

        ResponseEntity<CaseNote> response = restTemplate.exchange("/api/booking/-1/caseNotes", HttpMethod.POST, createEntity(pendingCaseNote), CaseNote.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        caseNote = response.getBody();
    }

    @Step("Verify case note")
    public void verify() {
        assertThat(caseNote).isNotNull();
        assertThat(caseNote.getCaseNoteId()).isGreaterThan(0);
        assertThat(caseNote.getType()).isEqualTo(pendingCaseNote.getType());
        assertThat(caseNote.getSubType()).isEqualTo(pendingCaseNote.getSubType());
        assertThat(caseNote.getText()).isEqualTo(pendingCaseNote.getText());
    }
}
