package net.syscon.elite.executablespecification;

import cucumber.api.Format;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executablespecification.steps.AdjudicationSteps;
import net.syscon.elite.executablespecification.steps.AdjudicationSteps.AwardOldDate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

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
        adjudicationSteps.getAwards(id, null, null);
    }

    @When("^adjudication details with booking id ([0-9-]+), award cutoff date \"([^\"]*)\" and adjudication cutoff date \"([^\"]*)\" is requested$")
    public void withBookingId(Long id, String awardCutoffDate, String adjudicationCutoffDate) {
        adjudicationSteps.setIndex(0);
        adjudicationSteps.getAwards(id, awardCutoffDate, adjudicationCutoffDate);
    }

    @Then("^resource not found response is received from adjudication details API$")
    public void resourceNotFoundResponseIsReceivedFromAwardsAPI() throws Throwable {
        adjudicationSteps.verifyResourceNotFound();
    }

    @Then("^the adjudication count is ([0-9-]+)")
    public void theAdjudicationCountIs(Integer value) throws Throwable {
        adjudicationSteps.verifyAdjudicationCount(value);
    }

    @Then("^the award (\\w+) is \"([^\"]*)\"$")
    public void theAwardsFieldIs(String field, String value) throws Throwable {
        adjudicationSteps.verifyAwardField(field, value);
    }

    @Then("^there are ([0-9]+) awards$")
    public void awardsNumber(Integer n) throws Throwable {
        adjudicationSteps.verifyAwardsNumber(n);
    }

    @Then("^the award result list is as follows:$")
    public void awardResultListIsAsFollows(@Format("yyyy-MM-dd") List<AwardOldDate> list) throws Throwable {
        adjudicationSteps.verifyAwards(list);
    }

    @Then("^There are no awards$")
    public void noAwards() throws Throwable {
        adjudicationSteps.verifyNoAwards();
    }
}
