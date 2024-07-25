package uk.gov.justice.hmpps.prison.aop.connectionproxy

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.check
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.sql.Connection
import java.sql.PreparedStatement

class NomisConfigurerTest {

  private val nomisConfigurer = NomisConfigurer("some default schema")
  private val connection = mock<Connection>()
  private val preparedStatement = mock<PreparedStatement>()

  @BeforeEach
  fun setUpMocks() {
    whenever(connection.prepareStatement(anyString())).thenReturn(preparedStatement)
  }

  @Nested
  inner class SetNomisContext {
    @Test
    fun `should set context`() {
      nomisConfigurer.setNomisContext(connection, "some user", "some IP", "some URI", "some app", false)

      verify(connection).prepareStatement(
        check {
          assertThat(it).isEqualTo(
            """
                BEGIN
                nomis_context.set_context('AUDIT_MODULE_NAME', ?);
                nomis_context.set_context('AUDIT_USER_ID', ?);
                nomis_context.set_client_nomis_context(?, ?, ?, ?);
                END;
            """.trimIndent(),
          )
        },
      )
      verify(preparedStatement).setString(1, "PRISON_API")
      verify(preparedStatement).setString(2, "some user")
      verify(preparedStatement).setString(3, "some user")
      verify(preparedStatement).setString(4, "some IP")
      verify(preparedStatement).setString(5, "some app")
      verify(preparedStatement).setString(6, "some URI")
      verify(preparedStatement).execute()
    }

    @Test
    fun `should set MERGE context`() {
      nomisConfigurer.setNomisContext(connection, "some user", "some IP", "some URI", "some app", true)

      verify(preparedStatement).setString(1, "MERGE")
      verify(preparedStatement).execute()
    }

    @Test
    fun `should set blanks if not passed`() {
      nomisConfigurer.setNomisContext(connection)

      verify(preparedStatement).setString(1, "")
      verify(preparedStatement).setString(2, "")
      verify(preparedStatement).setString(3, "")
      verify(preparedStatement).setString(4, "")
      verify(preparedStatement).setString(5, "")
      verify(preparedStatement).setString(6, "")
      verify(preparedStatement).execute()
    }
  }

  @Nested
  inner class SetSuppressXtagEvents {
    @Test
    fun `should set the merge audit module`() {
      nomisConfigurer.setSuppressXtagEvents(connection)

      verify(connection).prepareStatement(
        check {
          assertThat(it).isEqualTo(
            """
                BEGIN
                nomis_context.set_context('AUDIT_MODULE_NAME', 'MERGE');
                END;
            """.trimIndent(),
          )
        },
      )
      verify(preparedStatement).execute()
    }
  }

  @Nested
  inner class SetDefaultSchema {
    @Test
    fun `should set schema`() {
      nomisConfigurer.setDefaultSchema(connection)

      verify(connection).prepareStatement(
        check {
          assertThat(it).isEqualTo("ALTER SESSION SET CURRENT_SCHEMA=some default schema")
        },
      )
      verify(preparedStatement).execute()
    }

    @Test
    fun `should do nothing if no default schema`() {
      val nomisConfigurerWithoutSchema = NomisConfigurer("")

      nomisConfigurerWithoutSchema.setDefaultSchema(connection)

      verifyNoInteractions(connection)
    }
  }
}
