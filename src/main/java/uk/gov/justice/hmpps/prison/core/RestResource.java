package uk.gov.justice.hmpps.prison.core;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A custom annotation to stereotype Spring's component annotation for Restful resource implementations.
 * <p>
 * This is needed to work around integration issues between Jersey 2 (spring-jersey3 library) and Spring. If these are
 * ever resolved, it should be possible to revert back to using @Component annotation and removing any code that
 * utilises this custom annotation.
 *
 * @author andrewk
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface RestResource {
    String value() default "";
}
