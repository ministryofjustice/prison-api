package net.syscon.elite.executablespecification;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executablespecification.steps.LocationsSteps;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * BDD step definitions for Booking API endpoints:
 * <ul>
 *     <li>/locations</li>
 *     <li>/locations/{locationId}</li>
 *     <li>/locations/{locationId}/inmates</li>
 * </ul>
 *
 * NB: Not all API endpoints have associated tests at this point in time.
 */
public class LocationsStepDefinitions extends AbstractStepDefinitions {
    @Autowired
    private LocationsSteps location;

    @When("^a request is made to retrieve all locations available to the user$")
    public void aRequestIsMadeToRetrieveAllLocationsAvailableToTheUser() throws Throwable {
        location.findAll();
    }

    @Then("^\"([^\"]*)\" location records are returned$")
    public void locationRecordsAreReturned(String expectedCount) throws Throwable {
        location.verifyResourceRecordsReturned(Long.valueOf(expectedCount));
    }

    @And("^\"([^\"]*)\" total location records are available$")
    public void totalLocationRecordsAreAvailable(String expectedCount) throws Throwable {
        location.verifyTotalResourceRecordsAvailable(Long.valueOf(expectedCount));
    }

    @When("^a request is made to retrieve location with locationId of \"([^\"]*)\"$")
    public void aRequestIsMadeToRetrieveASpecificLocationBy(String locationId) throws Throwable {
        location.findByLocationId(Long.valueOf(locationId));
    }

    @And("^location type is \"([^\"]*)\"$")
    public void locationTypeIs(String type) throws Throwable {
        location.verifyLocationType(type);
    }

    @And("^description is \"([^\"]*)\"$")
    public void descriptionIs(String description) throws Throwable {
        location.verifyLocationDescription(description);
    }

    @Then("^resource not found response is received from locations API$")
    public void resourceNotFoundResponseIsReceivedFromLocationsAPI() throws Throwable {
        location.verifyResourceNotFound();
    }

    @When("^a request is made at agency \"([^\"]*)\" to retrieve the list named \"([^\"]*)\"$")
    public void aRequestIsMadeToRetrieveListNamed(String agencyId, String name) throws Throwable {
        location.findList(agencyId, name);
    }

    @Then("^locations are \"([^\"]*)\"$")
    public void locationsAre(String list) throws Throwable {
        location.verifyLocationList(list);
    }
    
    @Then("^location ids are \"([^\"]*)\"$")
    public void locationIdsAre(String list) throws Throwable {
        location.verifyLocationIdList(list);
    }
    
    @When("^a request is made at agency \"([^\"]*)\" to retrieve all the groups$")
    public void aRequestIsMadeToRetrieveAllGroups(String agencyId) throws Throwable {
        location.aRequestIsMadeToRetrieveAllGroups(agencyId);
    }

    @Then("^location groups are \"([^\"]*)\"$")
    public void groupsAre(String list) throws Throwable {
        location.groupsAre(list);
    }
}
