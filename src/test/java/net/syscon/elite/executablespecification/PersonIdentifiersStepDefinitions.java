package net.syscon.elite.executablespecification;

import cucumber.api.DataTable;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.api.model.PersonIdentifier;
import net.syscon.elite.executablespecification.steps.PersonIdentifierSteps;
import org.springframework.beans.factory.annotation.Autowired;

public class PersonIdentifiersStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private PersonIdentifierSteps personIdentifierSteps;

    @When("^the identifiers for a person represented by \"([^\"]*)\" are requested$")
    public void theIdentifiersForAPersonRepresentedByPersonIdAreRequested(final Long personId) {
        personIdentifierSteps.requestPersonIdentifiers(personId);
    }

    @Then("^the returned identifiers are:$")
    public void reasonCodesAreReturnedAsFollows(final DataTable table) throws Throwable {
        final var expected = table.asList(PersonIdentifier.class);
        personIdentifierSteps.verifyPersonIdentifiers(expected);
    }

}
