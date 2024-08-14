package uk.gov.justice.hmpps.prison.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder;
import uk.gov.justice.hmpps.prison.aop.connectionproxy.HsqlConnectionAspect;

@Profile("!connection-proxy")
@Configuration
@EnableAspectJAutoProxy
public class HsqlSimulatedProxyAopConfiguration {

    @Bean
    public HsqlConnectionAspect hsqlProxyConnectionAspect(final HmppsAuthenticationHolder hmppsAuthenticationHolder) {
        return new HsqlConnectionAspect(hmppsAuthenticationHolder);
    }
}
