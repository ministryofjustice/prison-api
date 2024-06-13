package uk.gov.justice.hmpps.prison.web.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade
import java.util.*

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@Service(value = "auditorAware")
class AuditorAwareImpl(
  private val authenticationFacade: AuthenticationFacade,
  @Value("\${spring.datasource.username}") private val datasourceUsername: String,
) : AuditorAware<String> {
  override fun getCurrentAuditor(): Optional<String> = Optional.ofNullable(
    with(authenticationFacade.getCurrentUsername()) {
      // don't write email addresses to the audit fields, only usernames
      // this will still mean some external users get written, but will mainly be nomis users
      if (this?.contains("@") == true) datasourceUsername else this
    },
  )
}
