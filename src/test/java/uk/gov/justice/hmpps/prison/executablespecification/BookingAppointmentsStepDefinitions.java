package uk.gov.justice.hmpps.prison.executablespecification;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.executablespecification.steps.BookingAppointmentSteps;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Autowired
    private BookingAppointmentSteps bookingAppointments;

    @When("^scheduled appointments are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledAppointmentsAreRequestedForAnOffenderWithBookingId(final String bookingId) throws Throwable {
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
    public void appointmentsAreReturned(final String expectedCount) throws Throwable {
        bookingAppointments.verifyResourceRecordsReturned(Long.valueOf(expectedCount));
    }

    @And("^booking id for all appointments is \"([^\"]*)\"$")
    public void bookingIdForAllAppointmentsIs(final String expectedBookingId) throws Throwable {
        bookingAppointments.verifyBookingId(Long.valueOf(expectedBookingId));
    }

    @And("^event class for all appointments is \"([^\"]*)\"$")
    public void eventClassForAllAppointmentsIs(final String expectedEventClass) throws Throwable {
        bookingAppointments.verifyEventClass(expectedEventClass);
    }

    @And("^event status for all appointments is \"([^\"]*)\"$")
    public void eventStatusForAllAppointmentsIs(final String expectedEventStatus) throws Throwable {
        bookingAppointments.verifyEventStatus(expectedEventStatus);
    }

    @And("^event type for all appointments is \"([^\"]*)\"$")
    public void eventTypeForAllAppointmentsIs(final String expectedEventType) throws Throwable {
        bookingAppointments.verifyEventType(expectedEventType);
    }

    @And("^event type description for all appointments is \"([^\"]*)\"$")
    public void eventTypeDescriptionForAllAppointmentsIs(final String expectedEventTypeDescription) throws Throwable {
        bookingAppointments.verifyEventTypeDescription(expectedEventTypeDescription);
    }

    @And("^event source for all appointments is \"([^\"]*)\"$")
    public void eventSourceForAllAppointmentsIs(final String expectedEventSource) throws Throwable {
        bookingAppointments.verifyEventSource(expectedEventSource);
    }

    @And("^event sub type for \"([^\"]*)\" returned appointment is \"([^\"]*)\"$")
    public void eventSubTypeForReturnedAppointmentIs(final String ordinal, final String expectedEventSubType) throws Throwable {
        bookingAppointments.verifyEventSubType(ord2idx(ordinal), expectedEventSubType);
    }

    @And("^event sub type description for \"([^\"]*)\" returned appointment is \"([^\"]*)\"$")
    public void eventSubTypeDescriptionForReturnedAppointmentIs(final String ordinal, final String expectedEventSubTypeDescription) throws Throwable {
        bookingAppointments.verifyEventSubTypeDescription(ord2idx(ordinal), expectedEventSubTypeDescription);
    }

    @And("^event date for \"([^\"]*)\" returned appointment is \"([^\"]*)\"$")
    public void eventDateForReturnedAppointmentIs(final String ordinal, final String expectedEventDate) throws Throwable {
        bookingAppointments.verifyEventDate(ord2idx(ordinal), expectedEventDate);
    }

    @And("^start time for \"([^\"]*)\" returned appointment is \"([^\"]*)\"$")
    public void startTimeForReturnedAppointmentIs(final String ordinal, final String expectedStartTime) throws Throwable {
        bookingAppointments.verifyStartTime(ord2idx(ordinal), expectedStartTime);
    }

    @And("^end time for \"([^\"]*)\" returned appointment is \"([^\"]*)\"$")
    public void endTimeForReturnedAppointmentIs(final String ordinal, final String expectedEndTime) throws Throwable {
        bookingAppointments.verifyEndTime(ord2idx(ordinal), expectedEndTime);
    }

    @And("^event location for \"([^\"]*)\" returned appointment is \"([^\"]*)\"$")
    public void eventLocationForReturnedAppointmentIs(final String ordinal, final String expectedEventLocation) throws Throwable {
        bookingAppointments.verifyEventLocation(ord2idx(ordinal), expectedEventLocation);
    }

    @And("^event source code for \"([^\"]*)\" returned appointment is \"([^\"]*)\"$")
    public void eventSourceCodeForReturnedAppointmentIs(final String ordinal, final String expectedSourceCode) throws Throwable {
        bookingAppointments.verifyEventSourceCode(ord2idx(ordinal), expectedSourceCode);
    }

    @And("^event source description for \"([^\"]*)\" returned appointment is \"([^\"]*)\"$")
    public void eventSourceDescriptionForReturnedAppointmentIs(final String ordinal, final String expectedSourceDescription) throws Throwable {
        bookingAppointments.verifyEventSourceDescription(ord2idx(ordinal), expectedSourceDescription);
    }

    @When("^scheduled appointments for current day are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledAppointmentsForCurrentDayAreRequestedForAnOffenderWithBookingId(final String bookingId) throws Throwable {
        bookingAppointments.getBookingAppointmentsForCurrentDay(Long.valueOf(bookingId));
    }

    @When("^scheduled appointments for this week are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledAppointmentsForThisWeekAreRequestedForAnOffenderWithBookingId(final String bookingId) throws Throwable {
        bookingAppointments.getBookingAppointmentsForThisWeek(Long.valueOf(bookingId));
    }

    @When("^scheduled appointments for next week are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledAppointmentsForNextWeekAreRequestedForAnOffenderWithBookingId(final String bookingId) throws Throwable {
        bookingAppointments.getBookingAppointmentsForNextWeek(Long.valueOf(bookingId));
    }

    @When("^scheduled appointments from \"([^\"]*)\" are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledAppointmentsFromAreRequestedForAnOffenderWithBookingId(final String fromDate, final String bookingId) throws Throwable {
        bookingAppointments.getBookingAppointments(Long.valueOf(bookingId), fromDate, null, null, null);
    }

    @When("^scheduled appointments to \"([^\"]*)\" are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledAppointmentsToAreRequestedForAnOffenderWithBookingId(final String toDate, final String bookingId) throws Throwable {
        bookingAppointments.getBookingAppointments(Long.valueOf(bookingId), null, toDate, null, null);
    }

    @When("^scheduled appointments between \"([^\"]*)\" and \"([^\"]*)\" are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledAppointmentsBetweenAndAreRequestedForAnOffenderWithBookingId(final String fromDate, final String toDate, final String bookingId) throws Throwable {
        bookingAppointments.getBookingAppointments(Long.valueOf(bookingId), fromDate, toDate, null, null);
    }

    @And("^\"([^\"]*)\" appointments in total are available$")
    public void appointmentsInTotalAreAvailable(final String expectedTotal) throws Throwable {
        bookingAppointments.verifyTotalResourceRecordsAvailable(Long.parseLong(expectedTotal));
    }

    @When("^scheduled appointments, sorted by \"([^\"]*)\" in \"([^\"]*)\" order, are requested for an offender with booking id \"([^\"]*)\"$")
    public void scheduledAppointmentsSortedByInOrderAreRequestedForAnOffenderWithBookingId(final String sortFields, final String sortOrder, final String bookingId) throws Throwable {
        bookingAppointments.getBookingAppointments(Long.valueOf(bookingId), null, null, sortFields, parseSortOrder(sortOrder));
    }

    @Then("^bad request response, with \"([^\"]*)\" message, is received from booking appointments API$")
    public void badRequestResponseWithMessageIsReceivedFromBookingAppointmentsAPI(final String expectedUserMessage) throws Throwable {
        bookingAppointments.verifyBadRequest(expectedUserMessage);
    }

    @When("^A medical appointment is created for an existing offender with booking id \"([^\"]*)\", tomorrow at \"([^\"]*)\", at location \"([^\"]*)\"")
    public void appointmentIsCreated(final String bookingId, final String time, final String locationId) throws Throwable {
        final var startDateTime = LocalDateTime.parse(LocalDate.now().plusDays(1).toString() + 'T' + time);
        bookingAppointments.createAppointment(Long.valueOf(bookingId), "MEDE", startDateTime, Long.valueOf(locationId), "a comment");
    }

    @Then("^The appointment exists in the database$")
    public void appointmentExists() throws Throwable {
        bookingAppointments.verifyCreatedAppointment();
    }

    @When("^An appointment is created with an invalid comment$")
    public void appointmentIsCreatedInThePast() throws Throwable {
        bookingAppointments.createAppointment(-4L, "MEDE", LocalDateTime.now().plusDays(1), -29L, StringUtils.repeat("0123456789", 1000));
    }

    @When("^An appointment is created for an invalid type$")
    public void appointmentIsCreatedInvalidType() throws Throwable {
        bookingAppointments.createAppointment(-4L, "doesnotexist", LocalDateTime.now().plusDays(1), -29L, null);
    }

    @When("^An appointment is created for an invalid location$")
    public void appointmentIsCreatedInvalidLocation() throws Throwable {
        bookingAppointments.createAppointment(-4L, "MEDE", LocalDateTime.now().plusDays(1), -999L, null);
    }

    @When("^An appointment is created for an invalid booking id$")
    public void appointmentIsCreatedInvalidBookingId() throws Throwable {
        bookingAppointments.createAppointment(-999L, "MEDE", LocalDateTime.now().plusDays(1), -29L, null);
    }
}
