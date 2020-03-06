package net.syscon.elite.web.config;

import net.syscon.elite.security.EntryPointUnauthorizedHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableScheduling
@EnableAsync(proxyTargetClass = true)
public class ResourceServerConfiguration extends WebSecurityConfigurerAdapter {

    @Bean
    public EntryPointUnauthorizedHandler unauthorizedHandler() {
        return new EntryPointUnauthorizedHandler();
    }

    @Override
    public void configure(final HttpSecurity http) throws Exception {
        http.headers().frameOptions().sameOrigin().and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                // Can't have CSRF protection as requires session
                .and().csrf().disable()
                .authorizeRequests(auth ->
                    auth.antMatchers("/webjars/**", "/favicon.ico", "/csrf",
                        "/health", "/info", "/ping", "/health/ping", "/h2-console/**",
                        "/v2/api-docs", "/api/swagger.json",
                        "/swagger-ui.html", "/swagger-resources", "/swagger-resources/configuration/ui",
                        "/swagger-resources/configuration/security", "/auth/.well-known/jwks.json").permitAll()
                    .anyRequest().authenticated()
                ).oauth2ResourceServer()
                    .jwt().jwtAuthenticationConverter(new AuthAwareAuthenticationConverter());
    }
}
