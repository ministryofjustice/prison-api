package uk.gov.justice.hmpps.prison.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException;
import uk.gov.justice.hmpps.prison.service.NoContentException;
import uk.gov.justice.hmpps.prison.util.MdcUtility;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Slf4j
@Component
public class LoggingAspect {

    @Pointcut("within(uk.gov.justice.hmpps.prison.repository..*) || within(uk.gov.justice.hmpps.prison.service..*) || within(uk.gov.justice.hmpps.prison.aop..*)")
    public void loggingPointcut() {
        // No code needed
    }

    @AfterThrowing(pointcut = "loggingPointcut()", throwing = "e")
    public void logAfterThrowing(final JoinPoint joinPoint, final Throwable e) {
        if (!(e instanceof EntityNotFoundException || e instanceof NoContentException)) {
            log.error("Exception in pointcut {} {}()",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    e);
        }
    }

    @Around("loggingPointcut()")
    public Object logAround(final ProceedingJoinPoint joinPoint) throws Throwable {
        final var start = LocalDateTime.now();
        if (log.isTraceEnabled() && MdcUtility.isLoggingAllowed()) {
            log.trace(
                    "Enter: {}.{}()",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName());
        }
        try {
            final var result = joinPoint.proceed();
            if (log.isTraceEnabled() && MdcUtility.isLoggingAllowed()) {
                log.trace(
                        "Exit: {}.{}() - Duration {} ms",
                        joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName(),
                        Duration.between(start, LocalDateTime.now()).toMillis());
            }
            return result;
        } catch (final IllegalArgumentException e) {
            log.error(
                    "Illegal argument: {} in {}.{}()",
                    Arrays.toString(joinPoint.getArgs()),
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName());

            throw e;
        }
    }
}
