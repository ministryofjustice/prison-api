package net.syscon.elite.executablespecification;


import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.api.model.OffenderAddress;
import net.syscon.elite.executablespecification.steps.OffenderAdjudicationSteps;
import net.syscon.elite.executablespecification.steps.OffenderSteps;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static net.syscon.elite.executablespecification.steps.OffenderAdjudicationSteps.AdjudicationRow;

public class OffenderStepDefinitions extends AbstractStepDefinitions {

    @Autowired private OffenderSteps offenderSteps;
    @Autowired private OffenderAdjudicationSteps adjudicationSteps;


    @When("^I view the addresses of offender with offender display number of \"([^\"]*)\"$")
    public void viewAddressNumber(final String offenderNumber) {
        offenderSteps.findAddresses(offenderNumber);
    }

    @Then("^the address results are:$")
    public void addressResultListIsAsFollows(final List<OffenderAddress> list) {
        offenderSteps.verifyAddressList(list);
    }

    @When("^I view the adjudications of offender with offender display number of \"([^\"]*)\"$")
    public void viewAdjudicationsFor(final String offenderNumber) {
        adjudicationSteps.findAdjudications(offenderNumber);
    }

    @Then("^the adjudication results are:$")
    public void adjudicationResultIsAsFollows(final List<AdjudicationRow> list) {
        adjudicationSteps.verifyAdjudications(list);
    }

    @Then("^resource not found response is received from offender API")
    public void verifyResourceNotFoundForOffenderApi() {
        offenderSteps.verifyResourceNotFound();
    }

    @Then("^resource not found response is received from adjudication API")
    public void verifyResourceNotFoundForAdjudicationApi() {
        adjudicationSteps.verifyResourceNotFound();
    }
}
