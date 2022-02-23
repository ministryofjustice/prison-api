package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.parser.OpenAPIV3Parser
import net.javacrumbs.jsonunit.assertj.assertThatJson
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.api.resource.impl.ResourceTest
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OpenApiDocsTest : ResourceTest() {
  @LocalServerPort
  private var port: Int = 0

  @Test
  fun `open api docs are available`() {
    val response = testRestTemplate.getForEntity("/swagger-ui/index.html?configUrl=/v3/api-docs", String::class.java)
    assertThatStatus(response, 200)
  }

  @Test
  fun `open api docs redirect to correct page`() {
    val response = testRestTemplate.getForEntity("/swagger-ui.html", String::class.java)
    assertThatStatus(response, 302)
    assertThat(response.headers["Location"]).contains("/swagger-ui/index.html")
  }

  @Test
  fun `the swagger json is valid`() {
    val response = testRestTemplate.getForEntity("/v3/api-docs", String::class.java)
    assertThatStatus(response, 200)
    assertThatJson(response.body!!).inPath(".messages").isArray.isEmpty()
  }

  @Test
  fun `the open api json is valid and contains documentation`() {
    val result = OpenAPIV3Parser().readLocation("http://localhost:$port/v3/api-docs", null, null)
    assertThat(result.messages).isEmpty()
    assertThat(result.openAPI.paths).isNotEmpty
  }

  @Test
  fun `the swagger json contains the version number`() {
    val response = testRestTemplate.getForEntity("/v3/api-docs", String::class.java)
    assertThatStatus(response, 200)
    assertThatJson(response.body!!).inPath(".info.version")
      .isArray.asList().containsExactly(DateTimeFormatter.ISO_DATE.format(LocalDate.now()))
  }
}
