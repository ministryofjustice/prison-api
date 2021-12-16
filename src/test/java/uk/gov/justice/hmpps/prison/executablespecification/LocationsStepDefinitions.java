package uk.gov.justice.hmpps.prison.executablespecification;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.executablespecification.steps.LocationsSteps;

/**
 * BDD step definitions for Booking API endpoints:
 * <ul>
 *     <li>/locations</li>
 *     <li>/locations/groups</li>
 *     <li>/locations/groups/{agencyId}/{name}</li>
 *     <li>/locations/description/{agencyId}/inmates</li>
 * </ul>
 */
public class LocationsStepDefinitions extends AbstractStepDefinitions {
    @Autowired
    private LocationsSteps location;

    @Then("^\"([^\"]*)\" location records are returned$")
    public void locationRecordsAreReturned(final String expectedCount) throws Throwable {
        location.verifyResourceRecordsReturned(Long.valueOf(expectedCount));
    }

    @And("^\"([^\"]*)\" total location records are available$")
    public void totalLocationRecordsAreAvailable(final String expectedCount) throws Throwable {
        location.verifyTotalResourceRecordsAvailable(Long.valueOf(expectedCount));
    }

    @When("^a request is made to retrieve location with locationId of \"([^\"]*)\"$")
    public void aRequestIsMadeToRetrieveASpecificLocationBy(final String locationId) throws Throwable {
        location.findByLocationId(Long.valueOf(locationId));
    }

    @And("^location type is \"([^\"]*)\"$")
    public void locationTypeIs(final String type) throws Throwable {
        location.verifyLocationType(type);
    }

    @And("^description is \"([^\"]*)\"$")
    public void descriptionIs(final String description) throws Throwable {
        location.verifyLocationDescription(description);
    }

    @Then("^resource not found response is received from locations API$")
    public void resourceNotFoundResponseIsReceivedFromLocationsAPI() throws Throwable {
        location.verifyResourceNotFound();
    }

    @When("^a request is made at agency \"([^\"]*)\" to retrieve the list named \"([^\"]*)\"$")
    public void aRequestIsMadeToRetrieveListNamed(final String agencyId, final String name) throws Throwable {
        location.findList(agencyId, name);
    }

    @Then("^locations are \"([^\"]*)\"$")
    public void locationsAre(final String list) throws Throwable {
        location.verifyLocationList(list);
    }

    @Then("^location ids are \"([^\"]*)\"$")
    public void locationIdsAre(final String list) throws Throwable {
        location.verifyLocationIdList(list);
    }

    @When("^a request is made at agency \"([^\"]*)\" to retrieve a list of inmates$")
    public void retrieveListOfInmates(final String agency) {
        location.retrieveListOfInmates(agency);
    }

    @Then("^there are \"([^\"]*)\" offenders returned$")
    public void verifyOffenderCount(final String countOffenders) {
        location.checkOffenderCount(Integer.parseInt(countOffenders));
    }

    @Then("^there are \"([^\"]*)\" offenders returned with the convicted status \"([^\"]*)\"$")
    public void checkConvictedOffenderCountByStatus(final String countOffenders, final String convictedStatus) {
        location.checkOffenderCountByConvictedStatus(Integer.parseInt(countOffenders), convictedStatus);
    }

    @When("^a request is made at agency \"([^\"]*)\" to retrieve a list of inmates with a convicted status of \"([^\"]*)\"$")
    public void retrieveInmatesByConvictedStatus(final String agency, final String convictedStatus) {
        location.retrieveListOfInmates(agency, convictedStatus);
    }

}
