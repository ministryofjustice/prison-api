package uk.gov.justice.hmpps.prison.executablespecification;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.api.model.Movement;
import uk.gov.justice.hmpps.prison.api.model.OffenderIn;
import uk.gov.justice.hmpps.prison.api.model.OffenderInReception;
import uk.gov.justice.hmpps.prison.api.model.OffenderOutTodayDto;
import uk.gov.justice.hmpps.prison.executablespecification.steps.MovementsSteps;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MovementsStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private MovementsSteps movementsSteps;

    @When("^a request is made to retrieve recent movements$")
    public void aRequestIsMadeToRetrieveAllRecords() {
        final var fromDateTime = "2017-02-20T13:56:00";
        final var movementDate = "2017-08-16";
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

    @When("^a request is made to retrieve the movement counts for an \"([^\"]*)\" on \"([^\"]*)\"$")
    public void aRequestIsMadeToRetrieveTheMovementCountsForAnOn(final String agency, final String date) throws Throwable {
        movementsSteps.retrieveMovementCounts(agency, date.equals("today") ? LocalDate.now().toString() : date);
    }

    @When("^a make a request for recent movements for \"([^\"]*)\" and \"([^\"]*)\"$")
    public void aMakeARequestForRecentMovementsForAnd(final String offenderNo1, final String offenderNo2) {
        movementsSteps.retrieveMovementsByOffenders(Arrays.asList(offenderNo1, offenderNo2), true);
    }

    @Then("^the records should contain a entry for \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\"$")
    public void theRecordsShouldContainAEntryFor(final String movementType, final String fromDescription, final String toDescription, final String movementReason, final String movementTime) {
        movementsSteps.verifyMovements(movementType, fromDescription, toDescription, movementReason, movementTime);
    }

    @Then("^a total count of out today as \"([^\"]*)\" offender numbers that are out today matching \"([^\"]*)\" and a count of in today as \"([^\"]*)\"\"$")
    public void aTotalCountOfOutTodayAsOffenderNumbersThatAreOutTodayMatchingAndACountOfInTodayAs(final Integer outToday, final String offenderNumbers, final Integer inToday) {
        movementsSteps.verifyMovementCounts(outToday, inToday);
    }

    @Then("^the following rows should be returned:$")
    public void theFollowingFieldsShouldBeReturned(final DataTable table) throws Throwable {
        movementsSteps.verifyOutToday(table.asList(OffenderOutTodayDto.class));
    }

    @Then("^\"([^\"]*)\" offenders are out today and \"([^\"]*)\" are in$")
    public void offendersOutTodayAndAreIn(final Integer outToday, final Integer inToday) throws Throwable {
        movementsSteps.verifyMovementCounts(outToday, inToday);
    }

    @When("^a request is made for en-route offenders for agency \"([^\"]*)\" on movement date \"([^\"]*)\"$")
    public void aMakeARequestForEnRouteOffendersForAgencyOnMovementDate(final String agencyId, final String date) {
        movementsSteps.retrieveEnrouteOffenders(agencyId, date);
    }

    @Then("^the records should contain a entry for \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\"$")
    public void theRecordsShouldContainAEntryFor(final String offenderNo, final String lastName, final String fromAgency, final String toAgency, final String reason, final String time) {
        movementsSteps.verifyOffenderMovements(offenderNo, lastName, fromAgency, toAgency, reason, time);
    }

    @When("^a request is made to retrieve the 'offenders in' for agency \"([^\"]*)\" on date \"([^\"]*)\"$")
    public void aRequestIsMadeToRetrieveTheOffendersInForAgencyOnDate(final String agencyId, final String isoDateString) {
        movementsSteps.getOffendersIn(agencyId, LocalDate.parse(isoDateString));
    }

    @Then("^information about 'offenders in' is returned as follows:$")
    public void informationAboutOffendersInIsReturnedAsFollows(final DataTable table) {
        final List<OffenderIn> offendersIn = table.asList(OffenderIn.class);
        movementsSteps.verifyOffendersIn(offendersIn.stream()
            .map(offender -> offender.toBuilder()
                .middleName(StringUtils.defaultString(offender.getMiddleName()))
                .location(StringUtils.defaultString(offender.getLocation()))
                .build()).collect(Collectors.toList())
        );
    }

    @When("^a request is made to retrieve the 'offenders out' for agency \"([^\"]*)\" for \"([^\"]*)\"$")
    public void aRequestIsMadeToRetrieveTheOffendersOutForAgencyFor(final String agencyId, final String isoDateString) throws Throwable {
        movementsSteps.getOffendersOut(agencyId, LocalDate.parse(isoDateString));
    }

    @When("^a request is made to retrieve 'offenders in reception' for agency \"([^\"]*)\"$")
    public void aRequestIsMadeToRetrieveOffendersInReceptionForAgency(final String agencyId) throws Throwable {
        movementsSteps.getOffendersInReception(agencyId);
    }

    @Then("^information about 'offenders in reception' is returned as follows:$")
    public void informationAboutOffendersInReceptionIsReturnedAsFollows(final DataTable table) {
        final List<OffenderInReception> offendersInReception = table.asList(OffenderInReception.class);
        movementsSteps.verifyOffendersInReception(offendersInReception);
    }

    @Then("^information about 'recent movements' is returned as follows:$")
    public void informationAboutRecentMovementsIsReturnedAsFollows(final DataTable table) {
        final List<Movement> recentMovements = table.asList(Movement.class);
        movementsSteps.verifyMovements(recentMovements);
    }

    @Then("^the records should contain a entry for \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\" \"([^\"]*)\"$")
    public void theRecordsShouldContainAEntryFor(final String offenderNo, final String movementType, final String fromDescription, final String toDescription, final String reasonDescription, final String movementTime, final String fromCity, final String toCity) throws Throwable {
        movementsSteps.verifyOffenderMovements(offenderNo, movementType, fromDescription, toDescription, reasonDescription, movementTime, fromCity, toCity);
    }

    @When("^a make a request for recent movements for \"([^\"]*)\" and \"([^\"]*)\" for all movement types$")
    public void aMakeARequestForRecentMovementsForAndForAllMovementTypes(final String offenderNo1, final String offenderNo2) throws Throwable {
        movementsSteps.retrieveMovementsByOffenders(Arrays.asList(offenderNo1, offenderNo2), false);
    }

    @When("^a request is made to retrieve events involving agencies \"([^\"]*)\" and \"([^\"]*)\" between \"([^\"]*)\" and \"([^\"]*)\"$")
    public void aRequestIsMadeToRetrieveMovementsForAgencies(final String agency1, final String agency2, final String fromTime, final String toTime) throws Throwable {
        final var agencies = List.of(agency1, agency2);
        movementsSteps.getMovementsForAgencies(agencies, fromTime, toTime);
    }

    @Then("^the response should contain \"([^\"]*)\" movements$")
    public void movementCountCheck(final String movementCount) {
        movementsSteps.verifyMovementCount(Integer.parseInt(movementCount));
    }

    @And("^the response should contain \"([^\"]*)\" court events$")
    public void courtCountCheck(final String courtCount) {
        movementsSteps.verifyCourtCount(Integer.parseInt(courtCount));
    }

    @And("^the response should contain \"([^\"]*)\" release events$")
    public void releaseCountCheck(final String releaseCount) {
        movementsSteps.verifyReleaseCount(Integer.parseInt(releaseCount));
    }

    @And("^the response should contain \"([^\"]*)\" transfer events$")
    public void transferCountCheck(final String transferCount) {
        movementsSteps.verifyTransferCount(Integer.parseInt(transferCount));
    }

    @And("^the response code should be \"([^\"]*)\"$")
    public void responseCodeCheck(final String responseCode) {
        movementsSteps.verifyErrorResponseCode(Integer.parseInt(responseCode));
    }

    @And("^the presence of an error response is \"([^\"]*)\"$")
    public void errorResponseCheck(final String errorResponsePresent) {
        movementsSteps.verifyErrorResponse(Boolean.parseBoolean(errorResponsePresent));
    }
}

