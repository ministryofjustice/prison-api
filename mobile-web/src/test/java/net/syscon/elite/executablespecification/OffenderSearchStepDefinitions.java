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


    @When("^an offender search is made without prisoner name or ID and across all locations$")
    public void aOffenderSearchIsMadeWithoutPrisonerNameOrIDAndAcrossAllLocations() throws Throwable {
        offenderSearch.findAll();
    }

    @When("^an offender search is made for location \"([^\"]*)\"$")
    public void aOffenderSearchIsMadeForLocation(String locationPrefix) throws Throwable {
        offenderSearch.search(locationPrefix, null);
    }

    @When("^an offender search is made with keywords \"([^\"]*)\" of existing offender$")
    public void aOffenderSearchIsMadeWithKeywordsExistingOffender(String keywords) throws Throwable {
        offenderSearch.search(null, keywords);
    }

    @When("^an offender search is made with keywords \"([^\"]*)\" in location \"([^\"]*)\"$")
    public void anOffenderSearchIsMadeWithKeywordsInLocation(String keywords, String locationPrefix) throws Throwable {
        offenderSearch.search(locationPrefix, keywords);
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

}
