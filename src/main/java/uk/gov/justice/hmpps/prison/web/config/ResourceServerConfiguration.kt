package uk.gov.justice.hmpps.prison.web.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import uk.gov.justice.hmpps.kotlin.auth.dsl.ResourceServerConfigurationCustomizer
import uk.gov.justice.hmpps.prison.security.EntryPointUnauthorizedHandler

@Configuration
@EnableScheduling
@EnableAsync(proxyTargetClass = true)
class ResourceServerConfiguration {
  @Bean
  fun unauthorizedHandler(): EntryPointUnauthorizedHandler = EntryPointUnauthorizedHandler()

  @Bean
  fun resourceServerCustomizer() = ResourceServerConfigurationCustomizer {
    unauthorizedRequestPaths {
      addPaths = setOf(
        "/swagger-resources", "/swagger-resources/configuration/ui",
        "/swagger-resources/configuration/security", "/api/restore-info",
      )
    }
  }
}
