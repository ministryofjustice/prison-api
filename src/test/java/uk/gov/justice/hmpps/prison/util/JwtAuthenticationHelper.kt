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

  fun createJwt(parameters: JwtParameters): String {
    val claims = HashMap<String, Any>()
    if (parameters.username != null) {
      claims["user_name"] = parameters.username
    }
    claims["client_id"] = parameters.clientId
    claims["internalUser"] = parameters.isInternalUser
    if (parameters.roles != null && parameters.roles.isNotEmpty()) claims["authorities"] = parameters.roles
    if (parameters.scope != null && parameters.scope.isNotEmpty()) claims["scope"] = parameters.scope
    return Jwts.builder()
      .setId(UUID.randomUUID().toString())
      .setSubject(parameters.username)
      .addClaims(claims)
      .setExpiration(Date(System.currentTimeMillis() + parameters.expiryTime.toMillis()))
      .signWith(keyPair.private, SignatureAlgorithm.RS256)
      .setHeaderParam("typ", "JWT")
      .setHeaderParam("kid", "dps-client-key")
      .compact()
  }
}
