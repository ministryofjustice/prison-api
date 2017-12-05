package net.syscon.elite.executablespecification;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executablespecification.steps.BookingVisitSteps;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * BDD step definitions for the Booking Visits API endpoints:
 * <ul>
 *     <li>/booking/{bookingId}/visits</li>
 *     <li>/booking/{bookingId}/visits/today</li>
 * </ul>
 */
public class BookingVisitsStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private BookingVisitSteps bookingVisits;

    @When("^scheduled visits are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledVisitsAreRequestedForAnOffenderWithBookingId(String bookingId) throws Throwable {
        bookingVisits.getBookingVisits(Long.valueOf(bookingId), null, null, null, null);
    }

    @Then("^response from booking visits API is an empty list$")
    public void responseIsAnEmptyList() throws Throwable {
        bookingVisits.verifyNoResourceRecordsReturned();
    }

    @Then("^resource not found response is received from booking visits API$")
    public void resourceNotFoundResponseIsReceivedFromBookingVisitsAPI() throws Throwable {
        bookingVisits.verifyResourceNotFound();
    }

    @Then("^\"([^\"]*)\" visits are returned$")
    public void visitsAreReturned(String expectedCount) throws Throwable {
        bookingVisits.verifyResourceRecordsReturned(Long.valueOf(expectedCount));
    }

    @And("^booking id for all visits is \"([^\"]*)\"$")
    public void bookingIdForAllVisitsIs(String expectedBookingId) throws Throwable {
        bookingVisits.verifyBookingId(Long.valueOf(expectedBookingId));
    }

    @And("^event class for all visits is \"([^\"]*)\"$")
    public void eventClassForAllVisitsIs(String expectedEventClass) throws Throwable {
        bookingVisits.verifyEventClass(expectedEventClass);
    }

    @And("^event status for all visits is \"([^\"]*)\"$")
    public void eventStatusForAllVisitsIs(String expectedEventStatus) throws Throwable {
        bookingVisits.verifyEventStatus(expectedEventStatus);
    }

    @And("^event type for all visits is \"([^\"]*)\"$")
    public void eventTypeForAllVisitsIs(String expectedEventType) throws Throwable {
        bookingVisits.verifyEventType(expectedEventType);
    }

    @And("^event type description for all visits is \"([^\"]*)\"$")
    public void eventTypeDescriptionForAllVisitsIs(String expectedEventTypeDescription) throws Throwable {
        bookingVisits.verifyEventTypeDescription(expectedEventTypeDescription);
    }

    @And("^event source for all visits is \"([^\"]*)\"$")
    public void eventSourceForAllVisitsIs(String expectedEventSource) throws Throwable {
        bookingVisits.verifyEventSource(expectedEventSource);
    }

    @And("^event sub type for \"([^\"]*)\" returned visit is \"([^\"]*)\"$")
    public void eventSubTypeForReturnedVisitIs(String ordinal, String expectedEventSubType) throws Throwable {
        bookingVisits.verifyEventSubType(ord2idx(ordinal), expectedEventSubType);
    }

    @And("^event sub type description for \"([^\"]*)\" returned visit is \"([^\"]*)\"$")
    public void eventSubTypeDescriptionForReturnedVisitIs(String ordinal, String expectedEventSubTypeDescription) throws Throwable {
        bookingVisits.verifyEventSubTypeDescription(ord2idx(ordinal), expectedEventSubTypeDescription);
    }

    @And("^event date for \"([^\"]*)\" returned visit is \"([^\"]*)\"$")
    public void eventDateForReturnedVisitIs(String ordinal, String expectedEventDate) throws Throwable {
        bookingVisits.verifyEventDate(ord2idx(ordinal), expectedEventDate);
    }

    @And("^start time for \"([^\"]*)\" returned visit is \"([^\"]*)\"$")
    public void startTimeForReturnedVisitIs(String ordinal, String expectedStartTime) throws Throwable {
        bookingVisits.verifyStartTime(ord2idx(ordinal), expectedStartTime);
    }

    @And("^end time for \"([^\"]*)\" returned visit is \"([^\"]*)\"$")
    public void endTimeForReturnedVisitIs(String ordinal, String expectedEndTime) throws Throwable {
        bookingVisits.verifyEndTime(ord2idx(ordinal), expectedEndTime);
    }

    @And("^event location for \"([^\"]*)\" returned visit is \"([^\"]*)\"$")
    public void eventLocationForReturnedVisitIs(String ordinal, String expectedEventLocation) throws Throwable {
        bookingVisits.verifyEventLocation(ord2idx(ordinal), expectedEventLocation);
    }

    @And("^event source code for \"([^\"]*)\" returned visit is \"([^\"]*)\"$")
    public void eventSourceCodeForReturnedVisitIs(String ordinal, String expectedSourceCode) throws Throwable {
        bookingVisits.verifyEventSourceCode(ord2idx(ordinal), expectedSourceCode);
    }

    @And("^event source description for \"([^\"]*)\" returned visit is \"([^\"]*)\"$")
    public void eventSourceDescriptionForReturnedVisitIs(String ordinal, String expectedSourceDescription) throws Throwable {
        bookingVisits.verifyEventSourceDescription(ord2idx(ordinal), expectedSourceDescription);
    }

    @When("^scheduled visits for current day are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledVisitsForCurrentDayAreRequestedForAnOffenderWithBookingId(String bookingId) throws Throwable {
        bookingVisits.getBookingVisitsForCurrentDay(Long.valueOf(bookingId));
    }

    @When("^scheduled visits from \"([^\"]*)\" are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledVisitsFromAreRequestedForAnOffenderWithBookingId(String fromDate, String bookingId) throws Throwable {
        bookingVisits.getBookingVisits(Long.valueOf(bookingId), fromDate, null, null, null);
    }

    @When("^scheduled visits to \"([^\"]*)\" are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledVisitsToAreRequestedForAnOffenderWithBookingId(String toDate, String bookingId) throws Throwable {
        bookingVisits.getBookingVisits(Long.valueOf(bookingId), null, toDate, null, null);
    }

    @When("^scheduled visits between \"([^\"]*)\" and \"([^\"]*)\" are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledVisitsBetweenAndAreRequestedForAnOffenderWithBookingId(String fromDate, String toDate, String bookingId) throws Throwable {
        bookingVisits.getBookingVisits(Long.valueOf(bookingId), fromDate, toDate, null, null);
    }

    @And("^\"([^\"]*)\" visits in total are available$")
    public void visitsInTotalAreAvailable(String expectedTotal) throws Throwable {
        bookingVisits.verifyTotalResourceRecordsAvailable(Long.parseLong(expectedTotal));
    }

    @When("^scheduled visits, sorted by \"([^\"]*)\" in \"([^\"]*)\" order, are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledVisitsSortedByInOrderAreRequestedForAnOffenderWithBookingId(String sortFields, String sortOrder, String bookingId) throws Throwable {
        bookingVisits.getBookingVisits(Long.valueOf(bookingId), null, null, sortFields, parseSortOrder(sortOrder));
    }

    @Then("^bad request response, with \"([^\"]*)\" message, is received from booking visits API$")
    public void badRequestResponseWithMessageIsReceivedFromBookingVisitsAPI(String expectedUserMessage) throws Throwable {
        bookingVisits.verifyBadRequest(expectedUserMessage);
    }
}
