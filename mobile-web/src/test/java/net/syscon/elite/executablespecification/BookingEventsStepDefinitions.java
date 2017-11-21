package net.syscon.elite.executablespecification;

import net.syscon.elite.executablespecification.steps.BookingEventSteps;

import org.springframework.beans.factory.annotation.Autowired;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

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
    public void todaysScheduledEventsAreRequested(Long bookingId) throws Throwable {
        eventsSteps.getTodaysEvents(bookingId);
    }

    @When("^this week's scheduled events are requested for an offender with booking id ([0-9-]+)$")
    public void thisWeeksScheduledEventsAreRequested(Long bookingId) throws Throwable {
        eventsSteps.getThisWeeksEvents(bookingId);
    }

    @When("^next week's scheduled events are requested for an offender with booking id ([0-9-]+)$")
    public void nextWeeksScheduledEventsAreRequested(Long bookingId) throws Throwable {
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

    @Then("^([0-9-]+) events are returned$")
    public void numberEventsAreReturned(int number) throws Throwable {
        eventsSteps.verifyNumber(number);
    }

    @Then("For index of ([0-9-]+),")
    public void forIndexOf(int index) throws Throwable {
        eventsSteps.setIndex(index);
    }

    @Then("the eventType is \"([^\"]*)\"")
    public void theEventTypeIs(String value) throws Throwable {
        eventsSteps.verifyIndexedEventType(value);
    }

    @Then("the eventLocation is \"([^\"]*)\"")
    public void theEventLocationIs(String value) throws Throwable {
        eventsSteps.verifyEventLocation(value);
    }
}
