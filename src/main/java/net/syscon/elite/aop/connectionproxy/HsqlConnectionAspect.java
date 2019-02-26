package net.syscon.elite.aop.connectionproxy;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.util.MdcUtility;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.util.Assert;

import java.sql.Connection;
import java.sql.SQLException;

@Aspect
@Slf4j
public class HsqlConnectionAspect {
    private final AuthenticationFacade authenticationFacade;

    public HsqlConnectionAspect(final AuthenticationFacade authenticationFacade) {
        this.authenticationFacade = authenticationFacade;
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
        final var pooledConnection = (Connection) joinPoint.proceed();
        try {
            final var connectionToReturn = openProxySessionIfIdentifiedAuthentication(pooledConnection);

            if (log.isDebugEnabled() && MdcUtility.isLoggingAllowed()) {
                log.debug(
                        "Exit: {}.{}()",
                        joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName());
            }
            return connectionToReturn;

        } catch (final Throwable e) {
            log.error(
                    "Exception thrown in HsqlConnectionAspect.connectionAround(), join point {}.{}(): {}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    e.getMessage());

            // pooledConnection will never be returned to the connection pool unless it is closed here...

            pooledConnection.close();

            throw e;
        }
    }

    private Connection openProxySessionIfIdentifiedAuthentication(final Connection pooledConnection) throws SQLException {
        if (authenticationFacade.isIdentifiedAuthentication()) {
            log.debug("Configuring Hsql Proxy Session.");
            return openAndConfigureProxySessionForConnection(pooledConnection);
        }
        return pooledConnection;
    }

    private Connection openAndConfigureProxySessionForConnection(final Connection pooledConnection) throws SQLException {

        openProxySessionForCurrentUsername(pooledConnection);

        return new ProxySessionClosingConnection(pooledConnection);
    }

    private void openProxySessionForCurrentUsername(final Connection pooledConnection) throws SQLException {

        final var statement = pooledConnection.prepareStatement("SELECT username FROM staff_user_accounts WHERE username = ?");
        final var currentUsername = authenticationFacade.getCurrentUsername();
        statement.setString(1, currentUsername);

        try {
            final var resultSet = statement.executeQuery();
            Assert.isTrue(resultSet.next(), String.format("User %s not found", currentUsername));
        } catch (final SQLException e) {
            log.error("User {} does not support Proxy Connection", currentUsername);
            throw e;
        }
        log.debug("Proxy Connection for {} Successful", currentUsername);
    }
}
