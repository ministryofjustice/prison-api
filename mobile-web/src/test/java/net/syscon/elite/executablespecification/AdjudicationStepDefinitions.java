package net.syscon.elite.executablespecification;

import net.syscon.elite.executablespecification.steps.AdjudicationSteps;

import org.springframework.beans.factory.annotation.Autowired;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * BDD step definitions for the following Booking API endpoints:
 * <ul>
 * <li>/booking/{bookingId}/contacts</li>
 * </ul>
 *
 * NB: Not all API endpoints have associated tests at this point in time.
 */
public class AdjudicationStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private AdjudicationSteps adjudicationSteps;

    @When("^adjudication details with booking id ([0-9-]+) is requested$")
    public void withBookingId(Long id) {
        adjudicationSteps.setIndex(0);
        adjudicationSteps.getAwards(id, null);
    }

    @When("^adjudication details with booking id ([0-9-]+) and cutoff date \"([^\"]*)\" is requested$")
    public void withBookingId(Long id, String fromDate) {
        adjudicationSteps.setIndex(0);
        adjudicationSteps.getAwards(id, fromDate);
    }

    @Then("^resource not found response is received from adjudication details API$")
    public void resourceNotFoundResponseIsReceivedFromAwardsAPI() throws Throwable {
        adjudicationSteps.verifyResourceNotFound();
    }

    @Then("^the award (\\w+) is \"([^\"]*)\"$")
    public void theAwardsFieldIs(String field, String value) throws Throwable {
        adjudicationSteps.verifyAwardField(field, value);
    }

    @Then("^there are ([0-9]+) awards$")
    public void awardsNumber(Integer n) throws Throwable {
        adjudicationSteps.verifyAwardsNumber(n);
    }

    @Then("^For award index ([0-9]+),$")
    public void awardsIndex(Integer i) throws Throwable {
        adjudicationSteps.setIndex(i);
    }

    @Then("^There are no awards$")
    public void noAwards() throws Throwable {
        adjudicationSteps.verifyNoAwards();
    }
}
