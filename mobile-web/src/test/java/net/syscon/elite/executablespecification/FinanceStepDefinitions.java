package net.syscon.elite.executablespecification;

import net.syscon.elite.executablespecification.steps.FinanceSteps;

import org.springframework.beans.factory.annotation.Autowired;

import cucumber.api.java.en.*;

/**
 * BDD step definitions for finance endpoints:
 * <ul>
 * <li>/bookings/{booking_id}/balances</li>
 * </ul>
 */
public class FinanceStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private FinanceSteps financeSteps;

    @When("^an account with booking id ([0-9-]+) is requested$")
    public void anAccountIsRequested(Long id) {
        financeSteps.getAccount(id);
    }

    @Then("^the returned account ([^\"]+) is ([\\w-\\.]+)$")
    public void theFieldIs(String field, String value) throws ReflectiveOperationException {
        financeSteps.verifyField(field, value);
    }

    @When("^an account with nonexistent booking id is requested$")
    public void anNonexistentAccountIsRequested() {
        financeSteps.getNonexistentAccount();
    }

    @Then("^resource not found response is received from finance API$")
    public void resourceNotFoundResponseIsReceivedFromBookingsAPI() throws Throwable {
        financeSteps.verifyResourceNotFound();
    }

    @When("^an account with booking id in different caseload is requested$")
    public void anAccountInDifferentCaseloadIsRequested() {
        financeSteps.getAccountInDifferentCaseload();
    }
}
