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
 * <li>/schedules/{agencyId}/locations/{locationId}/usage/{usage}</li>
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

    @When("^schedules are requested for a valid agency and location group with 'timeSlot' = '([APMED]+)'$")
    public void schedulesAreRequestedForValidAgencyAndLocationGroupwithTimeSlot(TimeSlot timeSlot) {
        schedulesSteps.getSchedulesForLocationGroup("LEI", "BlockA", null, timeSlot);
    }

    @When("^schedules are requested for a valid agency and location group with date = '([0-9-]+)' and 'timeSlot' = '([APMED]+)'$")
    public void schedulesAreRequestedForValidAgencyAndLocationGroupwithTimeSlot(String date, TimeSlot timeSlot) {
        schedulesSteps.getSchedulesForLocationGroup("LEI", "BlockA", date, timeSlot);
    }

    @Then("^response is a list of offender's schedules with size ([0-9]+)$")
    public void listOfOffendersSchedulesForCurrentDay(int size) throws Throwable {
        schedulesSteps.verifyListOfOffendersSchedules(size);
    }

    @Then("^returned schedules are ordered as defined by requested location group$")
    public void schedulesAreOrdered() throws Throwable {
        schedulesSteps.verifySchedulesAreOrdered();
    }

    @Then("^returned schedules are only for offenders located in locations \"([^\"]*)\"$")
    public void schedulesAreOnlyForOffendersLocated(String locations) throws Throwable {
        schedulesSteps.verifyOffendersAreLocatedInALocationThatBelongsToRequestedAgencyAndLocationGroup(locations);
    }

    @Then("^start time of all returned schedules is before 12h00$")
    public void schedulesAreOnlyBefore12h00() throws Throwable {
        schedulesSteps.schedulesAreOnlyBefore12h00();
    }

    @Then("^start time of all returned schedules is between 12h00 and 17h00$")
    public void schedulesAreOnlyBetween12And17() throws Throwable {
        schedulesSteps.schedulesAreOnlyBetween12And17();
    }

    @Then("^start time of all returned schedules is on or after 17h00$")
    public void schedulesAreOnlyOnOrAfter17h00() throws Throwable {
        schedulesSteps.schedulesAreOnlyOnOrAfter17h00();
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
       schedulesSteps.givenNonExistentAgency();
    }

    @When("^activities are requested with a valid agency for date \"([^\"]*)\" with a time slot \"([^\"]*)\" and offender numbers \"([^\"]*)\"$")
    public void activitiesAreRequestedWithAValidAgencyWithATimeSlotAndOffenderNumbers(String date, String timeSlot, String offenderNo) throws Throwable {
        schedulesSteps.getActivities(offenderNo, date, timeSlot);
    }

    @Then("^the following events should be returned \"([^\"]*)\"$")
    public void theFollowingEventsShouldBeReturned(String expected) throws Throwable {
        this.schedulesSteps.verifyEventComments(expected);
    }

    @Given("^an offender with scheduled visits$")
    public void anOffenderWithScheduledVisits() throws Throwable {
        //test data setup
    }

    @When("^visits are requested with a valid agency with a time slot \"([^\"]*)\" and offender numbers \"([^\"]*)\"$")
    public void visitsAreRequestedWithAValidAgencyWithATimeSlotAndOffenderNumbers(String timeSlot, String offenderNo) throws Throwable {
        schedulesSteps.getVisits(offenderNo, timeSlot);
    }

    @Then("^the following visits should be returned \"([^\"]*)\"$")
    public void theFollowingVisitsShouldBeReturned(String expected) throws Throwable {
       this.schedulesSteps.verifyEventComments(expected);
    }

    @Given("^an offender with scheduled appointments$")
    public void anOffenderWithScheduledAppointments() throws Throwable {
        //test data setup
    }

    @When("^appointments are requested with a valid agency with a time slot \"([^\"]*)\" and offender numbers \"([^\"]*)\"$")
    public void appointmentsAreRequestedWithAValidAgencyWithATimeSlotAndOffenderNumbers(String timeSlot, String offenderNo) throws Throwable {
        schedulesSteps.getAppointments(offenderNo, timeSlot);
    }

    @Then("^the following appointments should be returned \"([^\"]*)\"$")
    public void theFollowingAppointmentsShouldBeReturned(String appointments) throws Throwable {
        this.schedulesSteps.verifyEventDescriptions(appointments);
    }

    @When("^Court events are requested with a valid agency with a time slot \"([^\"]*)\", date \"([^\"]*)\" and offender number list \"([^\"]*)\"$")
    public void courtEventsAreRequestedWithAValidAgencyWithATimeSlotAndOffenderNumbers(String timeSlot, String date, String offenderNos) throws Throwable {
        schedulesSteps.getCourtEvents(offenderNos, date, timeSlot);
    }

    @Then("^the following events should be returned: \"([^\"]*)\"$")
    public void theFollowingCourtEventsShouldBeReturned(String events) throws Throwable {
        this.schedulesSteps.verifyCourtEvents(events);
    }

    @Given("^an offender that is scheduled to be transferred outside of the prison$")
    public void anOffenderThatIsScheduledToBeTransferredOutsideOfThePrison() throws Throwable {
    }

    @When("^Request an offenders external transfers for a given date$")
    public void transfersAreRequestedWithAValidAgencyAndDate() throws Throwable {
    }

    @When("^a request is made for transfers with the following parameters \"([^\"]*)\" and \"([^\"]*)\"$")
    public void aRequestIsMadeForTransfersWithTheFollowingParametersAnd(String offenderNumber, String date) throws Throwable {
        this.schedulesSteps.getExternalTransfers(offenderNumber, date);
    }

    @Then("^the following offender should be returned \"([^\"]*)\", \"([^\"]*)\" along with the \"([^\"]*)\"$")
    public void theFollowingOffenderShouldBeReturnedAlongWithThe(String firstName, String lastName, String transferDescription) throws Throwable {
        this.schedulesSteps.verifyTransfer(firstName, lastName, transferDescription);
    }
}
