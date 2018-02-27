package net.syscon.elite.executablespecification;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executablespecification.steps.StaffSteps;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * BDD step definitions for Staff API endpoints:
 * <ul>
 *     <li>/staff/{staffId}</li>
 *     <li>/staff/roles/{agencyId}/positions/{position}/roles/{role}</li>
 * </ul>
 */
public class StaffStepDefinitions extends AbstractStepDefinitions {
    @Autowired
    private StaffSteps staff;

    @When("^a staff member search is made using staff id \"([^\"]*)\"$")
    public void aStaffMemberSearchIsMadeUsingStaffId(String staffId) throws Throwable {
        staff.findStaffDetails(Long.valueOf(staffId));
    }

    @Then("^first name of staff details returned is \"([^\"]*)\"$")
    public void firstNameOfStaffDetailsReturnedIs(String firstName) throws Throwable {
        staff.verifyStaffFirstName(firstName);
    }

    @And("^last name of staff details returned is \"([^\"]*)\"$")
    public void lastNameOfStaffDetailsReturnedIs(String lastName) throws Throwable {
        staff.verifyStaffLastName(lastName);
    }

    @And("^email address of staff details returned is \"([^\"]*)\"$")
    public void emailAddressOfStaffDetailsReturnedIs(String email) throws Throwable {
        staff.verifyStaffEmail(email);
    }

    @Then("^resource not found response is received from staff API$")
    public void resourceNotFoundResponseIsReceivedFromStaffAPI() throws Throwable {
        staff.verifyResourceNotFound();
    }
}
