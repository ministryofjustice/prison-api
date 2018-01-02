package net.syscon.elite.executablespecification;

import net.syscon.elite.api.support.TimeSlot;
import net.syscon.elite.executablespecification.steps.SchedulesSteps;

import org.springframework.beans.factory.annotation.Autowired;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * BDD step definitions for the following API endpoints:
 * <ul>
 * <li>/schedules/{agencyId}/groups/{name}</li>
 * </ul>
 */
public class SchedulesStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private SchedulesSteps schedulesSteps;

    @Given("^one or more offenders have scheduled events for current day$")
    public void givenScheduledEventsForCurrentDay() {
        schedulesSteps.givenScheduledEventsForCurrentDay();
    }

    @Given("^no offender has any scheduled events for current day$")
    public void givenNoScheduledEventsForCurrentDay() {
        schedulesSteps.givenNoScheduledEventsForCurrentDay();
    }

    @Given("^an existing agency and location group$")
    public void givenAnExistingAgencyAndLocationGroup() {
        schedulesSteps.givenAnExistingAgencyAndLocationGroup();
    }

    @Given("^agency does not belong to a caseload accessible to current user$")
    public void givenAgencyDoesNotBelongToCaseload() {
        schedulesSteps.givenAgencyDoesNotBelongToCaseload();
    }

    @Given("^an agency which belongs to a caseload accessible to current user$")
    public void givenAgencyBelongsToCaseload() {
        schedulesSteps.givenAgencyBelongsToCaseload();
    }

    @Given("^location group does not exist for the agency$")
    public void givenLocationGroupDoesNotExistForTheAgency() {
        schedulesSteps.givenLocationGroupDoesNotExistForTheAgency();
    }

    @Then("^offenders are located in a location that belongs to requested agency and location group$")
    public void offendersAreLocatedInALocationThatBelongsToRequestedAgencyAndLocationGroup() throws Throwable {
        schedulesSteps.givenSchedulesAreOnlyForOffendersLocated();
    }

    @When("^schedules are requested for agency and location group$")
    public void schedulesAreRequested() {
        schedulesSteps.getSchedules();
    }

    @When("^schedules are requested for a valid agency and location group$")
    public void schedulesAreRequestedForValidAgencyAndLocationGroup() {
        schedulesSteps.getSchedules("LEI", "BlockA");
    }

    @When("^schedules are requested for a valid agency and location group with 'timeSlot' = '([APM]+)'$")
    public void schedulesAreRequestedForValidAgencyAndLocationGroupwithTimeSlot(TimeSlot timeSlot) {
        schedulesSteps.getSchedules("LEI", "BlockA", timeSlot);
    }

    @Then("^response is a list of offender's schedules for the current day with size ([0-9]+)$")
    public void listOfOffendersSchedulesForCurrentDay(int size) throws Throwable {
        schedulesSteps.verifyListOfOffendersSchedulesForCurrentDay(size);
    }

    @Then("^returned schedules are ordered as defined by requested location group$")
    public void schedulesAreOrdered() throws Throwable {
        schedulesSteps.verifySchedulesAreOrdered();
    }

    @Then("^returned schedules are only for offenders located in locations that belong to requested agency and location group$")
    public void schedulesAreOnlyForOffendersLocated() throws Throwable {
        schedulesSteps.verifyOffendersAreLocatedInALocationThatBelongsToRequestedAgencyAndLocationGroup();
    }

    @Then("^start time of all returned schedules is before 12h00$")
    public void schedulesAreOnlyBefore12h00() throws Throwable {
        schedulesSteps.schedulesAreOnlyBefore12h00();
    }

    @Then("^start time of all returned schedules is on or after 12h00$")
    public void schedulesAreOnlyOnOrAfter12h00() throws Throwable {
        schedulesSteps.schedulesAreOnlyOnOrAfter12h00();
    }

    @Then("^schedules response is HTTP 404 resource not found$")
    public void verifyResourceNotFound() throws Throwable {
        schedulesSteps.verifyResourceNotFound();
    }

    @Then("^schedules response error message is \"([^\"]*)\"$")
    public void verifyErrorMessage(String message) throws Throwable {
        schedulesSteps.verifyResourceNotFoundUserMessage(message);
    }

    @Then("^schedules response is an empty list$")
    public void verifyResponseIsEmpty() throws Throwable {
        schedulesSteps.verifyNoResourceRecordsReturned();
    }
}
