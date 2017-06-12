package net.syscon.elite.executableSpecification.steps;

import net.syscon.elite.web.api.model.CaseNote;
import net.syscon.elite.web.api.model.UpdateCaseNote;
import net.thucydides.core.annotations.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for Case Note domain.
 */
public class CaseNoteSteps {
    @Autowired
    private TestRestTemplate restTemplate;

    @Value("${security.authenication.header:Authorization}")
    private String authenicationHeader;

    private String token;

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

    public void setToken(String token) {
        this.token = token;
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

    public CaseNote updateCaseNote(final UpdateCaseNote updatedCaseNote) {
        ResponseEntity<CaseNote> response = restTemplate.exchange("/api/booking/6000/caseNotes/" + updatedCaseNote.getCaseNoteId(), HttpMethod.PUT, createEntity(updatedCaseNote, null), CaseNote.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return getCaseNote(updatedCaseNote.getCaseNoteId());
    }

    private HttpEntity createEntity(Object entity, Map<String, String> extraHeaders) {
        HttpHeaders headers = new HttpHeaders();

        if (token != null) {
            headers.add(authenicationHeader, token);
        }

        if (extraHeaders != null) {
            extraHeaders.forEach(headers::add);
        }

        return new HttpEntity<>(entity, headers);
    }
}
