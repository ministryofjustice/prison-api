package net.syscon.elite.executablespecification;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executablespecification.steps.AuthenticationSteps;
import net.syscon.elite.executablespecification.steps.UserSteps;
import org.springframework.beans.factory.annotation.Autowired;

import static net.syscon.elite.executablespecification.steps.AuthenticationSteps.AuthToken.*;

/**
 * BDD step definitions for User API endpoints:
 * <ul>
 *     <li>/users/{username}</li>
 *     <li>/users/me</li>
 *     <li>/users/me/bookingAssignments</li>
 *     <li>/users/me/caseLoads</li>
 *     <li>/users/me/activeCaseLoad</li>
 *     <li>/users/me/locations</li>
 *     <li>/users/me/roles</li>
 * </ul>
 */
public class UserStepDefinitions extends AbstractStepDefinitions {
    @Autowired
    private UserSteps user;

    @Given("^a user has a token name of \"([^\"]*)\"$")
    public void aUserHasAToken(String tokenName) {
        user.authenticateAsClient(AuthenticationSteps.AuthToken.valueOf(tokenName));
    }

    @Given("^a user has logged in with username \"([^\"]*)\" and password \"([^\"]*)\"$")
    public void aUserHasLoggedInWithUsernameAndPassword(String username, String password) {
        user.authenticateAsClient(NORMAL_USER);
    }
    @Given("^user \"([^\"]*)\" with password \"([^\"]*)\" has authenticated with the API$")
    public void userWithPasswordHasAuthenticatedWithTheAPI(String username, String password) {
        user.authenticateAsClient(NORMAL_USER);
    }

    @Given("^a user has authenticated with the API$")
    public void aUserHasAuthenticatedWithTheAPI() {
        user.authenticateAsClient(NORMAL_USER);
    }

    @Given("^a categorisation user has authenticated with the API$")
    public void aCategorisationUserHasAuthenticatedWithTheAPI() {
        user.authenticateAsClient(CATEGORISATION_CREATE);
    }

    @Given("^a system client \"([^\"]*)\" has authenticated with the API$")
    public void trustedClientWithPasswordHasAuthenticatedWithTheAPI(String clientId) {
        user.authenticateAsClient(LOCAL_ADMIN);
    }

    @Given("^a trusted client that can maintain access roles has authenticated with the API$")
    public void aTrustedClientThatCanMaintainAccessRolesHasAuthenticatedWithTheAPI() {
        user.authenticateAsClient(ADMIN_TOKEN);
    }

    @When("^a request is made to retrieve user locations$")
    public void aRequestIsMadeToRetrieveUserLocations() {
        user.retrieveUserLocations();
    }

    @Then("^\"([^\"]*)\" user locations are returned$")
    public void userLocationsAreReturned(String expectedCount) {
        user.verifyResourceRecordsReturned(Long.valueOf(expectedCount));
    }

    @And("^user location agency ids are \"([^\"]*)\"$")
    public void userLocationAgencyIdsAre(String expectedAgencies) {
        user.verifyLocationAgencies(expectedAgencies);
    }

    @And("^user location descriptions are \"([^\"]*)\"$")
    public void userLocationDescriptionsAre(String expectedDescriptions) {
        user.verifyLocationDescriptions(expectedDescriptions);
    }

    @And("^user location prefixes are \"([^\"]*)\"$")
    public void userLocationPrefixesAre(String expectedPrefixes) {
        user.verifyLocationPrefixes(expectedPrefixes);
    }

    @Then("^resource not found response is received from users API$")
    public void resourceNotFoundResponseIsReceivedFromUsersAPI() {
        user.verifyResourceNotFound();
    }

    @When("^a user role request is made for all roles$")
    public void aUserRoleRequestIsMadeForAllRoles() {
        user.getUserRoles(true);
    }

    @When("^a user role request is made$")
    public void aUserRoleRequestIsMade() {
        user.getUserRoles(false);
    }

    @Then("^the roles returned are \"([^\"]*)\"$")
    public void theRolesReturnedAre(String roles) {
        user.verifyRoles(roles);
    }

    @When("^request is made to retrieve valid case note types for current user$")
    public void requestIsMadeToRetrieveValidCaseNoteTypesForCurrentUser() {
        user.getUserCaseNoteTypes();
    }

    @Then("^\"([^\"]*)\" case note types are returned$")
    public void caseNoteTypesAreReturned(String expectedCount) {
        user.verifyResourceRecordsReturned(Long.parseLong(expectedCount));
    }

