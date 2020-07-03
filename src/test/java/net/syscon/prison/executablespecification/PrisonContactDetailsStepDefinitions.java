package net.syscon.prison.executablespecification;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.prison.executablespecification.steps.PrisonContactDetailsSteps;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * BDD step definitions for the following Booking API endpoints:
 * <ul>
 * <li>/agencies/prisons</li>
 * <li>/agencies/{agencyId}</li>
 * </ul>
 * <p>
 * NB: Not all API endpoints have associated tests at this point in time.
 */
public class PrisonContactDetailsStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private PrisonContactDetailsSteps agencySteps;

    @When("^a request is made to retrieve all prison contact details$")
    public void getPrisonContactDetails() {
        agencySteps.getPrisonContactDetails();
    }

    @When("^a request is made to retrieve contact details for prison \"([^\"]*)\"$")
    public void getPrisonContactDetails(final String agencyId) {
        agencySteps.getPrisonContactDetails(agencyId);
    }

    @Then("^a single prison contact details record is returned")
    public void aSinglePrisonContactDetailsIsReturned() throws Throwable {
        agencySteps.verifyPrisonContactDetails();
    }

    @Then("^a response of resource not found is received")
    public void verifyResourceNotFound() throws Throwable {
        agencySteps.verifyResourceNotFound();
    }

    @Then("^a list of prison contact details are returned$")
    public void aListOfPrisonContactDetailsAreReturned() {
        agencySteps.verifyAListOfPrisonContactDetailsIsReturned();
    }

    @Then("^a dummy list of prison contact details are returned$")
    public void aDummyListOfPrisonContactDetailsAreReturned() throws Throwable {
        agencySteps.verifyADummyListOfPrisonContactDetailsIsReturned();
    }
}
