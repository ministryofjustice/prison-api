package net.syscon.elite.executablespecification;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executablespecification.steps.BookingAppointmentSteps;

/**
 * BDD step definitions for the Booking Appointments API endpoints:
 * <ul>
 *     <li>/booking/{bookingId}/appointments</li>
 *     <li>/booking/{bookingId}/appointments/today</li>
 *     <li>/booking/{bookingId}/appointments/thisWeek</li>
 *     <li>/booking/{bookingId}/appointments/nextWeek</li>
 * </ul>
 */
public class BookingAppointmentsStepDefinitions extends AbstractStepDefinitions {
    private final BookingAppointmentSteps bookingAppointments;

    public BookingAppointmentsStepDefinitions(BookingAppointmentSteps bookingAppointments) {
        this.bookingAppointments = bookingAppointments;
    }

    @When("^scheduled appointments are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledAppointmentsAreRequestedForAnOffenderWithBookingId(String bookingId) throws Throwable {
        bookingAppointments.getBookingAppointments(Long.valueOf(bookingId), null, null, null, null);
    }

    @Then("^response from booking appointments API is an empty list$")
    public void responseIsAnEmptyList() throws Throwable {
        bookingAppointments.verifyNoResourceRecordsReturned();
    }

    @Then("^resource not found response is received from booking appointments API$")
    public void resourceNotFoundResponseIsReceivedFromBookingAppointmentsAPI() throws Throwable {
        bookingAppointments.verifyResourceNotFound();
    }

    @Then("^\"([^\"]*)\" appointments are returned$")
    public void appointmentsAreReturned(String expectedCount) throws Throwable {
        bookingAppointments.verifyResourceRecordsReturned(Long.valueOf(expectedCount));
    }

    @And("^booking id for all appointments is \"([^\"]*)\"$")
    public void bookingIdForAllAppointmentsIs(String expectedBookingId) throws Throwable {
        bookingAppointments.verifyBookingId(Long.valueOf(expectedBookingId));
    }

    @And("^event class for all appointments is \"([^\"]*)\"$")
    public void eventClassForAllAppointmentsIs(String expectedEventClass) throws Throwable {
        bookingAppointments.verifyEventClass(expectedEventClass);
    }

    @And("^event status for all appointments is \"([^\"]*)\"$")
    public void eventStatusForAllAppointmentsIs(String expectedEventStatus) throws Throwable {
        bookingAppointments.verifyEventStatus(expectedEventStatus);
    }

    @And("^event type for all appointments is \"([^\"]*)\"$")
    public void eventTypeForAllAppointmentsIs(String expectedEventType) throws Throwable {
        bookingAppointments.verifyEventType(expectedEventType);
    }

    @And("^event type description for all appointments is \"([^\"]*)\"$")
    public void eventTypeDescriptionForAllAppointmentsIs(String expectedEventTypeDescription) throws Throwable {
        bookingAppointments.verifyEventTypeDescription(expectedEventTypeDescription);
    }

    @And("^event source for all appointments is \"([^\"]*)\"$")
    public void eventSourceForAllAppointmentsIs(String expectedEventSource) throws Throwable {
        bookingAppointments.verifyEventSource(expectedEventSource);
    }

    @And("^event sub type for \"([^\"]*)\" returned appointment is \"([^\"]*)\"$")
    public void eventSubTypeForReturnedAppointmentIs(String ordinal, String expectedEventSubType) throws Throwable {
        bookingAppointments.verifyEventSubType(ord2idx(ordinal), expectedEventSubType);
    }

    @And("^event sub type description for \"([^\"]*)\" returned appointment is \"([^\"]*)\"$")
    public void eventSubTypeDescriptionForReturnedAppointmentIs(String ordinal, String expectedEventSubTypeDescription) throws Throwable {
        bookingAppointments.verifyEventSubTypeDescription(ord2idx(ordinal), expectedEventSubTypeDescription);
    }

    @And("^event date for \"([^\"]*)\" returned appointment is \"([^\"]*)\"$")
    public void eventDateForReturnedAppointmentIs(String ordinal, String expectedEventDate) throws Throwable {
        bookingAppointments.verifyEventDate(ord2idx(ordinal), expectedEventDate);
    }

