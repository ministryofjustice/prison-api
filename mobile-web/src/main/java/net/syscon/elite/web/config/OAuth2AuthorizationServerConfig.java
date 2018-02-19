package net.syscon.elite.web.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.util.Arrays;

@Configuration
@EnableAuthorizationServer
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class OAuth2AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private AuthenticationManager authManager;

    @Value("${jwt.signing.key}")
    private String privateKey;

    @Value("${jwt.expiration.seconds}")
    private int expirationSeconds;

    @Value("${jwt.refresh.expiration.seconds}")
    private int refreshExpirationSeconds;

    @Value("${oauth.client.id}")
    private String clientId;

    @Value("${oauth.client.secret}")
    private String clientSecret;

    @Value("${oauth.standard.client.id}")
    private String standardClientId;

    @Value("${oauth.standard.client.secret}")
    private String standardClientSecret;

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
        defaultTokenServices.setAccessTokenValiditySeconds(expirationSeconds);
        defaultTokenServices.setRefreshTokenValiditySeconds(refreshExpirationSeconds);
        defaultTokenServices.setAuthenticationManager(authManager);
        return defaultTokenServices;
    }


    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {

        clients.inMemory()
                .withClient(clientId).secret(clientSecret)
                .authorities("ROLE_SYSTEM_USER")
                .authorizedGrantTypes("client_credentials", "refresh_token")
                .scopes("read", "admin")
                .autoApprove("read")
        .and()
                .withClient(standardClientId).secret(standardClientSecret)
                .authorizedGrantTypes("password", "authorization_code", "refresh_token")
                .scopes("read", "write")
                .autoApprove("read", "write");
    }

}
