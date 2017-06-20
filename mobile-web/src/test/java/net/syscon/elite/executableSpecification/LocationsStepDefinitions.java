package net.syscon.elite.executableSpecification;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executableSpecification.steps.LocationsSteps;
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
}
