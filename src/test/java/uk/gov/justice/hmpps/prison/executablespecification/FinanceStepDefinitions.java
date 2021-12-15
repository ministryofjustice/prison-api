package uk.gov.justice.hmpps.prison.executablespecification;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.executablespecification.steps.FinanceSteps;

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
    public void anAccountIsRequested(final Long id) {
        finance.getAccount(id);
    }

    @Then("^the returned account ([^\"]+) is ([\\w-\\.]+)$")
    public void theFieldIs(final String field, final String value) throws ReflectiveOperationException {
        finance.verifyField(field, value);
    }

    @Then("^resource not found response is received from finance API$")
    public void resourceNotFoundResponseIsReceivedFromBookingsAPI() throws Throwable {
        finance.verifyResourceNotFound();
    }
}
