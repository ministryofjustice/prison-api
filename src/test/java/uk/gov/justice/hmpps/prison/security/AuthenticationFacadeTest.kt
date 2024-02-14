package uk.gov.justice.hmpps.prison.security

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mockito.mock
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import uk.gov.justice.hmpps.kotlin.auth.AuthAwareAuthenticationToken
import uk.gov.justice.hmpps.kotlin.auth.AuthSource
import uk.gov.justice.hmpps.kotlin.auth.AuthSource.AUTH
import uk.gov.justice.hmpps.kotlin.auth.AuthSource.NOMIS
import uk.gov.justice.hmpps.kotlin.auth.AuthSource.NONE

class AuthenticationFacadeTest {
  private val authenticationFacade = AuthenticationFacade()

  @Test
  fun getAuthenticationSource_AuthSource_nomis() {
    setAuthentication(NOMIS)
    assertThat(authenticationFacade.authenticationSource).isEqualTo(NOMIS)
  }

  @Test
  fun getProxyUserAuthenticationSource_AuthSource_auth() {
    setAuthentication(AUTH)
    assertThat(authenticationFacade.authenticationSource).isEqualTo(AUTH)
  }

  @Test
  fun proxyUserAuthenticationSource_NoUserAuthentication() {
    SecurityContextHolder.getContext().authentication = null
    assertThat(authenticationFacade.authenticationSource).isEqualTo(NONE)
  }

  @ParameterizedTest
  @CsvSource("ROLE_SYSTEM_USER,true", "SYSTEM_USER,true", "SYSTEMUSER,false")
  fun hasRolesTest(role: String, expected: Boolean) {
    setAuthentication(
      AUTH,
      setOf<GrantedAuthority>(
        SimpleGrantedAuthority("ROLE_SYSTEM_USER"),
      ),
    )
    assertThat(AuthenticationFacade.hasRoles(role)).isEqualTo(expected)
    assertThat(authenticationFacade.isOverrideRole(role)).isEqualTo(expected)
    assertThat(authenticationFacade.isClientOnly).isFalse
  }

  @ParameterizedTest
  @CsvSource("ROLE_SYSTEM_USER,true", "SYSTEM_USER,true", "SYSTEMUSER,false")
  fun hasClientRolesTest(role: String, expected: Boolean) {
    setAuthentication(
      AUTH,
      setOf<GrantedAuthority>(
        SimpleGrantedAuthority("ROLE_SYSTEM_USER"),
      ),
      userName = null,
    )
    assertThat(AuthenticationFacade.hasRoles(role)).isEqualTo(expected)
    assertThat(authenticationFacade.isOverrideRole(role)).isEqualTo(expected)
    assertThat(authenticationFacade.isClientOnly).isTrue
  }

  private fun setAuthentication(source: AuthSource) {
    setAuthentication(source, emptySet())
  }

  private fun setAuthentication(source: AuthSource, authoritySet: Set<GrantedAuthority>, userName: String? = "userName") {
    val auth: Authentication = AuthAwareAuthenticationToken(mock(Jwt::class.java), "clientId", userName, source, authoritySet)
    SecurityContextHolder.getContext().authentication = auth
  }

  @Test
  fun isOverrideRole_NoOverrideRoleSet() {
    assertThat(authenticationFacade.isOverrideRole()).isFalse()
  }

  @Test
  fun hasRoles_NoAllowedRoleSet() {
    assertThat(AuthenticationFacade.hasRoles()).isFalse()
  }

  @Test
  fun getClientId() {
    setAuthentication(NONE)
    assertThat(authenticationFacade.clientId).isEqualTo("clientId")
  }
}
