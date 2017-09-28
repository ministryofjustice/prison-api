package net.syscon.elite.executableSpecification;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executableSpecification.steps.CaseNoteSteps;
import net.syscon.elite.v2.api.model.CaseNote;
import net.syscon.elite.v2.api.model.NewCaseNote;
import net.syscon.elite.v2.api.model.UpdateCaseNote;
import net.syscon.util.DateTimeConverter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZoneOffset;
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

    private Long caseNoteBookingId = -99L;

    @And("^case note test harness initialized$")
    public void caseNoteTestHarnessInitialized() throws Throwable {
        caseNote.init();
    }

    @When("^a case note is created for an existing offender booking:$")
    public void aCaseNoteIsCreatedForAnExistingOffenderBooking(DataTable rawData) {
        Map<String, String> caseNoteData = rawData.asMap(String.class, String.class);

        NewCaseNote newCaseNote =
                buildNewCaseNote(caseNoteData.get("type"),
                        caseNoteData.get("subType"),
                        caseNoteData.get("text"),
                        caseNoteData.get("occurrenceDateTime"));

        caseNote.createCaseNote(caseNoteBookingId, newCaseNote, true);
    }

    @Then("^case note is successfully created$")
    public void caseNoteIsSuccessfullyCreated() {
        caseNote.verify();
    }

    @And("^I have created a case note with text of \"([^\"]*)\"$")
    public void iHaveCreatedACaseNoteTextOf(String caseNoteText) throws Throwable {
        NewCaseNote newCaseNote =
                buildNewCaseNote("CHAP","FAMMAR", caseNoteText, null);

        seededCaseNote = caseNote.createCaseNote(caseNoteBookingId, newCaseNote, true);
    }

    @When("^the created case note is updated with text \"([^\"]*)\"$")
    public void theCaseNoteIsUpdatedWithText(String caseNoteText) throws Throwable {
        updatedCaseNote = caseNote.updateCaseNote(seededCaseNote, UpdateCaseNote.builder().text(caseNoteText).build());
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

    @When("^a case note is created for an existing offender booking with incorrectly formatted occurrence dateTime:$")
    public void aCaseNoteIsCreatedForAnExistingOffenderBookingWithIncorrectlyFormattedOccurrenceDateTime(DataTable rawData) throws Throwable {
        Map<String, String> caseNoteData = rawData.asMap(String.class, String.class);

        NewCaseNote newCaseNote =
                buildNewCaseNote(caseNoteData.get("type"),
                                 caseNoteData.get("subType"),
                                 caseNoteData.get("text"),
                                 caseNoteData.get("occurrenceDateTime"));

        caseNote.createCaseNote(caseNoteBookingId, newCaseNote, false);
    }

    @Then("^case note is not created$")
    public void caseNoteIsNotCreated() throws Throwable {
        caseNote.verifyNotCreated();
    }

    private NewCaseNote buildNewCaseNote(String type, String subType, String text, String occurrenceDateTime) {
        NewCaseNote newCaseNote = new NewCaseNote();

        newCaseNote.setType(type);
        newCaseNote.setSubType(subType);
        newCaseNote.setText(text);
        if (StringUtils.isNotBlank(occurrenceDateTime)) {
            newCaseNote.setOccurrenceDateTime(DateTimeConverter.fromISO8601DateTimeToLocalDateTime(occurrenceDateTime, ZoneOffset.UTC));
        }

        return newCaseNote;
    }

    @When("^case notes are requested for offender booking \"([^\"]*)\"$")
    public void caseNotesAreRequestedForOffenderBooking(String bookingId) throws Throwable {
        caseNote.getCaseNotes(Long.valueOf(bookingId));
    }

    @Then("^\"([^\"]*)\" case notes are returned$")
    public void caseNotesAreReturned(String count) throws Throwable {
        caseNote.verifyResourceRecordsReturned(Long.valueOf(count));
    }

    @And("^case note types match \"([^\"]*)\"$")
    public void caseNoteTypesMatch(String caseNoteTypes) throws Throwable {
        caseNote.verifyCaseNoteTypes(caseNoteTypes);
    }

    @And("^case note sub types match \"([^\"]*)\"$")
    public void caseNoteSubTypesMatch(String caseNoteSubTypes) throws Throwable {
        caseNote.verifyCaseNoteSubTypes(caseNoteSubTypes);
    }

    @And("^case note type \"([^\"]*)\" filter applied$")
    public void caseNoteTypeFilterApplied(String caseNoteType) throws Throwable {
        caseNote.applyCaseNoteTypeFilter(caseNoteType);
    }

    @And("^case note sub type \"([^\"]*)\" filter applied$")
    public void caseNoteSubTypeFilterApplied(String caseNoteSubType) throws Throwable {
        caseNote.applyCaseNoteSubTypeFilter(caseNoteSubType);
    }

    @And("^date from \"([^\"]*)\" filter applied$")
    public void dateFromFilterApplied(String dateFrom) throws Throwable {
        caseNote.applyDateFromFilter(dateFrom);
    }

    @And("^date to \"([^\"]*)\" filter applied$")
    public void dateToFilterApplied(String dateTo) throws Throwable {
        caseNote.applyDateToFilter(dateTo);
    }

    @And("^pagination with limit \"([0-9]*)\" and offset \"([0-9]*)\" applied$")
    public void paginationWithLimitAndOffsetApplied(Long limit, Long offset) throws Throwable {
        caseNote.applyPagination(offset, limit);
    }

    @And("^filtered case notes are requested for offender booking \"([^\"]*)\"$")
    public void filteredCaseNotesAreRequestedForOffenderBooking(String bookingId) throws Throwable {
        caseNote.getCaseNotes(Long.valueOf(bookingId));
    }

    @And("^\"([^\"]*)\" case notes are available$")
    public void caseNotesAreAvailable(String count) throws Throwable {
        caseNote.verifyTotalResourceRecordsAvailable(Long.valueOf(count));
    }
}
