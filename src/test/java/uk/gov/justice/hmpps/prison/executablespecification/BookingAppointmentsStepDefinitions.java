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
 * </ul>
 */
public class BookingAppointmentsStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private BookingAppointmentSteps bookingAppointments;

    @Then("^resource not found response is received from booking appointments API$")
    public void resourceNotFoundResponseIsReceivedFromBookingAppointmentsAPI() throws Throwable {
        bookingAppointments.verifyResourceNotFound();
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
