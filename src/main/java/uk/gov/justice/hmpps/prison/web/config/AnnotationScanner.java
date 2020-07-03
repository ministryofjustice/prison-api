package uk.gov.justice.hmpps.prison.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A utility class with methods to locate classes within one or more packages that are annotated with a specified
 * annotation.
 *
 * @author andrewk
 * @since 1.0.15
 */
@Slf4j
public class AnnotationScanner {

    public static Class[] findAnnotatedClasses(final Class annotationClass, final String[] scanPackages) {
        Objects.requireNonNull(annotationClass, "Annotation class must be specified.");
        Objects.requireNonNull(scanPackages, "Scan packages must be specified.");

        if (scanPackages.length == 0) {
            throw new IllegalArgumentException("At least one scan package must be specified.");
        }

        final List<Class> annotatedClasses = new ArrayList<>();

        Arrays.asList(scanPackages).forEach(pkg -> {
            annotatedClasses.addAll(locateAnnotatedClasses(annotationClass, pkg));
        });

        return annotatedClasses.toArray(new Class[0]);
    }

    public static Class[] findAnnotatedClasses(final Class annotationClass, final String scanPackage) {
        Objects.requireNonNull(annotationClass, "Annotation class must be specified.");

        final var annotatedClasses = locateAnnotatedClasses(annotationClass, scanPackage);

        return annotatedClasses.toArray(new Class[0]);
    }

    private static List<Class> locateAnnotatedClasses(final Class annotationClass, final String scanPackage) {
        Objects.requireNonNull(scanPackage, "Scan package must be specified.");

        final List<Class> annotatedClasses = new ArrayList<>();

        final var provider = createComponentScanner(annotationClass);

        for (final var beanDef : provider.findCandidateComponents(scanPackage)) {
            try {
                annotatedClasses.add(Class.forName(beanDef.getBeanClassName()));
            } catch (final ClassNotFoundException e) {
                log.warn("Failed to locate annotated class:", e);
            }
        }

        return annotatedClasses;
    }

    private static ClassPathScanningCandidateComponentProvider createComponentScanner(final Class annotationClass) {
        // Don't pull default filters (@Component, etc.):
        final var provider
                = new ClassPathScanningCandidateComponentProvider(false);

        provider.addIncludeFilter(new AnnotationTypeFilter(annotationClass));

        return provider;
    }
}
