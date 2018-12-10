package net.syscon.elite.executablespecification;

import cucumber.api.DataTable;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.api.model.OffenderOutTodayDto;
import net.syscon.elite.api.model.OffenderIn;
import net.syscon.elite.executablespecification.steps.MovementsSteps;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    public void aMakeARequestForRecentMovementsForAnd(String offenderNo1, String offenderNo2) {
        movementsSteps.retrieveMovementsByOffenders(Arrays.asList(offenderNo1, offenderNo2));
    }

    @Then("^the records should contain a entry for \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\"$")
    public void theRecordsShouldContainAEntryFor(String movementType, String fromDescription, String toDescription, String movementReason, String movementTime)  {
        movementsSteps.verifyMovements(movementType, fromDescription, toDescription, movementReason, movementTime);
    }

    @Then("^a total count of out today as \"([^\"]*)\" offender numbers that are out today matching \"([^\"]*)\" and a count of in today as \"([^\"]*)\"\"$")
    public void aTotalCountOfOutTodayAsOffenderNumbersThatAreOutTodayMatchingAndACountOfInTodayAs(Integer outToday, String offenderNumbers, Integer inToday) {
        movementsSteps.verifyMovementCounts(outToday, Arrays.asList(offenderNumbers.split(",")), inToday);
    @When("^a request has been made for out today results$")
    public void aRequestHasBeenMadeForOutTodayResults() {
        movementsSteps.retrieveOutToday();
    }

    @Then("^the following fields should be returned: \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\"$")
    public void theFollowingFieldsShouldBeReturned(DataTable table) throws Throwable {
        movementsSteps.verifyOutToday(table.asList(OffenderOutTodayDto.class));
    }

    @Then("^\"([^\"]*)\" offenders are out today and \"([^\"]*)\" are in$")
    public void offendersOutTodayAndAreIn(Integer outToday, Integer inToday) throws Throwable {
        movementsSteps.verifyMovementCounts(outToday, inToday);
    }

    @When("^a request is made for en-route offenders for agency \"([^\"]*)\" on movement date \"([^\"]*)\"$")
    public void aMakeARequestForEnRouteOffendersForAgencyOnMovementDate(String agencyId, String date) {
        movementsSteps.retrieveEnrouteOffenders(agencyId, date);
    }

    @Then("^the records should contain a entry for \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\"$")
    public void theRecordsShouldContainAEntryFor(String offenderNo, String lastName, String fromAgency, String toAgency, String reason, String time) {
       movementsSteps.verifyOffenderMovements(offenderNo, lastName, fromAgency, toAgency, reason, time);
    }
    @When("^a request is made to retrieve the 'offenders in' for agency \"([^\"]*)\" on date \"([^\"]*)\"$")
    public void aRequestIsMadeToRetrieveTheOffendersInForAgencyOnDate(String agencyId, String isoDateString) {
        movementsSteps.getOffendersIn(agencyId, LocalDate.parse(isoDateString));
    }

    private static String nullIfEmpty(String s) {
        return s.length() == 0 ?  null : s;
    }


    @Then("^information about 'offenders in' is returned as follows:$")
    public void informationAboutOffendersInIsReturnedAsFollows(DataTable table) {
        List<OffenderIn> offendersIn = table
                .cells(1)
                .stream()
                .map(List::iterator)
                .map(i -> OffenderIn
                        .builder()
                        .offenderNo(i.next())
                        .dateOfBirth(LocalDate.parse(i.next()))
                        .firstName(i.next())
                        .middleName(nullIfEmpty(i.next()))
                        .lastName(i.next())
                        .fromAgencyDescription(i.next())
                        .movementTime(LocalTime.parse(i.next()))
                        .location(i.next())
                        .build()
                ).collect(Collectors.toList());
        movementsSteps.verifyOffendersIn(offendersIn);
    }
}
