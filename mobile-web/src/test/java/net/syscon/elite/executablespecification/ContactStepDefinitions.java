package net.syscon.elite.executablespecification;

import net.syscon.elite.executablespecification.steps.ContactSteps;
import net.syscon.elite.api.model.Contact;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * BDD step definitions for the following Booking API endpoints:
 * <ul>
 * <li>/booking/{bookingId}/contacts</li>
 * </ul>
 *
 * NB: Not all API endpoints have associated tests at this point in time.
 */
public class ContactStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private ContactSteps contactSteps;

    @When("^contact details with booking id ([0-9-]+) is requested$")
    public void sentenceWithBookingId(Long id) {
        contactSteps.getContacts(id);
    }

    @Then("^resource not found response is received from contact details API$")
    public void resourceNotFoundResponseIsReceivedFromSentenceDetailsAPI() throws Throwable {
        contactSteps.verifyResourceNotFound();
    }

    @Then("^the next of kin (\\w+) is \"([^\"]*)\"$")
    public void theNextOfKinFieldIs(String field, String value) throws Throwable {
        contactSteps.verifyNextOfKinField(field, value);
    }

    @Then("^the next of kin results are:$")
    public void nextOfKinResultListIsAsFollows(List<Contact> list) throws Throwable {
        contactSteps.verifyNextOfKinList(list);
    }

    @Then("^There is no next of kin$")
    public void noNextOfKin() throws Throwable {
        contactSteps.verifyNoNextOfKin();
    }
}
