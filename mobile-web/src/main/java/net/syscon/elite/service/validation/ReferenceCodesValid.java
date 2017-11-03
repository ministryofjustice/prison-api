package net.syscon.elite.service.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import javax.validation.Constraint;
import javax.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target({ METHOD, FIELD, PARAMETER })
@Retention(RUNTIME)
@Constraint(validatedBy = ReferenceCodesValidator.class)
public @interface ReferenceCodesValid {

    String message() default "Invalid reference code type or subtype";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
