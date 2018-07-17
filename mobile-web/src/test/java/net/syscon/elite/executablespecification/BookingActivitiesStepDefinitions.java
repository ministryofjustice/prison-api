package net.syscon.elite.executablespecification;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executablespecification.steps.BookingActivitySteps;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * BDD step definitions for the Booking Activities API endpoints:
 * <ul>
 *     <li>/booking/{bookingId}/activities</li>
 *     <li>/booking/{bookingId}/activities/today</li>
 * </ul>
 */
public class BookingActivitiesStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private BookingActivitySteps bookingActivities;

    @When("^scheduled activities are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledActivitiesAreRequestedForAnOffenderWithBookingId(String bookingId) throws Throwable {
        bookingActivities.getBookingActivities(Long.valueOf(bookingId), null, null, null, null);
    }

    @Then("^response from booking activities API is an empty list$")
    public void responseIsAnEmptyList() throws Throwable {
        bookingActivities.verifyNoResourceRecordsReturned();
    }

    @Then("^resource not found response is received from booking activities API$")
    public void resourceNotFoundResponseIsReceivedFromBookingActivitiesAPI() throws Throwable {
        bookingActivities.verifyResourceNotFound();
    }

    @Then("^\"([^\"]*)\" activities are returned$")
    public void activitiesAreReturned(String expectedCount) throws Throwable {
        bookingActivities.verifyResourceRecordsReturned(Long.valueOf(expectedCount));
    }

    @And("^booking id for all activities is \"([^\"]*)\"$")
    public void bookingIdForAllActivitiesIs(String expectedBookingId) throws Throwable {
        bookingActivities.verifyBookingId(Long.valueOf(expectedBookingId));
    }

    @And("^event class for all activities is \"([^\"]*)\"$")
    public void eventClassForAllActivitiesIs(String expectedEventClass) throws Throwable {
        bookingActivities.verifyEventClass(expectedEventClass);
    }

    @And("^event status for all activities is \"([^\"]*)\"$")
    public void eventStatusForAllActivitiesIs(String expectedEventStatus) throws Throwable {
        bookingActivities.verifyEventStatus(expectedEventStatus);
    }

    @And("^event type for all activities is \"([^\"]*)\"$")
    public void eventTypeForAllActivitiesIs(String expectedEventType) throws Throwable {
        bookingActivities.verifyEventType(expectedEventType);
    }

    @And("^event type description for all activities is \"([^\"]*)\"$")
    public void eventTypeDescriptionForAllActivitiesIs(String expectedEventTypeDescription) throws Throwable {
        bookingActivities.verifyEventTypeDescription(expectedEventTypeDescription);
    }

    @And("^event source for all activities is \"([^\"]*)\"$")
    public void eventSourceForAllActivitiesIs(String expectedEventSource) throws Throwable {
        bookingActivities.verifyEventSource(expectedEventSource);
    }

    @And("^event status for \"([^\"]*)\" returned activity is \"([^\"]*)\"$")
    public void eventStatusForReturnedActivityIs(String ordinal, String expectedEventStatus) throws Throwable {
        bookingActivities.verifyEventStatus(ord2idx(ordinal), expectedEventStatus);
    }

    @And("^event sub type for \"([^\"]*)\" returned activity is \"([^\"]*)\"$")
    public void eventSubTypeForReturnedActivityIs(String ordinal, String expectedEventSubType) throws Throwable {
        bookingActivities.verifyEventSubType(ord2idx(ordinal), expectedEventSubType);
    }

    @And("^event sub type description for \"([^\"]*)\" returned activity is \"([^\"]*)\"$")
    public void eventSubTypeDescriptionForReturnedActivityIs(String ordinal, String expectedEventSubTypeDescription) throws Throwable {
        bookingActivities.verifyEventSubTypeDescription(ord2idx(ordinal), expectedEventSubTypeDescription);
    }

    @And("^event date for \"([^\"]*)\" returned activity is \"([^\"]*)\"$")
    public void eventDateForReturnedActivityIs(String ordinal, String expectedEventDate) throws Throwable {
        bookingActivities.verifyEventDate(ord2idx(ordinal), expectedEventDate);
    }

    @And("^start time for \"([^\"]*)\" returned activity is \"([^\"]*)\"$")
    public void startTimeForReturnedActivityIs(String ordinal, String expectedStartTime) throws Throwable {
        bookingActivities.verifyStartTime(ord2idx(ordinal), expectedStartTime);
    }

    @And("^end time for \"([^\"]*)\" returned activity is \"([^\"]*)\"$")
    public void endTimeForReturnedActivityIs(String ordinal, String expectedEndTime) throws Throwable {
        bookingActivities.verifyEndTime(ord2idx(ordinal), expectedEndTime);
    }

    @And("^event location for \"([^\"]*)\" returned activity is \"([^\"]*)\"$")
    public void eventLocationForReturnedActivityIs(String ordinal, String expectedEventLocation) throws Throwable {
        bookingActivities.verifyEventLocation(ord2idx(ordinal), expectedEventLocation);
    }

    @And("^event source code for \"([^\"]*)\" returned activity is \"([^\"]*)\"$")
    public void eventSourceCodeForReturnedActivityIs(String ordinal, String expectedSourceCode) throws Throwable {
        bookingActivities.verifyEventSourceCode(ord2idx(ordinal), expectedSourceCode);
    }

    @And("^event source description for \"([^\"]*)\" returned activity is \"([^\"]*)\"$")
    public void eventSourceDescriptionForReturnedActivityIs(String ordinal, String expectedSourceDescription) throws Throwable {
        bookingActivities.verifyEventSourceDescription(ord2idx(ordinal), expectedSourceDescription);
    }

    @When("^scheduled activities for current day are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledActivitiesForCurrentDayAreRequestedForAnOffenderWithBookingId(String bookingId) throws Throwable {
        bookingActivities.getBookingActivitiesForCurrentDay(Long.valueOf(bookingId));
    }

    @When("^scheduled activities from \"([^\"]*)\" are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledActivitiesFromAreRequestedForAnOffenderWithBookingId(String fromDate, String bookingId) throws Throwable {
        bookingActivities.getBookingActivities(Long.valueOf(bookingId), fromDate, null, null, null);
    }

    @When("^scheduled activities to \"([^\"]*)\" are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledActivitiesToAreRequestedForAnOffenderWithBookingId(String toDate, String bookingId) throws Throwable {
        bookingActivities.getBookingActivities(Long.valueOf(bookingId), null, toDate, null, null);
    }

    @When("^scheduled activities between \"([^\"]*)\" and \"([^\"]*)\" are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledActivitiesBetweenAndAreRequestedForAnOffenderWithBookingId(String fromDate, String toDate, String bookingId) throws Throwable {
        bookingActivities.getBookingActivities(Long.valueOf(bookingId), fromDate, toDate, null, null);
    }

    @And("^\"([^\"]*)\" activities in total are available$")
    public void activitiesInTotalAreAvailable(String expectedTotal) throws Throwable {
        bookingActivities.verifyTotalResourceRecordsAvailable(Long.parseLong(expectedTotal));
    }

    @When("^scheduled activities, sorted by \"([^\"]*)\" in \"([^\"]*)\" order, are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledActivitiesSortedByInOrderAreRequestedForAnOffenderWithBookingId(String sortFields, String sortOrder, String bookingId) throws Throwable {
        bookingActivities.getBookingActivities(Long.valueOf(bookingId), null, null, sortFields, parseSortOrder(sortOrder));
    }

    @Then("^bad request response, with \"([^\"]*)\" message, is received from booking activities API$")
    public void badRequestResponseWithMessageIsReceivedFromBookingActivitiesAPI(String expectedUserMessage) throws Throwable {
        bookingActivities.verifyBadRequest(expectedUserMessage);
    }

    @When("^a request is made to update attendance for booking id \"([^\"]*)\" and activity \"([^\"]*)\" with outcome \"([^\"]*)\", performance \"([^\"]*)\" and comment \"([^\"]*)\"$")
    public void updateAttendance(Long bookingId, Long activityId, String outcome, String performance, String comment) throws Throwable {
        bookingActivities.updateAttendance(bookingId, activityId, outcome, performance, comment);
    }

    @Then("^the booking activities request is successful")
    public void success() throws Throwable {
        bookingActivities.verifyNoError();
    }

    @And("^the saved attendance details can be retrieved correctly")
    public void verifySavedDetails() throws Throwable {
        bookingActivities.verifySavedDetails();
    }
}
