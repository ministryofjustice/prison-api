package net.syscon.elite.executablespecification;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executablespecification.steps.CustodyStatusSteps;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * BDD step definitions for custody status endpoints:
 * <ul>
 * <li>/custody-statuses/{offenderNo}</li>
 * </ul>
 */
public class CustodyStatusStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private CustodyStatusSteps custodyStatus;

    @When("^a request is made to retrieve recent movements")
    public void aRequestIsMadeToRetrieveAllRecords() {
        final String fromDateTime = "2017-02-20T13:56:00";
        final String movementDate = "2017-08-16";
        custodyStatus.retrieveAllCustodyStatusRecords(fromDateTime, movementDate);
    }

    @Then("^a correct list of records are returned$")
    public void aListOfRecordsAreReturned() {
        custodyStatus.verifyListOfRecords();
    }
}
