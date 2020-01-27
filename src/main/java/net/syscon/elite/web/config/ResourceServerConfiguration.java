package net.syscon.elite.web.config;

import net.syscon.elite.security.EntryPointUnauthorizedHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;

@Configuration
@EnableResourceServer
@EnableScheduling
@EnableAsync(proxyTargetClass = true)
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    private final ResourceServerTokenServices tokenServices;

    public ResourceServerConfiguration(final ResourceServerTokenServices tokenServices) {
        this.tokenServices = tokenServices;
    }

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
                .authorizeRequests()
                .antMatchers("/webjars/**", "/favicon.ico", "/csrf",
                        "/health", "/info", "/ping", "/health/ping", "/h2-console/**",
                        "/v2/api-docs", "/api/swagger.json",
                        "/swagger-ui.html", "/swagger-resources", "/swagger-resources/configuration/ui",
                        "/swagger-resources/configuration/security").permitAll()
                .anyRequest()
                .authenticated();
    }

    @Override
    public void configure(final ResourceServerSecurityConfigurer config) {
        config.tokenServices(tokenServices);
    }
}
