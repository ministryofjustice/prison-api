package uk.gov.justice.hmpps.prison.executablespecification;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AddIepLevelSteps;

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
