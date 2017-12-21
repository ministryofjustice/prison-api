package net.syscon.elite.aop;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.service.BookingService;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;

@Aspect
@Slf4j
public class AuthorisationAspect {
    private static final String SYSTEM_USER_ROLE = "SYSTEM_USER";
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

        final Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        final boolean isSystemUser = authorities.stream().anyMatch(a -> a.getAuthority().contains(SYSTEM_USER_ROLE));
        if (!isSystemUser) {
            bookingService.verifyBookingAccess(bookingId);
        }
    }
}
