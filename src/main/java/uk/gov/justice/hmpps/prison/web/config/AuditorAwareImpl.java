package uk.gov.justice.hmpps.prison.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.stereotype.Service;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@Service(value = "auditorAware")
public class AuditorAwareImpl implements AuditorAware<String> {

    private final AuthenticationFacade userSecurityUtils;

    public AuditorAwareImpl(final AuthenticationFacade userSecurityUtils) {
        this.userSecurityUtils = userSecurityUtils;
    }

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.ofNullable(userSecurityUtils.getCurrentUsername());
    }
}
