package net.syscon.elite.executablespecification.steps;

import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.test.EliteClientException;
import net.syscon.elite.web.config.ClientConfigExtractor;
import net.syscon.elite.web.config.OauthClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.context.ApplicationContext;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD step implementations for API authentication.
 */
public class AuthenticationSteps {

    private static final String CLIENT_ID = "elite2apiclient";
    private static final String TRUSTED_CLIENT_ID = "elite2apitrustedclient";

    private final List<OauthClientConfig> clientConfigurations;

    @Value("${security.authentication.header:Authorization}")
    private String authenticationHeader;
    private OAuth2AccessToken token;

    @Autowired
    private ApplicationContext context;

    public AuthenticationSteps(String clientConfig, ClientConfigExtractor clientConfigExtractor) {
        clientConfigurations = clientConfigExtractor.getClientConfigurations(clientConfig);
    }

    public ErrorResponse authenticate(String username, String password, boolean clientCredentials) {
        if (clientCredentials) {
            return authenticate(clientCredentialsResource(TRUSTED_CLIENT_ID));
        } else {
            return authenticate(ownerPasswordResource(username, password, CLIENT_ID));
        }
    }

    public ErrorResponse authenticateAsClient(String clientId) {
        return authenticate(clientCredentialsResource(clientId));
    }

    public ErrorResponse refresh(OAuth2AccessToken currentToken) {
        try {
            BaseOAuth2ProtectedResourceDetails resource = ownerPasswordResource(null, null, CLIENT_ID);
            resource.setAccessTokenUri(accessTokenUri());

            ResourceOwnerPasswordAccessTokenProvider refresh = new ResourceOwnerPasswordAccessTokenProvider();
            token = refresh.refreshAccessToken(resource, currentToken.getRefreshToken(), new DefaultAccessTokenRequest());
            assertThat(token).isNotNull();
        } catch (Exception ex) {
            token = null;
            final ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setStatus(401);
        }
        return ErrorResponse.builder().status(HttpStatus.CREATED.value()).build();
    }

    private ErrorResponse authenticate(BaseOAuth2ProtectedResourceDetails resource) {
        resource.setAccessTokenUri(accessTokenUri());
        return authenticate(new OAuth2RestTemplate(resource, new DefaultOAuth2ClientContext(new DefaultAccessTokenRequest())));
    }

    private ErrorResponse authenticate(OAuth2RestTemplate oAuth2RestTemplate) {
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

    private String accessTokenUri() {
        return "http://localhost:" + getAccessTokenUriPort() + "/oauth/token";
    }

    private BaseOAuth2ProtectedResourceDetails ownerPasswordResource(String username, String password, String clientId) {
        ResourceOwnerPasswordResourceDetails resource = new ResourceOwnerPasswordResourceDetails();
        addClientConfiguration(resource, clientId);
        resource.setUsername(username);
        resource.setPassword(password);
        return resource;
    }

    private BaseOAuth2ProtectedResourceDetails clientCredentialsResource(String clientId) {
        ClientCredentialsResourceDetails resource = new ClientCredentialsResourceDetails();
        addClientConfiguration(resource, clientId);
//        resource.setScope(Arrays.asList("read", "admin"));
        return resource;
    }

    private void addClientConfiguration(BaseOAuth2ProtectedResourceDetails resource, String clientId) {
        OauthClientConfig client = findOauthClientConfig(clientId);
        resource.setClientId(client.getClientId());
        resource.setClientSecret(client.getClientSecret());
        resource.setScope(client.getScope());
    }

    private OauthClientConfig findOauthClientConfig(String clientId) {
        return clientConfigurations
                .stream()
                .filter(config -> config.getClientId().equals(clientId))
                .findFirst()
                .orElseThrow(new EntityNotFoundException("client"));
    }

    private int getAccessTokenUriPort() {
        return (((AnnotationConfigEmbeddedWebApplicationContext) context).getEmbeddedServletContainer()).getPort();
    }

    public OAuth2AccessToken getToken() {
        return token;
    }

    public String getAuthenticationHeader() {
        return authenticationHeader;
    }
}
