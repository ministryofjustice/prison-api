package net.syscon.elite.executablespecification;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executablespecification.steps.BookingActivitySteps;

/**
 * BDD step definitions for the Booking Activities API endpoints:
 * <ul>
 *     <li>/booking/{bookingId}/activities</li>
 * </ul>
 */
public class BookingActivitiesStepDefinitions extends AbstractStepDefinitions {
    private final BookingActivitySteps bookingActivities;

    public BookingActivitiesStepDefinitions(BookingActivitySteps bookingActivities) {
        this.bookingActivities = bookingActivities;
    }

    @When("^scheduled activities are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledActivitiesAreRequestedForAnOffenderWithBookingId(String bookingId) throws Throwable {
        bookingActivities.getBookingActivities(Long.valueOf(bookingId));
    }

    @Then("^response is an empty list$")
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

    @And("^event source code for \"([^\"]*)\" returned activitiy is \"([^\"]*)\"$")
    public void eventSourceCodeForReturnedActivitiyIs(String ordinal, String expectedSourceCode) throws Throwable {
        bookingActivities.verifyEventSourceCode(ord2idx(ordinal), expectedSourceCode);
    }

    @And("^event source description for \"([^\"]*)\" returned activity is \"([^\"]*)\"$")
    public void eventSourceDescriptionForReturnedActivityIs(String ordinal, String expectedSourceDescription) throws Throwable {
        bookingActivities.verifyEventSourceDescription(ord2idx(ordinal), expectedSourceDescription);
    }
}
