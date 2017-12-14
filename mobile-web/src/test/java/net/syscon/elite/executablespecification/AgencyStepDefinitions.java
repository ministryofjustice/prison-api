package net.syscon.elite.executablespecification;

import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.executablespecification.steps.AgencySteps;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * BDD step definitions for endpoints:
 * <ul>
 * <li>/agencies</li>
 * <li>/agencies/{agencyId}</li>
 * <li>/agencies/{agencyId}/locations</li>
 * </ul>
 */
public class AgencyStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private AgencySteps agencySteps;

    @When("^a request is submitted to retrieve all agencies$")
    public void requestSubmittedToRetrieveAllAgencies() throws Throwable {
        agencySteps.getAllAgencies();
        agencySteps.verifySuccess();
    }

    @Then("^the returned agencies are as follows:$")
    public void agenciesAreReturnedAsFollows(DataTable table) throws Throwable {
        final List<Agency> expected = table.asList(Agency.class);
        agencySteps.verifyAgencyList(expected);
    }

    @Then("^\"([^\"]*)\" agency records are returned$")
    public void locationRecordsAreReturned(String expectedCount) throws Throwable {
        agencySteps.verifyResourceRecordsReturned(Long.valueOf(expectedCount));
    }

    @And("^\"([^\"]*)\" total agency records are available$")
    public void totalLocationRecordsAreAvailable(String expectedCount) throws Throwable {
        agencySteps.verifyTotalResourceRecordsAvailable(Long.valueOf(expectedCount));
    }

    @When("^a request is submitted to retrieve agency \"([^\"]*)\"$")
    public void requestSubmittedToRetrieveAgency(String agencyId) throws Throwable {
        agencySteps.getAgency(agencyId);
    }

    @Then("^the returned agency ([^\"]+) is \"([^\"]*)\"$")
    public void theFieldIs(String field, String value) throws ReflectiveOperationException {
        agencySteps.verifyField(field, value);
    }

    @When("^a request is submitted to retrieve location codes for agency \"([^\"]*)\" and event type \"([^\"]*)\"$")
    public void requestSubmittedToRetrieveLocations(String agencyId, String eventType) throws Throwable {
        agencySteps.getLocations(agencyId, eventType);
    }

    @Then("^the returned agency locations are as follows:$")
    public void locationCodesAreReturnedAsFollows(DataTable table) throws Throwable {
        final List<Location> expected = table.asList(Location.class);
        agencySteps.verifyLocationList(expected);
    }
}
