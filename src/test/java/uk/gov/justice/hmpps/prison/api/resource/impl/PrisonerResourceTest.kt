package uk.gov.justice.hmpps.prison.api.resource.impl

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken

class PrisonerResourceTest : ResourceTest() {
  @Nested
  @DisplayName("GET /api/prisoners")
  inner class GetPrisoners {
    @Test
    fun testCanFindMultiplePrisoners() {
      val httpEntity = createEmptyHttpEntity(AuthToken.GLOBAL_SEARCH)
      val response = testRestTemplate.exchange(
        "/api/prisoners?offenderNo=A1234AC&offenderNo=A1234AA",
        HttpMethod.GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )
      assertThatJsonFileAndStatus(response, HttpStatus.OK, "prisoners_multiple.json")
    }

    @Test
    fun testCanFindMultiplePrisonersAndFilterByMoreThanOneCriteria() {
      val httpEntity = createEmptyHttpEntity(AuthToken.GLOBAL_SEARCH)
      val response = testRestTemplate.exchange(
        "/api/prisoners?offenderNo=A1181MV&offenderNo=A1234AC&offenderNo=A1234AA&lastName=BATES",
        HttpMethod.GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )
      assertThatJsonFileAndStatus(response, HttpStatus.OK, "prisoners_single.json")
    }
  }

  @Nested
  @DisplayName("POST /api/prisoners")
  inner class PostPrisoners {
    @Test
    fun testCanFindMultiplePrisonersUsingPost() {
      val token = authTokenHelper.getToken(AuthToken.GLOBAL_SEARCH)
      val httpEntity = createHttpEntity(token, "{ \"offenderNos\": [ \"A1234AC\", \"A1234AA\" ] }")
      val response = testRestTemplate.exchange(
        "/api/prisoners",
        HttpMethod.POST,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )
      assertThatJsonFileAndStatus(response, HttpStatus.OK, "prisoners_multiple.json")
    }

    @Test
    fun testCanFindMultiplePrisonersAndFilterByMoreThanOneCriteria() {
      val token = authTokenHelper.getToken(AuthToken.GLOBAL_SEARCH)
      val httpEntity = createHttpEntity(
        token,
        "{ \"offenderNos\": [ \"A1181MV\", \"A1234AC\", \"A1234AA\" ], \"lastName\": \"BATES\" }",
      )
      val response = testRestTemplate.exchange(
        "/api/prisoners",
        HttpMethod.POST,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )
      assertThatJsonFileAndStatus(response, HttpStatus.OK, "prisoners_single.json")
    }
  }

  @Nested
  @DisplayName("GET /api/prisoners/{offenderNo}/full-status")
  inner class GetPrisonerFullStatus {
    @Test
    fun testCanReturnPrisonerInformationByNomsId() {
      val token = authTokenHelper.getToken(AuthToken.VIEW_PRISONER_DATA)
      val httpEntity = createHttpEntity(token, null, emptyMap())
      val response = testRestTemplate.exchange(
        "/api/prisoners/A1234AA/full-status",
        HttpMethod.GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )
      assertThatJsonFileAndStatus(response, HttpStatus.OK, "prisoners_information_A1234AA.json")
    }

    @Test
    fun testCanReturnPrisonerInformationByNomsIdWhenReleased() {
      val token = authTokenHelper.getToken(AuthToken.VIEW_PRISONER_DATA)
      val httpEntity = createHttpEntity(token, null, emptyMap())
      val response = testRestTemplate.exchange(
        "/api/prisoners/Z0023ZZ/full-status",
        HttpMethod.GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )
      assertThatJsonFileAndStatus(response, HttpStatus.OK, "prisoners_information_Z0023ZZ.json")
    }

    @Test
    fun testReturn404WhenOffenderNotFound() {
      val token = authTokenHelper.getToken(AuthToken.VIEW_PRISONER_DATA)
      val httpEntity = createHttpEntity(token, null, emptyMap())
      val response = testRestTemplate.exchange(
        "/api/prisoners/X1111XX/full-status",
        HttpMethod.GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )
      assertThatStatus(response, HttpStatus.NOT_FOUND)
    }

    @Test
    fun testReturn404WhenDoesNotHavePrivs() {
      val token = authTokenHelper.getToken(AuthToken.NO_CASELOAD_USER)
      val httpEntity = createHttpEntity(token, null, emptyMap())
      val response = testRestTemplate.exchange(
        "/api/prisoners/A1234AA/full-status",
        HttpMethod.GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )
      assertThatStatus(response, HttpStatus.NOT_FOUND)
    }
  }

