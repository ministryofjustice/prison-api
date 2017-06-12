package net.syscon.elite.executableSpecification.steps;

import net.syscon.elite.web.api.model.CaseNote;
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
    private CaseNote defaultCaseNote;

    @Step("Create case note")
    public void create(String type, String subType, String text) {
        pendingCaseNote = new CaseNote();

        pendingCaseNote.setType(type);
        pendingCaseNote.setSubType(subType);
        pendingCaseNote.setText(text);

        ResponseEntity<CaseNote> response = restTemplate.exchange("/api/booking/6000/caseNotes", HttpMethod.POST, createEntity(pendingCaseNote, null), CaseNote.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        caseNote = response.getBody();
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
