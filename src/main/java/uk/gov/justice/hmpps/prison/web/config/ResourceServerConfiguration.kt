package uk.gov.justice.hmpps.prison.web.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import uk.gov.justice.hmpps.kotlin.auth.dsl.ResourceServerConfigurationCustomizer

@Configuration
@EnableScheduling
@EnableAsync(proxyTargetClass = true)
class ResourceServerConfiguration {
  @Bean
  fun resourceServerCustomizer() = ResourceServerConfigurationCustomizer {
    unauthorizedRequestPaths {
      addPaths = setOf("/api/restore-info")
    }
  }
}
