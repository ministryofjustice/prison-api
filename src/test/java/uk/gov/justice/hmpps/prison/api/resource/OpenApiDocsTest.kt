package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.parser.OpenAPIV3Parser
import net.minidev.json.JSONArray
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.http.MediaType
import uk.gov.justice.hmpps.prison.api.resource.impl.ResourceTest
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AutoConfigureWebTestClient(timeout = "PT60S")
class OpenApiDocsTest : ResourceTest() {
  @LocalServerPort
  private val port: Int = 0

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
      .expectBody().jsonPath("info.version").value<String> {
        assertThat(it).startsWith(DateTimeFormatter.ISO_DATE.format(LocalDate.now()))
      }
  }

  @Test
  fun `the generated open api for date times hasn't got the time zone`() {
    webTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.components.schemas.OffenderIn.properties.movementDateTime.example").isEqualTo("2021-07-16T12:34:56")
      .jsonPath("$.components.schemas.OffenderIn.properties.movementDateTime.type").isEqualTo("string")
      .jsonPath("$.components.schemas.OffenderIn.properties.movementDateTime.format").isEqualTo("date-time")
      .jsonPath("$.components.schemas.OffenderIn.properties.movementDateTime.description").isEqualTo("Movement date time")
      .jsonPath("$.components.schemas.OffenderIn.properties.movementDateTime.pattern").doesNotExist()
  }

  @ParameterizedTest
  @CsvSource(value = ["bearer-jwt"])
  fun `the security scheme is setup for bearer tokens`(key: String) {
    webTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.components.securitySchemes.$key.type").isEqualTo("http")
      .jsonPath("$.components.securitySchemes.$key.scheme").isEqualTo("bearer")
      .jsonPath("$.components.securitySchemes.$key.description").value<String> {
        assertThat(it).contains("An HMPPS Auth access token.")
      }
      .jsonPath("$.components.securitySchemes.$key.bearerFormat").isEqualTo("JWT")
      .jsonPath("$.security[0].$key").isEqualTo(
        JSONArray().apply { addAll(listOf("read", "write")) },
      )
  }

  @Test
  fun `the open api json doesn't include LocalTime`() {
    webTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("components.schemas.LocalTime").doesNotExist()
  }

  @Test
  fun `the response contains required fields`() {
    webTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.components.schemas.ErrorResponse.required").value<List<String>> {
        assertThat(it).containsExactlyInAnyOrder("status", "userMessage")
      }
  }
}
