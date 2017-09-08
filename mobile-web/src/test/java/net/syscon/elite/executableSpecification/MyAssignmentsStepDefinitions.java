package net.syscon.elite.executableSpecification;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executableSpecification.steps.MyAssignmentsSteps;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * BDD step definitions my keyworker assignments:
 * <ul>
 *     <li>/users/me/bookingAssignments</li>
 * </ul>
 *
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
    public void totalKeyworkerAssignmentsRecordsAreReturned(String expectedCount) throws Throwable {
        myAssignments.verifyTotalResourceRecordsAvailable(Long.valueOf(expectedCount));
    }

    @Then("^\"([^\"]*)\" keyworker assignments records are returned$")
    public void keyworkerAssignmentsRecordsAreReturned(String expectedCount) throws Throwable {
        myAssignments.verifyResourceRecordsReturned(Long.valueOf(expectedCount));
    }
}
