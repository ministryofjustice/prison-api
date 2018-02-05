package net.syscon.elite.executablespecification;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executablespecification.steps.KeyWorkerSteps;
import org.springframework.beans.factory.annotation.Autowired;

public class KeyworkerStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private KeyWorkerSteps keyworkerSteps;

    @When("^an available key worker request is made with agency id \"([^\"]*)\"$")
    public void anAvailableKeyWorkerRequestIsMadeWithAgencyId(String agencyId) throws Throwable {
        keyworkerSteps.getAvailableKeyworkersList(agencyId);
    }

    @Then("^a list of \"([^\"]*)\" key workers are returned$")
    public void aListOfKeyWorkersAreReturned(int count) throws Throwable {
        keyworkerSteps.verifyAListOfKeyworkersIsReturned(count);
    }

    @When("^a key worker details request is made with staff id \"([^\"]*)\"$")
    public void anAvailableKeyWorkerRequestIsMadeWithAgencyId(Long staffId) throws Throwable {
        keyworkerSteps.getKeyworkerDetails(staffId);
    }

    @Then("^the key worker details are returned$")
    public void aListOfKeyWorkersAreReturned() throws Throwable {
        keyworkerSteps.verifyKeyworkerDetails();
    }

    @Then("^the key worker service returns a resource not found response with message \"([^\"]*)\"$")
    public void aResourceNotFoundResponseIsReceivedWithMessage(String message) throws Throwable {
        keyworkerSteps.verifyResourceNotFound();
        keyworkerSteps.verifyErrorUserMessage(message);
    }
}
