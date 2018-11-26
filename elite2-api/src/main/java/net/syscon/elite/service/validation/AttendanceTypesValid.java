package net.syscon.elite.service.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, PARAMETER })
@Retention(RUNTIME)
@Constraint(validatedBy = AttendanceTypesValidator.class)
public @interface AttendanceTypesValid {

    String message() default "Invalid reference code";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
