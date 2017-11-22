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

    @When("^a request is made to retrieve all custody status records$")
    public void aRequestIsMadeToRetrieveAllCustodyStatusRecords() {
        custodyStatus.retrieveAllCustodyStatusRecords();
    }

    @When("^a request is made to retrieve a specific custody status record$")
    public void aRequestIsMadeToRetrieveASpecificCustodyStatusRecord() { custodyStatus.retrieveASpecificCustodyStatusRecord(); }

    @Then("^a list of records are returned$")
    public void aListOfRecordsAreReturned() { custodyStatus.verifyAListOfRecordsIsReturned(); }

    @Then("^a single record is returned$")
    public void aSingleRecordIsReturned() {
        custodyStatus.verifyASingleRecordIsReturned();
    }
}
