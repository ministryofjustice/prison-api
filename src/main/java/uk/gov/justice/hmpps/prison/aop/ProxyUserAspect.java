package uk.gov.justice.hmpps.prison.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.service.UserService;

import static uk.gov.justice.hmpps.prison.util.MdcUtility.NOMIS_USER_HEADER;
import static uk.gov.justice.hmpps.prison.util.MdcUtility.PROXY_USER;

@Aspect
@Slf4j
@Component
public class ProxyUserAspect {

    private final AuthenticationFacade authenticationFacade;
    private final UserService service;

    public ProxyUserAspect(final AuthenticationFacade authenticationFacade, final UserService userService) {
        this.authenticationFacade = authenticationFacade;
        this.service = userService;
    }

    @Pointcut("within(uk.gov.justice.hmpps.prison.api.resource.*) && @annotation(uk.gov.justice.hmpps.prison.core.ProxyUser)")
    public void proxyUserPointcut() {
        // No code needed
    }

    @Around("proxyUserPointcut()")
    public Object controllerCall(final ProceedingJoinPoint joinPoint) throws Throwable {

        var proxyUser = authenticationFacade.getCurrentUsername();
        try {
            if (proxyUser != null) {
                final var nomisStaffUser = Boolean.toString(service.isStaff(proxyUser));
                log.debug("Proxying User: {} for {}->{} NOMIS Staff = {}", proxyUser,
                        joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName(),
                        nomisStaffUser);

                MDC.put(PROXY_USER, proxyUser);
                MDC.put(NOMIS_USER_HEADER, nomisStaffUser);
            }
            return joinPoint.proceed();
        } finally {
            if (proxyUser != null) {
                MDC.remove(PROXY_USER);
                MDC.remove(NOMIS_USER_HEADER);
            }
        }
    }
}
