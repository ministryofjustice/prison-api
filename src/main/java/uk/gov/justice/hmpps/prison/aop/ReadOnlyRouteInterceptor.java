package uk.gov.justice.hmpps.prison.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.prison.web.config.RoutingDataSource;

@Aspect
@Component
@Order(0)
@Slf4j
public class ReadOnlyRouteInterceptor {

    @Around("@annotation(uk.gov.justice.hmpps.prison.core.SlowReportQuery)")
    public Object annotatedEndpoint(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        log.debug("SlowReportQuery Pointcut: {}.{}(), Routing database call to the REPLICA",
            proceedingJoinPoint.getSignature().getDeclaringTypeName(),
            proceedingJoinPoint.getSignature().getName());
        try {
            RoutingDataSource.setReplicaRoute();
            return proceedingJoinPoint.proceed();
        } finally {
            RoutingDataSource.clearRoute();
        }
    }
}
