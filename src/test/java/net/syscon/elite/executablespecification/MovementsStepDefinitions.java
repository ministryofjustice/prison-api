package net.syscon.elite.executablespecification;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executablespecification.steps.MovementsSteps;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

public class MovementsStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private MovementsSteps movementsSteps;

    @When("^a request is made to retrieve recent movements$")
    public void aRequestIsMadeToRetrieveAllRecords() {
        final String fromDateTime = "2017-02-20T13:56:00";
        final String movementDate = "2017-08-16";
        movementsSteps.retrieveAllMovementRecords(fromDateTime, movementDate);
    }

    @Then("^a correct list of records are returned$")
    public void aListOfRecordsAreReturned() {
        movementsSteps.verifyListOfRecords();
    }

    @When("^a request is made to retrieve the establishment roll count for an agency$")
    public void aRequestIsMadeToRetrieveRollCount() {
        movementsSteps.retrieveRollCounts("LEI");
    }

    @When("^a request is made to retrieve the establishment unassigned roll count for an agency$")
    public void aRequestIsMadeToRetrieveUnassignedRollCount() {
        movementsSteps.retrieveUnassignedRollCounts("LEI");
    }

    @Then("^a valid list of roll count records are returned$")
    public void aListOfRollCountRecordsAreReturned() {
        movementsSteps.verifyListOfRollCounts();
    }

    @Then("^a valid list of unassigned roll count records are returned$")
    public void aListOfUnassignedRollCountRecordsAreReturned() {
        movementsSteps.verifyListOfUnassignedRollCounts();
    }

    @When("^a request is made to retrieve the movement counts for an agency on \"([^\"]*)\"$")
    public void aRequestIsMadeToRetrieveMovementCounts(String date) {
        movementsSteps.retrieveMovementCounts("LEI", date);
    }

    @When("^a make a request for recent movements for \"([^\"]*)\" and \"([^\"]*)\"$")
    public void aMakeARequestForRecentMovementsForAnd(String offenderNo1, String offenderNo2) throws Throwable {
        movementsSteps.retrieveMovementsByOffenders(Arrays.asList(offenderNo1, offenderNo2));
    }

    @Then("^the records should contain a entry for \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\"$")
    public void theRecordsShouldContainAEntryFor(String movementType, String fromDescription, String toDescription, String movementReason, String movementTime) throws Throwable {
        movementsSteps.verifyMovements(movementType, fromDescription, toDescription, movementReason, movementTime);
    }

    @Then("^a total count of out today as \"([^\"]*)\" offender numbers that are out today matching \"([^\"]*)\" and a count of in today as \"([^\"]*)\"\"$")
    public void aTotalCountOfOutTodayAsOffenderNumbersThatAreOutTodayMatchingAndACountOfInTodayAs(Integer outToday, String offenderNumbers, Integer inToday) throws Throwable {
        movementsSteps.verifyMovementCounts(outToday, Arrays.asList(offenderNumbers.split(",")), inToday);
    }
}
