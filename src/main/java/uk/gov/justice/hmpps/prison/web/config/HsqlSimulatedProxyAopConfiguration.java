package uk.gov.justice.hmpps.prison.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import uk.gov.justice.hmpps.prison.aop.connectionproxy.HsqlConnectionAspect;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;


@Profile("!connection-proxy")
@Configuration
@EnableAspectJAutoProxy
public class HsqlSimulatedProxyAopConfiguration {

    @Bean
    public HsqlConnectionAspect hsqlProxyConnectionAspect(final AuthenticationFacade authenticationFacade) {
        return new HsqlConnectionAspect(authenticationFacade);
    }
}
