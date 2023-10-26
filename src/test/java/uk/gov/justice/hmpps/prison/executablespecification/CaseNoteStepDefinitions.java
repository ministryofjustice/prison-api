package uk.gov.justice.hmpps.prison.executablespecification;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.api.model.NewCaseNote;
import uk.gov.justice.hmpps.prison.executablespecification.steps.CaseNoteSteps;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;

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

    @When("^case notes are requested for offender booking \"([^\"]*)\"$")
    public void caseNotesAreRequestedForOffenderBooking(final String bookingId) throws Throwable {
        caseNote.getCaseNotes(Long.valueOf(bookingId));
    }

    @Then("^\"([^\"]*)\" case notes are returned$")
    public void caseNotesAreReturned(final String count) throws Throwable {
        caseNote.verifyResourceRecordsReturned(Long.valueOf(count));
    }

    @And("^case note types match \"([^\"]*)\"$")
    public void caseNoteTypesMatch(final String caseNoteTypes) throws Throwable {
        caseNote.verifyCaseNoteTypes(caseNoteTypes);
    }

    @And("^case note sub types match \"([^\"]*)\"$")
    public void caseNoteSubTypesMatch(final String caseNoteSubTypes) throws Throwable {
        caseNote.verifyCaseNoteSubTypes(caseNoteSubTypes);
    }

    @And("^case note type \"([^\"]*)\" filter applied$")
    public void caseNoteTypeFilterApplied(final String caseNoteType) throws Throwable {
        caseNote.applyCaseNoteTypeFilter(caseNoteType);
    }

    @And("^case note sub type \"([^\"]*)\" filter applied$")
    public void caseNoteSubTypeFilterApplied(final String caseNoteSubType) throws Throwable {
        caseNote.applyCaseNoteSubTypeFilter(caseNoteSubType);
    }

    @And("^case note agency \"([^\"]*)\" filter applied$")
    public void caseNoteAgencyFilterApplied(final String agencyId) throws Throwable {
        caseNote.applyAgencyFilter(agencyId);
    }

    @And("^date from \"([^\"]*)\" filter applied$")
    public void dateFromFilterApplied(final String dateFrom) throws Throwable {
        caseNote.applyDateFromFilter(dateFrom);
    }

    @And("^date to \"([^\"]*)\" filter applied$")
    public void dateToFilterApplied(final String dateTo) throws Throwable {
        caseNote.applyDateToFilter(dateTo);
    }

    @And("^pagination with limit \"([0-9]*)\" and offset \"([0-9]*)\" applied$")
    public void paginationWithLimitAndOffsetApplied(final Long limit, final Long offset) throws Throwable {
        caseNote.applyPagination(offset, limit);
    }

    @And("^pagination with size \"([0-9]*)\" and page number \"([0-9]*)\" applied$")
    public void paginationWithPageNumberAndSizeApplied(final Long size, final Long pageNumber) throws Throwable {
        caseNote.applyPageNumberAndSize(pageNumber, size);
    }

    @And("^filtered case notes are requested for offender booking \"([^\"]*)\"$")
    public void filteredCaseNotesAreRequestedForOffenderBooking(final String bookingId) throws Throwable {
        caseNote.getCaseNotes(Long.valueOf(bookingId));
    }

    @And("^\"([^\"]*)\" case notes are available$")
    public void caseNotesAreAvailable(final String count) throws Throwable {
        caseNote.verifyTotalResourceRecordsAvailable(Long.valueOf(count));
    }

    @Then("^resource not found response is received from casenotes API")
    public void caseNotesVerifyResourceNotFound() throws Throwable {
        caseNote.verifyResourceNotFound();
    }

    @Then("^case note size is \"([^\"]*)\"$")
    public void caseNoteSizeIs(final String size) throws Throwable {
        caseNote.verifyCaseNoteUsageSize(Integer.valueOf(size));
    }

    @Then("^case note staff usage size is \"([^\"]*)\"$")
    public void caseNoteStaffUsageSizeIs(final String size) throws Throwable {
        caseNote.verifyCaseNoteStaffUsageSize(Integer.valueOf(size));
    }

    @Then("^case note usage response \"([^\"]*)\" is \"([^\"]*)\"$")
    public void caseNoteUsageResponseIs(final String propertyName, final String expectedValue) throws Throwable {
        caseNote.verifyCaseNoteUsagePropertyValue(propertyName, expectedValue);
    }

    @Then("^case note staff usage response \"([^\"]*)\" is \"([^\"]*)\"$")
    public void caseNoteStaffUsageResponseIs(final String propertyName, final String expectedValue) throws Throwable {
        caseNote.verifyCaseNoteStaffUsagePropertyValue(propertyName, expectedValue);
    }


    @Then("^bad request response, with \"([^\"]*)\" message, is received from casenotes API$")
    public void badRequestResponseWithMessageIsReceivedFromCasenotesAPI(final String expectedUserMessage) throws Throwable {
        caseNote.verifyBadRequest(expectedUserMessage);
    }

    @Then("^access denied response, with \"([^\"]*)\" message, is received from booking case notes API$")
    public void accessDeniedResponseWithMessageIsReceivedFromBookingCaseNotesAPI(final String userMessage) throws Throwable {
        caseNote.verifyAccessDenied(userMessage);
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
