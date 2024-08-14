package uk.gov.justice.hmpps.prison.aop;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder;

import static uk.gov.justice.hmpps.prison.util.MdcUtility.PROXY_USER;

@Aspect
@Slf4j
@Component
@AllArgsConstructor
public class ProxyUserAspect {

    private final HmppsAuthenticationHolder hmppsAuthenticationHolder;

    @Pointcut("within(uk.gov.justice.hmpps.prison.api.resource.*) && @annotation(uk.gov.justice.hmpps.prison.core.ProxyUser)")
    public void proxyUserPointcut() {
        // No code needed
    }

    @Around("proxyUserPointcut()")
    public Object controllerCall(final ProceedingJoinPoint joinPoint) throws Throwable {

        var authentication = hmppsAuthenticationHolder.getAuthenticationOrNull();
        try {
            if (authentication != null) {
                log.info("Proxying User: {} for {}->{}", authentication.getPrincipal(),
                        joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName());

                MDC.put(PROXY_USER, authentication.getPrincipal());
            }
            return joinPoint.proceed();
        } finally {
            if (authentication != null) {
                MDC.remove(PROXY_USER);
            }
        }
    }
}
