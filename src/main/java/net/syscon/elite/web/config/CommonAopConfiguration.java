package net.syscon.elite.web.config;

import net.syscon.elite.aop.AuthorisationAspect;
import net.syscon.elite.aop.LoggingAspect;
import net.syscon.elite.aop.RequestAspect;
import net.syscon.elite.service.AgencyService;
import net.syscon.elite.service.BookingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration()
@EnableAspectJAutoProxy
public class CommonAopConfiguration {

    @Bean
    public LoggingAspect loggingAspect() {
        return new LoggingAspect();
    }

    @Bean
    public RequestAspect requestAspect() {
        return new RequestAspect();
    }

    @Bean
    public AuthorisationAspect authorisationAspect(BookingService bookingService, AgencyService agencyService) {
        return new AuthorisationAspect(bookingService, agencyService);
    }

}
