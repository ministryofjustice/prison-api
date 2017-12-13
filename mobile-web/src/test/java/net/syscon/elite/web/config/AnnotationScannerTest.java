package net.syscon.elite.web.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AnnotationScannerTest {
    @Test(expected = NullPointerException.class)
    public void testFindAnnotatedClassesSinglePackageParameterValidation1() {
        AnnotationScanner.findAnnotatedClasses(null, "");
    }

    @Test(expected = NullPointerException.class)
    public void testFindAnnotatedClassesSinglePackageParameterValidation2() {
        String nowt = null;

        AnnotationScanner.findAnnotatedClasses(AnnotationScanner.class, nowt);
    }

    @Test(expected = NullPointerException.class)
    public void testFindAnnotatedClassesMultiplePackagesParameterValidation1() {
        String[] packages = new String[] { "wibble", "wobble" };

        AnnotationScanner.findAnnotatedClasses(null, packages);
    }

    @Test(expected = NullPointerException.class)
    public void testFindAnnotatedClassesMultiplePackagesParameterValidation2() {
        String[] nowts = null;

        AnnotationScanner.findAnnotatedClasses(AnnotationScanner.class, nowts);
    }

    @Test
    public void testFindAnnotatedClassesSinglePackage4Self() {
        Class[] annotatedClasses = AnnotationScanner.findAnnotatedClasses(AnnotationScanner.class, AnnotationScanner.class.getPackage().getName());

        assertEquals(0, annotatedClasses.length);
    }

    @Test
    public void testFindAnnotatedClassesSinglePackage4Deprecated() {
        Class[] annotatedClasses = AnnotationScanner.findAnnotatedClasses(Deprecated.class, AnnotationScanner.class.getPackage().getName());

        assertEquals(1, annotatedClasses.length);

        assertEquals(GuineaPig.class, annotatedClasses[0]);
    }

    @Test
    public void testFindAnnotatedClassesMultiplePackages4Self() {
        String[] packages = new String[] {AnnotationScanner.class.getPackage().getName(), "net.syscon.elite.not.a.real.package"};

        Class[] annotatedClasses = AnnotationScanner.findAnnotatedClasses(AnnotationScanner.class, packages);

        assertEquals(0, annotatedClasses.length);
    }

    @Test
    public void testFindAnnotatedClassesMultiplePackages4Deprecated() {
        String[] packages = new String[] {AnnotationScanner.class.getPackage().getName(), "net.syscon.elite.not.a.real.package"};

        Class[] annotatedClasses = AnnotationScanner.findAnnotatedClasses(Deprecated.class, packages);

        assertEquals(1, annotatedClasses.length);

        assertEquals(GuineaPig.class, annotatedClasses[0]);
    }
}
