package net.syscon.elite.aop;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.security.VerifyAgencyAccess;
import net.syscon.elite.security.VerifyBookingAccess;
import net.syscon.elite.service.AgencyService;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.support.AgencyRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@Aspect
@Slf4j
public class AuthorisationAspect {
    private final BookingService bookingService;
    private final AgencyService agencyService;
    
    public AuthorisationAspect(BookingService bookingService, AgencyService agencyService) {
        this.bookingService = bookingService;
        this.agencyService = agencyService;
    }

    @Pointcut("@annotation(net.syscon.elite.security.VerifyBookingAccess) && execution(* *(Long,..)) && args(bookingId,..)")
    public void verifyBookingAccessPointcut(Long bookingId) {
        // no code needed - pointcut definition
    }

    @Pointcut("@annotation(net.syscon.elite.security.VerifyAgencyAccess) && execution(* *(String,..)) && args(agencyId,..)")
    public void verifyAgencyAccessPointcut(String agencyId) {
        // no code needed - pointcut definition
    }

    @Pointcut("@annotation(net.syscon.elite.security.VerifyAgencyAccess) && args(net.syscon.elite.service.support.AgencyRequest,..) && args(request,..)")
    public void verifyAgencyRequestAccessPointcut(AgencyRequest request) {
        // no code needed - pointcut definition
    }

    @Before(value = "verifyBookingAccessPointcut(bookingId)", argNames = "jp,bookingId")
    public void verifyBookingAccess(JoinPoint jp, Long bookingId) {
        log.debug("Verifying booking access for booking [{}]", bookingId);

        MethodSignature signature = (MethodSignature) jp.getSignature();
        Method method = signature.getMethod();
        VerifyBookingAccess annotation = method.getAnnotation(VerifyBookingAccess.class);
        String[] overrideRoles = annotation.overrideRoles();

        if (isAccessAllowed(overrideRoles)) {
            bookingService.checkBookingExists(bookingId);
        } else {
            bookingService.verifyBookingAccess(bookingId);
        }
    }

    @Before(value = "verifyAgencyAccessPointcut(agencyId)", argNames = "jp,agencyId")
    public void verifyAgencyAccess(JoinPoint jp, String agencyId) {
        log.debug("Verifying agency access for agency [{}]", agencyId);

        if (isAccessAllowed(getOverrideRoles(jp))) {
            agencyService.checkAgencyExists(agencyId);
        } else {
            agencyService.verifyAgencyAccess(agencyId);
        }
    }

    @Before(value = "verifyAgencyRequestAccessPointcut(request)", argNames = "jp,request")
    public void verifyAgencyRequestAccess(JoinPoint jp, AgencyRequest request) {
        log.debug("Verifying agency access for agency [{}]", request.getAgencyId());

        if (isAccessAllowed(getOverrideRoles(jp))) {
            agencyService.checkAgencyExists(request.getAgencyId());
        } else {
            agencyService.verifyAgencyAccess(request.getAgencyId());
        }
    }

    private String[] getOverrideRoles(JoinPoint jp) {
        MethodSignature signature = (MethodSignature) jp.getSignature();
        Method method = signature.getMethod();
        VerifyAgencyAccess annotation = method.getAnnotation(VerifyAgencyAccess.class);
        return annotation.overrideRoles();
    }

    private boolean isAccessAllowed(String[] overrideRoles) {
        final List<String> roles = Arrays.asList(overrideRoles);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null &&
                authentication.getAuthorities().stream()
                    .anyMatch(a ->  roles.contains(a.getAuthority()));
    }
}
