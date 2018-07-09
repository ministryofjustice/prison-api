package net.syscon.elite.web.config;

import net.syscon.elite.security.ApiAuthenticationProvider;
import net.syscon.elite.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;

@Configuration
public class AuthenticationManagerConfiguration extends GlobalAuthenticationConfigurerAdapter {

    @Autowired
    private UserDetailsServiceImpl userService;

    @Override
    public void init(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authenticationProvider()).authenticationProvider(preAuthProvider());
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new ApiAuthenticationProvider();
        provider.setUserDetailsService(userService);
        return provider;
    }

    @Bean
    public PreAuthenticatedAuthenticationProvider preAuthProvider() {
        PreAuthenticatedAuthenticationProvider preAuth = new PreAuthenticatedAuthenticationProvider();
        preAuth.setPreAuthenticatedUserDetailsService(userService);
        return preAuth;
    }

}
