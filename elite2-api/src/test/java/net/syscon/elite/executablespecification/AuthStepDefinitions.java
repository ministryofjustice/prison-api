package net.syscon.elite.executablespecification;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executablespecification.steps.UserSteps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step definitions for OAUTH2 authentication API endpoints:
 */
@TestPropertySource({ "/application-test-auth.properties" })
public class AuthStepDefinitions extends AbstractStepDefinitions {
    @Autowired
    private UserSteps user;

    private OAuth2AccessToken previousToken;
    private OAuth2AccessToken currentToken;

    @Value("${jwt.expiration.seconds}")
    private int expirationSeconds;

    @Value("${jwt.refresh.expiration.seconds}")
    private int refreshExpirationSeconds;

    @Given("^API authentication is attempted with the following credentials:$")
    public void apiAuthenticationIsAttemptedWithTheFollowingCredentials(DataTable rawData) {
        final Map<String, String> loginCredentials = rawData.asMap(String.class, String.class);
        authAndStoreToken(loginCredentials.get("username"), loginCredentials.get("password"));
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

    @When("^token refresh is attempted$")
    public void tokenRefreshIsAttempted() {
        previousToken = currentToken;
        user.refresh(previousToken);
        currentToken = user.getAuth().getToken();
    }

    @When("^a user role request is made after token has expired$")
    public void aUserRoleRequestIsMade() {
        user.getUserRoles(true);
    }

    @Then("^a new token is generated successfully$")
    public void aNewTokenIsGeneratedSuccessfully() throws Throwable {
        assertThat(currentToken.getValue()).isNotEqualTo(user.getAuth().getToken());
    }

    @Then("^authentication denied is returned$")
    public void authenticationDeniedIsReturned() throws Throwable {
        user.verifyNotAuthorised();
    }

    @Then("^a new token has been issued$")
    public void aNewTokenHasBeenIssued() {
        assertThat(currentToken.getValue()).isNotNull();
        assertThat(currentToken.getValue()).isNotEqualTo(previousToken.getValue());
    }

    @Then("^Unnaproved client exception is returned$")
    public void UnapprovedClientReturned() throws Throwable {
        user.verifyUnapprovedClient();
    }

    private void authAndStoreToken(String username, String password) {
        user.authenticates(username, password, false, null);
        currentToken = user.getAuth().getToken();
    }

    @And("^token timeout is valid$")
    public void tokenTimeoutIsValid() {
        assertThat(previousToken.getExpiration()).isBefore(currentToken.getExpiration());
    }

    @When("^I wait until the token as expired$")
    public void iWaitUntilTheTokenAsExpired() throws Throwable {
        Thread.sleep(( expirationSeconds + 1 ) * 1000);
    }

    @When("^I wait until the refresh token as expired$")
    public void iWaitUntilTheRefreshTokenAsExpired() throws Throwable {
        Thread.sleep(( refreshExpirationSeconds + 1 ) * 1000);
    }
}
