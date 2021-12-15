package uk.gov.justice.hmpps.prison.executablespecification;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.executablespecification.steps.StaffSteps;

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
    public void aStaffMemberSearchIsMadeUsingStaffId(final String staffId) {
        staff.findStaffDetails(Long.valueOf(staffId));
    }

    @Then("^first name of staff details returned is \"([^\"]*)\"$")
    public void firstNameOfStaffDetailsReturnedIs(final String firstName) {
        staff.verifyStaffFirstName(firstName);
    }

    @And("^last name of staff details returned is \"([^\"]*)\"$")
    public void lastNameOfStaffDetailsReturnedIs(final String lastName) {
        staff.verifyStaffLastName(lastName);
    }

    @And("^gender of staff details returned is \"([^\"]*)\"$")
    public void genderOfStaffDetailsReturnedIs(final String gender) {
        staff.verifyStaffGender(gender);
    }

    @And("^date of birth of staff details returned is \"([^\"]*)\"$")
    public void dateOfBirthOfStaffDetailsReturnedIs(final String dob) {
        staff.verifyStaffDob(dob);
    }

    @Then("^resource not found response is received from staff API$")
    public void resourceNotFoundResponseIsReceivedFromStaffAPI() {
        staff.verifyResourceNotFound();
    }

    @When("^request is submitted for staff members having role \"([^\"]*)\" in agency \"([^\"]*)\"$")
    public void requestIsSubmittedForStaffMembersHavingRoleInAgency(final String role, final String agencyId) {
        staff.findStaffByAgencyRole(agencyId, role, null, null);
    }

    @When("^request is submitted for staff members having role \"([^\"]*)\" in agency \"([^\"]*)\" with name filter \"([^\"]*)\" and staff id filter \"([^\"]*)\"$")
    public void requestIsSubmittedForStaffMembersHavingRoleInAgencyWithNameFilter(final String role, final String agencyId, final String nameFilter, final Long staffId) {
        staff.findStaffByAgencyRole(agencyId, role, nameFilter, staffId);
    }

    @Then("^\"([^\"]*)\" staff detail records are returned$")
    public void staffDetailRecordsAreReturned(final String expectedCount) {
        staff.verifyResourceRecordsReturned(Long.parseLong(expectedCount));
    }

    @And("^staff ids match \"([^\"]*)\"$")
    public void staffIdsMatch(final String staffIds) {
        staff.verifyStaffIds(staffIds);
    }

    @When("^request is submitted using \"([^\"]*)\" and \"([^\"]*)\"$")
    public void requestIsSubmittedUsingAnd(final Long staffId, final String agencyId) {
        staff.getRoles(staffId, agencyId);
    }

    @Then("^a role containing \"([^\"]*)\" \"([^\"]*)\" is returned without duplicates$")
    public void aRoleContainingIsReturnedWithoutDuplicates(final String role, final String roleDescription) {
        staff.verifyStaffRoleWithNoDuplicates(role, roleDescription);
    }

    @When("^request is submitted for email addresses associated with staff id \"([^\"]*)\"$")
    public void requestSubmittedForEmails(final Long staffId) {
        staff.getEmails(staffId);
    }

    @Then("^\"([^\"]*)\" email address records are returned$")
    public void staffEmailAddressesAreReturned(final String expectedEmails) {
        staff.verifyNumberOfEmailAddressesReturned(Long.valueOf(expectedEmails));
    }

    @And("^response code matches \"([^\"]*)\"$")
    public void emailResponseCodeMatches(final String responseCode) {
        staff.verifyResponseCodeMatches(Long.valueOf(responseCode).intValue());
    }

    @And("^response body is \"([^\"]*)\"$")
    public void emailResponseBody(final String responseBody) {
        staff.verifyResponseBody(responseBody);
    }
}
