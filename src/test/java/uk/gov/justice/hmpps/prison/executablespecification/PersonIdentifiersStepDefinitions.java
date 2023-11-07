package uk.gov.justice.hmpps.prison.executablespecification;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.api.model.PersonIdentifier;
import uk.gov.justice.hmpps.prison.executablespecification.steps.PersonIdentifierSteps;

import java.util.Collections;
import java.util.List;

public class PersonIdentifiersStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private PersonIdentifierSteps personIdentifierSteps;

    @When("^the identifiers for a person represented by \"([^\"]*)\" are requested$")
    public void theIdentifiersForAPersonRepresentedByPersonIdAreRequested(final Long personId) {
        personIdentifierSteps.requestPersonIdentifiers(personId);
    }

    @Then("^there are no returned identifiers")
    public void noReturnedIdentifiers() {
        personIdentifierSteps.verifyPersonIdentifiers(Collections.emptyList());
    }

    @Then("^the returned identifiers are:$")
    public void reasonCodesAreReturnedAsFollows(final DataTable table) {
        final List<PersonIdentifier> expected = table.asList(PersonIdentifier.class);
        personIdentifierSteps.verifyPersonIdentifiers(expected);
    }
}
