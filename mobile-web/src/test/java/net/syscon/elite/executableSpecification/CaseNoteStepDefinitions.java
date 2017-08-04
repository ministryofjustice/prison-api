package net.syscon.elite.executableSpecification;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executableSpecification.steps.CaseNoteSteps;
import net.syscon.elite.web.api.model.CaseNote;
import net.syscon.elite.web.api.model.NewCaseNote;
import net.syscon.elite.web.api.model.UpdateCaseNote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
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
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private CaseNoteSteps caseNote;

    private CaseNote seededCaseNote;
    private CaseNote updatedCaseNote;

    @When("^a case note is created for an existing offender booking:$")
    public void aCaseNoteIsCreatedForAnExistingOffenderBooking(DataTable rawData) {
        Map<String, String> caseNoteData = rawData.asMap(String.class, String.class);

        NewCaseNote newCaseNote = new NewCaseNote();
        newCaseNote.setType(caseNoteData.get("type"));
        newCaseNote.setSubType(caseNoteData.get("subType"));
        newCaseNote.setText(caseNoteData.get("text"));
        newCaseNote.setOccurrenceDateTime(caseNoteData.get("occurrenceDateTime"));
        caseNote.createCaseNote(newCaseNote);


        final List<CaseNote> allCaseNotesForBooking = caseNote.getAllCaseNotesForBooking(-1L);
        assertThat(allCaseNotesForBooking).hasSize(1);
        seededCaseNote = allCaseNotesForBooking.get(0);
        log.debug("Case Note ID {}",seededCaseNote.getCaseNoteId());

    }

    @Then("^case note is successfully created$")
    public void caseNoteIsSuccessfullyCreated() {
        caseNote.verify();
    }

    @And("^I have created a case note text of \"([^\"]*)\"$")
    public void iHaveCreatedACaseNoteTextOf(String caseNoteText) throws Throwable {
        NewCaseNote newCaseNote = new NewCaseNote();

        newCaseNote.setType("CHAP");
        newCaseNote.setSubType("FAMMAR");
        newCaseNote.setText(caseNoteText);

        seededCaseNote = caseNote.createCaseNote(newCaseNote);

    }

    @And("^I have (\\d+) case notes for my booking$")
    public void theCorectNumberOfCasesExist(int numCases) {
        final List<CaseNote> allCaseNotesForBooking = caseNote.getAllCaseNotesForBooking(-1L);
        assertThat(allCaseNotesForBooking).hasSize(numCases);
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
