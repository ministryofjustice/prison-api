package uk.gov.justice.hmpps.prison.web.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import uk.gov.justice.hmpps.prison.security.AuthSource

class AuthAwareAuthenticationConverterTest {
  private val authenticationConverter = AuthAwareAuthenticationConverter()
  private val jwt: Jwt = mock()

  @Test
  fun convert_basicUserAttributes_attributesCopied() {
    whenever(jwt.claims).thenReturn(claims("some_user", "auth", "ROLE_some", null))
    val authToken = authenticationConverter.convert(jwt)
    assertThat(authToken!!.principal).isEqualTo("some_user")
    assertThat(authToken.authSource).isEqualTo(AuthSource.AUTH)
    assertThat(authToken.authorities).containsExactlyInAnyOrder(SimpleGrantedAuthority("ROLE_some"))
    assertThat(authToken.isClientOnly).isFalse()
  }

  @Test
  fun convert_basicClientAttributes_attributesCopied() {
    whenever(jwt.claims).thenReturn(claims(null, "auth", "ROLE_some", "client-details"))
    whenever(jwt.subject).thenReturn("client-details")
    val authToken = authenticationConverter.convert(jwt)
    assertThat(authToken!!.principal).isEqualTo(null)
    assertThat(authToken.authSource).isEqualTo(AuthSource.AUTH)
    assertThat(authToken.authorities).containsExactlyInAnyOrder(SimpleGrantedAuthority("ROLE_some"))
    assertThat(authToken.isClientOnly).isTrue()
  }

  @Test
  fun convert_missingUserName_principalIsNull() {
    whenever(jwt.claims).thenReturn(claims(null, "some_auth_source", "ROLE_some", null))
    val authToken = authenticationConverter.convert(jwt)
    assertThat(authToken!!.principal).isEqualTo(null)
    assertThat(authToken.authSource).isEqualTo(AuthSource.NONE)
    assertThat(authToken.authorities).containsExactlyInAnyOrder(SimpleGrantedAuthority("ROLE_some"))
    assertThat(authToken.isClientOnly).isFalse()
  }

  @Test
  fun convert_missingAuthSource_authSourceIsNone() {
    whenever(jwt.claims).thenReturn(claims("some_user", null, "ROLE_some", null))
    val authToken = authenticationConverter.convert(jwt)
    assertThat(authToken!!.authSource).isEqualTo(AuthSource.NONE)
  }

  @Test
  fun convert_missingAuthorities_noGrantedAuthority() {
    whenever(jwt.claims).thenReturn(claims("some_user", "some_auth_source", null, null))
    val authToken = authenticationConverter.convert(jwt)
    assertThat(authToken!!.authorities).isEmpty()
  }

  private fun claims(username: String?, authSource: String?, scope: String?, clientId: String?): Map<String, Any> {
    val claims: MutableMap<String, Any> = HashMap()
    if (!username.isNullOrBlank()) {
      claims["user_name"] = username
    }
    if (!authSource.isNullOrBlank()) {
      claims["auth_source"] = authSource
    }
    if (!scope.isNullOrBlank()) {
      claims["authorities"] = setOf(scope)
    }
    if (!clientId.isNullOrBlank()) {
      claims["client_id"] = clientId
    }
    return claims
  }
}
