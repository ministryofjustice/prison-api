package uk.gov.justice.hmpps.prison.aop.connectionproxy

import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.util.Assert
import uk.gov.justice.hmpps.kotlin.auth.AuthSource
import uk.gov.justice.hmpps.kotlin.auth.AuthSource.NOMIS
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import uk.gov.justice.hmpps.prison.util.MdcUtility.PROXY_USER
import java.sql.Connection
import java.sql.SQLException

@Aspect
class HsqlConnectionAspect(private val hmppsAuthenticationHolder: HmppsAuthenticationHolder) : AbstractConnectionAspect() {

  private val log = LoggerFactory.getLogger(this::class.java)

  @Throws(SQLException::class)
  override fun configureNomisConnection(pooledConnection: Connection): Connection = with(pooledConnection) {
    when {
      isNomisProxyUser() -> openAndConfigureProxySessionForConnection(this)
      else -> this
    }
  }

  @Throws(SQLException::class)
  private fun openAndConfigureProxySessionForConnection(pooledConnection: Connection): Connection {
    log.trace("Configuring Hsql Proxy Session.")
    openProxySessionForCurrentUsername(pooledConnection)
    return pooledConnection
  }

  @Throws(SQLException::class)
  private fun openProxySessionForCurrentUsername(pooledConnection: Connection) {
    // just check that the current user exists in the database
    pooledConnection.prepareStatement("SELECT username FROM staff_user_accounts WHERE username = ?")
      .use { statement ->
        val currentUsername = hmppsAuthenticationHolder.principal
        statement.setString(1, currentUsername)

        try {
          statement.executeQuery().use { Assert.isTrue(it.next(), "User $currentUsername not found") }
        } catch (e: SQLException) {
          log.error("User {} does not support Proxy Connection", currentUsername)
          pooledConnection.close()
          throw e
        }
        log.debug("Proxy Connection for {} Successful", currentUsername)
      }
  }

  private fun isNomisProxyUser(): Boolean = isProxyUser() && authSource() == NOMIS

  private fun isProxyUser(): Boolean = !MDC.get(PROXY_USER).isNullOrBlank()

  private fun authSource(): AuthSource = hmppsAuthenticationHolder.authSource
}
