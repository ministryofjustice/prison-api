package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.springframework.context.annotation.Bean;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;

public class StubUserSecurityUtilsConfig {
    @Bean
    public AuthenticationFacade getUserSecurityUtils() {
        return new AuthenticationFacade();
    }
}
