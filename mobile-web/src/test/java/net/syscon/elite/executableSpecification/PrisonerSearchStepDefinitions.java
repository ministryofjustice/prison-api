package net.syscon.elite.executableSpecification;


import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executableSpecification.steps.PrisonerSearchSteps;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * BDD step definitions for the following Offender Search API endpoints:
 * <ul>
 *     <li>/v2/prisoners</li>
 * </ul>
 *
 * NB: Not all API endpoints have associated tests at this point in time.
 */
public class PrisonerSearchStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private PrisonerSearchSteps prisonerSearch;

    @Then("^\"([^\"]*)\" prisoner records are returned$")
    public void bookingRecordsAreReturned(String expectedCount) throws Throwable {
        prisonerSearch.verifyResourceRecordsReturned(Long.valueOf(expectedCount));
    }

    @Then("^\"([^\"]*)\" total prisoner records are available$")
    public void totalBookingRecordsAreAvailable(String expectedCount) throws Throwable {
        prisonerSearch.verifyTotalResourceRecordsAvailable(Long.valueOf(expectedCount));
    }

    @And("^the prisoners first names match \"([^\"]*)\"$")
    public void offenderFirstNamesMatch(String firstNames) throws Throwable {
        prisonerSearch.verifyFirstNames(firstNames);
    }

    @And("^the prisoners middle names match \"([^\"]*)\"$")
    public void offenderMiddleNamesMatch(String middleNames) throws Throwable {
        prisonerSearch.verifyMiddleNames(middleNames);
    }

    @And("^the prisoners last names match \"([^\"]*)\"$")
    public void offenderLastNamesMatch(String lastNames) throws Throwable {
        prisonerSearch.verifyLastNames(lastNames);
    }

    @When("^a search is made for prisoners with type \"([^\"]*)\" and value \"([^\"]*)\" for range ([0-9]*) -> ([0-9]*)$")
    public void aSearchIsMadeForPrisonersWithADobMoreThan(String queryName, String queryValue, int offset, int limit) throws Throwable {
        prisonerSearch.search(queryName, queryValue, offset, limit);
    }

}
