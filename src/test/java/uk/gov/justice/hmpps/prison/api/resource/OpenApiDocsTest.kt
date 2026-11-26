package uk.gov.justice.hmpps.prison.api.resource

import io.swagger.v3.parser.OpenAPIV3Parser
import net.minidev.json.JSONArray
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.info.BuildProperties
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.http.MediaType
import uk.gov.justice.hmpps.prison.api.resource.impl.ResourceTest
import kotlin.text.get

@AutoConfigureWebTestClient(timeout = "PT60S")
class OpenApiDocsTest(
  @Autowired private val buildProperties: BuildProperties,
  @LocalServerPort private val port: Int = 0,
) : ResourceTest() {

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
      .expectBody().jsonPath("info.version").isEqualTo(buildProperties.version)
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
      .jsonPath("$.components.schemas.OffenderIn.properties.movementDateTime.type").isEqualTo(["string", "null"])
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

  @ParameterizedTest
  @CsvSource(
    value = [
      "/api/agencies,/api/agencies",
      "/api/agencies/{agencyId},/api/agencies/{agencyId}",
      "/api/agencies/type/{type},/api/agencies/legacy-type/{type}",
      "/api/agencies/prisons,/api/agencies/legacy-type/INST",
      "/api/agencies/prison,/api/agencies/legacy-type/INST",
    ],
  )
  fun `the agency endpoints that HMPPS Registers replaces are deprecated and name the replacement`(path: String, replacement: String) {
    webTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.paths['$path'].get.deprecated").isEqualTo(true)
      .jsonPath("$.paths['$path'].get.description").value<String> {
        assertThat(it).contains("https://agency-register.hmpps.service.justice.gov.uk$replacement")
        assertThat(it).contains("HMPPS_REGISTERS_API__R")
      }
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "/api/agencies/prison/{agencyId}",
      "/api/agencies/{agencyId}/receptionsWithCapacity",
      "/api/agencies/{agencyId}/eventLocationsBooked",
      "/api/agencies/{agencyId}/pay-profile",
    ],
  )
  fun `the agency endpoints that HMPPS Registers does not replace are not deprecated`(path: String) {
    webTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.paths['$path'].get.summary").exists()
      .jsonPath("$.paths['$path'].get.deprecated").doesNotExist()
  }

  @Test
  fun `the swagger json don't contain any duplicate methods`() {
    webTestClient.get()
      .uri("/v3/api-docs")
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("*..operationId").value<List<String>> { list ->
        assertThat(list).filteredOn { it.contains("_") }.isEmpty()
      }
  }
}
