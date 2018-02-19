package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.test.EliteClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.BaseOAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for API authentication.
 */
public class AuthenticationSteps {


    @Value("${security.authentication.header:Authorization}")
    private String authenticationHeader;
    private OAuth2AccessToken token;

    @Value("${oauth.client.id}")
    private String clientId;

    @Value("${oauth.client.secret}")
    private String clientSecret;

    @Value("${oauth.standard.client.id}")
    private String standardClientId;

    @Value("${oauth.standard.client.secret}")
    private String standardClientSecret;

    @Autowired
    private ConfigurableApplicationContext context;

    protected BaseOAuth2ProtectedResourceDetails resource(String username, String password, boolean clientCredentials) {

        BaseOAuth2ProtectedResourceDetails resource;
        int port = (((AnnotationConfigEmbeddedWebApplicationContext)context).getEmbeddedServletContainer()).getPort();
        if (clientCredentials) {
            resource = new ClientCredentialsResourceDetails();
            resource.setClientId(clientId);
            resource.setClientSecret(clientSecret);
            resource.setScope(Arrays.asList("read", "admin"));

        } else {
            resource = new ResourceOwnerPasswordResourceDetails();
            resource.setClientId(standardClientId);
            resource.setClientSecret(standardClientSecret);
            ((ResourceOwnerPasswordResourceDetails)resource).setUsername(username);
            ((ResourceOwnerPasswordResourceDetails)resource).setPassword(password);
        }
        resource.setAccessTokenUri("http://localhost:"+ port + "/oauth/token");

        return resource;
    }

    public ErrorResponse authenticate(String username, String password, boolean clientCredentials) {

        DefaultAccessTokenRequest atr = new DefaultAccessTokenRequest();
        final OAuth2RestTemplate oAuth2RestTemplate = new OAuth2RestTemplate(resource(username, password, clientCredentials), new DefaultOAuth2ClientContext(atr));

        try {
            token = oAuth2RestTemplate.getAccessToken();

            assertThat(token).isNotNull();
        } catch (EliteClientException ex) {
            token = null;
            return ex.getErrorResponse();
        } catch (Exception ex) {
            token = null;
            final ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setStatus(401);
            return errorResponse;
        }
        return ErrorResponse.builder().status(HttpStatus.CREATED.value()).build();

    }

    public ErrorResponse refresh(OAuth2AccessToken currentToken) {
        ResourceOwnerPasswordAccessTokenProvider refresh = new ResourceOwnerPasswordAccessTokenProvider();
        DefaultAccessTokenRequest atr = new DefaultAccessTokenRequest();
        try {
            token = refresh.refreshAccessToken(resource(null, null,false), currentToken.getRefreshToken(), atr);
            assertThat(token).isNotNull();
        } catch (Exception ex) {
            token = null;
            final ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setStatus(401);
        }
        return ErrorResponse.builder().status(HttpStatus.CREATED.value()).build();
    }

    public OAuth2AccessToken getToken() {
        return token;
    }

    public String getAuthenticationHeader() {
        return authenticationHeader;
    }
}
