package uk.gov.justice.hmpps.prison.web.config

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.hmpps.prison.web.config.AnnotationScannerTest.Here

class AnnotationScannerTest {
  @Test
  fun testFindAnnotatedClassesSinglePackageParameterValidation1() {
    assertThatThrownBy { AnnotationScanner.findAnnotatedClasses(null, "") }
      .isInstanceOf(NullPointerException::class.java)
  }

  @Test
  fun testFindAnnotatedClassesSinglePackageParameterValidation2() {
    val nowt: String? = null

    assertThatThrownBy {
      AnnotationScanner.findAnnotatedClasses(
        AnnotationScanner::class.java,
        nowt,
      )
    }
      .isInstanceOf(NullPointerException::class.java)
  }

  @Test
  fun testFindAnnotatedClassesMultiplePackagesParameterValidation1() {
    val packages = arrayOf("wibble", "wobble")

    assertThatThrownBy {
      AnnotationScanner.findAnnotatedClasses(
        null,
        packages,
      )
    }
      .isInstanceOf(NullPointerException::class.java)
  }

  @Test
  fun testFindAnnotatedClassesMultiplePackagesParameterValidation2() {
    val nowts: Array<String>? = null

    assertThatThrownBy {
      AnnotationScanner.findAnnotatedClasses(
        AnnotationScanner::class.java,
        nowts,
      )
    }
      .isInstanceOf(NullPointerException::class.java)
  }

  @Test
  fun testFindAnnotatedClassesSinglePackage4Self() {
    val annotatedClasses = AnnotationScanner.findAnnotatedClasses(
      AnnotationScanner::class.java,
      AnnotationScanner::class.java.getPackage().name,
    )

    assertThat(annotatedClasses).isEmpty()
  }

  @Test
  fun testFindAnnotatedClassesSinglePackage4Deprecated() {
    val annotatedClasses =
      AnnotationScanner.findAnnotatedClasses(Here::class.java, AnnotationScanner::class.java.getPackage().name)

    assertThat(annotatedClasses).hasSize(1)

    assertThat(annotatedClasses[0]).isEqualTo(GuineaPig::class.java)
  }

  @Test
  fun testFindAnnotatedClassesMultiplePackages4Self() {
    val packages =
      arrayOf<String>(AnnotationScanner::class.java.getPackage().name, "net.syscon.prison.not.a.real.package")

    val annotatedClasses = AnnotationScanner.findAnnotatedClasses(AnnotationScanner::class.java, packages)

    assertThat(annotatedClasses).isEmpty()
  }

  @Test
  fun testFindAnnotatedClassesMultiplePackages4Deprecated() {
    val packages =
      arrayOf<String>(AnnotationScanner::class.java.getPackage().name, "net.syscon.prison.not.a.real.package")

    val annotatedClasses = AnnotationScanner.findAnnotatedClasses(Here::class.java, packages)

    assertThat(annotatedClasses).hasSize(1)

    assertThat(annotatedClasses[0]).isEqualTo(GuineaPig::class.java)
  }

  @Retention(AnnotationRetention.RUNTIME)
  internal annotation class Here
}

/**
 * This is a 'guinea pig' class which only exists to test the annotation scanner.
 */
@Here
class GuineaPig
