package uk.gov.justice.hmpps.prison.executablespecification;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;
import uk.gov.justice.hmpps.prison.executablespecification.steps.UserSteps;

import java.util.Arrays;

import static uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.ADMIN_TOKEN;
import static uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.CATEGORISATION_CREATE;
import static uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.GLOBAL_SEARCH;
import static uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.NORMAL_USER;
import static uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.PAY;
import static uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA;

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
    public void aUserHasAToken(final String tokenName) {
        user.authenticateAsClient(AuthTokenHelper.AuthToken.valueOf(tokenName));
    }

    @Given("^a user has logged in with username \"([^\"]*)\" and password \"([^\"]*)\"$")
    public void aUserHasLoggedInWithUsernameAndPassword(final String username, final String password) {
        user.authenticateAsClient(NORMAL_USER);
    }

    @Given("^user \"([^\"]*)\" with password \"([^\"]*)\" has authenticated with the API$")
    public void userWithPasswordHasAuthenticatedWithTheAPI(final String username, final String password) {
        user.authenticateAsClient(NORMAL_USER);
    }

    @Given("^a user has authenticated with the API$")
    public void aUserHasAuthenticatedWithTheAPI() {
        user.authenticateAsClient(NORMAL_USER);
    }

    @Given("^a user has authenticated with the API and has the pay role$")
    public void aUserHasAuthenticatedWithTheAPIAndHasThePayRole() {
        user.authenticateAsClient(PAY);
    }


    @Given("^a categorisation user has authenticated with the API$")
    public void aCategorisationUserHasAuthenticatedWithTheAPI() {
        user.authenticateAsClient(CATEGORISATION_CREATE);
    }

    @Given("^a trusted client that can maintain access roles has authenticated with the API$")
    public void aTrustedClientThatCanMaintainAccessRolesHasAuthenticatedWithTheAPI() {
        user.authenticateAsClient(ADMIN_TOKEN);
    }

    @Given("^a trusted client with GLOBAL_SEARCH role has authenticated with the API$")
    public void aTrustedClientThatHasGlobalSearchAuthenticatedWithTheAPI() {
        user.authenticateAsClient(GLOBAL_SEARCH);
    }

    @Given("^a trusted client with VIEW_PRISONER_DATA role has authenticated with the API$")
    public void aTrustedClientThatHasViewPrisonerDataAuthenticatedWithTheAPI() {
        user.authenticateAsClient(VIEW_PRISONER_DATA);
    }

    @When("^a request is made to retrieve user locations$")
    public void aRequestIsMadeToRetrieveUserLocations() {
        user.retrieveUserLocations();
    }

    @Then("^\"([^\"]*)\" user locations are returned$")
    public void userLocationsAreReturned(final String expectedCount) {
        user.verifyResourceRecordsReturned(Long.parseLong(expectedCount));
    }

    @And("^user location agency ids are \"([^\"]*)\"$")
    public void userLocationAgencyIdsAre(final String expectedAgencies) {
        user.verifyLocationAgencies(expectedAgencies);
    }

    @And("^user location descriptions are \"([^\"]*)\"$")
    public void userLocationDescriptionsAre(final String expectedDescriptions) {
        user.verifyLocationDescriptions(expectedDescriptions);
    }

    @And("^user location prefixes are \"([^\"]*)\"$")
    public void userLocationPrefixesAre(final String expectedPrefixes) {
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
    public void theRolesReturnedAre(final String roles) {
        user.verifyRoles(roles);
    }

    @When("^request is made to retrieve valid case note types for current user$")
    public void requestIsMadeToRetrieveValidCaseNoteTypesForCurrentUser() {
        user.getUserCaseNoteTypes();
    }

    @Then("^\"([^\"]*)\" case note types are returned$")
    public void caseNoteTypesAreReturned(final String expectedCount) {
        user.verifyResourceRecordsReturned(Long.parseLong(expectedCount));
    }

    @And("^each case note type is returned with one or more sub-types$")
    public void eachCaseNoteTypeIsReturnedWithOneOrMoreSubTypes() {
        user.verifyCaseNoteTypesHaveSubTypes();
    }

    @When("^a request for users with usernames \"([^\"]*)\" is made$")
    public void aRequestForUsersByusernamesIsMade(final String usernames) {
        user.getUsers(Arrays.asList(usernames.split(",")));
    }

    @When("^a request for users by local administrator is made$")
    public void aRequestForLocalAdministratorUsersWithCaseloadIsMade() {
        user.getUsersByLaa(null, null);
    }

    @Then("^a list of users is returned with usernames \"([^\"]*)\"$")
    public void aListOfUsersIsReturnedWithUsernames(final String usernameList) {
        user.verifyUserList(usernameList);
    }

    @Then("^a list of roles is returned with role codes \"([^\"]*)\"$")
    public void aListOfRolesIsReturnedWithRoleCodes(final String roleCodeList) {
        user.verifyRoleList(roleCodeList);
    }

    @When("^a request for users by local administrator with namefilter \"([^\"]*)\" and role \"([^\"]*)\" is made$")
    public void aRequestForLocalAdminUsersWithCaseloadAndNamefilterAndRoleIsMade(final String nameFilter, final String roleCode) {
        user.getUsersByLaa(roleCode, nameFilter);
    }

    @Then("^the request requiring admin privileges is rejected$")
    public void theCreateAccessRoleRequestIsRejected() {
        user.verifyAccessDenied("Maintain roles Admin access required to perform this action");
    }

}
