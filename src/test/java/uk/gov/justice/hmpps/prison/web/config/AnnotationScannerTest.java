package uk.gov.justice.hmpps.prison.web.config;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AnnotationScannerTest {
    @Test
    public void testFindAnnotatedClassesSinglePackageParameterValidation1() {
        assertThatThrownBy(() -> AnnotationScanner.findAnnotatedClasses(null, ""))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testFindAnnotatedClassesSinglePackageParameterValidation2() {
        final String nowt = null;

        assertThatThrownBy(() -> AnnotationScanner.findAnnotatedClasses(AnnotationScanner.class, nowt))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testFindAnnotatedClassesMultiplePackagesParameterValidation1() {
        final var packages = new String[]{"wibble", "wobble"};

        assertThatThrownBy(() -> AnnotationScanner.findAnnotatedClasses(null, packages))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testFindAnnotatedClassesMultiplePackagesParameterValidation2() {
        final String[] nowts = null;

        assertThatThrownBy(() -> AnnotationScanner.findAnnotatedClasses(AnnotationScanner.class, nowts))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testFindAnnotatedClassesSinglePackage4Self() {
        final var annotatedClasses = AnnotationScanner.findAnnotatedClasses(AnnotationScanner.class, AnnotationScanner.class.getPackage().getName());

        assertThat(annotatedClasses).isEmpty();
    }

    @Test
    public void testFindAnnotatedClassesSinglePackage4Deprecated() {
        final var annotatedClasses = AnnotationScanner.findAnnotatedClasses(Here.class, AnnotationScanner.class.getPackage().getName());

        assertThat(annotatedClasses.length).isEqualTo(1);

        assertThat(annotatedClasses[0]).isEqualTo(GuineaPig.class);
    }

    @Test
    public void testFindAnnotatedClassesMultiplePackages4Self() {
        final var packages = new String[]{AnnotationScanner.class.getPackage().getName(), "net.syscon.prison.not.a.real.package"};

        final var annotatedClasses = AnnotationScanner.findAnnotatedClasses(AnnotationScanner.class, packages);

        assertThat(annotatedClasses).isEmpty();
    }

    @Test
    public void testFindAnnotatedClassesMultiplePackages4Deprecated() {
        final var packages = new String[]{AnnotationScanner.class.getPackage().getName(), "net.syscon.prison.not.a.real.package"};

        final var annotatedClasses = AnnotationScanner.findAnnotatedClasses(Here.class, packages);

        assertThat(annotatedClasses).hasSize(1);

        assertThat(annotatedClasses[0]).isEqualTo(GuineaPig.class);
    }


    @Retention(RetentionPolicy.RUNTIME)
    @interface Here {

    }
}
