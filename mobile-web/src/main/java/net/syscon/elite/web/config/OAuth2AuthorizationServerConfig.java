package net.syscon.elite.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.config.annotation.builders.ClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.builders.InMemoryClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableAuthorizationServer
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
@Slf4j
public class OAuth2AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    private final String privateKey;
    private final List<OauthClientConfig> oauthClientConfig;

    @Autowired
    private ClientDetailsService clientDetailsService;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    public OAuth2AuthorizationServerConfig(@Value("${jwt.signing.key}") String privateKey,
                                           ClientConfigExtractor clientConfigExtractor,
                                           @Value("${oauth.client.data}") String clientData) {
        this.privateKey = privateKey;
        this.oauthClientConfig = clientConfigExtractor.getClientConfigurations(clientData);
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey(privateKey);
        return converter;

    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
            oauthServer
                .tokenKeyAccess("isAnonymous() || hasRole('ROLE_SYSTEM_USER')") // permitAll()
                .checkTokenAccess("hasRole('SYSTEM_USER')"); // isAuthenticated()
    }

    @Bean
    public TokenEnhancer jwtTokenEnhancer() {
        return new JWTTokenEnhancer();
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
                .tokenStore(tokenStore())
                .accessTokenConverter(accessTokenConverter())
                .tokenEnhancer(tokenEnhancerChain())
                .authenticationManager(authManager)
                .tokenServices(tokenServices());

    }

    @Bean
    public TokenEnhancerChain tokenEnhancerChain() {
        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(Arrays.asList(jwtTokenEnhancer(), accessTokenConverter()));
        return tokenEnhancerChain;
    }

    @Bean
    @Primary
    public DefaultTokenServices tokenServices() {
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenEnhancer(tokenEnhancerChain());
        defaultTokenServices.setTokenStore(tokenStore());
        defaultTokenServices.setReuseRefreshToken(true);
        defaultTokenServices.setSupportRefreshToken(true);
        defaultTokenServices.setAuthenticationManager(authManager);
        defaultTokenServices.setClientDetailsService(clientDetailsService);
        return defaultTokenServices;
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {

        if (oauthClientConfig != null) {
            ClientDetailsServiceBuilder<InMemoryClientDetailsServiceBuilder>.ClientBuilder clientBuilder = null;
            for (OauthClientConfig client : oauthClientConfig) {
                if (clientBuilder == null) {
                    clientBuilder = clients.inMemory().withClient(client.getClientId());
                } else {
                    clientBuilder = clientBuilder.and().withClient(client.getClientId());
                }
                log.info("Initialising OAUTH2 Client ID {}", client.getClientId());
                clientBuilder = clientBuilder
                        .secret(client.getClientSecret())
                        .accessTokenValiditySeconds(client.getAccessTokenValidity())
                        .refreshTokenValiditySeconds(client.getRefreshTokenValidity())
                        .redirectUris(client.getWebServerRedirectUri());

                if (client.getScope() != null) {
                    clientBuilder = clientBuilder.scopes(toArray(client.getScope()));
                }
                if (client.getAutoApprove() != null) {
                    clientBuilder = clientBuilder.autoApprove(toArray(client.getAutoApprove()));
                }
                if (client.getAuthorities() != null) {
                    clientBuilder = clientBuilder.authorities(toArray(client.getAuthorities()));
                }
                if (client.getAuthorizedGrantTypes() != null) {
                    clientBuilder = clientBuilder.authorizedGrantTypes(toArray(client.getAuthorizedGrantTypes()));
                }
            }
        }
    }

    private String[] toArray(List<String> array) {
        if (array != null) {
            return array.toArray(new String[array.size()]);
        }
        return null;
    }

}
