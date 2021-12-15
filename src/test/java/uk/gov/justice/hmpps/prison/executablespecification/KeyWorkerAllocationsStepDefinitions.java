package uk.gov.justice.hmpps.prison.executablespecification;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.executablespecification.steps.KeyWorkerAllocationSteps;

/**
 * BDD step definitions for the key worker endpoints
 */
public class KeyWorkerAllocationsStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private KeyWorkerAllocationSteps keyworkerSteps;

    @When("^an unallocated offender request is made with agency id \"([^\"]*)\"$")
    public void anUnallocatedOffenderRequestIsMadeWithAgencyId(final String agencyId) throws Throwable {
        keyworkerSteps.getUnallocatedOffendersList(agencyId);
    }

    @Then("^a list of \"([^\"]*)\" unallocated offenders are returned$")
    public void aListOfUnallocatedOffendersAreReturned(final int count) throws Throwable {
        keyworkerSteps.verifyAListOfUnallocatedOffendersIsReturned(count);
    }

    @And("^the list is sorted by lastName asc$")
    public void theListIsSorted() throws Throwable {
        keyworkerSteps.verifyListIsSortedByLastNameAsc();
    }

    @Then("^a resource not found response is received with message \"([^\"]*)\"$")
    public void aResourceNotFoundResponseIsReceivedWithMessage(final String message) throws Throwable {
        keyworkerSteps.verifyResourceNotFound();
        keyworkerSteps.verifyErrorUserMessage(message);
    }
}
