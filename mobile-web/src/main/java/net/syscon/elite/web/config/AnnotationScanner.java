package net.syscon.elite.web.config;

import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.config.BeanDefinition;
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
@Log4j
public class AnnotationScanner {

    public static Class[] findAnnotatedClasses(Class annotationClass, String[] scanPackages) {
        Objects.requireNonNull(annotationClass, "Annotation class must be specified.");
        Objects.requireNonNull(scanPackages, "Scan packages must be specified.");

        if (scanPackages.length == 0) {
            throw new IllegalArgumentException("At least one scan package must be specified.");
        }

        List<Class> annotatedClasses = new ArrayList<>();

        Arrays.asList(scanPackages).forEach(pkg -> {
            annotatedClasses.addAll(Arrays.asList(findAnnotatedClasses(annotationClass, pkg)));
        });

        return annotatedClasses.toArray(new Class[annotatedClasses.size()]);
    }

    public static Class[] findAnnotatedClasses(Class annotationClass, String scanPackage) {
        Objects.requireNonNull(annotationClass, "Annotation class must be specified.");
        Objects.requireNonNull(scanPackage, "Scan package must be specified.");

        List<Class> annotatedClasses = new ArrayList<>();

        ClassPathScanningCandidateComponentProvider provider = createComponentScanner(annotationClass);

        for (BeanDefinition beanDef : provider.findCandidateComponents(scanPackage)) {
            try {
                annotatedClasses.add(Class.forName(beanDef.getBeanClassName()));
            } catch (ClassNotFoundException e) {
                log.warn("Failed to locate annotated class:", e);
            }
        }

        return annotatedClasses.toArray(new Class[annotatedClasses.size()]);
    }

    private static ClassPathScanningCandidateComponentProvider createComponentScanner(Class annotationClass) {
        // Don't pull default filters (@Component, etc.):
        ClassPathScanningCandidateComponentProvider provider
                = new ClassPathScanningCandidateComponentProvider(false);

        provider.addIncludeFilter(new AnnotationTypeFilter(annotationClass));

        return provider;
    }
}
