package uk.gov.justice.hmpps.prison.service.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = CaseNoteTypeSubTypeValidator.class)
public @interface CaseNoteTypeSubTypeValid {

    String message() default "Invalid case note type/sub-type combination";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
