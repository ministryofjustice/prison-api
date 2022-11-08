package uk.gov.justice.hmpps.prison.aop.connectionproxy

import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.SQLException

class ResettableContextConnection(private val connection: Connection) : Connection by connection {

  @Throws(SQLException::class)
  override fun close() {
    log.debug("Closing context clearing connection {}", this)
    resetContext()
    connection.close()
  }

  @Throws(SQLException::class)
  fun resetContext(): Boolean = connection.run("BEGIN nomis_context.set_nomis_context(); END;")

  companion object {
    val log = LoggerFactory.getLogger(this::class.java)!!
  }
}
