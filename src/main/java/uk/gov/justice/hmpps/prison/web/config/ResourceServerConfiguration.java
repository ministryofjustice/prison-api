package uk.gov.justice.hmpps.prison.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import uk.gov.justice.hmpps.prison.security.EntryPointUnauthorizedHandler;

@Configuration
@EnableScheduling
@EnableAsync(proxyTargetClass = true)
@EnableMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
@EnableWebSecurity
public class ResourceServerConfiguration{

    @Bean
    public EntryPointUnauthorizedHandler unauthorizedHandler() {
        return new EntryPointUnauthorizedHandler();
    }

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        http.headers().frameOptions().sameOrigin().and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                // Can't have CSRF protection as requires session
                .and().csrf().disable()
                .authorizeHttpRequests(auth ->
                    auth.requestMatchers("/webjars/**", "/favicon.ico", "/csrf",
                        "/health/**", "/info", "/ping", "/h2-console/**",
                        "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**",
                        "/swagger-resources", "/swagger-resources/configuration/ui",
                        "/swagger-resources/configuration/security", "/api/restore-info").permitAll()
                    .anyRequest().authenticated()
                ).oauth2ResourceServer()
                    .jwt().jwtAuthenticationConverter(new AuthAwareAuthenticationConverter());
        return http.build();
    }
}
