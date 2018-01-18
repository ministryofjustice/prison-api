package net.syscon.elite.aop;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.service.AgencyService;
import net.syscon.elite.service.BookingService;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

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

    @Before("verifyBookingAccessPointcut(bookingId)")
    public void verifyBookingAccess(Long bookingId) {
        log.debug("Verifying booking access for booking [{}]", bookingId);

        if (bookingService.isSystemUser()) {
            bookingService.checkBookingExists(bookingId);
        } else {
            bookingService.verifyBookingAccess(bookingId);
        }
    }

    @Before("verifyAgencyAccessPointcut(agencyId)")
    public void verifyAgencyAccess(String agencyId) {
        log.debug("Verifying agency access for agency [{}]", agencyId);

        if (!bookingService.isSystemUser()) {
            agencyService.verifyAgencyAccess(agencyId);
        }
    }
}
