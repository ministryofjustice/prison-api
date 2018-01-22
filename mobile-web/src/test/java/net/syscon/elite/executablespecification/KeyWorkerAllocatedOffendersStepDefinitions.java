package net.syscon.elite.executablespecification;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executablespecification.steps.KeyWorkerAllocatedOffendersSteps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * BDD step definitions for the key worker endpoints
 */
public class KeyWorkerAllocatedOffendersStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private KeyWorkerAllocatedOffendersSteps allocatedSteps;

    @When("^an allocated offender request is made with agency id \"([^\"]*)\"$")
    public void anUnallocatedOffenderRequestIsMadeWithAgencyId(String agencyId) throws Throwable {
        doRequestWithParams(agencyId,null,null,null);
    }

    @When("^an allocated offender request is made$")
    public void anUnallocatedOffenderRequestIsMade() throws Throwable {
        doRequestWithParams(null,null,null,null);
    }

    @Then("^a list of \"([^\"]*)\" allocated offenders are returned$")
    public void aListOfUnallocatedOffendersAreReturned(int count) throws Throwable {
        allocatedSteps.verifyAListOfAllocatedOffendersIsReturned(count);
    }

    @And("^the list is sorted by offender name asc")
    public void theListIsSortedByNameAsc() throws Throwable {
        allocatedSteps.verifyListIsSortedByLastnameAsc();
    }

    @Then("^a bad request response is received with message \"([^\"]*)\"$")
    public void aBadRequestResponseIsReceivedWithMessage(String message) throws Throwable {
        allocatedSteps.verifyBadRequest(message);
        allocatedSteps.verifyErrorUserMessage(message);
    }

    @When("^an allocated offender request is made with from date \"([^\"]*)\"$")
    public void anAllocatedOffenderRequestIsMadeWithFromDate(String date) throws Throwable {
        doRequestWithParams( null, null, date, null);
    }

    @When("^an allocated offender request is made with agency \"([^\"]*)\", type \"([^\"]*)\", from date \"([^\"]*)\" and to date \"([^\"]*)\"$")
    public void anAllocatedOffenderRequestIsMadeWithFromDate(String agencyId, String allocationType, String fromDate, String toDate) throws Throwable {
        doRequestWithParams(agencyId, allocationType, fromDate, toDate);
    }

    @When("^I look at allocated response row \"([^\"]*)\"$")
    public void iLookAtRow(String index) {
        allocatedSteps.putARowFromListInContext(Integer.valueOf(index)-1);
    }

    @Then("^allocated first name matches \"([^\"]*)\"$")
    public void allocationFirstNameMatches(String name) {
        allocatedSteps.verifyAllocationFirstName(name);
    }

    @And("^allocated last name matches \"([^\"]*)\"$")
    public void allocationLastNameMatches(String name) {
        allocatedSteps.verifyAllocationLastName(name);
    }

    @And("^allocated agencyId matches \"([^\"]*)\"$")
    public void allocationAgencyMatches(String value) {
        allocatedSteps.verifyAllocationAgencyId(value);
    }

    @And("^allocated allocation type matches \"([^\"]*)\"$")
    public void allocationTypeMatches(String value) {
        allocatedSteps.verifyAllocationType(value);
    }

    @And("^allocated internal location matches \"([^\"]*)\"$")
    public void allocationInternalLocationDescMatches(String value) {
        allocatedSteps.verifyAllocationInternalLocationDesc(value);
    }

    @And("^allocated assigned date matches \"([^\"]*)\"$")
    public void allocatedAssignedDateMatches(String value) throws Throwable {
        allocatedSteps.verifyAllocationAssignedDate(value);
    }

    private void doRequestWithParams(String agencyId, String allocationType, String fromDate, String toDate) {
        Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotBlank(agencyId)) {
            params.put("agencyId", agencyId);
        }
        if (StringUtils.isNotBlank(allocationType)) {
            params.put("allocationType", allocationType);
        }
        if (StringUtils.isNotBlank(fromDate)) {
            params.put("fromDate", fromDate);
        }
        if (StringUtils.isNotBlank(toDate)) {
            params.put("toDate", toDate);
        }
        allocatedSteps.getAllocatedOffendersList(params);
    }


}
