package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.parser.OpenAPIV3Parser
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.hmpps.prison.api.resource.impl.ResourceTest

class OpenApiDocsTest : ResourceTest() {
  @Test
  fun `open api docs are available`() {
    val response = testRestTemplate.getForEntity("/swagger-ui/?configUrl=/api/swagger.json", String::class.java)
    assertThatStatus(response, 200)
  }

  @Test
  fun `the open api json is nearly valid and contains documentation`() {
    val result = OpenAPIV3Parser().readLocation("${testRestTemplate.rootUri}/api/swagger.json", null, null)
    assertThat(result.openAPI.paths).isNotEmpty
    assertThat(result.messages).isEmpty()
  }
}
