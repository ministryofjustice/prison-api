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

//    @And("^email address of staff details returned is \"([^\"]*)\"$")
//    public void emailAddressOfStaffDetailsReturnedIs(String email) throws Throwable {
//        staff.verifyStaffEmail(email);
//    }

    @Then("^resource not found response is received from staff API$")
    public void resourceNotFoundResponseIsReceivedFromStaffAPI() throws Throwable {
        staff.verifyResourceNotFound();
    }

    @When("^request is submitted for staff members having position \"([^\"]*)\" and role \"([^\"]*)\" in agency \"([^\"]*)\"$")
    public void requestIsSubmittedForStaffMembersHavingPositionAndRoleInAgency(String position, String role, String agencyId) throws Throwable {
        staff.findStaffByAgencyPositionRole(agencyId, position, role, null, null);
    }

    @When("^request is submitted for staff members having role \"([^\"]*)\" in agency \"([^\"]*)\"$")
    public void requestIsSubmittedForStaffMembersHavingRoleInAgency(String role, String agencyId) throws Throwable {
        staff.findStaffByAgencyRole(agencyId, role, null, null);
    }


    @When("^request is submitted for staff members having role \"([^\"]*)\" in agency \"([^\"]*)\" with name filter \"([^\"]*)\" and staff id filter \"([^\"]*)\"$")
    public void requestIsSubmittedForStaffMembersHavingRoleInAgencyWithNameFilter(String role, String agencyId, String nameFilter, Long staffId) throws Throwable {
        staff.findStaffByAgencyRole(agencyId, role, nameFilter, staffId);
    }

    @When("^request is submitted for staff members having position \"([^\"]*)\" and role \"([^\"]*)\" in agency \"([^\"]*)\" with name filter \"([^\"]*)\" and staff id filter \"([^\"]*)\"$")
    public void requestIsSubmittedForStaffMembersHavingPositionAndRoleInAgencyWithNameFilter(String position, String role, String agencyId, String nameFilter, Long staffId) throws Throwable {
        staff.findStaffByAgencyPositionRole(agencyId, position, role, nameFilter, staffId);
    }

    @Then("^\"([^\"]*)\" staff detail records are returned$")
    public void staffDetailRecordsAreReturned(String expectedCount) throws Throwable {
        staff.verifyResourceRecordsReturned(Long.valueOf(expectedCount));
    }

    @And("^staff ids match \"([^\"]*)\"$")
    public void staffIdsMatch(String staffIds) throws Throwable {
        staff.verifyStaffIds(staffIds);
    }
}
