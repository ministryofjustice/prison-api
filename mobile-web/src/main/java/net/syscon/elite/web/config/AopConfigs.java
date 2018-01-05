package net.syscon.elite.web.config;

import net.syscon.elite.aop.AuthorisationAspect;
import net.syscon.elite.aop.LoggingAspect;
import net.syscon.elite.aop.OracleConnectionAspect;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.AgencyService;
import net.syscon.elite.service.BookingService;
import net.syscon.util.SQLProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableAspectJAutoProxy
public class AopConfigs {

    @Bean
    public LoggingAspect loggingAspect() {
        return new LoggingAspect();
    }

    @Bean
    public AuthorisationAspect authorisationAspect(BookingService bookingService, AgencyService agencyService) {
        return new AuthorisationAspect(bookingService, agencyService);
    }

    @Bean
	@Profile("connection-proxy")
	public OracleConnectionAspect oracleProxyConnectionAspect(AuthenticationFacade authenticationFacade,
                                                              SQLProvider sqlProvider,
                                                              @Value("${spring.datasource.url}") String jdbcUrl,
                                                              @Value("${spring.datasource.username}") String username,
                                                              @Value("${spring.datasource.password}") String password,
                                                              @Value("${oracle.tag.role.name}") String tagUser,
                                                              @Value("${oracle.default.schema}") String defaultSchema
                                                              ) {
        return new OracleConnectionAspect(authenticationFacade, sqlProvider, jdbcUrl, username, password, tagUser, defaultSchema);
	}
}
