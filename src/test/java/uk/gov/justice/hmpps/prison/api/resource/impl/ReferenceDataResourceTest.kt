package uk.gov.justice.hmpps.prison.api.resource.impl

import net.javacrumbs.jsonunit.assertj.JsonAssertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.expectBodyList
import org.springframework.web.util.UriBuilder
import uk.gov.justice.hmpps.prison.api.model.ReferenceCode
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken

class ReferenceDataResourceTest : ResourceTest() {

  @Nested
  @DisplayName("GET /api/reference-domains/domains/{domain}")
  internal inner class GetDomainTest {

    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.get().uri("/api/reference-domains/domains/ADDRESS_TYPE")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun testReadDomainInformation() {
      val token = authTokenHelper.getToken(AuthToken.NORMAL_USER)

      val response = testRestTemplate.exchange(
        "/api/reference-domains/domains/{domain}",
        HttpMethod.GET,
        createHttpEntity(token, null),
        object : ParameterizedTypeReference<String>() {},
        "ADDRESS_TYPE",
      )

      assertThatStatus(response, 200)
    }

    @Test
    fun `should return 404 for request without sub-codes if domain does not exist`() {
      webTestClient.get()
        .uri("/api/reference-domains/domains/UNKNOWN")
        .headers(setAuthorisation(listOf("ROLE_SYSTEM")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Reference domain [UNKNOWN] not found.")
    }

    @Test
    fun `should return 404 for request with sub-codes if domain does not exist`() {
      webTestClient.get()
        .uri("/api/reference-domains/domains/UNKNOWN?withSubCodes=true")
        .headers(setAuthorisation(listOf("")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Reference domain [UNKNOWN] not found.")
    }

    @Test
    fun `should return multiple reference codes for SUB_AREA domain`() {
      webTestClient.get()
        .uri("/api/reference-domains/domains/SUB_AREA")
        .headers(setAuthorisation(listOf("")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("size()").isEqualTo(8)
        .jsonPath("[*].subCodes[*]").doesNotExist()
        .jsonPath("[*].parentDomain").doesNotExist()
        .jsonPath("[*].code").value<List<String>>
        { assertThat(it).containsExactly("E", "N", "NE", "NW", "S", "SE", "SW", "W") }
        .jsonPath("[2].description").isEqualTo("North East")
        .jsonPath("[7].description").isEqualTo("West")
    }

    @Test
    fun `should retrieve reference codes for a domain with sub-codes`() {
      val refDataList = webTestClient.get()
        .uri("/api/reference-domains/domains/MOVE_TYPE?withSubCodes=true")
        .headers(setAuthorisation(listOf("")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBodyList<ReferenceCode>()
        .returnResult()
        .responseBody!!

      assertThat(refDataList.size).isEqualTo(5)
      assertThat(refDataList).allMatch { it.subCodes.isNotEmpty() }
      assertThat(refDataList.all { it.domain == "MOVE_TYPE" })
      assert(refDataList.flatMap { it.subCodes }.all { it.domain in listOf("MOVE_RSN", "TAP_ABS_STYP", "TAP_ABS_TYPE") })

      with(refDataList[0]) {
        assertThat(code).isEqualTo("ADM")
        assertThat(subCodes.size).isEqualTo(34)
        assertThat(subCodes[0].domain).isEqualTo("MOVE_RSN")
      }
      assertThat(refDataList[1].description).isEqualTo("Court")
      assertThat(refDataList[2].code).isEqualTo("REL")
      assertThat(refDataList[2].subCodes.size).isEqualTo(37)
      assertThat(refDataList[3].description).isEqualTo("Temporary Absence")
      assertThat(refDataList[3].subCodes.all { it.domain == "MOVE_RSN" })
      with(refDataList[4]) {
        assertThat(code).isEqualTo("TRN")
        assertThat(subCodes.size).isEqualTo(14)
        assertThat(subCodes.all { it.domain == "MOVE_RSN" })
      }
    }
  }

  @Nested
  @DisplayName("GET /api/reference-domains/domains")
  internal inner class GetDomainsTest {
    @Test
    @DisplayName("must have a valid token to access endpoint")
    fun mustHaveAValidTokenToAccessEndpoint() {
      webTestClient.get()
        .uri("/api/reference-domains/domains")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized()
    }

    @Test
    @DisplayName("can have any role to access endpoint")
    fun canHaveAnyRoleToAccessEndpoint() {
      webTestClient.get()
        .uri("/api/reference-domains/domains")
        .headers(setAuthorisation(listOf("ROLE_BANANAS")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
    }

    @Test
    @DisplayName("will return a list of domains")
    fun willReturnAListOfDomains() {
      val currentNumberOfDomains = 430
      webTestClient.get()
        .uri("/api/reference-domains/domains")
        .headers(setAuthorisation(listOf("ROLE_SYSTEM")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.length()").isEqualTo(currentNumberOfDomains)
        .jsonPath("$[0].domain").isEqualTo("ACCESS_PRIVI")
        .jsonPath("$[429].domain").isEqualTo("WRK_FLW_ACT")
    }

    @Test
    @DisplayName("will return details of a domain")
    fun willReturnDetailsOfADomain() {
      webTestClient.get()
        .uri("/api/reference-domains/domains")
        .headers(setAuthorisation(listOf("ROLE_SYSTEM")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$[?(@.domain == 'ETHNICITY')]").isNotEmpty()
        .jsonPath("$[?(@.domain == 'SKL_SUB_TYPE')]").isNotEmpty()
        .jsonPath("$[?(@.domain == 'ETHNICITY')].description").isEqualTo("Ethnicity")
        .jsonPath("$[?(@.domain == 'ETHNICITY')].domainStatus").isEqualTo("ACTIVE")
        .jsonPath("$[?(@.domain == 'ETHNICITY')].ownerCode").isEqualTo("ADMIN")
        .jsonPath("$[?(@.domain == 'ETHNICITY')].applnCode").isEqualTo("OMS")
        .jsonPath("$[?(@.domain == 'SKL_SUB_TYPE')].parentDomain").isEqualTo("STAFF_SKILLS")
    }
  }

  @Nested
  @DisplayName("GET /api/reference-domains/domains/{domain}/codes")
  inner class GetByDomainDetailsTest {

    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.get().uri("/api/reference-domains/domains/VISIT_TYPE/codes")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    @DisplayName("can have any role to access endpoint")
    fun canHaveAnyRoleToAccessEndpoint() {
      webTestClient.get()
        .uri { builder: UriBuilder ->
          builder.path("/api/reference-domains/domains/{domain}/codes").build("ETHNICITY")
        }
        .headers(setAuthorisation(listOf("ROLE_BANANAS")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
    }

    @Test
    @DisplayName("will return a list of codes")
    fun willReturnAListOfDomains() {
      val currentNumberOfVisitTypeCodes = 2

      webTestClient.get()
        .uri { builder: UriBuilder ->
          builder.path("/api/reference-domains/domains/{domain}/codes").build("VISIT_TYPE")
        }
        .headers(setAuthorisation(listOf("ROLE_SYSTEM")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$.length()").isEqualTo(currentNumberOfVisitTypeCodes)
    }

    @Test
    @DisplayName("will return details of a domain code")
    fun willReturnDetailsOfADomainCode() {
      webTestClient.get()
        .uri { builder: UriBuilder ->
          builder.path("/api/reference-domains/domains/{domain}/codes").build("VISIT_TYPE")
        }
        .headers(setAuthorisation(listOf("ROLE_SYSTEM")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$[?(@.code == 'SCON')]").isNotEmpty()
        .jsonPath("$[?(@.code == 'SCON')].domain").isEqualTo("VISIT_TYPE")
        .jsonPath("$[?(@.code == 'SCON')].description").isEqualTo("Social Contact")
        .jsonPath("$[?(@.code == 'SCON')].activeFlag").isEqualTo("Y")
        .jsonPath("$[?(@.code == 'SCON')].systemDataFlag").isEqualTo("N")
    }
  }

  @Nested
  @DisplayName("GET /api/reference-domains/domains/codes/{code}")
  internal inner class GetCodesByDomainTest {

    @Test
    fun `should return 404 for request without sub-codes if domain does not exist`() {
      webTestClient.get()
        .uri("/api/reference-domains/domains/UNKNOWN/codes/NOT_A_CLUE")
        .headers(setAuthorisation(listOf("")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Reference domain [UNKNOWN] not found.")
    }

    @Test
    fun `should return 404 for request with sub-codes if domain does not exist`() {
      webTestClient.get()
        .uri("/api/reference-domains/domains/UNKNOWN/codes/NOT_A_CLUE?withSubCodes=true")
        .headers(setAuthorisation(listOf("")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Reference domain [UNKNOWN] not found.")
    }

    @Test
    fun `should return 404 for request without sub-codes if domain exists but code does not exist`() {
      webTestClient.get()
        .uri("/api/reference-domains/domains/NOTE_SOURCE/codes/DOES_NOT_EXIST")
        .headers(setAuthorisation(listOf("")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Reference code for domain [NOTE_SOURCE] and code [DOES_NOT_EXIST] not found.")
    }

    @Test
    fun `should return 404 for request with sub-codes if domain exists but code does not exist`() {
      webTestClient.get()
        .uri("/api/reference-domains/domains/NOTE_SOURCE/codes/DOES_NOT_EXIST?withSubCodes=true")
        .headers(setAuthorisation(listOf("")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Reference code for domain [NOTE_SOURCE] and code [DOES_NOT_EXIST] not found.")
    }

    @Test
    fun `should return 400 for request with sub-codes if domain exists and code exists but has no sub-codes`() {
      webTestClient.get()
        .uri("/api/reference-domains/domains/NOTE_SOURCE/codes/AUTO?withSubCodes=true")
        .headers(setAuthorisation(listOf("")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isBadRequest
        .expectBody()
        .jsonPath("userMessage").isEqualTo("Reference code for domain [NOTE_SOURCE] and code [AUTO] does not have sub-codes.")
    }

    @Test
    @DisplayName("can have any role to access endpoint")
    fun canHaveAnyRoleToAccessEndpoint() {
      webTestClient.get()
        .uri { builder: UriBuilder ->
          builder.path("/api/reference-domains/domains/{domain}/codes").build("ETHNICITY")
        }
        .headers(setAuthorisation(listOf("ROLE_BANANAS")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
    }

    @Test
    @DisplayName("must have a valid token to access endpoint")
    fun mustHaveAValidTokenToAccessEndpoint() {
      webTestClient.get()
        .uri { builder: UriBuilder ->
          builder.path("/api/reference-domains/domains/codes/{code}").build("ETHNICITY")
        }
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized()
    }

    @Test
    fun testReadDomainCodeInformation() {
      val token = authTokenHelper.getToken(AuthToken.NORMAL_USER)

      val response = testRestTemplate.exchange(
        "/api/reference-domains/domains/{domain}/codes/{code}",
        HttpMethod.GET,
        createHttpEntity(token, null),
        object : ParameterizedTypeReference<String>() {},
        "ADDRESS_TYPE",
        "ROTL",
      )

      assertThatStatus(response, 200)
      JsonAssertions.assertThatJson(response.body.orEmpty())
        .isEqualTo("""{"activeFlag":"Y","code":"ROTL","description":"Release on Temporary Licence","domain":"ADDRESS_TYPE","listSeq":8,"subCodes":[],"systemDataFlag":"N"}""")
    }

    @Test
    fun `should retrieve specific reference code details, with sub-codes`() {
      webTestClient.get()
        .uri("/api/reference-domains/domains/ALERT/codes/C?withSubCodes=true")
        .headers(setAuthorisation(listOf("")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("domain").isEqualTo("ALERT")
        .jsonPath("code").isEqualTo("C")
        .jsonPath("description").isEqualTo("Child Communication Measures")
        .jsonPath("activeFlag").isEqualTo("Y")
        .jsonPath("subCodes.size()").isEqualTo(4)
    }

    @Test
    fun `should retrieve specific reference code details, without sub-codes`() {
      webTestClient.get()
        .uri("/api/reference-domains/domains/ETHNICITY/codes/B1")
        .headers(setAuthorisation(listOf("")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("domain").isEqualTo("ETHNICITY")
        .jsonPath("code").isEqualTo("B1")
        .jsonPath("description").isEqualTo("Black/Black British: Caribbean")
        .jsonPath("activeFlag").isEqualTo("Y")
        .jsonPath("subCodes").isEmpty
    }
  }

  @Nested
  @DisplayName("GET /api/reference-domains/scheduleReasons")
  inner class GetScheduleReasonsTest {

    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.get().uri("/api/reference-domains/scheduleReasons?eventType=APP")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return all reason codes for event type APP`() {
      webTestClient.get()
        .uri("/api/reference-domains/scheduleReasons?eventType=APP")
        .headers(setAuthorisation(listOf("")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("size()").isEqualTo(6)
        .jsonPath("$").value<List<Map<String, Any>>> { list ->
          val pairs = list.map { it["code"] to it["description"] }
          assertThat(pairs).containsExactly(
            "CABA" to "Bail",
            "CHAP" to "Baptism",
            "EDUC" to "Computers",
            "MEDE" to "Dentist",
            "KWS" to "Key Work Session",
            "RES" to "Resolve",
          )
        }
    }
  }

  @Nested
  @DisplayName("GET /api/reference-domains/domains/{domain}/all-codes")
  internal inner class GetReferenceCodesOrProfileTypesTest {

    @Test
    fun `returns 401 without an auth token`() {
      webTestClient.get().uri("/api/reference-domains/domains/ADDRESS_TYPE/all-codes")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isUnauthorized
    }

    private val codesListLength = "$.length()"

    @Test
    fun `get reference codes`() {
      val currentNumberOfAddressTypeCodes = 21

      webTestClient.get()
        .uri { builder: UriBuilder ->
          builder.path("/api/reference-domains/domains/{domain}/all-codes").build("ADDRESS_TYPE")
        }
        .headers(setAuthorisation(listOf("ROLE_SYSTEM")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath(codesListLength).isEqualTo(currentNumberOfAddressTypeCodes)
        .jsonPath(codeAtIndex(0)).isEqualTo("CARE")
        .jsonPath(codeAtIndex(1)).isEqualTo("CURFEW")
        .jsonPath(codeAtIndex(20)).isEqualTo("ROTL")
    }

    @Test
    fun `get profile codes`() {
      val currentNumberOfBuildCodes = 12

      webTestClient.get()
        .uri { builder: UriBuilder ->
          builder.path("/api/reference-domains/domains/{domain}/all-codes").build("BUILD")
        }
        .headers(setAuthorisation(listOf("ROLE_SYSTEM")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath(codesListLength).isEqualTo(currentNumberOfBuildCodes)
        .jsonPath(codeAtIndex(0)).isEqualTo("FAT")
        .jsonPath(codeAtIndex(1)).isEqualTo("FRAIL")
        .jsonPath(codeAtIndex(11)).isEqualTo("THIN")
    }

    @Test
    fun `domain does not exist`() {
      webTestClient.get()
        .uri { builder: UriBuilder ->
          builder.path("/api/reference-domains/domains/{domain}/all-codes").build("NONSENSE")
        }
        .headers(setAuthorisation(listOf("ROLE_SYSTEM")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isNotFound()
    }

    private fun codeAtIndex(index: Int): String = "$[$index].code"
  }
}
