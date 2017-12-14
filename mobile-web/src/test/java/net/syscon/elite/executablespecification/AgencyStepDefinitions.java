package net.syscon.elite.executablespecification;

import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.executablespecification.steps.AgencySteps;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import cucumber.api.DataTable;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * BDD step definitions for reference domains endpoints:
 * <ul>
 * <li>/agencies/alertTypes</li>
 * <li>/reference-domains/domains/{domain}/codes/{code}</li>
 * </ul>
 */
public class AgencyStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private AgencySteps agencySteps;

    @When("^a request is submitted to retrieve all agencies$")
    public void requestSubmittedToRetrieveAllAgencies() throws Throwable {
        agencySteps.getAllAgencies();
    }

    @Then("^the returned agencies are as follows:$")
    public void agenciesAreReturnedAsFollows(DataTable table) throws Throwable {
        final List<Agency> expected = table.asList(Agency.class);
        agencySteps.verifyAgencyList(expected);
    }

    @When("^a request is submitted to retrieve agency \"([\\w-\\.]+)\"$")
    public void requestSubmittedToRetrieveAgency(String agencyId) throws Throwable {
        agencySteps.getAgency(agencyId);
    }

    @Then("^the returned agency ([^\"]+) is \"([\\w-\\.]+)\"$")
    public void theFieldIs(String field, String value) throws ReflectiveOperationException {
        agencySteps.verifyField(field, value);
    }

    @When("^a request is submitted to retrieve location codes for agency \"([\\w-\\.]+)\"$")
    public void requestSubmittedToRetrieveLocations(String agencyId) throws Throwable {
        agencySteps.getLocations(agencyId);
    }

    @Then("^the returned agency locations are as follows:$")
    public void locationCodesAreReturnedAsFollows(DataTable table) throws Throwable {
        final List<Location> expected = table.asList(Location.class);
        agencySteps.verifyLocationList(expected);
    }
}
