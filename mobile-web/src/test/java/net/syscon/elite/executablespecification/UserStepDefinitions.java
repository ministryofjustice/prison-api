package net.syscon.elite.executablespecification;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executablespecification.steps.UserSteps;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * BDD step definitions for User API endpoints:
 * <ul>
 *     <li>/users/{username}</li>
 *     <li>/users/me</li>
 *     <li>/users/me/bookingAssignments</li>
 *     <li>/users/me/caseLoads</li>
 *     <li>/users/me/activeCaseLoad</li>
 *     <li>/users/staff</li>
 *     <li>/users/me/locations</li>
 *     <li>/users/me/roles</li>
 * </ul>
 */
public class UserStepDefinitions extends AbstractStepDefinitions {
    @Autowired
    private UserSteps user;

    @Given("^a user has logged in with username \"([^\"]*)\" and password \"([^\"]*)\"$")
    public void aUserHasLoggedInWithUsernameAndPassword(String username, String password) throws Throwable {
        authenticate(username, password, false);
    }

    @Given("^a user has authenticated with the API$")
    public void aUserHasAuthenticatedWithTheAPI() {
        authenticate("itag_user", "password", false);
    }

    @Given("^a admin user has authenticated with the API$")
    public void aNonAdminUserHasAuthenticatedWithTheAPI() {
        authenticate("ELITE2_API_USER", "password", false);
    }

    @Given("^user \"([^\"]*)\" with password \"([^\"]*)\" has authenticated with the API$")
    public void userWithPasswordHasAuthenticatedWithTheAPI(String username, String password) throws Throwable {
        authenticate(username, password, false);
    }

    @Given("^a trusted client has authenticated with the API$")
    public void trustedClientWithPasswordHasAuthenticatedWithTheAPI() throws Throwable {
        authenticate(null, null, true);
    }

    @When("^a staff member search is made using staff id \"([^\"]*)\"$")
    public void aStaffMemberSearchIsMadeUsingStaffId(String staffId) throws Throwable {
        user.findStaffDetails(Long.valueOf(staffId));
    }

    @Then("^first name of staff details returned is \"([^\"]*)\"$")
    public void firstNameOfStaffDetailsReturnedIs(String firstName) throws Throwable {
        user.verifyStaffFirstName(firstName);
    }

    @And("^last name of staff details returned is \"([^\"]*)\"$")
    public void lastNameOfStaffDetailsReturnedIs(String lastName) throws Throwable {
        user.verifyStaffLastName(lastName);
    }

    @And("^email address of staff details returned is \"([^\"]*)\"$")
    public void emailAddressOfStaffDetailsReturnedIs(String email) throws Throwable {
        user.verifyStaffEmail(email);
    }

    @When("^a request is made to retrieve user locations$")
    public void aRequestIsMadeToRetrieveUserLocations() throws Throwable {
        user.retrieveUserLocations();
    }

    @Then("^\"([^\"]*)\" user locations are returned$")
    public void userLocationsAreReturned(String expectedCount) throws Throwable {
        user.verifyResourceRecordsReturned(Long.valueOf(expectedCount));
    }

    @And("^user location agency ids are \"([^\"]*)\"$")
    public void userLocationAgencyIdsAre(String expectedAgencies) throws Throwable {
        user.verifyLocationAgencies(expectedAgencies);
    }

    @And("^user location descriptions are \"([^\"]*)\"$")
    public void userLocationDescriptionsAre(String expectedDescriptions) throws Throwable {
        user.verifyLocationDescriptions(expectedDescriptions);
    }

    @And("^user location prefixes are \"([^\"]*)\"$")
    public void userLocationPrefixesAre(String expectedPrefixes) throws Throwable {
        user.verifyLocationPrefixes(expectedPrefixes);
    }

    @Then("^resource not found response is received from users API$")
    public void resourceNotFoundResponseIsReceivedFromUsersAPI() throws Throwable {
        user.verifyResourceNotFound();
    }

    @When("^a user role request is made$")
    public void aUserRoleRequestIsMade() throws Throwable {
        user.getUserRoles();
    }

    @Then("^the roles returned are \"([^\"]*)\"$")
    public void theRolesReturnedAre(String roles) throws Throwable {
        user.verifyRoles(roles);
    }

    @When("^request is made to retrieve valid case note types for current user$")
    public void requestIsMadeToRetrieveValidCaseNoteTypesForCurrentUser() throws Throwable {
        user.getUserCaseNoteTypes();
    }

    @Then("^\"([^\"]*)\" case note types are returned$")
    public void caseNoteTypesAreReturned(String expectedCount) throws Throwable {
        user.verifyResourceRecordsReturned(Long.parseLong(expectedCount));
    }

    @And("^each case note type is returned with one or more sub-types$")
    public void eachCaseNoteTypeIsReturnedWithOneOrMoreSubTypes() throws Throwable {
        user.verifyCaseNoteTypesHaveSubTypes();
    }

    private void authenticate(String username, String password, boolean clientCredentials) {
        user.authenticates(username, password, clientCredentials);
    }
}
