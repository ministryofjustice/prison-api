package net.syscon.elite.executablespecification;

import cucumber.api.DataTable;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.api.model.ScheduledEvent;
import net.syscon.elite.executablespecification.steps.BookingEventSteps;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * BDD step definitions for finance endpoints:
 * <ul>
 * <li>/bookings/{booking_id}/balances</li>
 * </ul>
 */
public class BookingEventsStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private BookingEventSteps eventsSteps;

    @When("^today's scheduled events are requested for an offender with booking id ([0-9-]+)$")
    public void todaysScheduledEventsAreRequested(final Long bookingId) throws Throwable {
        eventsSteps.getTodaysEvents(bookingId);
    }

    @When("^this week's scheduled events are requested for an offender with booking id ([0-9-]+)$")
    public void thisWeeksScheduledEventsAreRequested(final Long bookingId) throws Throwable {
        eventsSteps.getThisWeeksEvents(bookingId);
    }

    @When("^next week's scheduled events are requested for an offender with booking id ([0-9-]+)$")
    public void nextWeeksScheduledEventsAreRequested(final Long bookingId) throws Throwable {
        eventsSteps.getNextWeeksEvents(bookingId);
    }

    @Then("^resource not found response is received from booking events API$")
    public void resourceNotFoundResponseIsReceivedFromBookingsAPI() throws Throwable {
        eventsSteps.verifyResourceNotFound();
    }

    @Then("^response from booking events API is an empty list$")
    public void responseEmpty() throws Throwable {
        eventsSteps.verifyEmpty();
    }

    @Then("^events are returned as follows:$")
    public void eventsAreReturnedAsFollows(final DataTable table) throws Throwable {
        final var expected = table.asList(ScheduledEvent.class);
        eventsSteps.verifyEvents(expected);
    }
}
