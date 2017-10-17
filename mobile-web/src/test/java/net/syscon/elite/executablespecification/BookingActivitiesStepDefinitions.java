package net.syscon.elite.executablespecification;

import net.syscon.elite.executablespecification.steps.BookingActivitySteps;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

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
}