    @And("^each case note type is returned with one or more sub-types$")
    public void eachCaseNoteTypeIsReturnedWithOneOrMoreSubTypes() {
        user.verifyCaseNoteTypesHaveSubTypes();
    }

    @When("^a request for users having role \"([^\"]*)\" at caseload \"([^\"]*)\" is made$")
    public void aRequestForUsersHavingAtIsMade(String role, String caseload) {
        user.findUsernamesHavingRoleAtCaseload(role, caseload);
    }

    @Then("^the matching \"([^\"]*)\" are returned$")
    public void theMatchingAreReturned(String usernames) {
        user.verifyUsernames(usernames);
    }

    @When("^the client assigns api-role \"([^\"]*)\" to user \"([^\"]*)\"$")
    public void theClientAssignsApiRoleToUser(String role, String username) {
        user.assignApiRoleToUser(role, username);
    }

    @When("^the client assigns access role \"([^\"]*)\" to user \"([^\"]*)\" for caseload \"([^\"]*)\"$")
    public void theClientAssignsApiRoleToUserForCaseload(String role, String username, String caseload) {
        user.assignAccessRoleToUser(role, username, caseload);
    }

    @Then("^user \"([^\"]*)\" has been assigned api-role \"([^\"]*)\"$")
    public void userHasBeenAssignedApiRole(String username, String role) {
        user.verifyApiRoleAssignment(username, role);
    }

    @Then("^user \"([^\"]*)\" has been assigned access role \"([^\"]*)\" for caseload \"([^\"]*)\"$")
    public void userHasBeenAssignedAccessRole(String username, String role, String caseload){
        user.verifyAccessRoleAssignment(username, role, caseload);
    }

    @When("^the client removes role \"([^\"]*)\" from user \"([^\"]*)\" at caseload \"([^\"]*)\"$")
    public void theClientRemovesRoleFromUserAtCaseload(String role, String username, String caseload) {
        user.removeRole(role, username, caseload);
    }

    @Then("^user \"([^\"]*)\" does not have role \"([^\"]*)\" at caseload \"([^\"]*)\"$")
    public void userDoesNotHaveRoleAtCaseload(String username, String role, String caseload) {
        user.userDoesNotHaveRoleAtCaseload( username,  role,  caseload);
    }

    @When("^a request for users with caseload \"([^\"]*)\" is made$")
    public void aRequestForUsersWithCaseloadIsMade(String caseloadId) {
        user.getUsersByCaseload(caseloadId, null, null, false);
    }

    @When("^a request for users is made$")
    public void aRequestForUsersIsMade() {
        user.getUsers(null, null, false);
    }

    @When("^a request for local administrator users with caseload \"([^\"]*)\" is made$")
    public void aRequestForLocalAdministratorUsersWithCaseloadIsMade(String caseloadId) {
        user.getUsersByCaseload(caseloadId, null, null, true);
    }

    @Then("^a list of users is returned with usernames \"([^\"]*)\"$")
    public void aListOfUsersIsReturnedWithUsernames(String usernameList) {
        user.verifyUserList(usernameList);
    }

    @Then("^a list of roles is returned with role codes \"([^\"]*)\"$")
    public void aListOfRolesIsReturnedWithRoleCodes(String roleCodeList) {
        user.verifyRoleList(roleCodeList);
    }

    @When("^a request for users with caseload \"([^\"]*)\" and namefilter \"([^\"]*)\" and role \"([^\"]*)\" is made$")
    public void aRequestForUsersWithCaseloadAndNamefilterAndRoleIsMade(String caseloadId, String nameFilter, String roleCode) {
        user.getUsersByCaseload(caseloadId, roleCode, nameFilter, false);
    }

    @When("^a request for local administrator users with caseload \"([^\"]*)\" and namefilter \"([^\"]*)\" and role \"([^\"]*)\" is made$")
    public void aRequestForLocalAdminUsersWithCaseloadAndNamefilterAndRoleIsMade(String caseloadId, String nameFilter, String roleCode) {
        user.getUsersByCaseload(caseloadId, roleCode, nameFilter, true);
    }

    @When("^a request for roles for user \"([^\"]*)\" with caseload \"([^\"]*)\" is made$")
    public void aRequestForRolesForUserWithCaseloadIsMade(String username, String caseload) {
        user.getRolesByUserAndCaseload(username, caseload);
    }

    @Then("^the request requiring admin privileges is rejected$")
    public void theCreateAccessRoleRequestIsRejected() {
        user.verifyAccessDenied("Maintain roles Admin access required to perform this action");
    }

}
