package net.syscon.elite.executablespecification;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.api.model.Token;
import net.syscon.elite.executablespecification.steps.UserSteps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step definitions for User API endpoints:
 * <ul>
 *     <li>/users/{username}</li>
 *     <li>/users/me</li>
 *     <li>/users/me/bookingAssignments</li>
 *     <li>/users/me/caseLoads</li>
 *     <li>/users/me/activeCaseLoad</li>
 *     <li>/users/login</li>
 *     <li>/users/refresh</li>
 *     <li>/users/staff</li>
 *     <li>/users/token</li>
 *     <li>/users/me/locations</li>
 *     <li>/users/me/roles</li>
 * </ul>
 *
 * NB: Not all API endpoints have associated tests at this point in time.
 */
public class UserStepDefinitions extends AbstractStepDefinitions {
    @Autowired
    private UserSteps user;

    private Token currentToken;

    @Value("${jwt.expiration.seconds}")
    private int expirationSeconds;

    @Value("${jwt.refresh.expiration.seconds}")
    private int refreshExpirationSeconds;

    @Given("^API authentication is attempted with the following credentials:$")
    public void apiAuthenticationIsAttemptedWithTheFollowingCredentials(DataTable rawData) {
        final Map<String, String> loginCredentials = rawData.asMap(String.class, String.class);
        authAndStoreToken(loginCredentials.get("username"), loginCredentials.get("password"));
    }

    @Given("^a user has logged in with username \"([^\"]*)\" and password \"([^\"]*)\"$")
    public void aUserHasLoggedInWithUsernameAndPassword(String username, String password) throws Throwable {
        authAndStoreToken(username, password);
    }

    @Then("^a valid JWT token is generated$")
    public void aValidJWTTokenIsGenerated() {
        user.verifyToken();
    }

    @Then("^a valid JWT refresh token is generated$")
    public void aValidJWTrefreshTokenIsGenerated() {
        user.verifyRefreshToken();
    }

    @And("^current user details match the following:$")
    public void currentUserDetailsMatchTheFollowing(DataTable rawData) {
        Map<String, String> userCheck = rawData.asMap(String.class, String.class);

        user.verifyDetails(userCheck.get("username"), userCheck.get("firstName"), userCheck.get("lastName"));
    }

    @Given("^a user has authenticated with the API$")
    public void aUserHasAuthenticatedWithTheAPI() {
        authAndStoreToken("itag_user", "password");
    }

    @Given("^user \"([^\"]*)\" with password \"([^\"]*)\" has authenticated with the API$")
    public void userWithPasswordHasAuthenticatedWithTheAPI(String username, String password) throws Throwable {
        authAndStoreToken(username, password);
    }

    @When("^token refresh is attempted$")
    public void tokenRefreshIsAttempted() throws Throwable {
        user.refresh();
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

    @Then("^a new token is generated successfully$")
    public void aNewTokenIsGeneratedSuccessfully() throws Throwable {
        assertThat(currentToken.getToken()).isNotEqualTo(user.getAuth().getToken());
    }

    @Then("^authentication denied is returned$")
    public void authenticationDeniedIsReturned() throws Throwable {
        user.verifyNotAuthorised();
    }

    private void authAndStoreToken(String username, String password) {
        user.authenticates(username, password);
        currentToken = user.getAuth().getToken();
    }

    @And("^token timeout is valid$")
    public void tokenTimeoutIsValid() throws Throwable {
        final Token token = user.getAuth().getToken();
        assertThat(token.getExpiration()).isLessThan(token.getRefreshExpiration());
    }

    @When("^I wait until the token as expired$")
    public void iWaitUntilTheTokenAsExpired() throws Throwable {
        Thread.sleep(expirationSeconds * 1000);
    }

    @When("^I wait until the refresh token as expired$")
    public void iWaitUntilTheRefreshTokenAsExpired() throws Throwable {
        Thread.sleep(refreshExpirationSeconds * 1000);
    }
}
