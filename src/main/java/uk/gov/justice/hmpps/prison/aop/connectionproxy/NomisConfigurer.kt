package uk.gov.justice.hmpps.prison.aop.connectionproxy

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.aop.connectionproxy.AppModuleName.MERGE
import uk.gov.justice.hmpps.prison.aop.connectionproxy.AppModuleName.PRISON_API
import java.sql.Connection
import java.sql.SQLException

@Service
class NomisConfigurer(@Value("\${oracle.default.schema}") private val defaultSchema: String) {

  @Throws(SQLException::class)
  fun setNomisContext(
    conn: Connection,
    user: String? = null,
    ipAddress: String? = null,
    requestUri: String? = null,
    app: String? = null,
    suppressXtagEvents: Boolean? = null,
  ): Boolean {
    val auditModuleName = suppressXtagEvents?.let { "'${if (suppressXtagEvents) MERGE else PRISON_API }'" } ?: "''"
    fun String?.quotesOrEmpty() = this?.let { "'$this'" } ?: "''"
    val userOrEmpty = user.quotesOrEmpty()
    val ipOrEmpty = ipAddress.quotesOrEmpty()
    val appOrEmpty = app.quotesOrEmpty()
    val uriOrEmpty = requestUri.quotesOrEmpty()
    return """
      BEGIN
      nomis_context.set_context('AUDIT_MODULE_NAME', $auditModuleName);
      nomis_context.set_context('AUDIT_USER_ID', $userOrEmpty);
      nomis_context.set_client_nomis_context($userOrEmpty, $ipOrEmpty, $appOrEmpty, $uriOrEmpty);
      END;
    """
      .trimIndent()
      .also { sql -> log.info("Setting NOMIS context to: $sql") }
      .let { sql -> conn.run(sql) }
  }

  @Throws(SQLException::class)
  fun setSuppressXtagEvents(conn: Connection): Boolean {
    return """
      BEGIN
      nomis_context.set_context('AUDIT_MODULE_NAME', 'MERGE');
      END;
    """
      .trimIndent()
      .also { sql -> log.info("Setting NOMIS context to: $sql") }
      .let { sql -> conn.run(sql) }
  }

  @Throws(SQLException::class)
  fun setDefaultSchema(conn: Connection) {
    if (defaultSchema.isNotBlank()) {
      conn.run("ALTER SESSION SET CURRENT_SCHEMA=$defaultSchema")
    }
  }

  companion object {
    val log = LoggerFactory.getLogger(this::class.java)!!
  }
}
