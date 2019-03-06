package net.syscon.elite.aop;

import lombok.extern.slf4j.Slf4j;
import net.syscon.util.MdcUtility;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Aspect
@Slf4j
public class RequestAspect {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");

    @Pointcut("within(net.syscon.elite.api.resource.impl.*)")
    public void controllerPointcut() {
        // No code needed
    }

    @Around("controllerPointcut()")
    public Object controllerCall(final ProceedingJoinPoint joinPoint) throws Throwable {

        final var start = LocalDateTime.now();
        if (MdcUtility.isLoggingAllowed()) {
            log.debug("Enter: {}.{}() with argument[s] = {}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
        }
        final var result = joinPoint.proceed();

        if (MdcUtility.isLoggingAllowed()) {
            final var duration = Duration.between(start, LocalDateTime.now()).toMillis();
            log.debug("Exit: {}.{}() - Started: {}, Duration: {} ms",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(), start.format(formatter), duration);
        }
        return result;

    }
}
