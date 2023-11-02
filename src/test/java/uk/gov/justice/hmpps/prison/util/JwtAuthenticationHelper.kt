package uk.gov.justice.hmpps.prison.util

import io.jsonwebtoken.Jwts
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey
import java.time.Duration
import java.util.Date
import java.util.UUID

@Configuration
class JwtAuthenticationHelper {
  private val keyPair: KeyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()

  @Bean
  @Primary
  fun jwtDecoder(): JwtDecoder = NimbusJwtDecoder.withPublicKey(keyPair.public as RSAPublicKey).build()

  fun createJwt(parameters: JwtParameters): String = with(parameters) {
    createJwt(
      username = username,
      scope = scope,
      grantType = grantType,
      roles = roles,
      expiryTime = expiryTime,
      clientId = clientId,
      internalUser = internalUser,
    )
  }

  fun createJwt(
    username: String? = null,
    scope: List<String>? = null,
    grantType: String? = null,
    roles: List<String>? = null,
    expiryTime: Duration = Duration.ofDays(1),
    clientId: String = "prison-api-client",
    internalUser: Boolean = true,
  ): String {
    val claims = mutableMapOf<String, Any?>(
      "client_id" to clientId,
      "internalUser" to internalUser,
      "sub" to (username ?: clientId),
    ).apply {
      username?.let { this["user_name"] = username }
      roles?.let {
        // ensure that all roles have a ROLE_ prefix
        this["authorities"] = roles.map { "ROLE_${it.substringAfter("ROLE_")}" }
      }
      scope?.let { this["scope"] = scope }
      grantType?.let { this["grant_type"] = grantType }
    }
    return Jwts.builder()
      .id(UUID.randomUUID().toString())
      .claims(claims)
      .expiration(Date(System.currentTimeMillis() + expiryTime.toMillis()))
      .signWith(keyPair.private, Jwts.SIG.RS256)
      .header().add("typ", "JWT").add("kid", "dps-client-key").and()
      .compact()
  }
}
