package uk.gov.justice.hmpps.prison.aop.connectionproxy

import ch.qos.logback.classic.util.LogbackMDCAdapter
import oracle.jdbc.driver.OracleConnection
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.never
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.slf4j.MDC
import uk.gov.justice.hmpps.prison.security.AuthSource
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade
import uk.gov.justice.hmpps.prison.util.MdcUtility
import uk.gov.justice.hmpps.prison.web.config.RoutingDataSource
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException

class OracleConnectionAspectTest {
  private val authenticationFacade = mock<AuthenticationFacade>()
  private val roleConfigurer = mock<RoleConfigurer>()
  private val nomisConfigurer = mock<NomisConfigurer>()
  private val pooledConnection = mock<Connection>()
  private val pooledPreparedStatement = mock<PreparedStatement>()
  private val oracleConnection = mock<OracleConnection>()
  private val oraclePreparedStatement = mock<PreparedStatement>()
  private var mockMdc: MockedStatic<MDC>? = null
  private var mockRoutingDataSource: MockedStatic<RoutingDataSource>? = null

  private val connectionAspect = OracleConnectionAspect(authenticationFacade, roleConfigurer, nomisConfigurer)

  @BeforeEach
  fun init() {
    mockMdc = mockStatic(MDC::class.java)
    mockRoutingDataSource = mockStatic(RoutingDataSource::class.java)
  }

  @AfterEach
  fun close() {
    mockMdc!!.close()
    mockRoutingDataSource!!.close()
  }

