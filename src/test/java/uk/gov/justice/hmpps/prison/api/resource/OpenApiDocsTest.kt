package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.parser.OpenAPIV3Parser
import net.minidev.json.JSONArray
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.hmpps.prison.api.resource.impl.ResourceTest
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OpenApiDocsTest : ResourceTest() {
  @LocalServerPort
  private var port: Int = 0

  @Test
  fun `open api docs are available`() {
    webTestClient.get()
      .uri("/swagger-ui/index.html?configUrl=/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `open api docs redirect to correct page`() {
    webTestClient.get()
      .uri("/swagger-ui.html")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().is3xxRedirection
      .expectHeader().value("Location") { it.contains("/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config") }
  }

  @Test
  fun `the open api json contains documentation`() {
    webTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("paths").isNotEmpty
  }

  @Test
  fun `the open api json is valid and contains documentation`() {
    val result = OpenAPIV3Parser().readLocation("http://localhost:$port/v3/api-docs", null, null)
    assertThat(result.messages).isEmpty()
    assertThat(result.openAPI.paths).isNotEmpty
  }

  @Test
  fun `the open api json contains the version number`() {
    webTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("info.version").isEqualTo(DateTimeFormatter.ISO_DATE.format(LocalDate.now()))
  }

  @Test
  fun `the generated swagger for date times hasn't got the time zone`() {
    webTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.components.schemas.OffenderIn.properties.movementDateTime.example").isEqualTo("2021-07-05T10:35:17")
      .jsonPath("$.components.schemas.OffenderIn.properties.movementDateTime.type").isEqualTo("string")
      .jsonPath("$.components.schemas.OffenderIn.properties.movementDateTime.pattern")
      .isEqualTo("""^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}${'$'}""")
      .jsonPath("$.components.schemas.OffenderIn.properties.movementDateTime.format").doesNotExist()
  }

  @Test
  fun `the security scheme is setup for bearer tokens`() {
    val bearerJwts = JSONArray()
    bearerJwts.addAll(listOf("read", "write"))
    webTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.components.securitySchemes.bearer-jwt.type").isEqualTo("http")
      .jsonPath("$.components.securitySchemes.bearer-jwt.scheme").isEqualTo("bearer")
      .jsonPath("$.components.securitySchemes.bearer-jwt.bearerFormat").isEqualTo("JWT")
      .jsonPath("$.security[0].bearer-jwt")
      .isEqualTo(bearerJwts)
  }

  @Test
  fun `the open api json doesn't include LocalTime`() {
    webTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("components.schemas.LocalTime").doesNotExist()
  }
}
