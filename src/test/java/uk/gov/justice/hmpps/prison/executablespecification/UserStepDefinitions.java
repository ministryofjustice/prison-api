package uk.gov.justice.hmpps.prison.executablespecification;

import io.cucumber.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;
import uk.gov.justice.hmpps.prison.executablespecification.steps.UserSteps;

import static uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.NORMAL_USER;
import static uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.PAY;
import static uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA;

public class UserStepDefinitions extends AbstractStepDefinitions {
    @Autowired
    private UserSteps user;

    @Given("^a user has a token name of \"([^\"]*)\"$")
    public void aUserHasAToken(final String tokenName) {
        user.authenticateAsClient(AuthTokenHelper.AuthToken.valueOf(tokenName));
    }

    @Given("^a user has authenticated with the API$")
    public void aUserHasAuthenticatedWithTheAPI() {
        user.authenticateAsClient(NORMAL_USER);
    }
}
