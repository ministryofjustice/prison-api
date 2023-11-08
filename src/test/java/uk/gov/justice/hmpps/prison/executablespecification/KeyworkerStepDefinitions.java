package uk.gov.justice.hmpps.prison.executablespecification;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.executablespecification.steps.KeyWorkerSteps;

import java.util.Arrays;

public class KeyworkerStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private KeyWorkerSteps keyworker;

    @When("^a key worker details request is made with staff id \"([^\"]*)\"$")
    public void anAvailableKeyWorkerRequestIsMadeWithAgencyId(final Long staffId) throws Throwable {
        keyworker.getKeyworkerDetails(staffId);
    }

    @Then("^the key worker details are returned$")
    public void aListOfKeyWorkersAreReturned() throws Throwable {
        keyworker.verifyKeyworkerDetails();
    }

    @Then("^the key worker service returns a resource not found response with message \"([^\"]*)\"$")
    public void aResourceNotFoundResponseIsReceivedWithMessage(final String message) throws Throwable {
        keyworker.verifyResourceNotFound();
        keyworker.verifyErrorUserMessage(message);
    }

    @And("^the key worker has (\\d+) allocations$")
    public void theKeyWorkerHasAllocations(final int expectedAllocationCount) throws Throwable {
        keyworker.verifyKeyWorkerAllocationCount(expectedAllocationCount);
    }

    @And("^the key worker has (\\d+) allocation history entries$")
    public void theKeyWorkerHasAllocationHistoryEntries(final int expectedAllocationCount) throws Throwable {
        keyworker.verifyKeyWorkerAllocationHistoryCount(expectedAllocationCount);
    }

    @When("^a key worker allocations request is made with staff id \"([^\"]*)\" and agency \"([^\"]*)\"$")
    public void keyWorkerAllocationsRequestIsMade(final Long staffId, final String agency) throws Throwable {
        keyworker.getKeyworkerAllocations(staffId, agency);
    }

    @Then("^the correct key worker allocations are returned$")
    public void correctKeyWorkerAllocationsReceived() throws Throwable {
        keyworker.verifyKeyWorkerAllocations();
    }

    @When("^a key worker allocations request is made with staff ids \"([^\"]*)\" and agency \"([^\"]*)\"$")
    public void aKeyWorkerAllocationsRequestIsMadeWithStaffIdsAndAgency(final String staffIds, final String agencyId) throws Throwable {
        keyworker.getKeyworkerAllocationsByStaffIds(Arrays.stream(StringUtils.split(staffIds, ",")).map(Long::valueOf).toList(), agencyId);
    }

    @When("^a key worker allocations request is made with nomis ids \"([^\"]*)\" and agency \"([^\"]*)\"$")
    public void aKeyWorkerAllocationsRequestIsMadeWithNomisIdsAndAgency(final String offenderNos, final String agencyId) throws Throwable {
        keyworker.getKeyworkerAllocationsByOffenderNos(Arrays.asList(StringUtils.split(offenderNos, ",")), agencyId);
    }

    @When("^a key worker allocation history request is made with staff ids \"([^\"]*)\"$")
    public void aKeyWorkerAllocationHistoryRequestIsMadeWithStaffIdsAndAgency(final String staffIds) throws Throwable {
        keyworker.getKeyworkerAllocationHistoryByStaffIds(Arrays.stream(StringUtils.split(staffIds, ",")).map(Long::valueOf).toList());
    }

    @When("^a key worker allocation history request is made with nomis ids \"([^\"]*)\"$")
    public void aKeyWorkerAllocationHistoryRequestIsMadeWithNomisIdsAndAgency(final String offenderNos) throws Throwable {
        keyworker.getKeyworkerAllocationHistoryByOffenderNos(Arrays.asList(StringUtils.split(offenderNos, ",")));
    }
}
