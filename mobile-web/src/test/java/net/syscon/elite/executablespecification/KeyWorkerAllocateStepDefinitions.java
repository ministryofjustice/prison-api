package net.syscon.elite.executablespecification;

import cucumber.api.java.After;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executablespecification.steps.KeyWorkerAllocateSteps;
import net.syscon.elite.executablespecification.steps.KeyWorkerAllocationSteps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * BDD step definitions for the key worker endpoints
 */
public class KeyWorkerAllocateStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private KeyWorkerAllocateSteps keyworkerAllocateSteps;

    @Autowired
    private KeyWorkerAllocationSteps keyworkerAllocationSteps;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${oracle.default.schema}")
    private String oracleDefaultSchema;

    @When("^offender booking \"([^\"]*)\" is allocated to staff user id \"([^\"]*)\" with reason \"([^\"]*)\" and type \"([^\"]*)\"$")
    public void offenderIsAllocated(Long bookingId, Long staffId, String reason, String type) throws Throwable {
        keyworkerAllocateSteps.offenderIsAllocated(bookingId,  staffId,  reason,  type);
    }

    @Then("^the allocation is successfully created$")
    public void allocationIsSuccessfullyCreated() throws Throwable {
        keyworkerAllocateSteps.allocationIsSuccessfullyCreated();
    }

    @When("^the allocation returns a 404 resource not found with message '(.*)'$")
    public void resourceNotFound(String expectedMessage) throws Throwable {
        keyworkerAllocateSteps.verifyResourceNotFound();
        keyworkerAllocateSteps.verifyErrorUserMessage(expectedMessage);
    }

    @And("^the allocation returns a 401 bad request with message '(.*)'$")
    public void badRequest(String expectedMessage) throws Throwable {
        keyworkerAllocateSteps.verifyBadRequest(expectedMessage);
    }

    /**
     * Remove any allocations added by these tests. Note 
     * <li>autocommit = true here
     * <li>this runs just for a test which has the matching tag.
     * <li>the oracle.default.schema property does not apply to this jdbcTemplate, so do it manually.
     * <li>this runs as API_PROXY_OWNER due to being client-side with no access to the security thread-local
     */
    @After("@allocate-database-cleanup")
    public void afterScenario() {
        if (StringUtils.isBlank(oracleDefaultSchema)) {
            jdbcTemplate
                    .update("delete from OFFENDER_KEY_WORKERS where OFFENDER_BOOK_ID in (-33,-34) and OFFICER_ID = -5");
        } else {
            jdbcTemplate.update("delete from " + oracleDefaultSchema
                    + ".OFFENDER_KEY_WORKERS where OFFENDER_BOOK_ID in (-33,-34) and OFFICER_ID = -5");
        }
    }

    @When("^auto-allocation initiated for agency \"([^\"]*)\"$")
    public void autoAllocationInitiatedForAgency(String agencyId) throws Throwable {
        keyworkerAllocateSteps.initAutoAllocation(agencyId);
    }

    @Then("^auto-allocation requests responds with \"([^\"]*)\" allocations processed$")
    public void autoAllocationRequestsRespondsWithAllocationsProcessed(String expectedAutoAllocCount) throws Throwable {
        keyworkerAllocateSteps.verifyAutoAllocationsProcessed(expectedAutoAllocCount);
    }

    @Then("^auto-allocation request fails with a resource conflict error with message \"([^\"]*)\"$")
    public void autoAllocationRequestFailsWithAResourceConflictErrorWithMessage(String expectedUserMessage) throws Throwable {
        keyworkerAllocateSteps.verifyResourceConflict(expectedUserMessage);
    }

    @And("^there are (\\d+) unallocated offenders for agency \"([^\"]*)\"$")
    public void thereAreUnallocatedOffendersForAgency(int expectedCount, String agencyId) throws Throwable {
        keyworkerAllocationSteps.getUnallocatedOffendersList(agencyId);
        keyworkerAllocationSteps.verifyAListOfUnallocatedOffendersIsReturned(expectedCount);
    }
}
