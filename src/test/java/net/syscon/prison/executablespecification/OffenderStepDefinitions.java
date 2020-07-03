package net.syscon.prison.executablespecification;


import com.google.common.base.Splitter;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.prison.executablespecification.steps.OffenderAdjudicationSteps;
import net.syscon.prison.executablespecification.steps.OffenderSteps;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static net.syscon.prison.executablespecification.steps.OffenderAdjudicationSteps.AdjudicationRow;

public class OffenderStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private OffenderSteps offenderSteps;
    @Autowired
    private OffenderAdjudicationSteps adjudicationSteps;


    @When("^I view the addresses of offender with offender display number of \"([^\"]*)\"$")
    public void viewAddressNumber(final String offenderNumber) {
        offenderSteps.findAddresses(offenderNumber);
    }

    @Then("^the address results are returned$")
    public void addressResultListIsAsFollows() {
        offenderSteps.verifyAddressList();
    }

    @When("^I view the adjudications of offender with offender display number of \"([^\"]*)\"$")
    public void viewAdjudicationsFor(final String offenderNumber) {
        adjudicationSteps.findAdjudications(offenderNumber, Map.of());
    }

    @When("^I view the adjudications of offender with offender display number of \"([^\"]*)\" at \"([^\"]*)\" with charge of type: \"([^\"]*)\"$")
    public void viewAdjudicationsFor(final String offenderNumber, final String location, final String offence) {
        adjudicationSteps.findAdjudications(offenderNumber, Map.of("agencyId", location, "offenceId", offence));
    }

    @When("^I view the adjudication details of offender display number of \"([^\"]*)\" with a adjudication number of \"([^\"]*)\"$")
    public void viewAdjudication(final String offenderNumber, final String adjudicationNumber) {
        adjudicationSteps.findAdjudicationDetails(offenderNumber, adjudicationNumber);
    }

    @Then("^the adjudication results are:$")
    public void adjudicationResultIsAsFollows(final List<AdjudicationRow> list) {
        adjudicationSteps.verifyAdjudications(list);
    }

    @Then("^the adjudication details are found$")
    public void detailsAreCorrect() {
        adjudicationSteps.verifyAdjudicationDetails();
    }

    @And("^the associated offences for this offender are: \"([^\"]*)\"$")
    public void associatedChargesAreAsFollows(final String vals) {
        adjudicationSteps.verifyOffenceCodes(Splitter.on(',').trimResults().splitToList(vals));
    }

    @And("^the associated agencies for this offender are: \"([^\"]*)\"$")
    public void theAssociatedAgenciesForThisOffenderAre(String vals) {
        adjudicationSteps.verifyAgencies(Splitter.on(',').trimResults().splitToList(vals));
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
