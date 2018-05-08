package net.syscon.elite.executablespecification;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.api.model.CaseNote;
import net.syscon.elite.api.model.NewCaseNote;
import net.syscon.elite.api.model.UpdateCaseNote;
import net.syscon.elite.executablespecification.steps.CaseNoteSteps;
import net.syscon.util.DateTimeConverter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZoneOffset;
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

    @Autowired
    private CaseNoteSteps caseNote;

    private CaseNote seededCaseNote;
    private CaseNote updatedCaseNote;

    private Long caseNoteBookingId = -32L; // this must exist and must be accessible to test user

    @And("^case note test harness initialized$")
    public void caseNoteTestHarnessInitialized() throws Throwable {
        caseNote.init();

        seedCaseNoteForUpdateTest();
    }

    private void seedCaseNoteForUpdateTest() {
        NewCaseNote newCaseNote = buildNewCaseNote(
                "CHAP",
                "FAMMAR",
                "Hello this is a new case note",
                null);

        seededCaseNote = caseNote.createCaseNote(caseNoteBookingId, newCaseNote);
    }

    @When("^a case note is created for booking:$")
    public void aCaseNoteIsCreatedForBooking(DataTable rawData) throws Throwable {
        Map<String, String> caseNoteData = rawData.asMap(String.class, String.class);

        Long bookingId = Long.valueOf(caseNoteData.get("bookingId"));

        NewCaseNote newCaseNote =
                buildNewCaseNote(caseNoteData.get("type"),
                        caseNoteData.get("subType"),
                        caseNoteData.get("text"),
                        caseNoteData.get("occurrenceDateTime"));

        caseNote.createCaseNote(bookingId, newCaseNote);
    }

    @When("^attempt is made to create case note for booking:$")
    public void attemptIsMadeToCreateCaseNoteForBooking(DataTable rawData) throws Throwable {
        Map<String, String> caseNoteData = rawData.asMap(String.class, String.class);

        Long bookingId = Long.valueOf(caseNoteData.get("bookingId"));

        NewCaseNote newCaseNote =
                buildNewCaseNote(caseNoteData.get("type"),
                        caseNoteData.get("subType"),
                        caseNoteData.get("text"),
                        caseNoteData.get("occurrenceDateTime"));

        caseNote.createCaseNote(bookingId, newCaseNote);
        caseNote.verifyNotCreated();
    }

    @Then("^case note is successfully created$")
    public void caseNoteIsSuccessfullyCreated() {
        caseNote.verify();
    }

    @Then("case note validation errors are:")
    public void caseNoteValidationErrorsAre(DataTable rawData) {
        List<String> errors = rawData.asList(String.class);
        caseNote.verifyBadRequest(errors);
    }

    @Then("^case note validation error \"([^\"]*)\" occurs$")
    public void caseNoteValidationErrorOccurs(String error) {
        caseNote.verifyBadRequest(error);
    }

    @When("^existing case note is updated with text \"([^\"]*)\"$")
    public void theCaseNoteIsUpdatedWithText(String caseNoteText) throws Throwable {
        updatedCaseNote = caseNote.updateCaseNote(seededCaseNote, UpdateCaseNote.builder().text(caseNoteText).build());
    }

    @When("^existing case note is updated with valid text$")
    public void theCaseNoteIsUpdatedWithValidText() throws Throwable {
        CaseNote existingCaseNote = caseNote.getCaseNote(-5, -2);
        // Allow 100 chars for the 1st part of the updated text which includes the
        // original text and the user/timestamp text
        String caseNoteText = StringUtils.repeat("a", 100);
        updatedCaseNote = caseNote.updateCaseNote(existingCaseNote, UpdateCaseNote.builder().text(caseNoteText).build());
    }

    @When("^existing case note for a different user is updated with valid text$")
    public void existingCaseNoteForADifferentUserIsUpdatedWithValidText() throws Throwable {
        CaseNote existingCaseNote = caseNote.getCaseNote(-1, -1);
        String caseNoteText = StringUtils.repeat("a", 100);
        updatedCaseNote = caseNote.updateCaseNote(existingCaseNote, UpdateCaseNote.builder().text(caseNoteText).build());
    }

    @When("^the created case note is updated with long text$")
    public void theCaseNoteIsUpdatedWithInvalidText() throws Throwable {
        final String caseNoteText = StringUtils.repeat("a", 3950); // total text will be over 4000
        updatedCaseNote = caseNote.updateCaseNote(seededCaseNote, UpdateCaseNote.builder().text(caseNoteText).build());
    }

    @When("^a case note is created to use up all free space$")
    public void aCaseNoteIsCreatedToUseUpAllFreeSpace() throws Throwable {
        final String caseNoteText = StringUtils.repeat("a", 3900);
        updatedCaseNote = caseNote.updateCaseNote(seededCaseNote, UpdateCaseNote.builder().text(caseNoteText).build());
    }

    @Then("^case note is successfully updated with valid text$")
    public void caseNoteIsSuccessfullyUpdated() throws Throwable {
        assertThat(updatedCaseNote.getText()).contains(StringUtils.repeat("a", 100));
    }

    @And("^the original text is not replaced$")
    public void theAmendedFlagIsSet() throws Throwable {
        assertThat(updatedCaseNote.getText()).contains(seededCaseNote.getText());
    }

    @And("^correct case note source is used$")
    public void correctCaseNoteSourceIsUsed() throws Throwable {
        caseNote.verifyCaseNoteSource();
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

    @When("^attempt is made to update case note for booking with id \"([^\"]*)\"$")
    public void attemptIsMadeToUpdateCaseNoteForBookingWithId(String bookingId) throws Throwable {
        seededCaseNote.setBookingId(Long.valueOf(bookingId));
        caseNote.updateCaseNote(seededCaseNote, UpdateCaseNote.builder().text("Updated text").build());
    }

    @When("^a case note is requested for offender booking \"([^\"]*)\"")
    public void caseNotesDifferentCaseloadGet(long bookingId) throws Throwable {
        caseNote.getCaseNote(bookingId, -1L);
    }

    @Then("^resource not found response is received from casenotes API")
    public void caseNotesVerifyResourceNotFound() throws Throwable {
        caseNote.verifyResourceNotFound();
    }

    @When("^case note count is requested for offender booking \"([^\"]*)\" for case note type \"([^\"]*)\" and sub-type \"([^\"]*)\"$")
    public void caseNoteCountIsRequestedForOffenderBookingForCaseNoteTypeAndSubType(String bookingId, String type, String subType) throws Throwable {
        caseNote.getCaseNoteCount(Long.valueOf(bookingId), type, subType, null, null);
    }

    @Then("^case note count response \"([^\"]*)\" is \"([^\"]*)\"$")
    public void caseNoteCountResponseIs(String propertyName, String expectedValue) throws Throwable {
        caseNote.verifyCaseNoteCountPropertyValue(propertyName, expectedValue);
    }

    @Then("^bad request response, with \"([^\"]*)\" message, is received from casenotes API$")
    public void badRequestResponseWithMessageIsReceivedFromCasenotesAPI(String expectedUserMessage) throws Throwable {
        caseNote.verifyBadRequest(expectedUserMessage);
    }

    @When("^case note count between \"([^\"]*)\" and \"([^\"]*)\" is requested for offender booking \"([^\"]*)\" for case note type \"([^\"]*)\" and sub-type \"([^\"]*)\"$")
    public void caseNoteCountBetweenAndIsRequestedForOffenderBookingForCaseNoteTypeAndSubType(String fromDate, String toDate, String bookingId, String type, String subType) throws Throwable {
        caseNote.getCaseNoteCount(Long.valueOf(bookingId), type, subType, fromDate, toDate);
    }

    @Then("^access denied response, with \"([^\"]*)\" message, is received from booking case notes API$")
    public void accessDeniedResponseWithMessageIsReceivedFromBookingCaseNotesAPI(String userMessage) throws Throwable {
        caseNote.verifyAccessDenied(userMessage);
    }
}
