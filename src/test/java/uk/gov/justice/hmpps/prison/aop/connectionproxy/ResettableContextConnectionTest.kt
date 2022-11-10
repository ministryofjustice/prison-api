package uk.gov.justice.hmpps.prison.aop.connectionproxy

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.sql.Connection
import java.sql.PreparedStatement

class ResettableContextConnectionTest {

  private val connection = mock<Connection>()
  private val resettableContextConnectionTest = ResettableContextConnection(connection)
  private val preparedStatement = mock<PreparedStatement>()

  @Nested
  inner class Close {
    @BeforeEach
    fun `set up mocks`() {
      whenever(connection.prepareStatement(anyString())).thenReturn(preparedStatement)
    }

    @Test
    fun `sets Nomis context`() {
      resettableContextConnectionTest.close()

      verify(connection).prepareStatement("BEGIN nomis_context.set_nomis_context(); END;")
      verify(preparedStatement).execute()
    }

    @Test
    fun `closes connection`() {
      resettableContextConnectionTest.close()

      verify(connection).close()
    }
  }

  @Nested
  inner class Delegates {
    @Test
    fun `delegates to the local connection`() {
      resettableContextConnectionTest.commit()

      verify(connection).commit()
    }
  }
}
