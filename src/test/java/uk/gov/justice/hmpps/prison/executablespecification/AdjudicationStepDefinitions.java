package uk.gov.justice.hmpps.prison.executablespecification;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.api.model.adjudications.Award;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AdjudicationSteps;

import java.util.List;

/**
 * BDD step definitions for the following Booking API endpoints:
 * <ul>
 * <li>/booking/{bookingId}/contacts</li>
 * </ul>
 * <p>
 * NB: Not all API endpoints have associated tests at this point in time.
 */
public class AdjudicationStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private AdjudicationSteps adjudicationSteps;

    @When("^adjudication summary with booking id ([0-9-]+) is requested$")
    public void withBookingId(final Long id) {
        adjudicationSteps.setIndex(0);
        adjudicationSteps.getAdjudicationSummary(id, null, null);
    }

    @When("^adjudication summary with booking id ([0-9-]+), award cutoff date \"([^\"]*)\" and adjudication cutoff date \"([^\"]*)\" is requested$")
    public void withBookingId(final Long id, final String awardCutoffDate, final String adjudicationCutoffDate) {
        adjudicationSteps.setIndex(0);
        adjudicationSteps.getAdjudicationSummary(id, awardCutoffDate, adjudicationCutoffDate);
    }

    @Then("^resource not found response is received from adjudication summary API$")
    public void resourceNotFoundResponseIsReceivedFromAwardsAPI() throws Throwable {
        adjudicationSteps.verifyResourceNotFound();
    }

    @Then("^the adjudication count is ([0-9-]+)")
    public void theAdjudicationCountIs(final Integer value) throws Throwable {
        adjudicationSteps.verifyAdjudicationCount(value);
    }

    @Then("^the award (\\w+) is \"([^\"]*)\"$")
    public void theAwardsFieldIs(final String field, final String value) throws Throwable {
        adjudicationSteps.verifyAwardField(field, value);
    }

    @Then("^there are ([0-9]+) awards$")
    public void awardsNumber(final Integer n) throws Throwable {
        adjudicationSteps.verifyAwardsNumber(n);
    }

    @Then("^the award result list is as follows:$")
    public void awardResultListIsAsFollows(final List<Award> list) throws Throwable {
        adjudicationSteps.verifyAwards(list);
    }

    @Then("^There are no awards$")
    public void noAwards() throws Throwable {
        adjudicationSteps.verifyNoAwards();
    }
}
