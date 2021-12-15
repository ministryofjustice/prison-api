package uk.gov.justice.hmpps.prison.executablespecification;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.executablespecification.steps.MyAssignmentsSteps;

/**
 * BDD step definitions my keyworker assignments:
 * <ul>
 *     <li>/users/me/bookingAssignments</li>
 * </ul>
 * <p>
 * NB: Not all API endpoints have associated tests at this point in time.
 */
public class MyAssignmentsStepDefinitions extends AbstractStepDefinitions {

    @Autowired
    private MyAssignmentsSteps myAssignments;

    @When("^I view my assignments$")
    public void iViewMyAssignments() throws Throwable {
        myAssignments.getMyAssignments();
    }

    @Then("^\"([^\"]*)\" total keyworker assignments records are returned$")
    public void totalKeyworkerAssignmentsRecordsAreReturned(final String expectedCount) throws Throwable {
        myAssignments.verifyTotalResourceRecordsAvailable(Long.valueOf(expectedCount));
    }

    @Then("^\"([^\"]*)\" keyworker assignments records are returned$")
    public void keyworkerAssignmentsRecordsAreReturned(final String expectedCount) throws Throwable {
        myAssignments.verifyResourceRecordsReturned(Long.valueOf(expectedCount));
    }
}
