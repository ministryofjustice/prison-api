package uk.gov.justice.hmpps.prison.executablespecification;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.api.support.TimeSlot;
import uk.gov.justice.hmpps.prison.executablespecification.steps.SchedulesSteps;

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

    @Then("^response is a list of offender's schedules with size ([0-9]+)$")
    public void listOfOffendersSchedulesForCurrentDay(final int size) throws Throwable {
        schedulesSteps.verifyListOfOffendersSchedules(size);
    }

    @Then("^returned schedules are ordered as defined by requested location group$")
    public void schedulesAreOrdered() throws Throwable {
        schedulesSteps.verifySchedulesAreOrdered();
    }

    @Then("^returned schedules are only for offenders located in locations \"([^\"]*)\"$")
    public void schedulesAreOnlyForOffendersLocated(final String locations) throws Throwable {
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
    public void verifyErrorMessage(final String message) throws Throwable {
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
    public void schedulesAreRequestedForValidAgencyAndLocationwithTimeSlot(final Long locationId, final String usage, final TimeSlot timeSlot) {
        schedulesSteps.getSchedulesForLocation("LEI", locationId, usage, timeSlot);
    }

    @Then("^response is a list of offender's schedules for the current day with last name list \"([^\"]*)\"$")
    public void verifyListOfOffendersSchedulesForCurrentDayLastNames(final String list) throws Throwable {
        schedulesSteps.verifyListOfOffendersSchedulesForCurrentDayLastNames(list);
    }

    @Then("^bad request response, with \"([^\"]*)\" message, is received from schedules API$")
    public void verifyBadRequest(final String message) throws Throwable {
        schedulesSteps.verifyBadRequest(message);
    }

    @Then("^the schedule event type list is \"([^\"]*)\"$")
    public void verifyListOfScheduleEventTypes(final String list) throws Throwable {
        schedulesSteps.verifyListOfScheduleEventTypes(list);
    }

    @Then("^the schedule start time list is \"([^\"]*)\"$")
    public void verifyListOfScheduleStartTimes(final String list) throws Throwable {
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
    public void activitiesAreRequestedWithAValidAgencyWithATimeSlotAndOffenderNumbers(final String date, final String timeSlot, final String offenderNo) throws Throwable {
        schedulesSteps.getActivities(offenderNo, date, timeSlot);
    }

    @Then("^the following events should be returned \"([^\"]*)\"$")
    public void theFollowingEventsShouldBeReturned(final String expected) throws Throwable {
        this.schedulesSteps.verifyEventComments(expected);
    }

    @Given("^an offender with scheduled visits$")
    public void anOffenderWithScheduledVisits() throws Throwable {
        //test data setup
    }

    @When("^visits are requested with a valid agency with a time slot \"([^\"]*)\" and offender numbers \"([^\"]*)\"$")
    public void visitsAreRequestedWithAValidAgencyWithATimeSlotAndOffenderNumbers(final String timeSlot, final String offenderNo) throws Throwable {
        schedulesSteps.getVisits(offenderNo, timeSlot);
    }

    @Then("^the following visits should be returned \"([^\"]*)\"$")
    public void theFollowingVisitsShouldBeReturned(final String expected) throws Throwable {
        this.schedulesSteps.verifyEventComments(expected);
    }

    @Given("^an offender with scheduled appointments$")
    public void anOffenderWithScheduledAppointments() throws Throwable {
        //test data setup
    }

    @When("^appointments are requested with a valid agency with a time slot \"([^\"]*)\" and offender numbers \"([^\"]*)\"$")
    public void appointmentsAreRequestedWithAValidAgencyWithATimeSlotAndOffenderNumbers(final String timeSlot, final String offenderNo) throws Throwable {
        schedulesSteps.getAppointments(offenderNo, timeSlot);
    }

    @Then("^the following appointments should be returned \"([^\"]*)\"$")
    public void theFollowingAppointmentsShouldBeReturned(final String appointments) throws Throwable {
        this.schedulesSteps.verifyEventDescriptionsWithLocation(appointments);
    }

    @When("^Court events are requested with a valid agency with a time slot \"([^\"]*)\", date \"([^\"]*)\" and offender number list \"([^\"]*)\"$")
    public void courtEventsAreRequestedWithAValidAgencyWithATimeSlotAndOffenderNumbers(final String timeSlot, final String date, final String offenderNos) throws Throwable {
        schedulesSteps.getCourtEvents(offenderNos, date, timeSlot);
    }

    @Then("^the following events should be returned: \"([^\"]*)\"$")
    public void theFollowingCourtEventsShouldBeReturned(final String events) throws Throwable {
        this.schedulesSteps.verifyCourtEvents(events);
    }

    @Given("^an offender that is scheduled to be transferred outside of the prison$")
    public void anOffenderThatIsScheduledToBeTransferredOutsideOfThePrison() throws Throwable {
    }

    @When("^Request an offenders external transfers for a given date$")
    public void transfersAreRequestedWithAValidAgencyAndDate() throws Throwable {
    }

    @When("^a request is made for transfers with the following parameters \"([^\"]*)\" and \"([^\"]*)\"$")
    public void aRequestIsMadeForTransfersWithTheFollowingParametersAnd(final String offenderNumber, final String date) throws Throwable {
        this.schedulesSteps.getExternalTransfers(offenderNumber, date);
    }

    @Then("^the following offender should be returned \"([^\"]*)\", \"([^\"]*)\" along with the \"([^\"]*)\"$")
    public void theFollowingOffenderShouldBeReturnedAlongWithThe(final String firstName, final String lastName, final String transferDescription) throws Throwable {
        this.schedulesSteps.verifyTransfer(firstName, lastName, transferDescription);
    }

    @Then("^an event is returned with \"([^\"]*)\" and \"([^\"]*)\"$")
    public void anEventIsReturnedWithAnd(final String eventDescription, final String eventLocation) throws Throwable {
        this.schedulesSteps.verifyEventDescriptionAndLocation(eventDescription, eventLocation);
    }
}