    @And("^start time for \"([^\"]*)\" returned appointment is \"([^\"]*)\"$")
    public void startTimeForReturnedAppointmentIs(String ordinal, String expectedStartTime) throws Throwable {
        bookingAppointments.verifyStartTime(ord2idx(ordinal), expectedStartTime);
    }

    @And("^end time for \"([^\"]*)\" returned appointment is \"([^\"]*)\"$")
    public void endTimeForReturnedAppointmentIs(String ordinal, String expectedEndTime) throws Throwable {
        bookingAppointments.verifyEndTime(ord2idx(ordinal), expectedEndTime);
    }

    @And("^event location for \"([^\"]*)\" returned appointment is \"([^\"]*)\"$")
    public void eventLocationForReturnedAppointmentIs(String ordinal, String expectedEventLocation) throws Throwable {
        bookingAppointments.verifyEventLocation(ord2idx(ordinal), expectedEventLocation);
    }

    @And("^event source code for \"([^\"]*)\" returned appointment is \"([^\"]*)\"$")
    public void eventSourceCodeForReturnedAppointmentIs(String ordinal, String expectedSourceCode) throws Throwable {
        bookingAppointments.verifyEventSourceCode(ord2idx(ordinal), expectedSourceCode);
    }

    @And("^event source description for \"([^\"]*)\" returned appointment is \"([^\"]*)\"$")
    public void eventSourceDescriptionForReturnedAppointmentIs(String ordinal, String expectedSourceDescription) throws Throwable {
        bookingAppointments.verifyEventSourceDescription(ord2idx(ordinal), expectedSourceDescription);
    }

    @When("^scheduled appointments for current day are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledAppointmentsForCurrentDayAreRequestedForAnOffenderWithBookingId(String bookingId) throws Throwable {
        bookingAppointments.getBookingAppointmentsForCurrentDay(Long.valueOf(bookingId));
    }

    @When("^scheduled appointments for this week are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledAppointmentsForThisWeekAreRequestedForAnOffenderWithBookingId(String bookingId) throws Throwable {
        bookingAppointments.getBookingAppointmentsForThisWeek(Long.valueOf(bookingId));
    }

    @When("^scheduled appointments for next week are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledAppointmentsForNextWeekAreRequestedForAnOffenderWithBookingId(String bookingId) throws Throwable {
        bookingAppointments.getBookingAppointmentsForNextWeek(Long.valueOf(bookingId));
    }

    @When("^scheduled appointments from \"([^\"]*)\" are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledAppointmentsFromAreRequestedForAnOffenderWithBookingId(String fromDate, String bookingId) throws Throwable {
        bookingAppointments.getBookingAppointments(Long.valueOf(bookingId), fromDate, null, null, null);
    }

    @When("^scheduled appointments to \"([^\"]*)\" are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledAppointmentsToAreRequestedForAnOffenderWithBookingId(String toDate, String bookingId) throws Throwable {
        bookingAppointments.getBookingAppointments(Long.valueOf(bookingId), null, toDate, null, null);
    }

    @When("^scheduled appointments between \"([^\"]*)\" and \"([^\"]*)\" are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledAppointmentsBetweenAndAreRequestedForAnOffenderWithBookingId(String fromDate, String toDate, String bookingId) throws Throwable {
        bookingAppointments.getBookingAppointments(Long.valueOf(bookingId), fromDate, toDate, null, null);
    }

    @And("^\"([^\"]*)\" appointments in total are available$")
    public void appointmentsInTotalAreAvailable(String expectedTotal) throws Throwable {
        bookingAppointments.verifyTotalResourceRecordsAvailable(Long.parseLong(expectedTotal));
    }

    @When("^scheduled appointments, sorted by \"([^\"]*)\" in \"([^\"]*)\" order, are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledAppointmentsSortedByInOrderAreRequestedForAnOffenderWithBookingId(String sortFields, String sortOrder, String bookingId) throws Throwable {
        bookingAppointments.getBookingAppointments(Long.valueOf(bookingId), null, null, sortFields, parseSortOrder(sortOrder));
    }

    @Then("^bad request response, with \"([^\"]*)\" message, is received from booking appointments API$")
    public void badRequestResponseWithMessageIsReceivedFromBookingAppointmentsAPI(String expectedUserMessage) throws Throwable {
        bookingAppointments.verifyBadRequest(expectedUserMessage);
    }
}
