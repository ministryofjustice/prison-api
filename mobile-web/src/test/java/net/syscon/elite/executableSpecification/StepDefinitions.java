package net.syscon.elite.executableSpecification;

import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.syscon.elite.test.DatasourceActiveProfilesResolver;
import net.syscon.elite.web.api.model.AuthLogin;
import net.syscon.elite.web.api.model.Token;
import net.syscon.elite.web.api.model.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ActiveProfiles(resolver = DatasourceActiveProfilesResolver.class)
@SuppressWarnings("SpringJavaAutowiringInspection")
@ContextConfiguration
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class StepDefinitions {

    @Autowired
    private ApplicationContext context;

    @Value("${security.authenication.header:Authorization}")
    private String authenicationHeader;

    @Autowired
    private TestRestTemplate restTemplate;

    private String token;


    @When("^I call the login endpoint with the following credentials:$")
    public void iCallTheLoginEndpointWithTheFollowingCredentials(DataTable rawData) throws Throwable {
        final Map<String, String> loginCredentials = rawData.asMap(String.class, String.class);

        AuthLogin credentials = new AuthLogin(loginCredentials.get("username"), loginCredentials.get("password"));

        final ResponseEntity<Token> response = restTemplate.exchange("/api/users/login", HttpMethod.POST, createEntity(credentials, null), Token.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        token = response.getBody().getToken();
    }


    @Then("^I receive a JWT token response$")
    public void iReceiveAJWTTokenResponse() throws Throwable {
        assertThat(token).isNotEmpty();
    }


    @And("^I when I lookup my details I get the following data:$")
    public void iWhenILookupMyDetailsIGetTheFollowingData(DataTable rawData) throws Throwable {
        final ResponseEntity<UserDetails> response = restTemplate.exchange("/api/users/me", HttpMethod.GET, createEntity(null, null), UserDetails.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        final Map<String, String> userCheck = rawData.asMap(String.class, String.class);
        final UserDetails responseBody = response.getBody();
        assertThat(responseBody).hasFieldOrPropertyWithValue("username", userCheck.get("username"));
        assertThat(responseBody).hasFieldOrPropertyWithValue("firstName", userCheck.get("firstName"));
        assertThat(responseBody).hasFieldOrPropertyWithValue("lastName", userCheck.get("lastName"));
    }


    private HttpEntity createEntity(Object entity, Map<String, String> extraHeaders) {
        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.add(authenicationHeader, token);
        }
        if (extraHeaders != null) {
            extraHeaders.forEach(headers::add);
        }
        return new HttpEntity<>(entity, headers);
    }
}
