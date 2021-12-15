package uk.gov.justice.hmpps.prison.executablespecification;


import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.executablespecification.steps.OffenderSearchSteps;

import java.time.LocalDate;

/**
 * BDD step definitions for the following Offender Search API endpoints:
 * <ul>
 *     <li>/v2/offender-search/_/_</li>
 * </ul>
 * <p>
 * NB: Not all API endpoints have associated tests at this point in time.
 */
public class OffenderSearchStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private OffenderSearchSteps offenderSearch;


    @When("^an offender search is made without prisoner name or ID and across \"([^\"]*)\" location$")
    public void aOffenderSearchIsMadeWithoutPrisonerNameOrIDAndAcrossAllLocations(final String locationPrefix) throws Throwable {
        offenderSearch.findAll(locationPrefix);
    }

    @When("^an offender search is made for location \"([^\"]*)\"$")
    public void aOffenderSearchIsMadeForLocation(final String locationPrefix) throws Throwable {
        offenderSearch.search(locationPrefix, null, false, false, false, null, null, null);
    }

    @When("^an offender search is made with keywords \"([^\"]*)\" in location \"([^\"]*)\"$")
    public void anOffenderSearchIsMadeWithKeywordsInLocation(final String keywords, final String locationPrefix) throws Throwable {
        offenderSearch.search(locationPrefix, keywords, true, false, false, null, null, null);
    }

    @When("^an offender search is made filtering by alerts \"([^\"]*)\" in location \"([^\"]*)\"$")
    public void aBookingSearchIsMadeWithAlerts(final String alerts, final String locationPrefix) {
        offenderSearch.search(locationPrefix, null, false, true, true, alerts, null, null);
    }

    @When("^an offender search is made in location \"([^\"]*)\" filtering between DOB between \"([^\"]*)\" and \"([^\"]*)\"$")
    public void aBookingSearchIsMadeFilteringOnDobRange(final String locationPrefix, final String fromDob, final String toDob) {
        offenderSearch.search(locationPrefix, null, false, true, true, null, StringUtils.isNotBlank(fromDob) ? LocalDate.parse(fromDob) : null, StringUtils.isNotBlank(toDob) ? LocalDate.parse(toDob) : null);
    }

    @Then("^\"([^\"]*)\" offender records are returned$")
    public void bookingRecordsAreReturned(final String expectedCount) throws Throwable {
        offenderSearch.verifyResourceRecordsReturned(Long.valueOf(expectedCount));
    }

    @Then("^\"([^\"]*)\" total offender records are available$")
    public void totalBookingRecordsAreAvailable(final String expectedCount) throws Throwable {
        offenderSearch.verifyTotalResourceRecordsAvailable(Long.valueOf(expectedCount));
    }

    @And("^the offender first names match \"([^\"]*)\"$")
    public void offenderFirstNamesMatch(final String firstNames) throws Throwable {
        offenderSearch.verifyFirstNames(firstNames);
    }

    @And("^the offender middle names match \"([^\"]*)\"$")
    public void offenderMiddleNamesMatch(final String middleNames) throws Throwable {
        offenderSearch.verifyMiddleNames(middleNames);
    }

    @And("^the offender last names match \"([^\"]*)\"$")
    public void offenderLastNamesMatch(final String lastNames) throws Throwable {
        offenderSearch.verifyLastNames(lastNames);
    }

    @And("^location name match \"([^\"]*)\"$")
    public void livingUnitDescriptionsMatch(final String livingUnits) throws Throwable {
        offenderSearch.verifyLivingUnits(livingUnits);
    }

    @And("^DOB match \"([^\"]*)\"$")
    public void dobMatch(final String dobs) throws Throwable {
        offenderSearch.verifyDob(dobs);
    }

    @And("^the offender alerts match \"([^\"]*)\"$")
    public void offenderAlertsMatch(final String alerts) throws Throwable {
        offenderSearch.verifyAlerts(alerts);
    }

    @And("^the offender categories match \"([^\"]*)\"$")
    public void offenderCategoriesMatch(final String categories) throws Throwable {
        offenderSearch.verifyCategories(categories);
    }

    @When("^a booking search is made in \"([^\"]*)\"$")
    public void aBookingSearchIsMadeIn(final String subLocation) throws Throwable {
        offenderSearch.search(subLocation, null, true, true, false, null, null, null);
    }

    @Then("^only offenders situated in \"([^\"]*)\" be present in the results$")
    public void onlyOffendersSituatedInBePresentInTheResults(final String subLocationPrefix) throws Throwable {
        offenderSearch.verifySubLocationPrefixInResults(subLocationPrefix);
    }
}
