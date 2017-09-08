package net.syscon.elite.executableSpecification;


import cucumber.api.java.en.Then;
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

}
