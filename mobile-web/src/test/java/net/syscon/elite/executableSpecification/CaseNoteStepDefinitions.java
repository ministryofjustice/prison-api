package net.syscon.elite.executableSpecification;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executableSpecification.steps.CaseNoteSteps;
import net.syscon.elite.web.api.model.CaseNote;
import net.syscon.elite.web.api.model.UpdateCaseNote;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step definitions for case note related Booking API endpoints:
 * <ul>
 *     <li>/booking/{bookingId}/caseNotes</li>
 *     <li>/booking/{bookingId}/caseNotes/{caseNoteId}</li>
 * </ul>
 *
 * NB: Not all API endpoints have associated tests at this point in time.
 */
public class CaseNoteStepDefinitions extends AbstractStepDefinitions {
    @Autowired
    private CaseNoteSteps caseNote;

    private CaseNote seededCaseNote;
    private CaseNote updatedCaseNote;

    @When("^a case note is created for an existing offender booking:$")
    public void aCaseNoteIsCreatedForAnExistingOffenderBooking(DataTable rawData) {
        Map<String, String> caseNoteData = rawData.asMap(String.class, String.class);

        caseNote.create(caseNoteData.get("type"), caseNoteData.get("subType"), caseNoteData.get("text"));
    }

    @Then("^case note is successfully created$")
    public void caseNoteIsSuccessfullyCreated() {
        caseNote.verify();
    }

    @And("^I have created a case note text of \"([^\"]*)\"$")
    public void iHaveCreatedACaseNoteTextOf(String caseNoteText) throws Throwable {
        CaseNote newCaseNote = new CaseNote();

        newCaseNote.setType("CHAP");
        newCaseNote.setSubType("STUFF");
        newCaseNote.setText(caseNoteText);

        seededCaseNote = caseNote.createCaseNote(newCaseNote);
    }

    @When("^the created case note is updated with text \"([^\"]*)\"$")
    public void theCaseNoteIsUpdatedWithText(String caseNoteText) throws Throwable {
        updatedCaseNote = caseNote.updateCaseNote(seededCaseNote.getCaseNoteId(), new UpdateCaseNote(caseNoteText));
    }

    @Then("^case note is successfully updated with \"([^\"]*)\"$")
    public void caseNoteIsSuccessfullyUpdated(String caseNoteText) throws Throwable {
        assertThat(updatedCaseNote.getText()).contains(caseNoteText);
    }

    @And("^the original text is not replaced$")
    public void theAmendedFlagIsSet() throws Throwable {
        assertThat(updatedCaseNote.getText()).contains(seededCaseNote.getText());
    }

    @And("^correct case note source is used$")
    public void correctCaseNoteSourceIsUsed() throws Throwable {
        caseNote.verifyCaseNoteSource();
    }
}
