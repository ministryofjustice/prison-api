package uk.gov.justice.hmpps.prison.web.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.SecurityFilterChain
import uk.gov.justice.hmpps.prison.security.EntryPointUnauthorizedHandler

@Configuration
@EnableScheduling
@EnableAsync(proxyTargetClass = true)
@EnableMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
@EnableWebSecurity
class ResourceServerConfiguration {
  @Bean
  fun unauthorizedHandler(): EntryPointUnauthorizedHandler = EntryPointUnauthorizedHandler()

  @Bean
  fun filterChain(http: HttpSecurity): SecurityFilterChain = http {
    headers { frameOptions { sameOrigin = true } }
    sessionManagement { sessionCreationPolicy = SessionCreationPolicy.STATELESS }
    // Can't have CSRF protection as requires session
    csrf { disable() }
    authorizeHttpRequests {
      listOf(
        "/webjars/**", "/favicon.ico", "/csrf",
        "/health/**", "/info", "/ping", "/h2-console/**",
        "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**",
        "/swagger-resources", "/swagger-resources/configuration/ui",
        "/swagger-resources/configuration/security", "/api/restore-info",
      ).forEach { authorize(it, permitAll) }
      authorize(anyRequest, authenticated)
    }
    oauth2ResourceServer { jwt { jwtAuthenticationConverter = AuthAwareAuthenticationConverter() } }
  }.let { http.build() }

  @Bean
  fun locallyCachedJwtDecoder(
    @Value("\${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") jwkSetUri: String,
    cacheManager: CacheManager,
  ): JwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).cache(cacheManager.getCache("jwks")).build()
}
