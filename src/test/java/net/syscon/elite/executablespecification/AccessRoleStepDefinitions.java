package net.syscon.elite.executablespecification;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executablespecification.steps.AccessRoleSteps;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * BDD step definitions for Access-roles API endpoints:
 * <ul>
 *     <li>/access-roles</li>
 * </ul>
 */
public class AccessRoleStepDefinitions extends AbstractStepDefinitions {
    @Autowired
    private AccessRoleSteps steps;

    @When("^an access role creation request is made with role code \"([^\"]*)\"$")
    public void anAccessRoleCreationRequestIsMadeWithRoleCode(final String roleCode) {
        steps.createAccessRole(roleCode, "roleName", null);
    }

    @Then("^access role is successfully created$")
    public void accessRoleIsSuccessfullyCreated() {
        steps.verifyCreated();
    }

    @Then("^the create access role request is rejected$")
    public void theCreateAccessRoleRequestIsRejected() {
        steps.verifyAccessDenied("Access is denied");
    }

    @Then("^the update access role request is rejected$")
    public void theUpdateAccessRoleRequestIsRejected() {
        steps.verifyAccessDenied("Access is denied");
    }

    @When("^an update access role request is made with role code \"([^\"]*)\" and role name \"([^\"]*)\"$")
    public void anUpdateAccessRoleCreationRequestIsMadeWithRoleCodeAndRoleName(final String roleCode, final String roleName) {
        steps.updateAccessRole(roleCode, roleName);
    }

    @Then("^access role is successfully updated$")
    public void accessRoleIsSuccessfullyUpdated() {
        steps.verifyUpdated();
    }

    @When("^a request to get access roles is made$")
    public void aRequestToGetAccessRolesIsMade() {
        steps.getAccessRoles(false);
    }

    @When("^a request to get access roles, including admin roles is made$")
    public void aRequestToGetAccessRolesIncludingAdminIsMade() {
        steps.getAccessRoles(true);
    }

    @Then("^the access role list is returned$")
    public void theAccessRoleListIsReturned() {
        steps.verifyAccessRoles();
    }

    @Then("^the access role list is returned without admin roles$")
    public void theAccessRoleListIsReturnedWithoutAdmin() {
        steps.verifyAccessRoles();
        steps.verifyAccessRolesDoNotIncludeAdminRoles();
    }

    @Then("^role to update is not found$")
    public void roleToUpdateIsNotFound() throws Throwable {
        steps.verifyAccessRoleNotFound();
    }
}
