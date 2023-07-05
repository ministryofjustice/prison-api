package uk.gov.justice.hmpps.prison.util

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
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
  private val keyPair: KeyPair

  init {
    val gen = KeyPairGenerator.getInstance("RSA")
    gen.initialize(2048)
    keyPair = gen.generateKeyPair()
  }

  @Bean
  @Primary
  fun jwtDecoder(): JwtDecoder = NimbusJwtDecoder.withPublicKey(keyPair.public as RSAPublicKey).build()

  fun createJwt(parameters: JwtParameters): String = with(parameters) {
    createJwt(
      username = username,
      scope = scope,
      roles = roles,
      expiryTime = expiryTime,
      clientId = clientId,
      internalUser = internalUser,
    )
  }

  fun createJwt(
    username: String? = null,
    scope: List<String>? = null,
    roles: List<String>? = null,
    expiryTime: Duration = Duration.ofDays(1),
    clientId: String = "prison-api-client",
    internalUser: Boolean = true,
  ): String {
    val claims = mutableMapOf<String, Any?>(
      "client_id" to clientId,
      "internalUser" to internalUser,
    ).apply {
      username?.let { this["user_name"] = username }
      roles?.let {
        // ensure that all roles have a ROLE_ prefix
        this["authorities"] = roles.map { "ROLE_${it.substringAfter("ROLE_")}" }
      }
      scope?.let { this["scope"] = scope }
    }
    return Jwts.builder()
      .setId(UUID.randomUUID().toString())
      .setSubject(username)
      .addClaims(claims)
      .setExpiration(Date(System.currentTimeMillis() + expiryTime.toMillis()))
      .signWith(keyPair.private, SignatureAlgorithm.RS256)
      .setHeaderParam("typ", "JWT")
      .setHeaderParam("kid", "dps-client-key")
      .compact()
  }
}
