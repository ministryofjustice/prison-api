package net.syscon.elite.executablespecification;


import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.api.model.OffenderAddress;
import net.syscon.elite.executablespecification.steps.OffenderSteps;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class OffenderStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private OffenderSteps offenderSteps;

    @When("^I view the addresses of offender with offender display number of \"([^\"]*)\"$")
    public void viewAddressNumber(final String offenderNumber) {
        offenderSteps.findAddresses(offenderNumber);
    }

    @Then("^the address results are:$")
    public void addressResultListIsAsFollows(final List<OffenderAddress> list) {
        offenderSteps.verifyAddressList(list);
    }

    @Then("^resource not found response is received from offender API")
    public void caseNotesVerifyResourceNotFound() {
        offenderSteps.verifyResourceNotFound();
    }
}
