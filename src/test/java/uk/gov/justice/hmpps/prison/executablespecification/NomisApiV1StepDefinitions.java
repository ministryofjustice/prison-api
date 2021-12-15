package uk.gov.justice.hmpps.prison.executablespecification;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.executablespecification.steps.NomisApiV1Steps;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class NomisApiV1StepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private NomisApiV1Steps steps;

    @And("^v1 alerts are requested for noms Id \"([^\"]*)\"$")
    public void alertsAreRequestedForOffenderNos(String nomsId) {
        steps.getAlerts(nomsId);

    }

    @Then("^(\\d+) v1 alerts are returned$")
    public void alertsAreReturned(int size) {
        assertThat(((List) (steps.getAlerts().get("alerts")))).hasSize(size);
    }
}
