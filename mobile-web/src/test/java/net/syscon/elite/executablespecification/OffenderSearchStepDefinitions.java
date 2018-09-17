package net.syscon.elite.executablespecification;


import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executablespecification.steps.OffenderSearchSteps;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * BDD step definitions for the following Offender Search API endpoints:
 * <ul>
 *     <li>/v2/offender-search/_/_</li>
 * </ul>
 *
 * NB: Not all API endpoints have associated tests at this point in time.
 */
public class OffenderSearchStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private OffenderSearchSteps offenderSearch;


    @When("^an offender search is made without prisoner name or ID and across \"([^\"]*)\" location$")
    public void aOffenderSearchIsMadeWithoutPrisonerNameOrIDAndAcrossAllLocations(String locationPrefix) throws Throwable {
        offenderSearch.findAll(locationPrefix);
    }

    @When("^an offender search is made for location \"([^\"]*)\"$")
    public void aOffenderSearchIsMadeForLocation(String locationPrefix) throws Throwable {
        offenderSearch.search(locationPrefix, null, false, false, null);
    }

    @When("^an offender search is made with keywords \"([^\"]*)\" in location \"([^\"]*)\"$")
    public void anOffenderSearchIsMadeWithKeywordsInLocation(String keywords, String locationPrefix) throws Throwable {
        offenderSearch.search(locationPrefix, keywords, true, false, null);
    }

    @When("^an offender search is made filtering by alerts \"([^\"]*)\" in location \"([^\"]*)\"$")
    public void aBookingSearchIsMadeWithAlerts(String alerts, String locationPrefix) {
        offenderSearch.search(locationPrefix,  null, false, true, alerts);
    }

    @Then("^\"([^\"]*)\" offender records are returned$")
    public void bookingRecordsAreReturned(String expectedCount) throws Throwable {
        offenderSearch.verifyResourceRecordsReturned(Long.valueOf(expectedCount));
    }

    @Then("^\"([^\"]*)\" total offender records are available$")
    public void totalBookingRecordsAreAvailable(String expectedCount) throws Throwable {
        offenderSearch.verifyTotalResourceRecordsAvailable(Long.valueOf(expectedCount));
    }

    @And("^the offender first names match \"([^\"]*)\"$")
    public void offenderFirstNamesMatch(String firstNames) throws Throwable {
        offenderSearch.verifyFirstNames(firstNames);
    }

    @And("^the offender middle names match \"([^\"]*)\"$")
    public void offenderMiddleNamesMatch(String middleNames) throws Throwable {
        offenderSearch.verifyMiddleNames(middleNames);
    }

    @And("^the offender last names match \"([^\"]*)\"$")
    public void offenderLastNamesMatch(String lastNames) throws Throwable {
        offenderSearch.verifyLastNames(lastNames);
    }

    @And("^location name match \"([^\"]*)\"$")
    public void livingUnitDescriptionsMatch(String livingUnits) throws Throwable {
        offenderSearch.verifyLivingUnits(livingUnits);
    }

    @And("^the offender alerts match \"([^\"]*)\"$")
    public void offenderAlertsMatch(String alerts) throws Throwable {
        offenderSearch.verifyAlerts(alerts);
    }

    @When("^a booking search is made in \"([^\"]*)\"$")
    public void aBookingSearchIsMadeIn(String subLocation) throws Throwable {
        offenderSearch.search(subLocation, null, true, true, null);
    }

    @Then("^only offenders situated in \"([^\"]*)\" be present in the results$")
    public void onlyOffendersSituatedInBePresentInTheResults(String subLocationPrefix) throws Throwable {
       offenderSearch.verifySubLocationPrefixInResults(subLocationPrefix);
    }
}
