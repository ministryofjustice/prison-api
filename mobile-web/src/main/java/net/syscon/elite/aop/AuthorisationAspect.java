package net.syscon.elite.aop;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.service.BookingService;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
@Slf4j
public class AuthorisationAspect {
    private final BookingService bookingService;

    public AuthorisationAspect(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Pointcut("@annotation(net.syscon.elite.security.VerifyBookingAccess) && execution(* *(Long,..)) && args(bookingId,..)")
    public void verifyBookingAccessPointcut(Long bookingId) {
        // no code needed - pointcut definition
    }

    @Before("verifyBookingAccessPointcut(bookingId)")
    public void verifyBookingAccess(Long bookingId) {
        log.debug("Verifying booking access for booking [{}]", bookingId);
        if (!bookingService.isSystemUser()) {
            bookingService.verifyBookingAccess(bookingId);
        }
    }
}
