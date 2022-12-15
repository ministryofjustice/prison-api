package uk.gov.justice.hmpps.prison.aop.connectionproxy;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import uk.gov.justice.hmpps.prison.util.MdcUtility;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public abstract class AbstractConnectionAspect {

    @Pointcut("execution (* org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource.getConnection())")
    protected void onNewConnectionPointcut() {
        // No code needed
    }

    @Around("onNewConnectionPointcut()")
    public Object connectionAround(final ProceedingJoinPoint joinPoint) throws Throwable {

        if (log.isTraceEnabled() && MdcUtility.isLoggingAllowed()) {
            log.trace("Enter: {}.{}()", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
        }
        final var pooledConnection = (Connection) joinPoint.proceed();
        try {
            final var connectionToReturn = configureNomisConnection(pooledConnection);

            if (log.isTraceEnabled() && MdcUtility.isLoggingAllowed()) {
                log.trace(
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

    protected abstract Connection configureNomisConnection(final Connection pooledConnection) throws SQLException;
}
