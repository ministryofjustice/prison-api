package net.syscon.elite.web.config;

import net.syscon.elite.aop.connectionproxy.HsqlConnectionAspect;
import net.syscon.elite.security.AuthenticationFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;


@Profile("!connection-proxy")
@Configuration
@EnableAspectJAutoProxy
public class HsqlSimulatedProxyAopConfiguration {

    @Bean
    public HsqlConnectionAspect hsqlProxyConnectionAspect(final AuthenticationFacade authenticationFacade) {
        return new HsqlConnectionAspect(authenticationFacade);
    }
}
