package net.syscon.elite.executablespecification;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.api.support.TimeSlot;
import net.syscon.elite.executablespecification.steps.SchedulesSteps;
import org.springframework.beans.factory.annotation.Autowired;

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

    @Given("^location group does not define any locations$")
    public void givenLocationGroupDoesNotDefineAnyLocations() {
        schedulesSteps.givenLocationGroupDoesNotDefineAnyLocations();
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
        schedulesSteps.getSchedulesForLocationGroup();
    }

    @When("^schedules are requested for a valid agency and location group$")
    public void schedulesAreRequestedForValidAgencyAndLocationGroup() {
        schedulesSteps.getSchedulesForLocationGroup("LEI", "BlockA");
    }

    @When("^schedules are requested for a valid agency and location group with 'timeSlot' = '([APM]+)'$")
    public void schedulesAreRequestedForValidAgencyAndLocationGroupwithTimeSlot(TimeSlot timeSlot) {
        schedulesSteps.getSchedulesForLocationGroup("LEI", "BlockA", timeSlot);
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

    @Then("^schedules response is HTTP 500 server error$")
    public void verify500Error() throws Throwable {
        schedulesSteps.verify500Error();
    }

    @Then("^schedules response error message is \"([^\"]*)\"$")
    public void verifyErrorMessage(String message) throws Throwable {
        schedulesSteps.verifyErrorUserMessage(message);
    }

    @Then("^schedules response is an empty list$")
    public void verifyResponseIsEmpty() throws Throwable {
        schedulesSteps.verifyNoResourceRecordsReturned();
    }

    // ----------------------------------------------------------------------

    @Given("^an existing agency and location$")
    public void givenAnExistingAgencyAndLocation() {
        schedulesSteps.givenAnExistingAgencyAndLocation();
    }

    @Given("^location does not exist for the agency$")
    public void givenLocationDoesNotExistForTheAgency() {
        schedulesSteps.givenLocationDoesNotExistForTheAgency();
    }

    @Given("^usage value is invalid$")
    public void givenUsageInvalid() {
        schedulesSteps.givenUsageInvalid();
    }

    @Given("^the location within the agency has no scheduled events for current day$")
    public void givenLocationNoScheduledEventsForCurrentDay() {
        schedulesSteps.givenNoScheduledEventsForCurrentDay();
    }

    @Given("^one or more offenders are due to attend a scheduled event on the current day at a location within an agency$")
    public void givenScheduledEventsForCurrentDayAtLocation() {
        schedulesSteps.givenScheduledEventsForCurrentDayAtLocation();
    }

    @When("^schedules are requested for agency and location$")
    public void schedulesAreRequestedForLocation() {
        schedulesSteps.getSchedulesForLocation();
    }

    @When("^schedules are requested for a valid agency with location \"([^\"]*)\" and usage \"([^\"]*)\" and timeSlot \"([^\"]*)\"$")
    public void schedulesAreRequestedForValidAgencyAndLocationwithTimeSlot(Long locationId, String usage, TimeSlot timeSlot) {
        schedulesSteps.getSchedulesForLocation("LEI", locationId, usage, timeSlot);
    }

    @Then("^response is a list of offender's schedules for the current day with last name list \"([^\"]*)\"$")
    public void verifyListOfOffendersSchedulesForCurrentDayLastNames(String list) throws Throwable {
        schedulesSteps.verifyListOfOffendersSchedulesForCurrentDayLastNames(list);
    }

    @Then("^bad request response, with \"([^\"]*)\" message, is received from schedules API$")
    public void verifyBadRequest(String message) throws Throwable {
        schedulesSteps.verifyBadRequest(message);
    }

    @Then("^the schedule event type list is \"([^\"]*)\"$")
    public void verifyListOfScheduleEventTypes(String list) throws Throwable {
        schedulesSteps.verifyListOfScheduleEventTypes(list);
    }

    @Then("^the schedule start time list is \"([^\"]*)\"$")
    public void verifyListOfScheduleStartTimes(String list) throws Throwable {
        schedulesSteps.verifyListOfScheduleStartTimes(list);
    }

    @Then("^returned schedules are ordered in ascending alphabetical order by offender last name$")
    public void verifySchedulesAreOrderedAlphabetically() throws Throwable {
        schedulesSteps.verifySchedulesAreOrderedAlphabetically();
    }

    @Then("^returned schedules are only for offenders due to attend a scheduled event on current day for requested agency and location$")
    public void schedulesAreOnlyForOffendersOnCurrentDayAgencyLocation() throws Throwable {
        schedulesSteps.schedulesAreOnlyForOffendersOnCurrentDay();
    }

    @Given("^an agency which does not exists has been set")
    public void anAgencyWhichDoesNotExists() throws Throwable {
       schedulesSteps.givenNoneExistentAgency();
    }
}
