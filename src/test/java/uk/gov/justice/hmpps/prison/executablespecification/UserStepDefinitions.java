package uk.gov.justice.hmpps.prison.executablespecification;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;
import uk.gov.justice.hmpps.prison.executablespecification.steps.UserSteps;

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

    @Given("^a trusted client with VIEW_PRISONER_DATA role has authenticated with the API$")
    public void aTrustedClientThatHasViewPrisonerDataAuthenticatedWithTheAPI() {
        user.authenticateAsClient(VIEW_PRISONER_DATA);
    }

    @When("^a request is made to retrieve user locations non-residential, include non-res = \"([^\"]*)\"$")
    public void aRequestIsMadeToRetrieveUserLocations(final String includeNonRes) {
        user.retrieveUserLocations("true".equals(includeNonRes));
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
}
