package uk.gov.justice.hmpps.prison.executablespecification;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.executablespecification.steps.KeyWorkerSteps;

import java.util.Arrays;

public class KeyworkerStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private KeyWorkerSteps keyworker;

    @When("^an available key worker request is made with agency id \"([^\"]*)\"$")
    public void anAvailableKeyWorkerRequestIsMadeWithAgencyId(final String agencyId) {
        keyworker.getAvailableKeyworkersList(agencyId);
    }

    @Then("^a list of \"([^\"]*)\" key workers are returned$")
    public void aListOfKeyWorkersAreReturned(final int count) {
        keyworker.verifyAListOfKeyworkersIsReturned(count);
    }

    @Then("^the key worker service returns a resource not found response with message \"([^\"]*)\"$")
    public void aResourceNotFoundResponseIsReceivedWithMessage(final String message) {
        keyworker.verifyResourceNotFound();
        keyworker.verifyErrorUserMessage(message);
    }

    @And("^the key worker has (\\d+) allocations$")
    public void theKeyWorkerHasAllocations(final int expectedAllocationCount) {
        keyworker.verifyKeyWorkerAllocationCount(expectedAllocationCount);
    }

    @And("^the key worker has (\\d+) allocation history entries$")
    public void theKeyWorkerHasAllocationHistoryEntries(final int expectedAllocationCount) {
        keyworker.verifyKeyWorkerAllocationHistoryCount(expectedAllocationCount);
    }

    @Then("^the correct key worker allocations are returned$")
    public void correctKeyWorkerAllocationsReceived() {
        keyworker.verifyKeyWorkerAllocations();
    }

    @When("^a key worker allocations request is made with staff ids \"([^\"]*)\" and agency \"([^\"]*)\"$")
    public void aKeyWorkerAllocationsRequestIsMadeWithStaffIdsAndAgency(final String staffIds, final String agencyId) {
        keyworker.getKeyworkerAllocationsByStaffIds(Arrays.stream(StringUtils.split(staffIds, ",")).map(Long::valueOf).toList(), agencyId);
    }

    @When("^a key worker allocations request is made with nomis ids \"([^\"]*)\" and agency \"([^\"]*)\"$")
    public void aKeyWorkerAllocationsRequestIsMadeWithNomisIdsAndAgency(final String offenderNos, final String agencyId) {
        keyworker.getKeyworkerAllocationsByOffenderNos(Arrays.asList(StringUtils.split(offenderNos, ",")), agencyId);
    }

    @When("^a key worker allocation history request is made with nomis ids \"([^\"]*)\"$")
    public void aKeyWorkerAllocationHistoryRequestIsMadeWithNomisIdsAndAgency(final String offenderNos) {
        keyworker.getKeyworkerAllocationHistoryByOffenderNos(Arrays.asList(StringUtils.split(offenderNos, ",")));
    }
}
