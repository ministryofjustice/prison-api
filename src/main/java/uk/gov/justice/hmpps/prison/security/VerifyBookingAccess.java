package uk.gov.justice.hmpps.prison.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface   VerifyBookingAccess {
    String[] overrideRoles() default "ROLE_SYSTEM_USER";
}
