package uk.gov.justice.hmpps.prison.api.resource.impl

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.GLOBAL_SEARCH

class PrisonerResourceTest : ResourceTest() {
  @Nested
  @DisplayName("GET /api/prisoners")
  inner class GetPrisoners {

    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.get().uri("/api/prisoners?offenderNo=A1234AC")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 when there is no override role`() {
      webTestClient.get().uri("/api/prisoners?offenderNo=A1234AC")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns success if has override ROLE_GLOBAL_SEARCH`() {
      webTestClient.get().uri("/api/prisoners?offenderNo=A1234AC")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("[0].offenderNo").isEqualTo("A1234AC")
    }

    @Test
    fun `returns 403 if has override SYSTEM_USER`() {
      webTestClient.get().uri("/api/prisoners?offenderNo=A1234AC")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun testReturnEmptyArrayWhenOffenderNotFound() {
      val httpEntity = createEmptyHttpEntity(GLOBAL_SEARCH)
      val response = testRestTemplate.exchange(
        "/api/prisoners?ofenderNo=A1476AE",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )
      assertThatJsonAndStatus(response, OK.value(), "[]")
    }

    @Test
    fun testCanFindMultiplePrisoners() {
      val httpEntity = createEmptyHttpEntity(GLOBAL_SEARCH)
      val response = testRestTemplate.exchange(
        "/api/prisoners?offenderNo=A1234AC&offenderNo=A1234AA",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )
      assertThatJsonFileAndStatus(response, OK, "prisoners_multiple.json")
    }

    @Test
    fun testCanFindMultiplePrisonersAndFilterByMoreThanOneCriteria() {
      val httpEntity = createEmptyHttpEntity(GLOBAL_SEARCH)
      val response = testRestTemplate.exchange(
        "/api/prisoners?offenderNo=A1181MV&offenderNo=A1234AC&offenderNo=A1234AA&lastName=BATES",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )
      assertThatJsonFileAndStatus(response, OK, "prisoners_single.json")
    }

    @Test
    fun testReturn403WhenDoesNotHavePrivs() {
      val httpEntity = createEmptyHttpEntity(AuthToken.NO_CASELOAD_USER)
      val response = testRestTemplate.exchange(
        "/api/prisoners?offenderNo=A1234AC&offenderNo=A1234AA",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )
      assertThatStatus(response, FORBIDDEN)
    }
  }

  @Nested
  @DisplayName("POST /api/prisoners")
  inner class PostPrisoners {

    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.post().uri("/api/prisoners")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue("{ \"offenderNos\": [ \"A1234AA\" ] }")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 when client has no override role`() {
      webTestClient.post().uri("/api/prisoners")
        .headers(setClientAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue("{ \"offenderNos\": [ \"A1234AA\" ] }")
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns success when client has override role ROLE_GlOBAL_SEARCH`() {
      webTestClient.post().uri("/api/prisoners")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue("{ \"offenderNos\": [ \"A1234AA\" ] }")
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("length()").isEqualTo(1)
    }

    @Test
    fun testCanFindMultiplePrisonersUsingPost() {
      val token = authTokenHelper.getToken(GLOBAL_SEARCH)
      val httpEntity = createHttpEntity(token, "{ \"offenderNos\": [ \"A1234AC\", \"A1234AA\" ] }")
      val response = testRestTemplate.exchange(
        "/api/prisoners",
        POST,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )
      assertThatJsonFileAndStatus(response, OK, "prisoners_multiple.json")
    }

    @Test
    fun testCanFindMultiplePrisonersAndFilterByMoreThanOneCriteria() {
      val token = authTokenHelper.getToken(GLOBAL_SEARCH)
      val httpEntity = createHttpEntity(
        token,
        "{ \"offenderNos\": [ \"A1181MV\", \"A1234AC\", \"A1234AA\" ], \"lastName\": \"BATES\" }",
      )
      val response = testRestTemplate.exchange(
        "/api/prisoners",
        POST,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )
      assertThatJsonFileAndStatus(response, OK, "prisoners_single.json")
    }
  }

  @Nested
  @DisplayName("GET /api/prisoners/{offenderNo}/full-status")
  inner class GetPrisonerFullStatus {

    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.get().uri("/api/prisoners/A1234AA/full-status")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 when client does not have any roles`() {
      webTestClient.get().uri("/api/prisoners/A1234AA/full-status")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 403 when client has override role ROLE_APPROVE_CATEGORISATION`() {
      webTestClient.get().uri("/api/prisoners/A1234AA/full-status")
        .headers(setClientAuthorisation(listOf("ROLE_APPROVE_CATEGORISATION")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 403 when client has override role ROLE_SYSTEM_USER`() {
      webTestClient.get().uri("/api/prisoners/A1234AA/full-status")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 200 when client has override role ROLE_GLOBAL_SEARCH`() {
      webTestClient.get().uri("/api/prisoners/A1234AA/full-status")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns 200 when client has override role ROLE_VIEW_PRISONER_DATA`() {
      webTestClient.get().uri("/api/prisoners/A1234AA/full-status")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun testCanReturnPrisonerInformationByNomsId() {
      val httpEntity = createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA)
      val response = testRestTemplate.exchange(
        "/api/prisoners/A1234AA/full-status",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )
      assertThatJsonFileAndStatus(response, OK, "prisoners_information_A1234AA.json")
    }

    @Test
    fun testCanReturnPrisonerInformationByNomsIdWhenReleased() {
      val httpEntity = createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA)
      val response = testRestTemplate.exchange(
        "/api/prisoners/Z0023ZZ/full-status",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )
      assertThatJsonFileAndStatus(response, OK, "prisoners_information_Z0023ZZ.json")
    }

    @Test
    fun `does not return released prisoner release date when client does not have override role ROLE_INACTIVE_BOOKINGS`() {
      webTestClient.get().uri("/api/prisoners/Z0020ZZ/full-status")
        .headers(setClientAuthorisation(listOf("VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("establishmentCode").isEqualTo("OUT")
        .jsonPath("releaseDate").doesNotExist()
    }

    @Test
    fun `returns released prisoner release date when client has override role ROLE_INACTIVE_BOOKINGS`() {
      webTestClient.get().uri("/api/prisoners/Z0020ZZ/full-status")
        .headers(setClientAuthorisation(listOf("VIEW_PRISONER_DATA", "INACTIVE_BOOKINGS")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("establishmentCode").isEqualTo("OUT")
        .jsonPath("releaseDate").isNotEmpty
    }

    @Test
    fun `does not return released prisoner release date when client has override role ROLE_SYSTEM_USER`() {
      webTestClient.get().uri("/api/prisoners/Z0020ZZ/full-status")
        .headers(setClientAuthorisation(listOf("VIEW_PRISONER_DATA", "SYSTEM_USER")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("establishmentCode").isEqualTo("OUT")
        .jsonPath("releaseDate").doesNotExist()
    }

    @Test
    fun testReturn404WhenOffenderNotFound() {
      val httpEntity = createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA)
      val response = testRestTemplate.exchange(
        "/api/prisoners/X1111XX/full-status",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )
      assertThatStatus(response, NOT_FOUND)
    }

    @Test
    fun testReturn404WhenDoesNotHavePrivs() {
      val httpEntity = createEmptyHttpEntity(AuthToken.NO_CASELOAD_USER)
      val response = testRestTemplate.exchange(
        "/api/prisoners/A1234AA/full-status",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )
      assertThatStatus(response, NOT_FOUND)
    }
  }

  @Nested
  @DisplayName("GET /api/prisoners/{offenderNo}")
  inner class GetPrisoner {
    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.get().uri("/api/prisoners/A1234AC")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 when there is no override role`() {
      webTestClient.get().uri("/api/prisoners/A1234AC")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns success if has override ROLE_VIEW_PRISONER_DATA`() {
      webTestClient.get().uri("/api/prisoners/A1234AC")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("[0].offenderNo").isEqualTo("A1234AC")
    }

    @Test
    fun testCanReturnPrisonerInformationByNomsId() {
      val httpEntity = createEmptyHttpEntity(AuthToken.VIEW_PRISONER_DATA)
      val response = testRestTemplate.exchange(
        "/api/prisoners/A1234AC",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )
      assertThatJsonFileAndStatus(response, OK, "prisoners_single.json")
    }

    @Test
    fun testCanReturnPrisonerInformationByNomsIdWhenReleased() {
    }

    @Test
    fun testReturn404WhenOffenderNotFound() {
      webTestClient.get().uri("/api/prisoners/X1111XX")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isNotFound
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Resource with id [X1111XX] not found.")
    }

    @Test
    fun `returns success if in user caseload`() {
      webTestClient.get().uri("/api/prisoners/A1234AC")
        .headers(setAuthorisation(listOf())).exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("[0].offenderNo").isEqualTo("A1234AC")
    }

    @Test
    fun testReturn403WhenDoesNotHavePrivs() {
      webTestClient.get().uri("/api/prisoners/A1234AA")
        .headers(setClientAuthorisation(emptyList()))
        .exchange()
        .expectStatus().isForbidden
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Client not authorised to access booking with id -1.")
    }
  }

  @Nested
  @DisplayName("GET /api/prisoners/prisoner-numbers")
  inner class GetPrisonerNumbers {

    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.get().uri("/api/prisoners?offenderNo=A1234AC")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 when there is no override role`() {
      webTestClient.get().uri("/api/prisoners/prisoner-numbers")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 403 if has override role SYSTEM_USER`() {
      webTestClient.get().uri("/api/prisoners/prisoner-numbers")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns success if has override role ROLE_GLOBAL_SEARCH`() {
      webTestClient.get().uri("/api/prisoners/prisoner-numbers")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns success if has override role ROLE_PRISONER_INDEX`() {
      webTestClient.get().uri("/api/prisoners/prisoner-numbers")
        .headers(setClientAuthorisation(listOf("ROLE_PRISONER_INDEX")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns success if has override role ROLE_PRISON_API__CORE_PERSON__NUMBERS__RO`() {
      webTestClient.get().uri("/api/prisoners/prisoner-numbers")
        .headers(setClientAuthorisation(listOf("ROLE_PRISON_API__CORE_PERSON__NUMBERS__RO")))
        .exchange()
        .expectStatus().isOk
    }

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
  }
}