  @Nested
  inner class OpenProxySessionIfIdentifiedAuthentication {

    @Nested
    inner class NomisProxyUser {
      @BeforeEach
      fun `set up mocks`() {
        configureMocks(AuthSource.NOMIS, "some user name")
      }

      @Test
      fun `should open proxy connection`() {
        connectionAspect.openProxySessionIfIdentifiedAuthentication(pooledConnection)

        verify(oracleConnection).openProxySession(
          eq(OracleConnection.PROXYTYPE_USER_NAME),
          check {
            assertThat(it[OracleConnection.PROXY_USER_NAME]).isEqualTo("some user name")
          }
        )
      }

      @Test
      fun `should configure role`() {
        connectionAspect.openProxySessionIfIdentifiedAuthentication(pooledConnection)

        verify(roleConfigurer).setRoleForConnection(oracleConnection)
      }

      @Test
      fun `should set Nomis context and schema`() {
        connectionAspect.openProxySessionIfIdentifiedAuthentication(pooledConnection)

        verify(nomisConfigurer).setDefaultSchema(any<ProxySessionClosingConnection>())
        verify(nomisConfigurer).setNomisContext(any<ProxySessionClosingConnection>(), eq("some user name"), eq("some IP"), eq("some URI"), eq("APP"), eq(false))
      }

      @Test
      fun `should suppress Xtag events`() {
        whenever(MDC.get(MdcUtility.SUPPRESS_XTAG_EVENTS)).thenReturn("true")

        connectionAspect.openProxySessionIfIdentifiedAuthentication(pooledConnection)

        verify(nomisConfigurer).setNomisContext(any<ProxySessionClosingConnection>(), eq("some user name"), eq("some IP"), eq("some URI"), eq("APP"), eq(true))
      }

      @Test
      fun `should return proxy connection`() {
        val conn = connectionAspect.openProxySessionIfIdentifiedAuthentication(pooledConnection)

        assertThat(conn).isExactlyInstanceOf(ProxySessionClosingConnection::class.java)
      }

      @Test
      fun `should throw if replica`() {
        whenever(RoutingDataSource.isReplica()).thenReturn(true)

        assertThatThrownBy { connectionAspect.openProxySessionIfIdentifiedAuthentication(pooledConnection) }
          .isInstanceOf(RuntimeException::class.java)
      }
    }

    @Nested
    inner class OtherProxyUser {
      @BeforeEach
      fun `set up mocks`() {
        configureMocks(AuthSource.NONE, "some user name")
      }

      @Test
      fun `should not open proxy connection`() {
        connectionAspect.openProxySessionIfIdentifiedAuthentication(pooledConnection)

        verify(oracleConnection, never()).openProxySession(anyInt(), any())
      }

      @Test
      fun `should not configure role`() {
        connectionAspect.openProxySessionIfIdentifiedAuthentication(pooledConnection)

        verify(roleConfigurer, never()).setRoleForConnection(any())
      }

      @Test
      fun `should set Nomis context and schema`() {
        connectionAspect.openProxySessionIfIdentifiedAuthentication(pooledConnection)

        verify(nomisConfigurer).setDefaultSchema(any<ResettableContextConnection>())
        verify(nomisConfigurer).setNomisContext(any<ResettableContextConnection>(), eq("some user name"), eq("some IP"), eq("some URI"), eq("APP"), eq(false))
      }

      @Test
      fun `should suppress Xtag events`() {
        whenever(MDC.get(MdcUtility.SUPPRESS_XTAG_EVENTS)).thenReturn("true")

        connectionAspect.openProxySessionIfIdentifiedAuthentication(pooledConnection)

        verify(nomisConfigurer).setNomisContext(any<ResettableContextConnection>(), eq("some user name"), eq("some IP"), eq("some URI"), eq("APP"), eq(true))
      }

      @Test
      fun `should return resettable context connection`() {
        val conn = connectionAspect.openProxySessionIfIdentifiedAuthentication(pooledConnection)

        assertThat(conn).isExactlyInstanceOf(ResettableContextConnection::class.java)
      }

      @Test
      fun `should throw if replica`() {
        whenever(RoutingDataSource.isReplica()).thenReturn(true)

        assertThatThrownBy { connectionAspect.openProxySessionIfIdentifiedAuthentication(pooledConnection) }
          .isInstanceOf(RuntimeException::class.java)
      }
    }

    @Nested
    inner class SupppressXtagEvents {
      @BeforeEach
      fun `set up mocks`() {
        configureMocks(AuthSource.NONE, "")
        whenever(MDC.get(MdcUtility.SUPPRESS_XTAG_EVENTS)).thenReturn("true")
      }

      @Test
      fun `should not open proxy connection`() {
        connectionAspect.openProxySessionIfIdentifiedAuthentication(pooledConnection)

        verify(oracleConnection, never()).openProxySession(eq(OracleConnection.PROXYTYPE_USER_NAME), any())
      }

      @Test
      fun `should not configure role`() {
        connectionAspect.openProxySessionIfIdentifiedAuthentication(pooledConnection)

        verify(roleConfigurer, never()).setRoleForConnection(any())
      }

      @Test
      fun `should set Nomis context and schema`() {
        connectionAspect.openProxySessionIfIdentifiedAuthentication(pooledConnection)

        verify(nomisConfigurer).setSuppressXtagEvents(any<ResettableContextConnection>())
        verify(nomisConfigurer).setDefaultSchema(any<ResettableContextConnection>())
      }

      @Test
      fun `should return resettable context connection`() {
        val conn = connectionAspect.openProxySessionIfIdentifiedAuthentication(pooledConnection)

        assertThat(conn).isExactlyInstanceOf(ResettableContextConnection::class.java)
      }

      @Test
      fun `should throw if replica`() {
        whenever(RoutingDataSource.isReplica()).thenReturn(true)

        assertThatThrownBy { connectionAspect.openProxySessionIfIdentifiedAuthentication(pooledConnection) }
          .isInstanceOf(RuntimeException::class.java)
      }
    }

    @Nested
    inner class AnyOtherUser {
      @BeforeEach
      fun `set up mocks`() {
        configureMocks(AuthSource.NONE, "")
      }

      @Test
      fun `should not open proxy connection`() {
        connectionAspect.openProxySessionIfIdentifiedAuthentication(pooledConnection)

        verify(oracleConnection, never()).openProxySession(eq(OracleConnection.PROXYTYPE_USER_NAME), any())
      }

      @Test
      fun `should not configure role`() {
        connectionAspect.openProxySessionIfIdentifiedAuthentication(pooledConnection)

        verify(roleConfigurer, never()).setRoleForConnection(any())
      }

      @Test
      fun `should set schema but not Nomis context`() {
        connectionAspect.openProxySessionIfIdentifiedAuthentication(pooledConnection)

        verify(nomisConfigurer).setDefaultSchema(pooledConnection)
        verify(nomisConfigurer, never()).setNomisContext(any(), anyString(), anyString(), anyString(), anyString(), anyBoolean())
      }

      @Test
      fun `should not throw if replica`() {
        whenever(RoutingDataSource.isReplica()).thenReturn(true)

        connectionAspect.openProxySessionIfIdentifiedAuthentication(pooledConnection)

        verify(nomisConfigurer).setDefaultSchema(pooledConnection)
      }
    }
  }

  @Throws(SQLException::class)
  private fun configureMocks(authSource: AuthSource, proxyUser: String) {
    whenever(authenticationFacade.authenticationSource).thenReturn(authSource)
    whenever(authenticationFacade.currentUsername).thenReturn(proxyUser)
    whenever(pooledConnection.unwrap(Connection::class.java)).thenReturn(oracleConnection)
    whenever(oracleConnection.prepareStatement(anyString())).thenReturn(oraclePreparedStatement)
    whenever(pooledConnection.prepareStatement(anyString())).thenReturn(pooledPreparedStatement)
    whenever(MDC.getMDCAdapter()).thenReturn(LogbackMDCAdapter())
    whenever(MDC.get(MdcUtility.PROXY_USER)).thenReturn(proxyUser)
    whenever(MDC.get(MdcUtility.SUPPRESS_XTAG_EVENTS)).thenReturn("false")
    whenever(MDC.get(MdcUtility.USER_ID_HEADER)).thenReturn(proxyUser)
    whenever(MDC.get(MdcUtility.IP_ADDRESS)).thenReturn("some IP")
    whenever(MDC.get(MdcUtility.REQUEST_URI)).thenReturn("some URI")
    whenever(RoutingDataSource.isReplica()).thenReturn(false)
  }
}
