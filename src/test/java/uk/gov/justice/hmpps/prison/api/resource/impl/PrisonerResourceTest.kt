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

    @Nested
    inner class Authorisation {
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
    }

    @Test
    fun testReturnEmptyArrayWhenOffenderNotFound() {
      val httpEntity = createEmptyHttpEntity(GLOBAL_SEARCH)
      val response = testRestTemplate.exchange(
        "/api/prisoners?ofenderNo=A1476AE",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String>() {},
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
        object : ParameterizedTypeReference<String>() {},
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
        object : ParameterizedTypeReference<String>() {},
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
        object : ParameterizedTypeReference<String>() {},
      )
      assertThatStatus(response, FORBIDDEN)
    }

    @Test
    fun `returns success if searching using date-of-birth and paging`() {
      webTestClient.get().uri("/api/prisoners?dobFrom=1970-01-01")
        .header("Page-Offset", "0")
        .header("Page-Limit", "15")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("size()").isEqualTo(14)
    }

    @Test
    fun `returns success if searching using date-of-birth and limited paging`() {
      webTestClient.get().uri("/api/prisoners?dobFrom=1970-01-01")
        .header("Page-Offset", "0")
        .header("Page-Limit", "2")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("size()").isEqualTo(2)
    }

    @Test
    fun `returns 400 if invalid PNC number`() {
      webTestClient.get().uri("/api/prisoners?pncNumber=234/EE45FX")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Incorrectly formatted PNC number.")
    }

    @Test
    fun `returns success and results if valid PNC number`() {
      webTestClient.get().uri("/api/prisoners?pncNumber=1998/1234567L")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("size()").isEqualTo(1)
        .jsonPath("[0].firstName").isEqualTo("CHARLES")
        .jsonPath("[0].lastName").isEqualTo("CHAPLIN")
    }

    @Test
    fun `returns success and no results if valid PNC number`() {
      webTestClient.get().uri("/api/prisoners?pncNumber=1898/1234567L")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("[]")
    }

    @Test
    fun `returns success when searching for OUT prisoners and by lastName`() {
      webTestClient.get().uri("/api/prisoners?location=OUT&lastName=ELBOW")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("size()").isEqualTo(1)
    }

    @Test
    fun `returns success when searching for IN prisoners and by lastName`() {
      webTestClient.get().uri("/api/prisoners?location=IN&lastName=ELBOW")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("[]")
    }

    @Test
    fun `returns success when searching for ALL prisoners and by lastName`() {
      webTestClient.get().uri("/api/prisoners?location=ALL&lastName=ELBOW")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("size()").isEqualTo(1)
    }

    @Test
    fun `returns 400 when searching with invalid gender code`() {
      webTestClient.get().uri("/api/prisoners?gender=ABC&lastName=SARLY")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Gender filter value ABC not recognised.")
    }

    @Test
    fun `returns 400 when searching with female gender code`() {
      webTestClient.get().uri("/api/prisoners?gender=F&lastName=ELBOW")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("size()").isEqualTo(1)
    }

    @Test
    fun `returns 400 when searching with male gender code`() {
      webTestClient.get().uri("/api/prisoners?gender=M&lastName=SARLY")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("size()").isEqualTo(2)
    }

    @Test
    fun `returns success when searching by CRO Number`() {
      webTestClient.get().uri("/api/prisoners?croNumber=CRO112233")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("size()").isEqualTo(1)
        .jsonPath("[0].firstName").isEqualTo("NORMAN")
        .jsonPath("[0].lastName").isEqualTo("BATES")
    }

    @Test
    fun `returns success with multiple results when searching by Date of Birth`() {
      webTestClient.get().uri("/api/prisoners?dob=1970-01-01")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("size()").isEqualTo(2)
        .jsonPath("[0].lastName").isEqualTo("CHAPLIN")
        .jsonPath("[1].lastName").isEqualTo("TALBOT")
    }

    @Test
    fun `returns success with single results when searching by Date of Birth`() {
      webTestClient.get().uri("/api/prisoners?dob=1999-10-27")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("size()").isEqualTo(1)
        .jsonPath("[0].lastName").isEqualTo("BATES")
    }

    @Test
    fun `returns success with no results when searching by Date of Birth`() {
      webTestClient.get().uri("/api/prisoners?dob=1959-10-28")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("[]")
    }

    @Test
    fun `returns success when searching by names, with partial name matching by lastName only`() {
      webTestClient.get().uri("/api/prisoners?partialNameMatch=TRUE&lastName=AND")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("size()").isEqualTo(3)
        .jsonPath("[*].offenderNo").value<List<String>> { assertThat(it).containsExactly("A1234AA", "A1234AB", "A1234AF") }
        .jsonPath("[*].firstName").value<List<String>> { assertThat(it).containsExactly("ARTHUR", "GILLIAN", "ANTHONY") }
        .jsonPath("[*].middleNames").value<List<String>> { assertThat(it).containsExactly("BORIS", "EVE", "") }
        .jsonPath("[*].lastName").value<List<String>> { assertThat(it).containsExactly("ANDERSON", "ANDERSON", "ANDREWS") }
    }

    @Test
    fun `returns success when searching by names, with includeAliases, partial name matching by lastName only`() {
      webTestClient.get().uri("/api/prisoners?includeAliases=true&partialNameMatch=TRUE&lastName=AND")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("size()").isEqualTo(3)
        .jsonPath("[*].offenderNo").value<List<String>> { assertThat(it).containsExactly("A1234AA", "A1234AB", "A1234AF") }
        .jsonPath("[*].firstName").value<List<String>> { assertThat(it).containsExactly("ARTHUR", "GILLIAN", "ANTHONY") }
        .jsonPath("[*].middleNames").value<List<String>> { assertThat(it).containsExactly("BORIS", "EVE", "") }
        .jsonPath("[*].lastName").value<List<String>> { assertThat(it).containsExactly("ANDERSON", "ANDERSON", "ANDREWS") }
        .jsonPath("[*].currentWorkingFirstName").value<List<String>> { assertThat(it).containsExactly("ARTHUR", "GILLIAN", "ANTHONY") }
        .jsonPath("[*].currentWorkingLastName").value<List<String>> { assertThat(it).containsExactly("ANDERSON", "ANDERSON", "ANDREWS") }
        .jsonPath("[*].currentWorkingBirthDate").value<List<String>> { assertThat(it).containsExactly("1969-12-30", "1998-08-28", "1964-12-01") }
    }

    @Test
    fun `returns success when searching by names, with partial name matching by firstName only`() {
      webTestClient.get().uri("/api/prisoners?partialNameMatch=TRUE&firstName=CHES")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("size()").isEqualTo(1)
        .jsonPath("[0].offenderNo").isEqualTo("A1234AI")
        .jsonPath("[0].firstName").isEqualTo("CHESTER")
        .jsonPath("[0].middleNames").isEqualTo("JAMES")
        .jsonPath("[0].lastName").isEqualTo("THOMPSON")
    }

    @Test
    fun `returns success when searching by names, with includeAliases, partial name matching by firstName only`() {
      webTestClient.get().uri("/api/prisoners?includeAliases=true&partialNameMatch=TRUE&firstName=CHES")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("size()").isEqualTo(3)
        .jsonPath("[0].offenderNo").isEqualTo("A1234AI")
        .jsonPath("[0].firstName").isEqualTo("CHESTER")
        .jsonPath("[0].middleNames").isEqualTo("JAMES")
        .jsonPath("[0].lastName").isEqualTo("THOMPSON")
        .jsonPath("[*].offenderNo").value<List<String>> { assertThat(it).containsExactly("A1234AI", "A1183CW", "A1234AI") }
        .jsonPath("[*].firstName").value<List<String>> { assertThat(it).containsExactly("CHESTER", "CHESNEY", "CHESNEY") }
        .jsonPath("[*].middleNames").value<List<String>> { assertThat(it).containsExactly("JAMES", "", "") }
        .jsonPath("[*].lastName").value<List<String>> { assertThat(it).containsExactly("THOMPSON", "THOMSON", "THOMSON") }
        .jsonPath("[*].currentWorkingFirstName").value<List<String>> { assertThat(it).containsExactly("CHESTER", "CHRIS", "CHESTER") }
        .jsonPath("[*].currentWorkingLastName").value<List<String>> { assertThat(it).containsExactly("THOMPSON", "WOAKES", "THOMPSON") }
        .jsonPath("[*].currentWorkingBirthDate").value<List<String>> { assertThat(it).containsExactly("1970-03-01", "1989-03-02", "1970-03-01") }
    }

    @Test
    fun `returns success when searching by names, with partial name matching by middleName only`() {
      webTestClient.get().uri("/api/prisoners?partialNameMatch=TRUE&middleNames=JEFF")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("size()").isEqualTo(1)
        .jsonPath("[0].offenderNo").isEqualTo("A1234AE")
        .jsonPath("[0].firstName").isEqualTo("DONALD")
        .jsonPath("[0].middleNames").isEqualTo("JEFFREY ROBERT")
        .jsonPath("[0].lastName").isEqualTo("MATTHEWS")
    }

    @Test
    fun `returns success when searching by names, with includeAliases, partial name matching by middleName only`() {
      webTestClient.get().uri("/api/prisoners?includeAliases=true&partialNameMatch=TRUE&middleNames=JEFF")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("size()").isEqualTo(1)
        .jsonPath("[0].offenderNo").isEqualTo("A1234AE")
        .jsonPath("[0].firstName").isEqualTo("DONALD")
        .jsonPath("[0].middleNames").isEqualTo("JEFFREY ROBERT")
        .jsonPath("[0].lastName").isEqualTo("MATTHEWS")
        .jsonPath("[*].currentWorkingFirstName").value<List<String>> { assertThat(it).containsExactly("DONALD") }
        .jsonPath("[*].currentWorkingLastName").value<List<String>> { assertThat(it).containsExactly("MATTHEWS") }
        .jsonPath("[*].currentWorkingBirthDate").value<List<String>> { assertThat(it).containsExactly("1956-02-28") }
    }

    @Test
    fun `returns success when searching by names, without partial name matching by firstName only`() {
      webTestClient.get().uri("/api/prisoners?firstName=DANIEL")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("size()").isEqualTo(2)
        .jsonPath("[*].offenderNo").value<List<String>> { assertThat(it).containsExactly("A1234AL", "A1234AJ") }
        .jsonPath("[*].internalLocation").value<List<String>> { assertThat(it).containsExactly("LEI-AABCW-1", "LEI-A-1-6") }
    }

    @Test
    fun `returns success when searching by names, without partial name matching by middleName only`() {
      webTestClient.get().uri("/api/prisoners?middleNames=JAMES")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("size()").isEqualTo(2)
        .jsonPath("[*].offenderNo").value<List<String>> { assertThat(it).containsExactly("A1234AD", "A1234AI") }
        .jsonPath("[*].internalLocation").value<List<String>> { assertThat(it).containsExactly("LEI-A-1", "LEI-A-1-5") }
    }

    @Test
    fun `returns success when searching by names, without partial name matching by lastName only`() {
      webTestClient.get().uri("/api/prisoners?lastName=ANDERSON")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("size()").isEqualTo(2)
        .jsonPath("[*].offenderNo").value<List<String>> { assertThat(it).containsExactly("A1234AA", "A1234AB") }
        .jsonPath("[*].internalLocation").value<List<String>> { assertThat(it).containsExactly("LEI-A-1-1", "LEI-H-1-5") }
    }

    @Test
    fun `returns success when searching by names, without partial name matching by middle and lastName`() {
      webTestClient.get().uri("/api/prisoners?middleNames=EVE&lastName=ANDERSON")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("size()").isEqualTo(1)
        .jsonPath("[0].offenderNo").isEqualTo("A1234AB")
        .jsonPath("[0].internalLocation").isEqualTo("LEI-H-1-5")
    }

    @Test
    fun `returns success when searching by names, without partial name matching by firstName and middleName`() {
      webTestClient.get().uri("/api/prisoners?firstName=CHESTER&middleNames=JAMES")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("size()").isEqualTo(1)
        .jsonPath("[0].offenderNo").isEqualTo("A1234AI")
        .jsonPath("[0].internalLocation").isEqualTo("LEI-A-1-5")
    }

    @Test
    fun `returns success when searching by names, without partial name matching with no results`() {
      webTestClient.get().uri("/api/prisoners?firstName=CHESTER&middleNames=JAMES&lastName=WILLIS")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("size()").isEqualTo(0)
    }

    @Test
    fun `returns success when searching within a dates of birth range from both from and to date`() {
      webTestClient.get().uri("/api/prisoners?dobFrom=1990-01-01&dobTo=2000-01-01")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("size()").isEqualTo(4)
        .jsonPath("[*].dateOfBirth").value<List<String>> {
          assertThat(it).containsExactly("1998-08-28", "1999-10-27", "1990-12-30", "1991-06-04")
        }
    }

    @Test
    fun `returns success when searching within a dates of birth range from both from and to date and includeAliases`() {
      webTestClient.get().uri("/api/prisoners?includeAliases=true&dobFrom=1990-01-01&dobTo=2000-01-01")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("size()").isEqualTo(6)
        .jsonPath("[*].dateOfBirth").value<List<String>> {
          assertThat(it).containsExactly("1998-08-28", "1999-10-27", "1990-12-30", "1995-08-21", "1991-06-04", "1998-11-01")
        }
    }

    @Test
    fun `returns success when searching within a dates of birth range with from date only`() {
      webTestClient.get().uri("/api/prisoners?dobFrom=1970-01-01")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .header("Page-Offset", "0")
        .header("Page-Limit", "100")
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("size()").isEqualTo(14)
        .jsonPath("[*].dateOfBirth").value<List<String>> {
          assertThat(it).containsExactly(
            "1970-01-01", "1971-01-15", "1974-10-29", "1972-01-01", "1970-12-30", "1972-01-01", "1977-07-07",
            "1979-12-31", "1977-01-02", "1974-01-01", "1970-01-01", "1970-03-01", "1977-11-14", "1975-12-25",
          )
        }
    }

    @Test
    fun `returns success when searching within a dates of birth range with to date only`() {
      webTestClient.get().uri("/api/prisoners?dobTo=2000-01-01")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("size()").isEqualTo(4)
        .jsonPath("[*].dateOfBirth").value<List<String>> { assertThat(it).containsExactly("1998-08-28", "1999-10-27", "1990-12-30", "1991-06-04") }
    }

    @Test
    fun `returns success when searching without partial name matching`() {
      webTestClient.get().uri("/api/prisoners?lastName=ANDERSON")
        .headers(setClientAuthorisation(listOf("ROLE_GLOBAL_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("size()").isEqualTo(2)
        .jsonPath("[*].offenderNo").value<List<String>> { assertThat(it).containsExactly("A1234AA", "A1234AB") }
        .jsonPath("[*].internalLocation").value<List<String>> { assertThat(it).containsExactly("LEI-A-1-1", "LEI-H-1-5") }
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
        object : ParameterizedTypeReference<String>() {},
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
        object : ParameterizedTypeReference<String>() {},
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
        object : ParameterizedTypeReference<String>() {},
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
        object : ParameterizedTypeReference<String>() {},
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
        object : ParameterizedTypeReference<String>() {},
      )
      assertThatStatus(response, NOT_FOUND)
    }

    @Test
    fun testReturn403WhenDoesNotHavePrivs() {
      val httpEntity = createEmptyHttpEntity(AuthToken.NO_CASELOAD_USER)
      val response = testRestTemplate.exchange(
        "/api/prisoners/A1234AA/full-status",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String>() {},
      )
      assertThatStatus(response, FORBIDDEN)
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
        object : ParameterizedTypeReference<String>() {},
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
        .jsonPath("userMessage").isEqualTo("Unauthorised access to booking with id -1.")
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
    fun `returns success if has override role ROLE_PRISONER_INDEX`() {
      webTestClient.get().uri("/api/prisoners/prisoner-numbers")
        .headers(setClientAuthorisation(listOf("ROLE_PRISONER_INDEX")))
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
            .doesNotContain("A1071AA")
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
            .contains("A1071AA")
            .doesNotContain("A1234AN", "A1234AO")
        }
    }

    // This test might seem strange but it's based upon real bad data found in production NOMIS
    @Test
    fun `will not return prisoners with multiple prisoner numbers for same root offender`() {
      webTestClient.get()
        .uri("/api/prisoners/prisoner-numbers?size=1000")
        .headers(setAuthorisation(listOf("ROLE_PRISONER_INDEX")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("content").value<List<String>> {
          assertThat(it)
            // A1062AA / A1062AB aren't included because the root offender id doesn't exist
            // A1064AB isn't included because the offender has no bookings and it's not the root offender
            .doesNotContain("A1062AA", "A1062AB", "A1064AB")
            // A1064AA is included because despite having no bookings it is the root offender
            .contains("A1064AA")
        }
    }
  }

  @Nested
  @DisplayName("GET /api/prisoners/prisoner-numbers/active")
  inner class GetPrisonerIdsActive {
    @Nested
    inner class Security {
      @Test
      fun `access forbidden when no role`() {
        webTestClient.get().uri("/api/prisoners/prisoner-numbers/active")
          .headers(setAuthorisation(roles = listOf()))
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.get().uri("/api/prisoners/prisoner-numbers/active")
          .headers(setAuthorisation(roles = listOf("ROLE_BANANAS")))
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access unauthorised with no auth token`() {
        webTestClient.get().uri("/api/prisoners/prisoner-numbers/active")
          .exchange()
          .expectStatus().isUnauthorized
      }
    }

    @Nested
    inner class HappyPath {
      @Test
      fun `will return count of all active prisoners by default`() {
        webTestClient.get().uri("/api/prisoners/prisoner-numbers/active?size=1&page=0")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISONER_INDEX")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.totalElements").value<Int> { assertThat(it).isGreaterThan(50) }
          .jsonPath("$.numberOfElements").isEqualTo(1)
      }

      @Test
      fun `will return a page of prisoners ordered by root offender id ASC`() {
        webTestClient.get().uri("/api/prisoners/prisoner-numbers/active?page=0")
          .headers(setAuthorisation(roles = listOf("ROLE_PRISONER_INDEX")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.totalElements").value<Int> { assertThat(it).isGreaterThan(50) }
          .jsonPath("$.numberOfElements").isEqualTo(10)
          .jsonPath("$.content[0]").isEqualTo("A1237AI")
          .jsonPath("$.content[1]").isEqualTo("A1076AA")
          .jsonPath("$.content[2]").isEqualTo("A1075AA")
      }
    }
  }
}
