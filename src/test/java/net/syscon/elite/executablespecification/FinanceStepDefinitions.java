package net.syscon.elite.executablespecification;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executablespecification.steps.FinanceSteps;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * BDD step definitions for finance endpoints:
 * <ul>
 * <li>/bookings/{booking_id}/balances</li>
 * </ul>
 */
public class FinanceStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private FinanceSteps finance;

    @When("^an account with booking id ([0-9-]+) is requested$")
    public void anAccountIsRequested(Long id) {
        finance.getAccount(id);
    }

    @Then("^the returned account ([^\"]+) is ([\\w-\\.]+)$")
    public void theFieldIs(String field, String value) throws ReflectiveOperationException {
        finance.verifyField(field, value);
    }

    @Then("^resource not found response is received from finance API$")
    public void resourceNotFoundResponseIsReceivedFromBookingsAPI() throws Throwable {
        finance.verifyResourceNotFound();
    }
}
