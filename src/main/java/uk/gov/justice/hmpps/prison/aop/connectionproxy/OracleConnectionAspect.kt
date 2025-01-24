package uk.gov.justice.hmpps.prison.aop.connectionproxy

import oracle.jdbc.driver.OracleConnection
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.TransactionException
import uk.gov.justice.hmpps.kotlin.auth.AuthSource
import uk.gov.justice.hmpps.kotlin.auth.AuthSource.NOMIS
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import uk.gov.justice.hmpps.prison.util.MdcUtility.IP_ADDRESS
import uk.gov.justice.hmpps.prison.util.MdcUtility.PROXY_USER
import uk.gov.justice.hmpps.prison.util.MdcUtility.REQUEST_URI
import uk.gov.justice.hmpps.prison.util.MdcUtility.SUPPRESS_XTAG_EVENTS
import uk.gov.justice.hmpps.prison.util.MdcUtility.USER_ID_HEADER
import uk.gov.justice.hmpps.prison.web.config.RoutingDataSource
import java.lang.Exception
import java.sql.Connection
import java.sql.SQLException
import java.util.Properties

@Aspect
@Component
@Profile("connection-proxy")
class OracleConnectionAspect(
  private val hmppsAuthenticationHolder: HmppsAuthenticationHolder,
  private val roleConfigurer: RoleConfigurer,
  private val nomisConfigurer: NomisConfigurer,
) : AbstractConnectionAspect() {

  private val log = LoggerFactory.getLogger(this::class.java)

  @Throws(SQLException::class, ProxyConnectionTransactionException::class)
  public override fun configureNomisConnection(pooledConnection: Connection): Connection = with(pooledConnection) {
    when {
      isNomisProxyUser() -> openProxySessionConnection()
      isProxyUser() -> openResettableContextConnection()
      isSuppressXTags() -> openXtagsSuppressingConnection()
      else -> this.also { nomisConfigurer.setDefaultSchema(it) }
    }
  }

  @Throws(SQLException::class)
  private fun Connection.openProxySessionConnection(): Connection {
    log.debug("Configuring Oracle Proxy Session")
    assertNotSlow()
    openProxySessionForCurrentUsername()
      .also { oracleConnection -> roleConfigurer.setRoleForConnection(oracleConnection) }
    return ProxySessionClosingConnection(this)
      .also { proxySessionConnection ->
        nomisConfigurer.setDefaultSchema(proxySessionConnection)
        nomisConfigurer.setNomisContext(proxySessionConnection, mdc(USER_ID_HEADER), mdc(IP_ADDRESS), mdc(REQUEST_URI), "APP", isSuppressXTags())
      }
  }

  @Throws(SQLException::class)
  private fun Connection.openProxySessionForCurrentUsername(): OracleConnection {
    val currentUsername = hmppsAuthenticationHolder.principal
    val info = Properties().apply {
      this[OracleConnection.PROXY_USER_NAME] = currentUsername
    }

    return (this.unwrap(Connection::class.java) as OracleConnection)
      .apply {
        try {
          openProxySession(OracleConnection.PROXYTYPE_USER_NAME, info)
        } catch (e: SQLException) {
          if (e.errorCode == 1017) throw ProxyConnectionTransactionException("Could not open proxy session as $currentUsername - account doesn't support proxy connections", e)
          if (e.errorCode == 28000) throw ProxyConnectionTransactionException("Could not open proxy session as $currentUsername - account is locked", e)
          throw e
        }
      }
  }

  @Throws(SQLException::class)
  private fun Connection.openResettableContextConnection(): Connection {
    log.info("Configuring session for client credentials user")
    assertNotSlow()
    return ResettableContextConnection(this)
      .also { connection ->
        nomisConfigurer.setDefaultSchema(connection)
        nomisConfigurer.setNomisContext(connection, mdc(USER_ID_HEADER), mdc(IP_ADDRESS), mdc(REQUEST_URI), "APP", isSuppressXTags())
      }
  }

  @Throws(SQLException::class)
  private fun Connection.openXtagsSuppressingConnection(): Connection {
    log.debug("Configuring session to suppress XTag events")
    assertNotSlow()
    return ResettableContextConnection(this)
      .also { connection ->
        nomisConfigurer.setDefaultSchema(connection)
        nomisConfigurer.setSuppressXtagEvents(connection)
      }
  }

  private fun assertNotSlow() {
    if (RoutingDataSource.isReplica()) {
      throw RuntimeException("Found a non read-only transaction annotated with @SlowReportQuery")
    }
  }

  private fun isNomisProxyUser(): Boolean = isProxyUser() && authSource() == NOMIS

  private fun isProxyUser(): Boolean = !mdc(PROXY_USER).isNullOrBlank()

  private fun isSuppressXTags(): Boolean = "true" == MDC.get(SUPPRESS_XTAG_EVENTS)

  private fun authSource(): AuthSource = hmppsAuthenticationHolder.authSource

  private fun mdc(key: String): String? = MDC.get(key)
}

@Throws(SQLException::class)
internal fun Connection.run(sql: String) = prepareStatement(sql).use { ps -> ps.execute() }

class ProxyConnectionTransactionException(message: String, e: Exception) : TransactionException(message, e)
