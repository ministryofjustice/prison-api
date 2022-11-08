package uk.gov.justice.hmpps.prison.aop.connectionproxy

import oracle.jdbc.driver.OracleConnection
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.sql.Connection
import java.sql.PreparedStatement

class ProxySessionClosingConnectionTest {

  private val connection = mock<Connection>()
  private val proxySessionConnection = ProxySessionClosingConnection(connection)
  private val oracleConnection = mock<OracleConnection>()
  private val oraclePreparedStatement = mock<PreparedStatement>()

  @Nested
  inner class Close {
    @BeforeEach
    fun `set up mocks`() {
      whenever(connection.unwrap(Connection::class.java)).thenReturn(oracleConnection)
      whenever(oracleConnection.prepareStatement(anyString())).thenReturn(oraclePreparedStatement)
    }

    @Test
    fun `closes session`() {
      proxySessionConnection.close()

      verify(oracleConnection).prepareStatement("BEGIN nomis_context.close_session(); END;")
      verify(oraclePreparedStatement).execute()
    }

    @Test
    fun `closes oracle connection`() {
      proxySessionConnection.close()

      verify(oracleConnection).close(OracleConnection.PROXY_SESSION)
    }

    @Test
    fun `closes connection`() {
      proxySessionConnection.close()

      verify(connection).close()
    }
  }

  @Nested
  inner class Delegates {
    @Test
    fun `delegates to the local connection`() {
      proxySessionConnection.commit()

      verify(connection).commit()
    }
  }
}
