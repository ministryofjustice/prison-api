package uk.gov.justice.hmpps.prison.aop.connectionproxy;

import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.driver.OracleConnection;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static java.lang.String.format;
import static uk.gov.justice.hmpps.prison.security.AuthSource.NOMIS;
import static uk.gov.justice.hmpps.prison.util.MdcUtility.IP_ADDRESS;
import static uk.gov.justice.hmpps.prison.util.MdcUtility.NOMIS_CONTEXT;
import static uk.gov.justice.hmpps.prison.util.MdcUtility.PROXY_USER;
import static uk.gov.justice.hmpps.prison.util.MdcUtility.REQUEST_URI;
import static uk.gov.justice.hmpps.prison.util.MdcUtility.USER_ID_HEADER;

@Aspect
@Slf4j
public class OracleConnectionAspect extends AbstractConnectionAspect {

    private final AuthenticationFacade authenticationFacade;
    private final RoleConfigurer roleConfigurer;
    private final String defaultSchema;

    public OracleConnectionAspect(
            final AuthenticationFacade authenticationFacade,
            final RoleConfigurer roleConfigurer,
            final String defaultSchema) {

        this.authenticationFacade = authenticationFacade;
        this.roleConfigurer = roleConfigurer;
        this.defaultSchema = defaultSchema;
    }

    @Override
    protected Connection openProxySessionIfIdentifiedAuthentication(final Connection pooledConnection) throws SQLException {
        final var proxyUserAuthSource = authenticationFacade.getProxyUserAuthenticationSource();
        if (proxyUserAuthSource == NOMIS ) {
            log.trace("Configuring Oracle Proxy Session for NOMIS user {}", pooledConnection);
            return openAndConfigureProxySessionForConnection(pooledConnection);
        } else if (StringUtils.isNotBlank(MDC.get(PROXY_USER))) {
            // If proxy user is set - try to set the context - allow it to fail and carry on
            try {
                setContext(pooledConnection);
            } catch (SQLException e) {
                log.warn("Failed to set context for proxy user {}", MDC.get(PROXY_USER));
            }
        }

        setDefaultSchema(pooledConnection);
        return pooledConnection;
    }

    private Connection openAndConfigureProxySessionForConnection(final Connection pooledConnection) throws SQLException {

        final var oracleConnection = openProxySessionForCurrentUsername(pooledConnection);

        final Connection wrappedConnection = new ProxySessionClosingConnection(pooledConnection);

        roleConfigurer.setRoleForConnection(oracleConnection);

        configureConnection(wrappedConnection);

        return wrappedConnection;
    }

    private void configureConnection(final Connection pooledConnection) throws SQLException {
        setDefaultSchema(pooledConnection);
        setContext(pooledConnection);
    }

    private OracleConnection openProxySessionForCurrentUsername(final Connection pooledConnection) throws SQLException {

        final var oracleConnection = (OracleConnection) pooledConnection.unwrap(Connection.class);

        final var info = new Properties();
        final var currentUsername = authenticationFacade.getCurrentUsername();
        info.put(OracleConnection.PROXY_USER_NAME, currentUsername);

        try {
            oracleConnection.openProxySession(OracleConnection.PROXYTYPE_USER_NAME, info);
            log.debug("Opened proxy connection {}", oracleConnection);
        } catch (final SQLException e) {
            log.error("User {} does not support Proxy Connection", currentUsername);
            throw e;
        }
        log.debug("Proxy Connection for {} Successful", currentUsername);
        return oracleConnection;
    }

    private void setDefaultSchema(final Connection conn) throws SQLException {
        if (StringUtils.isNotBlank(defaultSchema)) {
            try (final var ps = conn.prepareStatement(format("ALTER SESSION SET CURRENT_SCHEMA=%s", defaultSchema))) {
                ps.execute();
            }
        }
    }

    private void setContext(final Connection conn) throws SQLException {
        final var sql = format("""
                BEGIN
                nomis_context.set_context('AUDIT_MODULE_NAME','%s');
                nomis_context.set_context('AUDIT_USER_ID', '%s');
                nomis_context.set_client_nomis_context('%s', '%s', '%s', '%s');
                END;""",
                MDC.get(NOMIS_CONTEXT),
                MDC.get(USER_ID_HEADER),
                MDC.get(USER_ID_HEADER),
                MDC.get(IP_ADDRESS),
                "API",
                MDC.get(REQUEST_URI));
        try (final var ps = conn.prepareStatement(sql)) {
            ps.execute();
        }
    }
}
