package net.syscon.elite.executablespecification;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executablespecification.steps.AddIepLevelSteps;
import org.springframework.beans.factory.annotation.Autowired;

public class AddIepLevelStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private AddIepLevelSteps steps;

    @And("^a booking having id \"([^\"]*)\"$")
    public void aBookingHavingId(String bookingIdString) {
        long bookingId = Long.parseLong(bookingIdString);
        steps.setBookingId(bookingId);
    }


    @And("^the new IEP Level should be \"([^\"]*)\" with comment \"([^\"]*)\"$")
    public void theNewIEPLevelShouldBeWithComment(String iepLevel, String comment) {
        steps.setIepLevelAndComment(iepLevel, comment);
    }

    @When("^the new level is applied to the booking$")
    public void theNewLevelIsAppliedToTheBooking() {
        steps.addIepLevel();
    }

    @Then("^the response status code is \"([^\"]*)\"$")
    public void theHttpResponseStatusCodeIs(String httpStatusCodeString) {
        steps.assertHttpResponseStatusCode(Integer.parseInt(httpStatusCodeString));
    }

    @And("^the error response message contains \"([^\"]*)\"$")
    public void theErrorResponseMessageContains(String expectedMessage) {
        steps.assertErrorMessageContains(expectedMessage);
    }
}
