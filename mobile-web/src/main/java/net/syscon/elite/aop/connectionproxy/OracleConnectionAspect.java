package net.syscon.elite.aop.connectionproxy;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.util.MdcUtility;
import oracle.jdbc.driver.OracleConnection;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import static java.lang.String.format;

@Aspect
@Slf4j
public class OracleConnectionAspect {

    private final AuthenticationFacade authenticationFacade;
    private final RoleConfigurer roleConfigurer;
    private final String defaultSchema;

    public OracleConnectionAspect(
            AuthenticationFacade authenticationFacade,
            RoleConfigurer roleConfigurer,
            String defaultSchema) {

        this.authenticationFacade = authenticationFacade;
        this.roleConfigurer = roleConfigurer;
        this.defaultSchema = defaultSchema;
    }

    @Pointcut("execution (* com.zaxxer.hikari.HikariDataSource.getConnection())")
    protected void onNewConnectionPointcut() {
        // No code needed
    }

    @Around("onNewConnectionPointcut()")
    public Object connectionAround(final ProceedingJoinPoint joinPoint) throws Throwable {

        if (log.isDebugEnabled() && MdcUtility.isLoggingAllowed()) {
            log.debug("Enter: {}.{}()", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
        }
        final Connection pooledConnection = (Connection) joinPoint.proceed();
        try {
            final Connection connectionToReturn = openProxySessionIfIdentifiedAuthentication(pooledConnection);

            if (log.isDebugEnabled() && MdcUtility.isLoggingAllowed()) {
                log.debug(
                        "Exit: {}.{}()",
                        joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName());
            }
            return connectionToReturn;

        } catch (final Throwable e) {
            log.error(
                    "Exception thrown in OracleConnectionAspect.connectionAround(), join point {}.{}(): {}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    e.getMessage());

            // pooledConnection will never be returned to the connection pool unless it is closed here...

            pooledConnection.close();

            throw e;
        }
    }

    private Connection openProxySessionIfIdentifiedAuthentication(Connection pooledConnection) throws SQLException {
        if (authenticationFacade.isIdentifiedAuthentication()) {
            log.debug("Configuring Oracle Proxy Session.");
            return openAndConfigureProxySessionForConnection(pooledConnection);
        } else {
            setDefaultSchema(pooledConnection);
            roleConfigurer.setRoleForConnection(pooledConnection);
            return pooledConnection;
        }
    }

    private Connection openAndConfigureProxySessionForConnection(Connection pooledConnection) throws SQLException {

        final OracleConnection oracleConnection = openProxySessionForCurrentUsername(pooledConnection);

        final Connection wrappedConnection = new ProxySessionClosingConnection(pooledConnection);

        setDefaultSchema(wrappedConnection);

        roleConfigurer.setRoleForConnection(oracleConnection);

        return wrappedConnection;
    }

    private OracleConnection openProxySessionForCurrentUsername(Connection pooledConnection) throws SQLException {

        final OracleConnection oracleConnection = (OracleConnection) pooledConnection.unwrap(Connection.class);

        final Properties info = new Properties();
        String currentUsername = authenticationFacade.getCurrentUsername();
        info.put(OracleConnection.PROXY_USER_NAME, currentUsername);

        try {
            oracleConnection.openProxySession(OracleConnection.PROXYTYPE_USER_NAME, info);
        } catch (SQLException e) {
            log.error("User {} does not support Proxy Connection", currentUsername);
            throw e;
        }
        log.debug("Proxy Connection for {} Successful", currentUsername);
        return oracleConnection;
    }

    private void setDefaultSchema(final Connection conn) throws SQLException {
        if (StringUtils.isNotBlank(defaultSchema)) {
            try (PreparedStatement ps = conn.prepareStatement(format("ALTER SESSION SET CURRENT_SCHEMA=%s", defaultSchema))) {
                ps.execute();
            }
        }
    }
}
