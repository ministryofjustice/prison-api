package net.syscon.elite.aop;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.web.config.RoutingDataSource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Arrays;

@Aspect
@Component
@Order(0)
@Slf4j
public class ReadOnlyRouteInterceptor {

    @Around("@within(transactional)")
    public Object annotatedTransaction(ProceedingJoinPoint proceedingJoinPoint, Transactional transactional) throws Throwable {

        var tx = Arrays.stream(proceedingJoinPoint.getSignature().getDeclaringType().getMethods())
                .filter(m -> m.getName().equals(proceedingJoinPoint.getSignature().getName()))
                .findFirst()
                .map(m -> m.getAnnotation(Transactional.class)).orElse(transactional);

        log.debug("Transaction Pointcut: {}.{}() - Transaction Read Only = {}",
                proceedingJoinPoint.getSignature().getDeclaringTypeName(),
                proceedingJoinPoint.getSignature().getName(), tx.readOnly());
        try {
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                log.debug("Transaction already active, skipping ...");
            } else {
                if (tx.readOnly()) {
                    RoutingDataSource.setReplicaRoute();
                    log.debug("Routing database call to the replica");
                } else {
                    log.debug("Routing database call to the master");
                }
            }
            return proceedingJoinPoint.proceed();
        } finally {
            RoutingDataSource.clearRoute();
        }
    }
}
