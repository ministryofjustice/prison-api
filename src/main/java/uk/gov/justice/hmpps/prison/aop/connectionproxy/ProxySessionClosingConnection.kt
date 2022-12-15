package uk.gov.justice.hmpps.prison.aop.connectionproxy

import oracle.jdbc.driver.OracleConnection
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.SQLException

class ProxySessionClosingConnection(private val connection: Connection) : Connection by connection {

  @Throws(SQLException::class)
  override fun close() {
    log.info("Closing proxy connection")
    (connection.unwrap(Connection::class.java) as OracleConnection)
      .also { oracleConnection ->
        closeSession(oracleConnection)
        oracleConnection.close(OracleConnection.PROXY_SESSION)
        connection.close()
      }
  }

  // TODO This appears to be redundant but we're currently scared as it's always been there. Needs reviewing.
  @Throws(SQLException::class)
  private fun closeSession(conn: Connection): Boolean = conn.run("BEGIN nomis_context.close_session(); END;")

  companion object {
    val log = LoggerFactory.getLogger(this::class.java)!!
  }
}
