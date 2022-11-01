package uk.gov.justice.hmpps.prison.aop.connectionproxy;

import oracle.jdbc.driver.OracleConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.slf4j.MDC;
import uk.gov.justice.hmpps.prison.security.AuthSource;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.hmpps.prison.util.MdcUtility.PROXY_USER;
import static uk.gov.justice.hmpps.prison.util.MdcUtility.SUPPRESS_XTAG_EVENTS;

class OracleConnectionAspectTest {

    private final AuthenticationFacade authenticationFacade = mock(AuthenticationFacade.class);
    private final RoleConfigurer roleConfigurer = mock(RoleConfigurer.class);
    private final String defaultSchema = "some default schema";
    private final Connection pooledConnection = mock(Connection.class);
    private final PreparedStatement pooledPreparedStatement = mock(PreparedStatement.class);
    private final OracleConnection proxyConnection = mock(OracleConnection.class);
    private final PreparedStatement proxyPreparedStatement = mock(PreparedStatement.class);

    private final OracleConnectionAspect connectionAspect = new OracleConnectionAspect(authenticationFacade, roleConfigurer, defaultSchema);

    private MockedStatic<MDC> mockMdc;

    @BeforeEach
    void init() {
        mockMdc = mockStatic(MDC.class);
    }

    @AfterEach
    void close() {
        mockMdc.close();
    }

    @Nested
    class OpenProxySessionIfIdentifiedAuthentication {

        @Nested
        class NomisUser {

            @Test
            void opensProxyConnection() throws SQLException {
                configureMocks(AuthSource.NOMIS, "");

                connectionAspect.openProxySessionIfIdentifiedAuthentication(pooledConnection);

                ArgumentCaptor<Properties> propertiesCaptor = ArgumentCaptor.forClass(Properties.class);
                verify(proxyConnection).openProxySession(eq(OracleConnection.PROXYTYPE_USER_NAME), propertiesCaptor.capture());
                assertThat(propertiesCaptor.getValue().get((OracleConnection.PROXY_USER_NAME))).isEqualTo("some user name");
            }

            @Test
            void setSchemaAndContext() throws SQLException {
                configureMocks(AuthSource.NOMIS, "");

                connectionAspect.openProxySessionIfIdentifiedAuthentication(pooledConnection);

                ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
                verify(pooledConnection, times(2)).prepareStatement(sqlCaptor.capture());
                assertThat(sqlCaptor.getAllValues().get(0)).contains("ALTER SESSION SET CURRENT_SCHEMA");
                assertThat(sqlCaptor.getAllValues().get(1)).contains("nomis_context.set_client_nomis_context");
            }

        }

        @Nested
        class ProxyUser {
            @Test
            void doesntOpenProxyConnection() throws SQLException {
                configureMocks(AuthSource.NONE, "some_user");

                connectionAspect.openProxySessionIfIdentifiedAuthentication(pooledConnection);

                verify(proxyConnection, never()).openProxySession(eq(OracleConnection.PROXYTYPE_USER_NAME), any(Properties.class));
            }

            @Test
            void setSchemaAndContext() throws SQLException {
                configureMocks(AuthSource.NONE, "some_user");

                connectionAspect.openProxySessionIfIdentifiedAuthentication(pooledConnection);

                ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
                verify(pooledConnection, times(2)).prepareStatement(sqlCaptor.capture());
                assertThat(sqlCaptor.getAllValues().get(0)).contains("nomis_context.set_client_nomis_context");
                assertThat(sqlCaptor.getAllValues().get(1)).contains("ALTER SESSION SET CURRENT_SCHEMA");
            }
        }

        @Nested
        class NotNomisOrProxyUser {
            @Test
            void doesntOpenProxyConnection() throws SQLException {
                configureMocks(AuthSource.NONE, "");

                connectionAspect.openProxySessionIfIdentifiedAuthentication(pooledConnection);

                verify(proxyConnection, never()).openProxySession(eq(OracleConnection.PROXYTYPE_USER_NAME), any(Properties.class));
            }

            @Test
            void dontSuppressEvents_thenDontSetContextAuditModule() throws SQLException {
                configureMocks(AuthSource.NONE, "");

                connectionAspect.openProxySessionIfIdentifiedAuthentication(pooledConnection);

                ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
                verify(pooledConnection, times(1)).prepareStatement(sqlCaptor.capture());
                assertThat(sqlCaptor.getAllValues().get(0)).doesNotContain("nomis_context.set_context('AUDIT_MODULE_NAME'");
                assertThat(sqlCaptor.getAllValues().get(0)).contains("ALTER SESSION SET CURRENT_SCHEMA");
            }

            @Test
            void suppressEvents_thenSetContextAuditModuleToMerge() throws SQLException {
                configureMocks(AuthSource.NONE, "");
                when(MDC.get(SUPPRESS_XTAG_EVENTS)).thenReturn("true");

                connectionAspect.openProxySessionIfIdentifiedAuthentication(pooledConnection);

                ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
                verify(pooledConnection, times(2)).prepareStatement(sqlCaptor.capture());
                assertThat(sqlCaptor.getAllValues().get(0)).doesNotContain("nomis_context.set_client_nomis_context");
                assertThat(sqlCaptor.getAllValues().get(0)).contains("nomis_context.set_context('AUDIT_MODULE_NAME', 'MERGE')");
                assertThat(sqlCaptor.getAllValues().get(1)).contains("ALTER SESSION SET CURRENT_SCHEMA");
            }
        }
    }

    private void configureMocks(AuthSource authSource, String proxyUser) throws SQLException {
        when(authenticationFacade.getProxyUserAuthenticationSource()).thenReturn(authSource);
        when(authenticationFacade.getCurrentUsername()).thenReturn("some user name");
        when(pooledConnection.unwrap(Connection.class)).thenReturn(proxyConnection);
        when(proxyConnection.prepareStatement(anyString())).thenReturn(proxyPreparedStatement);
        when(pooledConnection.prepareStatement(anyString())).thenReturn(pooledPreparedStatement);
        when(MDC.get(PROXY_USER)).thenReturn(proxyUser);
        when(MDC.get(SUPPRESS_XTAG_EVENTS)).thenReturn("false");
    }
}
