package net.syscon.elite.aop;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.security.VerifyAgencyAccess;
import net.syscon.elite.security.VerifyBookingAccess;
import net.syscon.elite.security.VerifyOffenderAccess;
import net.syscon.elite.service.AgencyService;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.support.AgencyRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
public class AuthorisationAspect {
    private final BookingService bookingService;
    private final AgencyService agencyService;

    public AuthorisationAspect(final BookingService bookingService, final AgencyService agencyService) {
        this.bookingService = bookingService;
        this.agencyService = agencyService;
    }

    @Pointcut("@annotation(net.syscon.elite.security.VerifyOffenderAccess) && execution(* *(String,..)) && args(offenderNo,..)")
    public void verifyOffenderAccessPointcut(final String offenderNo) {
        // no code needed - pointcut definition
    }

    @Pointcut("@annotation(net.syscon.elite.security.VerifyBookingAccess) && execution(* *(Long,..)) && args(bookingId,..)")
    public void verifyBookingAccessPointcut(final Long bookingId) {
        // no code needed - pointcut definition
    }

    @Pointcut("@annotation(net.syscon.elite.security.VerifyAgencyAccess) && execution(* *(String,..)) && args(agencyId,..)")
    public void verifyAgencyAccessPointcut(final String agencyId) {
        // no code needed - pointcut definition
    }

    @Pointcut("@annotation(net.syscon.elite.security.VerifyAgencyAccess) && args(net.syscon.elite.service.support.AgencyRequest,..) && args(request,..)")
    public void verifyAgencyRequestAccessPointcut(final AgencyRequest request) {
        // no code needed - pointcut definition
    }

    @Before(value = "verifyBookingAccessPointcut(bookingId)", argNames = "jp,bookingId")
    public void verifyBookingAccess(final JoinPoint jp, final Long bookingId) {
        log.debug("Verifying booking access for booking [{}]", bookingId);

        final var signature = (MethodSignature) jp.getSignature();
        final var method = signature.getMethod();
        final var annotation = method.getAnnotation(VerifyBookingAccess.class);
        final var overrideRoles = annotation.overrideRoles();

        if (AuthenticationFacade.hasRoles(overrideRoles)) {
            bookingService.checkBookingExists(bookingId);
        } else {
            bookingService.verifyBookingAccess(bookingId, overrideRoles);
        }
    }

    @Before(value = "verifyOffenderAccessPointcut(offenderNo)", argNames = "jp,offenderNo")
    public void verifyOffenderAccess(final JoinPoint jp, final String offenderNo) {
        log.debug("Verifying offender access for offender No [{}]", offenderNo);
        final var signature = (MethodSignature) jp.getSignature();
        final var method = signature.getMethod();
        final var annotation = method.getAnnotation(VerifyOffenderAccess.class);
        final var overrideRoles = annotation.overrideRoles();

        bookingService.verifyCanViewSensitiveBookingInfo(offenderNo, overrideRoles);
    }

    @Before(value = "verifyAgencyAccessPointcut(agencyId)", argNames = "jp,agencyId")
    public void verifyAgencyAccess(final JoinPoint jp, final String agencyId) {
        log.debug("Verifying agency access for agency [{}]", agencyId);

        if (AuthenticationFacade.hasRoles(getOverrideRoles(jp))) {
            agencyService.checkAgencyExists(agencyId);
        } else {
            agencyService.verifyAgencyAccess(agencyId);
        }
    }

    @Before(value = "verifyAgencyRequestAccessPointcut(request)", argNames = "jp,request")
    public void verifyAgencyRequestAccess(final JoinPoint jp, final AgencyRequest request) {
        log.debug("Verifying agency access for agency [{}]", request.getAgencyId());

        if (AuthenticationFacade.hasRoles(getOverrideRoles(jp))) {
            agencyService.checkAgencyExists(request.getAgencyId());
        } else {
            agencyService.verifyAgencyAccess(request.getAgencyId());
        }
    }

    private String[] getOverrideRoles(final JoinPoint jp) {
        final var signature = (MethodSignature) jp.getSignature();
        final var method = signature.getMethod();
        final var annotation = method.getAnnotation(VerifyAgencyAccess.class);
        return annotation.overrideRoles();
    }

}
