package net.syscon.elite.executableSpecification;

import cucumber.api.DataTable;
import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.executableSpecification.steps.CaseNoteSteps;
import net.syscon.elite.executableSpecification.steps.UserSteps;
import net.syscon.elite.test.DatasourceActiveProfilesResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ActiveProfiles(resolver = DatasourceActiveProfilesResolver.class)
@SuppressWarnings("SpringJavaAutowiringInspection")
@ContextConfiguration
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class StepDefinitions {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserSteps user;

    @Autowired
    private CaseNoteSteps caseNote;

    @When("^API authentication is attempted with the following credentials:$")
    public void apiAuthenticationIsAttemptedWithTheFollowingCredentials(DataTable rawData) {
        final Map<String, String> loginCredentials = rawData.asMap(String.class, String.class);

        user.authenticates(loginCredentials.get("username"), loginCredentials.get("password"));
    }

    @Then("^a valid JWT token is generated$")
    public void aValidJWTTokenIsGenerated() {
        assertThat(user.getToken()).isNotEmpty();
    }

    @And("^current user details match the following:$")
    public void currentUserDetailsMatchTheFollowing(DataTable rawData) {
        Map<String, String> userCheck = rawData.asMap(String.class, String.class);

        user.verifyDetails(userCheck.get("username"), userCheck.get("firstName"), userCheck.get("lastName"));
    }

    @Given("^a user has authenticated with the API$")
    public void aUserHasAuthenticatedWithTheAPI() {
        user.authenticates("ITAG_USER", "password");
    }

    @When("^a case note is created for an existing offender booking:$")
    public void aCaseNoteIsCreatedForAnExistingOffenderBooking(DataTable rawData) {
        Map<String, String> caseNoteData = rawData.asMap(String.class, String.class);

        caseNote.setToken(user.getToken());
        caseNote.create(caseNoteData.get("type"), caseNoteData.get("subType"), caseNoteData.get("text"));
    }

    @Then("^case note is successfully created$")
    public void caseNoteIsSuccessfullyCreated() {
        caseNote.verify();
    }

    @Given("^I have created a case note text of \"([^\"]*)\"$")
    public void iHaveCreatedACaseNoteTextOf(String caseNoteText) throws Throwable {
        caseNote.setToken(user.getToken());
        caseNote.create("CNOTE", "GEN", caseNoteText);
    }

    @Given("^a case note has already been created and the case note is updated with text \"([^\"]*)\"$")
    public void aCaseNoteHasAlreadyBeenCreatedAndTheCaseNoteIsUpdatedWithText(String arg0) throws Throwable {

    }

    @Then("^case note is successfully updated$")
    public void caseNoteIsSuccessfullyUpdated() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("^the amended flag is set$")
    public void theAmendedFlagIsSet() throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }


    @TestConfiguration
    static class Config {
        @Bean
        public UserSteps user() {
            return new UserSteps();
        }

        @Bean
        public CaseNoteSteps caseNote() {
            return new CaseNoteSteps();
        }
    }
}