  @Nested
  @DisplayName("GET /api/prisoners/{offenderNo}")
  inner class GetPrisoner {
    @Test
    fun testCanReturnPrisonerInformationByNomsId() {
      val token = authTokenHelper.getToken(AuthToken.VIEW_PRISONER_DATA)
      val httpEntity = createHttpEntity(token, null, emptyMap())
      val response = testRestTemplate.exchange(
        "/api/prisoners/A1234AC",
        HttpMethod.GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )
      assertThatJsonFileAndStatus(response, HttpStatus.OK, "prisoners_single.json")
    }

    @Test
    fun testCanReturnPrisonerInformationByNomsIdWhenReleased() {
    }

    @Test
    fun testReturnEmptyArrayWhenOffenderNotFound() {
      val token = authTokenHelper.getToken(AuthToken.VIEW_PRISONER_DATA)
      val httpEntity = createHttpEntity(token, null, emptyMap())
      val response = testRestTemplate.exchange(
        "/api/prisoners/X1111XX",
        HttpMethod.GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )
      assertThatJsonAndStatus(response, HttpStatus.OK.value(), "[]")
    }

    @Test
    @Disabled("SDIT-910: No access restrictions currently on this endpoint")
    fun testReturnEmptyArrayWhenDoesNotHavePrivs() {
      val token = authTokenHelper.getToken(AuthToken.NO_CASELOAD_USER)
      val httpEntity = createHttpEntity(token, null, emptyMap())
      val response = testRestTemplate.exchange(
        "/api/prisoners/A1234AA",
        HttpMethod.GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )
      assertThatJsonAndStatus(response, HttpStatus.OK.value(), "[]")
    }
  }

  @Nested
  @DisplayName("GET /api/prisoners/prisoner-numbers")
  inner class GetPrisonerNumbers {
    @Test
    fun `can return prisoner numbers`() {
      webTestClient.get()
        .uri("/api/prisoners/prisoner-numbers")
        .headers(setAuthorisation(listOf("ROLE_PRISONER_INDEX")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("content").value<List<String>> {
          assertThat(it)
            .hasSize(10)
            .startsWith("A1234AN", "A1234AO")
            .doesNotContain("A1181FF")
        }
    }

    @Test
    fun `can specify page size`() {
      webTestClient.get()
        .uri("/api/prisoners/prisoner-numbers?size=200")
        .headers(setAuthorisation(listOf("ROLE_PRISONER_INDEX")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("pageable.offset").isEqualTo(0)
        .jsonPath("pageable.pageNumber").isEqualTo(0)
        .jsonPath("pageable.pageSize").isEqualTo(200)
        .jsonPath("numberOfElements").value<Int> { assertThat(it).isGreaterThan(30) }
        .jsonPath("totalElements").value<Int> { assertThat(it).isGreaterThan(30) }
        .jsonPath("totalPages").isEqualTo(1)
    }

    @Test
    fun `can return total count and page sizes`() {
      webTestClient.get()
        .uri("/api/prisoners/prisoner-numbers")
        .headers(setAuthorisation(listOf("ROLE_PRISONER_INDEX")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("pageable.offset").isEqualTo(0)
        .jsonPath("pageable.pageNumber").isEqualTo(0)
        .jsonPath("pageable.pageSize").isEqualTo(10)
        .jsonPath("numberOfElements").isEqualTo(10)
        .jsonPath("totalElements").value<Int> { assertThat(it).isGreaterThan(30) }
        .jsonPath("totalPages").value<Int> { assertThat(it).isGreaterThan(1) }
    }

    @Test
    fun `can return other pages`() {
      webTestClient.get()
        .uri("/api/prisoners/prisoner-numbers?page=1")
        .headers(setAuthorisation(listOf("ROLE_PRISONER_INDEX")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("pageable.offset").isEqualTo(10)
        .jsonPath("pageable.pageNumber").isEqualTo(1)
        .jsonPath("pageable.pageSize").isEqualTo(10)
        .jsonPath("numberOfElements").isEqualTo(10)
        .jsonPath("totalElements").value<Int> { assertThat(it).isGreaterThan(30) }
        .jsonPath("totalPages").value<Int> { assertThat(it).isGreaterThan(1) }
        .jsonPath("content").value<List<String>> {
          assertThat(it)
            .hasSize(10)
            .contains("A1181FF")
            .doesNotContain("A1234AN", "A1234AO")
        }
    }

    @Test
    fun testReturn404WhenDoesNotHavePrivs() {
      webTestClient.get()
        .uri("/api/prisoners/prisoner-numbers")
        .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isForbidden
    }
  }
}
