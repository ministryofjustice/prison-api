package uk.gov.justice.hmpps.prison.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import uk.gov.justice.hmpps.prison.core.SlowReportQuery;
import uk.gov.justice.hmpps.prison.web.config.RoutingDataSource;

@Aspect
@Component
@Order(0)
@Slf4j
public class ReadOnlyRouteInterceptor {

    @Around("@target(slowReportQuery)")
    public Object annotatedTransaction(ProceedingJoinPoint proceedingJoinPoint, SlowReportQuery slowReportQuery) throws Throwable {
        log.debug("SlowReportQuery Pointcut: {}.{}() ",
            proceedingJoinPoint.getSignature().getDeclaringTypeName(),
            proceedingJoinPoint.getSignature().getName());
        try {
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                log.debug("SlowReportQuery: Transaction already active, skipping ...");
            } else {
                RoutingDataSource.setReplicaRoute();
                log.debug("SlowReportQuery: Routing database call to the replica");
            }
            return proceedingJoinPoint.proceed();
        } finally {
            RoutingDataSource.clearRoute();
        }
    }
}
