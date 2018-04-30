package net.syscon.elite.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static net.syscon.elite.web.filter.MdcUtility.REQUEST_DURATION;

@Aspect
@Slf4j
public class RequestAspect {

    @Autowired(required = false)
    private HttpServletRequest request;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");

    @Pointcut("within(net.syscon.elite.api.resource.impl.*)")
    public void controllerPointcut() {
        // No code needed
    }

    @Around("controllerPointcut()")
    public Object controllerCall(final ProceedingJoinPoint joinPoint) throws Throwable {

        try {
            LocalDateTime start = LocalDateTime.now();
            final Object result = joinPoint.proceed();
            long duration = Duration.between(start, LocalDateTime.now()).toMillis();
            MDC.put(REQUEST_DURATION, String.valueOf(duration));
            log.debug("URI: {} - Start {}, Duration {} ms", request.getRequestURI(), start.format(formatter), duration);
            return result;
        } finally {
            MDC.remove(REQUEST_DURATION);
        }


    }
}
