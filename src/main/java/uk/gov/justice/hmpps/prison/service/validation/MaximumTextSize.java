package uk.gov.justice.hmpps.prison.service.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = MaximumTextSizeValidator.class)
public @interface MaximumTextSize {

    String message() default "Length exceeds the maximum size allowed";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
