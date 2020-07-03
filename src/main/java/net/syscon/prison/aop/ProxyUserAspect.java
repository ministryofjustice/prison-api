package net.syscon.prison.aop;

import lombok.extern.slf4j.Slf4j;
import net.syscon.prison.security.AuthenticationFacade;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import static net.syscon.util.MdcUtility.PROXY_USER;

@Aspect
@Slf4j
@Component
public class ProxyUserAspect {

    private final AuthenticationFacade authenticationFacade;

    public ProxyUserAspect(AuthenticationFacade authenticationFacade) {
        this.authenticationFacade = authenticationFacade;
    }

    @Pointcut("within(net.syscon.prison.api.resource.impl.*) && @annotation(net.syscon.prison.core.ProxyUser)")
    public void proxyUserPointcut() {
        // No code needed
    }

    @Around("proxyUserPointcut()")
    public Object controllerCall(final ProceedingJoinPoint joinPoint) throws Throwable {

        var proxyUser = authenticationFacade.getCurrentUsername();
        try {
            if (proxyUser != null) {
                log.debug("Proxying User: {} for {}->{}", proxyUser,
                        joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName());

                MDC.put(PROXY_USER, proxyUser);
            }
            return joinPoint.proceed();
        } finally {
            if (proxyUser != null) {
                MDC.remove(PROXY_USER);
            }
        }
    }
}
