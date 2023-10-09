package uk.gov.justice.hmpps.prison.security

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mockito
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import uk.gov.justice.hmpps.prison.exception.MissingRoleCheckException
import uk.gov.justice.hmpps.prison.web.config.AuthAwareAuthenticationToken

class AuthenticationFacadeTest {
  private val authenticationFacade = AuthenticationFacade()

  @Test
  fun getAuthenticationSource_AuthSource_nomis() {
    setAuthentication("nomis")
    assertThat(authenticationFacade.authenticationSource).isEqualTo(AuthSource.NOMIS)
  }

  @Test
  fun getProxyUserAuthenticationSource_AuthSource_auth() {
    setAuthentication("auth")
    assertThat(authenticationFacade.authenticationSource).isEqualTo(AuthSource.AUTH)
  }

  @Test
  fun getProxyUserAuthenticationSource_AuthSource_null() {
    setAuthentication(null)
    assertThat(authenticationFacade.authenticationSource).isEqualTo(AuthSource.NONE)
  }

  @Test
  fun proxyUserAuthenticationSource_NoUserAuthentication() {
    SecurityContextHolder.getContext().authentication = null
    assertThat(authenticationFacade.authenticationSource).isEqualTo(AuthSource.NONE)
  }

  @ParameterizedTest
  @CsvSource("ROLE_SYSTEM_USER,true", "SYSTEM_USER,true", "SYSTEMUSER,false")
  fun hasRolesTest(role: String?, expected: Boolean) {
    setAuthentication(
      "auth",
      java.util.Set.of<GrantedAuthority>(
        SimpleGrantedAuthority("ROLE_SYSTEM_USER"),
      ),
    )
    assertThat(AuthenticationFacade.hasRoles(role)).isEqualTo(expected)
    assertThat(authenticationFacade.isOverrideRole(role)).isEqualTo(expected)
  }

  private fun setAuthentication(source: String?) {
    setAuthentication(source, emptySet())
  }

  private fun setAuthentication(source: String?, authoritySet: Set<GrantedAuthority>) {
    val auth: Authentication = AuthAwareAuthenticationToken(Mockito.mock(Jwt::class.java), "client", source, authoritySet)
    SecurityContextHolder.getContext().authentication = auth
  }

  @Test
  fun isOverrideRole_NoOverrideRoleSet() {
    Assertions.assertThatThrownBy { authenticationFacade.isOverrideRole() }
      .isInstanceOf(MissingRoleCheckException::class.java)
      .hasMessage("Authentication override role is missing")
  }

  @Test
  fun hasRoles_NoAllowedRoleSet() {
    Assertions.assertThatThrownBy { AuthenticationFacade.hasRoles() }
      .isInstanceOf(MissingRoleCheckException::class.java)
      .hasMessage("Authentication override role is missing")
  }
}
