package net.syscon.elite.executableSpecification;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executableSpecification.steps.BookingSearchSteps;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * BDD step definitions for Booking API endpoints (excluding those related to case notes):
 * <ul>
 *     <li>/booking</li>
 *     <li>/booking/{bookingId}</li>
 *     <li>/booking/{bookingId}/alerts</li>
 *     <li>/booking/{bookingId}/alerts/{alertId}</li>
 *     <li>/booking/{bookingId}/aliases</li>
 * </ul>
 *
 * NB: Not all API endpoints have associated tests at this point in time.
 */
public class BookingStepDefinitions extends AbstractStepDefinitions {
    @Autowired
    private BookingSearchSteps booking;

    @When("^a booking search is made with full last \"([^\"]*)\" of existing offender$")
    public void aBookingSearchIsMadeWithFullLastNameOfExistingOffender(String fullLastName) throws Throwable {
        booking.fullLastNameSearch(fullLastName);
    }

    @When("^a booking search is made with partial last \"([^\"]*)\" of existing offender$")
    public void aBookingSearchIsMadeWithPartialLastNameOfExistingOffender(String partialLastName) throws Throwable {
        booking.partialLastNameSearch(partialLastName);
    }

    @When("^a booking search is made with full first \"([^\"]*)\" of existing offender$")
    public void aBookingSearchIsMadeWithFullFirstNameOfExistingOffender(String fullFirstName) throws Throwable {
        booking.fullFirstNameSearch(fullFirstName);
    }

    @When("^a booking search is made with partial first \"([^\"]*)\" of existing offender$")
    public void aBookingSearchIsMadeWithPartialFirstNameOfExistingOffender(String partialFirstName) throws Throwable {
        booking.partialFirstNameSearch(partialFirstName);
    }

    @Then("^expected \"([^\"]*)\" of offender records are returned$")
    public void expectedNumberOfOffenderRecordsAreReturned(String expectedCount) throws Throwable {
        Integer expectedOffenderCount = Integer.valueOf(expectedCount);

        booking.verifySearchCount(expectedOffenderCount);
    }

    @And("^offender first names match \"([^\"]*)\"$")
    public void offenderFirstNamesMatch(String firstNames) throws Throwable {
        booking.verifyFirstNames(firstNames);
    }

    @And("^offender middle names match \"([^\"]*)\"$")
    public void offenderMiddleNamesMatch(String middleNames) throws Throwable {
        booking.verifyMiddleNames(middleNames);
    }

    @When("^a booking search is made without any criteria$")
    public void aBookingSearchIsMadeWithoutAnyCriteria() throws Throwable {
        booking.findAll();
    }

    @And("^offender last names match \"([^\"]*)\"$")
    public void offenderLastNamesMatch(String lastNames) throws Throwable {
        booking.verifyLastNames(lastNames);
    }

    @Then("^all \"([^\"]*)\" offender records are returned$")
    public void allOffenderRecordsAreReturned(String expectedCount) throws Throwable {
        Integer expectedOffenderCount = Integer.valueOf(expectedCount);

        booking.verifySearchCount(expectedOffenderCount);
    }
}
