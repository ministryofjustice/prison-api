package net.syscon.elite.executablespecification;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executablespecification.steps.KeyWorkerSteps;
import org.springframework.beans.factory.annotation.Autowired;

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

    @When("^a key worker allocations request is made with staff id \"([^\"]*)\"$")
    public void keyWorkerAllocationsRequestIsMade(Long staffId) throws Throwable {
        keyworker.getKeyworkerAllocations(staffId);
    }
    

    @Then("^the correct key worker allocations are returned$")
    public void correctKeyWorkerAllocationsReceived() throws Throwable {
        keyworker.verifyKeyWorkerAllocations();
    }

}
