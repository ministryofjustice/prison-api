package uk.gov.justice.hmpps.prison.aop.connectionproxy

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
    val auditModuleName = suppressXtagEvents?.let { "${if (suppressXtagEvents) MERGE else PRISON_API}" } ?: ""
    val userOrEmpty = user.orEmpty()
    val userOrModule = if (user.isEmail()) auditModuleName else userOrEmpty
    val ipOrEmpty = ipAddress.orEmpty()
    val appOrEmpty = app.orEmpty()
    val uriOrEmpty = requestUri.orEmpty()
    return """
      BEGIN
      nomis_context.set_context('AUDIT_MODULE_NAME', ?);
      nomis_context.set_context('AUDIT_USER_ID', ?);
      nomis_context.set_client_nomis_context(?, ?, ?, ?);
      END;
    """
      .trimIndent()
      .let { sql ->
        conn
          .prepareStatement(sql)
          .use { ps ->
            ps.setString(1, auditModuleName)
            ps.setString(2, userOrModule)
            ps.setString(3, userOrEmpty)
            ps.setString(4, ipOrEmpty)
            ps.setString(5, appOrEmpty)
            ps.setString(6, uriOrEmpty)
            ps.execute()
          }
      }
  }

  @Throws(SQLException::class)
  fun setSuppressXtagEvents(conn: Connection): Boolean = """
      BEGIN
      nomis_context.set_context('AUDIT_MODULE_NAME', 'MERGE');
      END;
    """
    .trimIndent()
    .let { sql -> conn.run(sql) }

  @Throws(SQLException::class)
  fun setDefaultSchema(conn: Connection) {
    if (defaultSchema.isNotBlank()) {
      conn.run("ALTER SESSION SET CURRENT_SCHEMA=$defaultSchema")
    }
  }
}

fun String?.isEmail() = this?.contains("@") == true
