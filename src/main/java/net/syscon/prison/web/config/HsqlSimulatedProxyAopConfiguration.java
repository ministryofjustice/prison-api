package net.syscon.prison.web.config;

import net.syscon.prison.aop.connectionproxy.HsqlConnectionAspect;
import net.syscon.prison.security.AuthenticationFacade;
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
