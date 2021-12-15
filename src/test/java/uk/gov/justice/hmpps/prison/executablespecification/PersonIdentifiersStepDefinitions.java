package uk.gov.justice.hmpps.prison.executablespecification;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.api.model.PersonIdentifier;
import uk.gov.justice.hmpps.prison.executablespecification.steps.OffenderIdentifierSteps;
import uk.gov.justice.hmpps.prison.executablespecification.steps.PersonIdentifierSteps;

import java.util.List;

public class PersonIdentifiersStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private PersonIdentifierSteps personIdentifierSteps;

    @Autowired
    private OffenderIdentifierSteps offenderIdentifierSteps;

    @When("^the identifiers for a person represented by \"([^\"]*)\" are requested$")
    public void theIdentifiersForAPersonRepresentedByPersonIdAreRequested(final Long personId) {
        personIdentifierSteps.requestPersonIdentifiers(personId);
    }

    @Then("^the returned identifiers are:$")
    public void reasonCodesAreReturnedAsFollows(final DataTable table) throws Throwable {
        final List<PersonIdentifier> expected = table.asList(PersonIdentifier.class);
        personIdentifierSteps.verifyPersonIdentifiers(expected);
    }

    @When("^the identifiers are requested for \"([^\"]*)\" and \"([^\"]*)\"$")
    public void theIdentifiersAreRequestedForAnd(String type, String value) throws Throwable {
        offenderIdentifierSteps.requestOffenderIdentifiers(type, value);
    }

    @Then("^the Offender Nos returned are \"([^\"]*)\"$")
    public void theOffenderNoReturnedIs(String offenderNos) throws Throwable {
        offenderIdentifierSteps.verifyIdentifierOffenderNos(offenderNos);
    }

    @And("^the Booking Ids returned are \"([^\"]*)\"$")
    public void theBookingIdIs(String bookingIds) throws Throwable {
        offenderIdentifierSteps.verifyIdentifierBookingIds(bookingIds);
    }
}
