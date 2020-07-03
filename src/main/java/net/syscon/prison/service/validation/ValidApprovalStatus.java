package net.syscon.prison.service.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = {ApprovalStatusValidator.class})
public @interface ValidApprovalStatus {
    String message() default "{net.syscon.elite.service.validation.ApprovalStatusValidator.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
