package uk.gov.justice.hmpps.prison.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.kotlin.auth.AuthAwareAuthenticationToken
import uk.gov.justice.hmpps.kotlin.auth.AuthSource

@Component
class AuthenticationFacade {
  /**
   * This will return null if the token hasn't come from auth.  This is fine for application code, but tests need to
   * then use @WithMockAuthUser rather than using a TestingAuthenticationToken or @WithMockUser annotation.
   */
  val authentication: AuthAwareAuthenticationToken?
    get() = SecurityContextHolder.getContext().authentication as? AuthAwareAuthenticationToken

  /**
   * This is nullable since this can be called from an unprotected endpoint, but in the majority of cases it should
   * be not null.  This gets the current username from the authentication, falling back to the clientId if there
   * isn't a username passed in.
   */
  val currentPrincipal: String?
    get() = authentication?.principal

  val currentRoles: Collection<GrantedAuthority?>?
    get() = authentication?.authorities

  val isClientOnly: Boolean
    get() = authentication?.isSystemClientCredentials() ?: false

  val clientId: String?
    get() = authentication?.clientId

  /**
   * We are gradually moving away from user tokens and instead using client credentials more often.  This will be NONE
   * if client credentials are used, even if a NOMIS username is passed in.  This means that this method isn't an
   * adequate test to see if the user is a NOMIS user. The only test at present is to look the user up in the database
   * too see if they exist.
   */
  val authenticationSource: AuthSource
    get() = authentication?.authSource ?: AuthSource.NONE

  fun isOverrideRole(vararg overrideRoles: String): Boolean =
    hasMatchingRole(getRoles(*overrideRoles), authentication)

  companion object {
    fun hasRoles(vararg allowedRoles: String): Boolean =
      hasMatchingRole(getRoles(*allowedRoles), SecurityContextHolder.getContext().authentication)

    private fun hasMatchingRole(roles: List<String>, authentication: Authentication?): Boolean =
      authentication?.authorities?.any { roles.contains(it?.authority?.replaceFirst("ROLE_", "")) }
        ?: false

    private fun getRoles(vararg roles: String): List<String> = roles.map { it.replaceFirst("ROLE_", "") }
  }
}
