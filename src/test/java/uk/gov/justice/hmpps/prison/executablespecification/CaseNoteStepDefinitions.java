package uk.gov.justice.hmpps.prison.executablespecification;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.executablespecification.steps.CaseNoteSteps;

/**
 * BDD step definitions for case note related Case Note API endpoints:
 * <ul>
 *     <li>/api/case-notes/usage</li>
 *     <li>/api/case-notes/staff-usage</li>
 * </ul>
 * <p>
 * NB: Not all API endpoints have associated tests at this point in time.
 */
public class CaseNoteStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private CaseNoteSteps caseNote;

    @And("^case note test harness initialized$")
    public void caseNoteTestHarnessInitialized() throws Throwable {
        caseNote.init();
    }

    @Then("^case note size is \"([^\"]*)\"$")
    public void caseNoteSizeIs(final String size) throws Throwable {
        caseNote.verifyCaseNoteUsageSize(Integer.valueOf(size));
    }

    @Then("^case note usage response \"([^\"]*)\" is \"([^\"]*)\"$")
    public void caseNoteUsageResponseIs(final String propertyName, final String expectedValue) throws Throwable {
        caseNote.verifyCaseNoteUsagePropertyValue(propertyName, expectedValue);
    }

    @Then("^case note staff usage response \"([^\"]*)\" is \"([^\"]*)\"$")
    public void caseNoteStaffUsageResponseIs(final String propertyName, final String expectedValue) throws Throwable {
        caseNote.verifyCaseNoteStaffUsagePropertyValue(propertyName, expectedValue);
    }

    @When("^case note usage between \"([^\"]*)\" and \"([^\"]*)\" is requested of offender No \"([^\"]*)\" for case note type \"([^\"]*)\" and sub-type \"([^\"]*)\"$")
    public void caseNoteUsageBetweenAndIsRequestedOfOffenderNoForCaseNoteTypeAndSubType(final String fromDate, final String toDate, final String offenderNos, final String type, final String subType) throws Throwable {
        caseNote.getCaseNoteUsage(offenderNos, null, null, type, subType, fromDate, toDate);
    }

    @When("^case note usage by booking id between \"([^\"]*)\" and \"([^\"]*)\" is requested of booking Id \"([^\"]*)\" for case note type \"([^\"]*)\" and sub-type \"([^\"]*)\"$")
    public void caseNoteUsageByBookingIdBetweenAndIsRequestedOfOffenderNoForCaseNoteTypeAndSubType(final String fromDate, final String toDate, final String offenderNos, final String type, final String subType) throws Throwable {
        caseNote.getCaseNoteUsageByBookingId(offenderNos, type, subType, fromDate, toDate);
    }

    @When("^case note usage between \"([^\"]*)\" and \"([^\"]*)\" is requested of offender No \"([^\"]*)\" with staff Id \"([^\"]*)\" for case note type \"([^\"]*)\" and sub-type \"([^\"]*)\"$")
    public void caseNoteUsageBetweenAndIsRequestedOfOffenderNoForCaseNoteTypeAndSubType(final String fromDate, final String toDate, final String offenderNos, final String staffId, final String type, final String subType) throws Throwable {
        caseNote.getCaseNoteUsage(offenderNos, staffId, null, type, subType, fromDate, toDate);
    }

    @When("^case note usage between \"([^\"]*)\" and \"([^\"]*)\" is requested of offender No \"([^\"]*)\" with staff Id \"([^\"]*)\" for case note type \"([^\"]*)\" and sub-type \"([^\"]*)\" and agencyId \"([^\"]*)\"$")
    public void caseNoteUsageBetweenAndIsRequestedOfOffenderNoForCaseNoteTypeAndSubTypeAndAgency(final String fromDate, final String toDate, final String offenderNos, final String staffId, final String type, final String subType, final String agencyId) throws Throwable {
        caseNote.getCaseNoteUsage(offenderNos, staffId, agencyId, type, subType, fromDate, toDate);
    }

    @When("^case note usage between \"([^\"]*)\" and \"([^\"]*)\" is requested of staff ID \"([^\"]*)\" for case note type \"([^\"]*)\" and sub-type \"([^\"]*)\"$")
    public void caseNoteUsageBetweenAndIsRequestedOfStaffIDForCaseNoteTypeAndSubType(final String fromDate, final String toDate, final String staffIds, final String type, final String subType) throws Throwable {
        caseNote.getCaseNoteStaffUsage(staffIds, type, subType, fromDate, toDate);
    }

    @When("^case note usage between \"([^\"]*)\" and \"([^\"]*)\" is requested for case note type \"([^\"]*)\" and sub-type \"([^\"]*)\" and agencyId \"([^\"]*)\"$")
    public void caseNoteUsageBetweenAndIsRequestedForCaseNoteTypeAndSubTypeAndAgencyId(final String fromDate, final String toDate, final String type, final String subType, final String agencyId) throws Throwable {
        caseNote.getCaseNoteUsage(null, null, agencyId, type, subType, fromDate, toDate);
    }
}
