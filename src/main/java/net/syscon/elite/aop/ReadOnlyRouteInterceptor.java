package net.syscon.elite.aop;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.web.config.RoutingDataSource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Aspect
@Component
@Order(0)
@Slf4j
public class ReadOnlyRouteInterceptor {

    @Around("@annotation(transactional)")
    public Object proceed(ProceedingJoinPoint proceedingJoinPoint, Transactional transactional) throws Throwable {
        try {
            if (!transactional.readOnly()) {
                RoutingDataSource.setPrimaryRoute();
                log.info("Routing database call to the primary");
            }
            return proceedingJoinPoint.proceed();
        } finally {
            RoutingDataSource.clearReplicaRoute();
        }
    }
}
