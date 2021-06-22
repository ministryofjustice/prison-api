package uk.gov.justice.hmpps.prison.executablespecification;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.executablespecification.steps.BookingVisitSteps;

import java.time.Duration;
import java.time.LocalDate;

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
    public void scheduledVisitsAreRequestedForAnOffenderWithBookingId(final String bookingId) throws Throwable {
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
    public void visitsAreReturned(final String expectedCount) throws Throwable {
        bookingVisits.verifyResourceRecordsReturned(Long.valueOf(expectedCount));
    }

    @And("^booking id for all visits is \"([^\"]*)\"$")
    public void bookingIdForAllVisitsIs(final String expectedBookingId) throws Throwable {
        bookingVisits.verifyBookingId(Long.valueOf(expectedBookingId));
    }

    @And("^event class for all visits is \"([^\"]*)\"$")
    public void eventClassForAllVisitsIs(final String expectedEventClass) throws Throwable {
        bookingVisits.verifyEventClass(expectedEventClass);
    }

    @And("^event status for all visits is present$")
    public void eventStatusForAllVisitsIs() throws Throwable {
        bookingVisits.verifyEventStatusPresent();
    }

    @And("^event type for all visits is \"([^\"]*)\"$")
    public void eventTypeForAllVisitsIs(final String expectedEventType) throws Throwable {
        bookingVisits.verifyEventType(expectedEventType);
    }

    @And("^event type description for all visits is \"([^\"]*)\"$")
    public void eventTypeDescriptionForAllVisitsIs(final String expectedEventTypeDescription) throws Throwable {
        bookingVisits.verifyEventTypeDescription(expectedEventTypeDescription);
    }

    @And("^event source for all visits is \"([^\"]*)\"$")
    public void eventSourceForAllVisitsIs(final String expectedEventSource) throws Throwable {
        bookingVisits.verifyEventSource(expectedEventSource);
    }

    @And("^event sub type for \"([^\"]*)\" returned visit is \"([^\"]*)\"$")
    public void eventSubTypeForReturnedVisitIs(final String ordinal, final String expectedEventSubType) throws Throwable {
        bookingVisits.verifyEventSubType(ord2idx(ordinal), expectedEventSubType);
    }

    @And("^event sub type description for \"([^\"]*)\" returned visit is \"([^\"]*)\"$")
    public void eventSubTypeDescriptionForReturnedVisitIs(final String ordinal, final String expectedEventSubTypeDescription) throws Throwable {
        bookingVisits.verifyEventSubTypeDescription(ord2idx(ordinal), expectedEventSubTypeDescription);
    }

    @And("^event date for \"([^\"]*)\" returned visit is \"([^\"]*)\"$")
    public void eventDateForReturnedVisitIs(final String ordinal, final String expectedEventDate) throws Throwable {
        bookingVisits.verifyEventDate(ord2idx(ordinal), expectedEventDate);
    }

    @And("^start time for \"([^\"]*)\" returned visit is \"([^\"]*)\"$")
    public void startTimeForReturnedVisitIs(final String ordinal, final String expectedStartTime) throws Throwable {
        bookingVisits.verifyStartTime(ord2idx(ordinal), expectedStartTime);
    }

    @And("^end time for \"([^\"]*)\" returned visit is \"([^\"]*)\"$")
    public void endTimeForReturnedVisitIs(final String ordinal, final String expectedEndTime) throws Throwable {
        bookingVisits.verifyEndTime(ord2idx(ordinal), expectedEndTime);
    }

    @And("^event location for \"([^\"]*)\" returned visit is \"([^\"]*)\"$")
    public void eventLocationForReturnedVisitIs(final String ordinal, final String expectedEventLocation) throws Throwable {
        bookingVisits.verifyEventLocation(ord2idx(ordinal), expectedEventLocation);
    }

    @And("^event source code for \"([^\"]*)\" returned visit is \"([^\"]*)\"$")
    public void eventSourceCodeForReturnedVisitIs(final String ordinal, final String expectedSourceCode) throws Throwable {
        bookingVisits.verifyEventSourceCode(ord2idx(ordinal), expectedSourceCode);
    }

    @And("^event source description for \"([^\"]*)\" returned visit is \"([^\"]*)\"$")
    public void eventSourceDescriptionForReturnedVisitIs(final String ordinal, final String expectedSourceDescription) throws Throwable {
        bookingVisits.verifyEventSourceDescription(ord2idx(ordinal), expectedSourceDescription);
    }

    @When("^scheduled visits for current day are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledVisitsForCurrentDayAreRequestedForAnOffenderWithBookingId(final String bookingId) throws Throwable {
        bookingVisits.getBookingVisitsForCurrentDay(Long.valueOf(bookingId));
    }

    @When("^scheduled visits from \"([^\"]*)\" are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledVisitsFromAreRequestedForAnOffenderWithBookingId(final String fromDate, final String bookingId) throws Throwable {
        bookingVisits.getBookingVisits(Long.valueOf(bookingId), fromDate, null, null, null);
    }

    @When("^scheduled visits to \"([^\"]*)\" are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledVisitsToAreRequestedForAnOffenderWithBookingId(final String toDate, final String bookingId) throws Throwable {
        bookingVisits.getBookingVisits(Long.valueOf(bookingId), null, toDate, null, null);
    }

    @When("^scheduled visits between \"([^\"]*)\" and \"([^\"]*)\" are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledVisitsBetweenAndAreRequestedForAnOffenderWithBookingId(final String fromDate, final String toDate, final String bookingId) throws Throwable {
        bookingVisits.getBookingVisits(Long.valueOf(bookingId), fromDate, toDate, null, null);
    }

    @And("^\"([^\"]*)\" visits in total are available$")
    public void visitsInTotalAreAvailable(final String expectedTotal) throws Throwable {
        bookingVisits.verifyTotalResourceRecordsAvailable(Long.parseLong(expectedTotal));
    }

    @When("^scheduled visits, sorted by \"([^\"]*)\" in \"([^\"]*)\" order, are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledVisitsSortedByInOrderAreRequestedForAnOffenderWithBookingId(final String sortFields, final String sortOrder, final String bookingId) throws Throwable {
        bookingVisits.getBookingVisits(Long.valueOf(bookingId), null, null, sortFields, parseSortOrder(sortOrder));
    }

    @Then("^bad request response, with \"([^\"]*)\" message, is received from booking visits API$")
    public void badRequestResponseWithMessageIsReceivedFromBookingVisitsAPI(final String expectedUserMessage) throws Throwable {
        bookingVisits.verifyBadRequest(expectedUserMessage);
    }

    @When("the last visit is requested for an offender with booking id \"([^\"]*)\"$")
    public void lastVisitIsRequested(final String bookingId) throws Throwable {
        bookingVisits.getBookingVisitLast(Long.valueOf(bookingId));
    }

    @Then("the visit ([^\\\"]*) is \"([^\"]*)\"$")
    public void theVisitFieldIs(final String field, final String value) throws Throwable {
        bookingVisits.verifyVisitField(field, value);
    }

    @When("^the next visit is requested for an offender with booking id \"([^\"]*)\"$")
    public void theNextVisitIsRequestedForAnOffenderWithBookingId(final Long bookingId) throws Throwable {
        bookingVisits.getBookingVisitNext(bookingId);
    }

    @And("^the visit startTime is offset from the start of today by \"([^\"]*)\"$")
    public void theVisitStartTimeIsOffsetFromTheStartOfTodayBy(final String durationString) throws Throwable {
        final var offset = Duration.parse(durationString);
        final var expectedDateTime = LocalDate.now().atStartOfDay().plus(offset);
        bookingVisits.verifyStartDateTime(expectedDateTime);
    }

    @And("^the visit endTime is offset from the start of today by \"([^\"]*)\"$")
    public void theVisitEndTimeIsIsOffsetFromTheStartOfTodayBy(final String durationString) throws Throwable {
        final var offset = Duration.parse(durationString);
        final var expectedDateTime = LocalDate.now().atStartOfDay().plus(offset);
        bookingVisits.verifyEndDateTime(expectedDateTime);
    }
}
