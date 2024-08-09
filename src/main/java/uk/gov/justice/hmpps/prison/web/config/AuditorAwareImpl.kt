package uk.gov.justice.hmpps.prison.web.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import uk.gov.justice.hmpps.prison.aop.connectionproxy.isEmail
import java.util.Optional

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@Service(value = "auditorAware")
class AuditorAwareImpl(
  private val hmppsAuthenticationHolder: HmppsAuthenticationHolder,
  @Value("\${spring.datasource.username}") private val datasourceUsername: String,
) : AuditorAware<String> {
  override fun getCurrentAuditor(): Optional<String> = Optional.ofNullable(
    with(hmppsAuthenticationHolder.authenticationOrNull?.principal) {
      // don't write email addresses to the audit fields, only usernames
      // this will still mean some external users get written, but will mainly be nomis users
      if (isEmail()) datasourceUsername.uppercase() else this
    },
  )
}
