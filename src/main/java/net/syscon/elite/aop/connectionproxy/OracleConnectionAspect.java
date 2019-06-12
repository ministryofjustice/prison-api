package net.syscon.elite.aop.connectionproxy;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.security.AuthenticationFacade;
import oracle.jdbc.driver.OracleConnection;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.annotation.Aspect;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static java.lang.String.format;

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
        if (authenticationFacade.isIdentifiedAuthentication()) {
            log.trace("Configuring Oracle Proxy Session {}", pooledConnection);
            return openAndConfigureProxySessionForConnection(pooledConnection);
        }
        setDefaultSchema(pooledConnection);
        return pooledConnection;
    }

    private Connection openAndConfigureProxySessionForConnection(final Connection pooledConnection) throws SQLException {

        final var oracleConnection = openProxySessionForCurrentUsername(pooledConnection);

        final Connection wrappedConnection = new ProxySessionClosingConnection(pooledConnection);

        setDefaultSchema(wrappedConnection);

        roleConfigurer.setRoleForConnection(oracleConnection);

        return wrappedConnection;
    }

    private OracleConnection openProxySessionForCurrentUsername(final Connection pooledConnection) throws SQLException {

        final var oracleConnection = (OracleConnection) pooledConnection.unwrap(Connection.class);

        final var info = new Properties();
        final var currentUsername = authenticationFacade.getCurrentUsername();
        info.put(OracleConnection.PROXY_USER_NAME, currentUsername);

        try {
            oracleConnection.openProxySession(OracleConnection.PROXYTYPE_USER_NAME, info);
            log.trace("Opened proxy connection {}", oracleConnection);
        } catch (final SQLException e) {
            log.error("User {} does not support Proxy Connection", currentUsername);
            throw e;
        }
        log.trace("Proxy Connection for {} Successful", currentUsername);
        return oracleConnection;
    }

    private void setDefaultSchema(final Connection conn) throws SQLException {
        if (StringUtils.isNotBlank(defaultSchema)) {
            try (final var ps = conn.prepareStatement(format("ALTER SESSION SET CURRENT_SCHEMA=%s", defaultSchema))) {
                ps.execute();
            }
        }
    }
}
