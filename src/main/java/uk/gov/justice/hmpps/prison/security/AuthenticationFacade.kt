package uk.gov.justice.hmpps.prison.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.kotlin.auth.AuthAwareAuthenticationToken
import uk.gov.justice.hmpps.kotlin.auth.AuthSource

@Component
class AuthenticationFacade {
  val authentication: AuthAwareAuthenticationToken?
    get() = SecurityContextHolder.getContext().authentication as? AuthAwareAuthenticationToken

  val currentPrincipal: String?
    get() = authentication?.principal

  val currentRoles: Collection<GrantedAuthority?>?
    get() = authentication?.authorities

  val isClientOnly: Boolean
    get() = authentication?.isSystemClientCredentials() ?: false

  val clientId: String?
    get() = authentication?.clientId

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
