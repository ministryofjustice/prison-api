package net.syscon.elite.executablespecification;

import net.syscon.elite.executablespecification.steps.KeyWorkerAllocateSteps;

import org.springframework.beans.factory.annotation.Autowired;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * BDD step definitions for the key worker endpoints
 */
public class KeyWorkerAllocateStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private KeyWorkerAllocateSteps keyworkerSteps;

    @When("^offender booking \"([^\"]*)\" is allocated to staff user id \"([^\"]*)\" with reason \"([^\"]*)\" and type \"([^\"]*)\"$")
    public void offenderIsAllocated(Long bookingId, Long staffId, String reason, String type) throws Throwable {
        keyworkerSteps.offenderIsAllocated(bookingId,  staffId,  reason,  type);
    }

    @Then("^the allocation is successfully created$")
    public void allocationIsSuccessfullyCreated() throws Throwable {
        keyworkerSteps.allocationIsSuccessfullyCreated();
    }

    @When("^the allocation returns a 404 resource not found with message '(.*)'$")
    public void resourceNotFound(String expectedMessage) throws Throwable {
        keyworkerSteps.verifyResourceNotFound();
           keyworkerSteps.verifyErrorUserMessage(expectedMessage);
    }

    @And("^the allocation returns a 401 bad request with message '(.*)'$")
    public void badRequest(String expectedMessage) throws Throwable {
        keyworkerSteps.verifyBadRequest(expectedMessage);
    }
}
