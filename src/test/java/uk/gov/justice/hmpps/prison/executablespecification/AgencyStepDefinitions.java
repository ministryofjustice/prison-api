package uk.gov.justice.hmpps.prison.executablespecification;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.api.model.Agency;
import uk.gov.justice.hmpps.prison.api.model.IepLevel;
import uk.gov.justice.hmpps.prison.api.model.Location;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AgencySteps;

import java.util.List;

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
    }

    @When("^a request is submitted to retrieve all agencies by caseload \"([^\"]*)\"$")
    public void requestSubmittedToRetrieveAgenciesByCaseload(final String caseload) throws Throwable {
        agencySteps.getAgenciesByCaseload(caseload);
    }

    @Then("^the returned agencies are as follows:$")
    public void agenciesAreReturnedAsFollows(final DataTable table) throws Throwable {
        final List<Agency> expected = table.asList(Agency.class);
        agencySteps.verifyAgencyList(expected);
    }

    @Then("^\"([^\"]*)\" agency records are returned$")
    public void locationRecordsAreReturned(final String expectedCount) throws Throwable {
        agencySteps.verifyResourceRecordsReturned(Long.valueOf(expectedCount));
    }

    @And("^\"([^\"]*)\" total agency records are available$")
    public void totalLocationRecordsAreAvailable(final String expectedCount) throws Throwable {
        agencySteps.verifyTotalResourceRecordsAvailable(Long.valueOf(expectedCount));
    }

    @When("^a request is submitted to retrieve agency \"([^\"]*)\"$")
    public void requestSubmittedToRetrieveAgency(final String agencyId) throws Throwable {
        agencySteps.getAgency(agencyId, null);
    }

    @When("^a request is submitted to retrieve agency \"([^\"]*)\" when \"([^\"]*)\" inactive$")
    public void requestSubmittedToRetrieveInactiveAgency(final String agencyId, final String filter) throws Throwable {
        agencySteps.getAgency(agencyId, filter.equalsIgnoreCase("excluding"));
    }

    @Then("^the returned agency ([^\"]+) is \"([^\"]*)\"$")
    public void theFieldIs(final String field, final String value) throws ReflectiveOperationException {
        agencySteps.verifyField(field, value);
    }

    @When("^a request is submitted to retrieve location codes for agency \"([^\"]*)\" and event type \"([^\"]*)\"$")
    public void requestSubmittedToRetrieveLocations(final String agencyId, final String eventType) throws Throwable {
        agencySteps.getLocations(agencyId, eventType, null, null);
    }

    @When("^a request is submitted to retrieve location codes for agency \"([^\"]*)\"$")
    public void aRequestIsSubmittedToRetrieveLocationCodesForAgency(final String agencyId) throws Throwable {
        agencySteps.getLocations(agencyId, null, null, null);
    }

    @When("^a request is submitted to retrieve location codes for agency \"([^\"]*)\" for any events$")
    public void aRequestIsSubmittedToRetrieveLocationCodesForAgencyForAnyEvents(final String agencyId) throws Throwable {
        agencySteps.getLocationsForAnyEvents(agencyId);
    }

    @When("^a request is submitted to retrieve location codes for agency \"([^\"]*)\" and event type \"([^\"]*)\" sorted by \"([^\"]*)\" in \"([^\"]*)\" order$")
    public void aRequestIsSubmittedToRetrieveLocationCodesForAgencyAndEventTypeSortedByInOrder(final String agencyId, final String eventType, final String sortFields, final String sortOrder) throws Throwable {
        agencySteps.getLocations(agencyId, eventType, sortFields, parseSortOrder(sortOrder));
    }

    @Then("^the returned agency locations are as follows:$")
    public void locationCodesAreReturnedAsFollows(final DataTable table) throws Throwable {
        final List<Location> expected = table.asList(Location.class);
        agencySteps.verifyLocationList(expected);
    }

    @Then("^\"([^\"]*)\" location records are returned for agency$")
    public void locationRecordsAreReturnedForAgency(final String expectedCount) throws Throwable {
        agencySteps.verifyResourceRecordsReturned(Long.valueOf(expectedCount));
    }

    @Then("^the agency is found$")
    public void theAgencyIsFound() {
        agencySteps.verifyNoError();
    }

    @Then("^the agency is not found$")
    public void theAgencyIsNotFound() {
        agencySteps.verifyResourceNotFound();
    }

    @When("^a request is submitted to retrieve IEP levels for agency \"([^\"]*)\"$")
    public void aRequestIsMadeToRetrieveIepLevels(final String agencyId) throws Throwable {
        agencySteps.aRequestIsMadeToRetrieveIepLevels(agencyId);
    }

    @Then("^the returned IEP levels are as follows:$")
    public void IepLevelsAreReturnedAsFollows(final DataTable table) throws Throwable {
        final List<IepLevel> expected = table.asList(IepLevel.class);
        agencySteps.verifyIepLevelsList(expected);
    }
}
