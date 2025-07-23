package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.oas.annotations.media.Schema
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.hmpps.prison.web.config.AnnotationScanner.findAnnotatedClasses

class OpenApiDocsDuplicateTest {
  @Test
  fun `Schemas need to have unique names - duplicates do not render correctly`() {
    val duplicates = findAnnotatedClasses(
      Schema::class.java,
      arrayOf("uk.gov.justice.hmpps.prison.api"),
    ).map {
      val schema = it.annotations.find { it is Schema } as Schema
      schema.name.ifEmpty { it.simpleName }
    }
      .groupBy { it }.filter { it.value.size > 1 }.values

    assertThat(duplicates).isEmpty()
  }
}
