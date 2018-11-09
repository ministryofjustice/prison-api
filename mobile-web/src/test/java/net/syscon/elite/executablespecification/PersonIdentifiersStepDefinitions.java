package net.syscon.elite.executablespecification;

import cucumber.api.DataTable;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.api.model.PersonIdentifier;
import net.syscon.elite.executablespecification.steps.PersonIdentifierSteps;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class PersonIdentifiersStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private PersonIdentifierSteps personIdentifierSteps;

    @When("^the identifiers for a person represented by \"([^\"]*)\" are requested$")
    public void theIdentifiersForAPersonRepresentedByPersonIdAreRequested(Long personId) {
        personIdentifierSteps.requestPersonIdentifiers(personId);
    }

    @Then("^the returned identifiers are:$")
    public void reasonCodesAreReturnedAsFollows(DataTable table) throws Throwable {
        final List<PersonIdentifier> expected = table.asList(PersonIdentifier.class);
        personIdentifierSteps.verifyPersonIdentifiers(expected);
    }

}
