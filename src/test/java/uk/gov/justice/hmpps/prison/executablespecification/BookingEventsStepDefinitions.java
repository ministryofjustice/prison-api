package uk.gov.justice.hmpps.prison.executablespecification;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.api.model.ScheduledEvent;
import uk.gov.justice.hmpps.prison.executablespecification.steps.BookingEventSteps;

import java.util.List;

/**
 * BDD step definitions for events endpoints:
 * <ul>
 * <li>/bookings/{booking_id}/events</li>
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
        final List<ScheduledEvent> expected = table.asList(ScheduledEvent.class);
        eventsSteps.verifyEvents(expected);
    }
}
