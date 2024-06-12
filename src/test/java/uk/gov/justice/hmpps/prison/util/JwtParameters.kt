package uk.gov.justice.hmpps.prison.util

import java.time.Duration

class JwtParameters internal constructor(
  internal val username: String?,
  internal val scope: List<String>?,
  internal val grantType: String?,
  internal val roles: List<String>?,
  internal val expiryTime: Duration,
  internal val clientId: String,
  internal val internalUser: Boolean,
) {
  class JwtParametersBuilder {
    private var username: String? = null
    private var scope: List<String>? = null
    private var grantType: String? = null
    private var roles: List<String>? = null
    private var expiryTime = Duration.ofDays(1)
    private var clientId = "prison-api-client"
    private var internalUser = true

    fun username(username: String): JwtParametersBuilder {
      this.username = username
      return this
    }

    fun scope(scope: List<String>): JwtParametersBuilder {
      this.scope = scope
      return this
    }

    fun grantType(grantType: String): JwtParametersBuilder {
      this.grantType = grantType
      return this
    }

    fun roles(roles: List<String>): JwtParametersBuilder {
      this.roles = roles
      return this
    }

    fun expiryTime(expiryTime: Duration): JwtParametersBuilder {
      this.expiryTime = expiryTime
      return this
    }

    fun clientId(clientId: String): JwtParametersBuilder {
      this.clientId = clientId
      return this
    }

    fun internalUser(internalUser: Boolean): JwtParametersBuilder {
      this.internalUser = internalUser
      return this
    }

    fun build(): JwtParameters = JwtParameters(username, scope, grantType, roles, expiryTime, clientId, internalUser)

    override fun toString(): String =
      "JwtParameters.JwtParametersBuilder(username=$username, scope=$scope, grantType=$grantType, roles=$roles, expiryTime=$expiryTime, clientId=$clientId, internalUser=$internalUser)"
  }

  companion object {
    @JvmStatic
    fun builder(): JwtParametersBuilder = JwtParametersBuilder()
  }
}
