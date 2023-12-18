package uk.gov.justice.hmpps.prison.executablespecification;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.executablespecification.steps.BookingActivitySteps;

/**
 * BDD step definitions for the Booking Activities API endpoints:
 * <ul>
 *     <li>/booking/{bookingId}/activities</li>
 * </ul>
 */
public class BookingActivitiesStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private BookingActivitySteps bookingActivities;

    @When("^scheduled activities are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledActivitiesAreRequestedForAnOffenderWithBookingId(final String bookingId) throws Throwable {
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
    public void activitiesAreReturned(final String expectedCount) throws Throwable {
        bookingActivities.verifyResourceRecordsReturned(Long.valueOf(expectedCount));
    }

    @And("^booking id for all activities is \"([^\"]*)\"$")
    public void bookingIdForAllActivitiesIs(final String expectedBookingId) throws Throwable {
        bookingActivities.verifyBookingId(Long.valueOf(expectedBookingId));
    }

    @And("^event class for all activities is \"([^\"]*)\"$")
    public void eventClassForAllActivitiesIs(final String expectedEventClass) throws Throwable {
        bookingActivities.verifyEventClass(expectedEventClass);
    }

    @And("^event status for all activities is \"([^\"]*)\"$")
    public void eventStatusForAllActivitiesIs(final String expectedEventStatus) throws Throwable {
        bookingActivities.verifyEventStatus(expectedEventStatus);
    }

    @And("^event type for all activities is \"([^\"]*)\"$")
    public void eventTypeForAllActivitiesIs(final String expectedEventType) throws Throwable {
        bookingActivities.verifyEventType(expectedEventType);
    }

    @And("^event type description for all activities is \"([^\"]*)\"$")
    public void eventTypeDescriptionForAllActivitiesIs(final String expectedEventTypeDescription) throws Throwable {
        bookingActivities.verifyEventTypeDescription(expectedEventTypeDescription);
    }

    @And("^event source for all activities is \"([^\"]*)\"$")
    public void eventSourceForAllActivitiesIs(final String expectedEventSource) throws Throwable {
        bookingActivities.verifyEventSource(expectedEventSource);
    }

    @And("^event status for \"([^\"]*)\" returned activity is \"([^\"]*)\"$")
    public void eventStatusForReturnedActivityIs(final String ordinal, final String expectedEventStatus) throws Throwable {
        bookingActivities.verifyEventStatus(ord2idx(ordinal), expectedEventStatus);
    }

    @And("^event sub type for \"([^\"]*)\" returned activity is \"([^\"]*)\"$")
    public void eventSubTypeForReturnedActivityIs(final String ordinal, final String expectedEventSubType) throws Throwable {
        bookingActivities.verifyEventSubType(ord2idx(ordinal), expectedEventSubType);
    }

    @And("^event sub type description for \"([^\"]*)\" returned activity is \"([^\"]*)\"$")
    public void eventSubTypeDescriptionForReturnedActivityIs(final String ordinal, final String expectedEventSubTypeDescription) throws Throwable {
        bookingActivities.verifyEventSubTypeDescription(ord2idx(ordinal), expectedEventSubTypeDescription);
    }

    @And("^event date for \"([^\"]*)\" returned activity is \"([^\"]*)\"$")
    public void eventDateForReturnedActivityIs(final String ordinal, final String expectedEventDate) throws Throwable {
        bookingActivities.verifyEventDate(ord2idx(ordinal), expectedEventDate);
    }

    @And("^start time for \"([^\"]*)\" returned activity is \"([^\"]*)\"$")
    public void startTimeForReturnedActivityIs(final String ordinal, final String expectedStartTime) throws Throwable {
        bookingActivities.verifyStartTime(ord2idx(ordinal), expectedStartTime);
    }

    @And("^end time for \"([^\"]*)\" returned activity is \"([^\"]*)\"$")
    public void endTimeForReturnedActivityIs(final String ordinal, final String expectedEndTime) throws Throwable {
        bookingActivities.verifyEndTime(ord2idx(ordinal), expectedEndTime);
    }

    @And("^event location for \"([^\"]*)\" returned activity is \"([^\"]*)\"$")
    public void eventLocationForReturnedActivityIs(final String ordinal, final String expectedEventLocation) throws Throwable {
        bookingActivities.verifyEventLocation(ord2idx(ordinal), expectedEventLocation);
    }

    @And("^event source code for \"([^\"]*)\" returned activity is \"([^\"]*)\"$")
    public void eventSourceCodeForReturnedActivityIs(final String ordinal, final String expectedSourceCode) throws Throwable {
        bookingActivities.verifyEventSourceCode(ord2idx(ordinal), expectedSourceCode);
    }

    @And("^event source description for \"([^\"]*)\" returned activity is \"([^\"]*)\"$")
    public void eventSourceDescriptionForReturnedActivityIs(final String ordinal, final String expectedSourceDescription) throws Throwable {
        bookingActivities.verifyEventSourceDescription(ord2idx(ordinal), expectedSourceDescription);
    }

    @When("^scheduled activities from \"([^\"]*)\" are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledActivitiesFromAreRequestedForAnOffenderWithBookingId(final String fromDate, final String bookingId) throws Throwable {
        bookingActivities.getBookingActivities(Long.valueOf(bookingId), fromDate, null, null, null);
    }

    @When("^scheduled activities to \"([^\"]*)\" are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledActivitiesToAreRequestedForAnOffenderWithBookingId(final String toDate, final String bookingId) throws Throwable {
        bookingActivities.getBookingActivities(Long.valueOf(bookingId), null, toDate, null, null);
    }

    @When("^scheduled activities between \"([^\"]*)\" and \"([^\"]*)\" are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledActivitiesBetweenAndAreRequestedForAnOffenderWithBookingId(final String fromDate, final String toDate, final String bookingId) throws Throwable {
        bookingActivities.getBookingActivities(Long.valueOf(bookingId), fromDate, toDate, null, null);
    }

    @And("^\"([^\"]*)\" activities in total are available$")
    public void activitiesInTotalAreAvailable(final String expectedTotal) throws Throwable {
        bookingActivities.verifyTotalResourceRecordsAvailable(Long.parseLong(expectedTotal));
    }

    @When("^scheduled activities, sorted by \"([^\"]*)\" in \"([^\"]*)\" order, are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledActivitiesSortedByInOrderAreRequestedForAnOffenderWithBookingId(final String sortFields, final String sortOrder, final String bookingId) throws Throwable {
        bookingActivities.getBookingActivities(Long.valueOf(bookingId), null, null, sortFields, parseSortOrder(sortOrder));
    }

    @Then("^bad request response, with \"([^\"]*)\" message, is received from booking activities API$")
    public void badRequestResponseWithMessageIsReceivedFromBookingActivitiesAPI(final String expectedUserMessage) throws Throwable {
        bookingActivities.verifyBadRequest(expectedUserMessage);
    }

    @When("^a request is made to update attendance for offender id \"([^\"]*)\" and activity \"([^\"]*)\" with outcome \"([^\"]*)\", performance \"([^\"]*)\" and comment \"([^\"]*)\"$")
    public void updateAttendance(final String offenderNo, final Long activityId, final String outcome, final String performance, final String comment) throws Throwable {
        bookingActivities.updateAttendance(offenderNo, activityId, outcome, performance, comment);
    }

    @When("^a request is made to update attendance for booking id \"([^\"]*)\" and activity \"([^\"]*)\" with outcome \"([^\"]*)\", performance \"([^\"]*)\" and comment \"([^\"]*)\"$")
    public void updateAttendance(final Long bookingId, final Long activityId, final String outcome, final String performance, final String comment) throws Throwable {
        bookingActivities.updateAttendance(bookingId, activityId, outcome, performance, comment);
    }

    @Then("^the booking activities request is successful$")
    public void success() throws Throwable {
        bookingActivities.verifyNoError();
    }

    @Then("^the booking activity is rejected as offender has already been paid for \"([^\"]*)\"$")
    public void offenderAlreadyPaid(final String paidActivity) throws Throwable {
        bookingActivities.verifyOffenderAlreadyPaid(paidActivity);
    }
}
