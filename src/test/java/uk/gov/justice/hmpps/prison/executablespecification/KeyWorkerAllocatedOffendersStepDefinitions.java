package uk.gov.justice.hmpps.prison.executablespecification;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.executablespecification.steps.KeyWorkerAllocatedOffendersSteps;

import java.util.HashMap;
import java.util.Map;

/**
 * BDD step definitions for the key worker endpoints
 */
public class KeyWorkerAllocatedOffendersStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private KeyWorkerAllocatedOffendersSteps allocatedSteps;

    @When("^an allocated offender request is made with agency id \"([^\"]*)\"$")
    public void anUnallocatedOffenderRequestIsMadeWithAgencyId(final String agencyId) throws Throwable {
        doRequestWithParams(agencyId, null, null, null);
    }

    @Then("^a list of \"([^\"]*)\" allocated offenders are returned$")
    public void aListOfUnallocatedOffendersAreReturned(final int count) throws Throwable {
        allocatedSteps.verifyAllocations(count);
    }

    @And("^the list is sorted by offender name asc")
    public void theListIsSortedByNameAsc() throws Throwable {
        allocatedSteps.verifyListIsSortedByLastnameAsc();
    }

    @Then("^a bad request response is received with message \"([^\"]*)\"$")
    public void aBadRequestResponseIsReceivedWithMessage(final String message) throws Throwable {
        allocatedSteps.verifyBadRequest(message);
        allocatedSteps.verifyErrorUserMessage(message);
    }

    @When("^an allocated offender request is made with from date \"([^\"]*)\"$")
    public void anAllocatedOffenderRequestIsMadeWithFromDate(final String date) throws Throwable {
        doRequestWithParams(null, null, date, null);
    }

    @When("^an allocated offender request is made with agency \"([^\"]*)\", type \"([^\"]*)\", from date \"([^\"]*)\" and to date \"([^\"]*)\"$")
    public void anAllocatedOffenderRequestIsMadeWithFromDate(final String agencyId, final String allocationType, final String fromDate, final String toDate) throws Throwable {
        doRequestWithParams(agencyId, allocationType, fromDate, toDate);
    }

    @When("^I look at allocated response row \"([^\"]*)\"$")
    public void iLookAtRow(final String index) {
        allocatedSteps.putARowFromListInContext(Integer.valueOf(index) - 1);
    }

    @Then("^allocated first name matches \"([^\"]*)\"$")
    public void allocationFirstNameMatches(final String name) {
        allocatedSteps.verifyAllocationFirstName(name);
    }

    @And("^allocated last name matches \"([^\"]*)\"$")
    public void allocationLastNameMatches(final String name) {
        allocatedSteps.verifyAllocationLastName(name);
    }

    @And("^allocated agencyId matches \"([^\"]*)\"$")
    public void allocationAgencyMatches(final String value) {
        allocatedSteps.verifyAllocationAgencyId(value);
    }


    @And("^allocated internal location matches \"([^\"]*)\"$")
    public void allocationInternalLocationDescMatches(final String value) {
        allocatedSteps.verifyAllocationInternalLocationDesc(value);
    }

    @And("^allocated assigned date matches \"([^\"]*)\"$")
    public void allocatedAssignedDateMatches(final String value) throws Throwable {
        allocatedSteps.verifyAllocationAssignedDate(value);
    }

    private void doRequestWithParams(final String agencyId, final String allocationType, final String fromDate, final String toDate) {
        final Map<String, String> params = new HashMap<>();

        if (StringUtils.isNotBlank(allocationType)) {
            params.put("allocationType", allocationType);
        }
        if (StringUtils.isNotBlank(fromDate)) {
            params.put("fromDate", fromDate);
        }
        if (StringUtils.isNotBlank(toDate)) {
            params.put("toDate", toDate);
        }
        allocatedSteps.getAllocations(agencyId, params);
    }
}
