package net.syscon.elite.executablespecification;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executablespecification.steps.KeyWorkerSteps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.stream.Collectors;

public class KeyworkerStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private KeyWorkerSteps keyworker;

    @When("^an available key worker request is made with agency id \"([^\"]*)\"$")
    public void anAvailableKeyWorkerRequestIsMadeWithAgencyId(String agencyId) throws Throwable {
        keyworker.getAvailableKeyworkersList(agencyId);
    }

    @Then("^a list of \"([^\"]*)\" key workers are returned$")
    public void aListOfKeyWorkersAreReturned(int count) throws Throwable {
        keyworker.verifyAListOfKeyworkersIsReturned(count);
    }

    @When("^a key worker details request is made with staff id \"([^\"]*)\"$")
    public void anAvailableKeyWorkerRequestIsMadeWithAgencyId(Long staffId) throws Throwable {
        keyworker.getKeyworkerDetails(staffId);
    }

    @Then("^the key worker details are returned$")
    public void aListOfKeyWorkersAreReturned() throws Throwable {
        keyworker.verifyKeyworkerDetails();
    }

    @Then("^the key worker service returns a resource not found response with message \"([^\"]*)\"$")
    public void aResourceNotFoundResponseIsReceivedWithMessage(String message) throws Throwable {
        keyworker.verifyResourceNotFound();
        keyworker.verifyErrorUserMessage(message);
    }

    @And("^the key worker has (\\d+) allocations$")
    public void theKeyWorkerHasAllocations(int expectedAllocationCount) throws Throwable {
        keyworker.verifyKeyWorkerAllocationCount(expectedAllocationCount);
    }

    @And("^the key worker has (\\d+) allocation history entries$")
    public void theKeyWorkerHasAllocationHistoryEntries(int expectedAllocationCount) throws Throwable {
        keyworker.verifyKeyWorkerAllocationHistoryCount(expectedAllocationCount);
    }

    @When("^a key worker allocations request is made with staff id \"([^\"]*)\" and agency \"([^\"]*)\"$")
    public void keyWorkerAllocationsRequestIsMade(Long staffId, String agency) throws Throwable {
        keyworker.getKeyworkerAllocations(staffId, agency);
    }
    

    @Then("^the correct key worker allocations are returned$")
    public void correctKeyWorkerAllocationsReceived() throws Throwable {
        keyworker.verifyKeyWorkerAllocations();
    }

    @When("^a key worker allocations request is made with staff ids \"([^\"]*)\" and agency \"([^\"]*)\"$")
    public void aKeyWorkerAllocationsRequestIsMadeWithStaffIdsAndAgency(String staffIds, String agencyId) throws Throwable {
        keyworker.getKeyworkerAllocationsByStaffIds(Arrays.stream(StringUtils.split(staffIds, ",")).map(Long::new).collect(Collectors.toList()), agencyId);
    }

    @When("^a key worker allocations request is made with nomis ids \"([^\"]*)\" and agency \"([^\"]*)\"$")
    public void aKeyWorkerAllocationsRequestIsMadeWithNomisIdsAndAgency(String offenderNos, String agencyId) throws Throwable {
        keyworker.getKeyworkerAllocationsByOffenderNos(Arrays.asList(StringUtils.split(offenderNos, ",")), agencyId);
    }

    @When("^a key worker allocation history request is made with staff ids \"([^\"]*)\"$")
    public void aKeyWorkerAllocationHistoryRequestIsMadeWithStaffIdsAndAgency(String staffIds) throws Throwable {
        keyworker.getKeyworkerAllocationHistoryByStaffIds(Arrays.stream(StringUtils.split(staffIds, ",")).map(Long::new).collect(Collectors.toList()));
    }

    @When("^a key worker allocation history request is made with nomis ids \"([^\"]*)\"$")
    public void aKeyWorkerAllocationHistoryRequestIsMadeWithNomisIdsAndAgency(String offenderNos) throws Throwable {
        keyworker.getKeyworkerAllocationHistoryByOffenderNos(Arrays.asList(StringUtils.split(offenderNos, ",")));
    }
}
