package uk.gov.justice.hmpps.prison.api.resource.impl

import net.javacrumbs.jsonunit.assertj.JsonAssertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.util.UriBuilder
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken

class ReferenceDataResourceTest : ResourceTest() {
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

    assertThatStatus<String>(response, 200)
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

    assertThatStatus<String>(response, 200)
    JsonAssertions.assertThatJson(response.body.orEmpty())
      .isEqualTo("""{"activeFlag":"Y","code":"ROTL","description":"Release on Temporary Licence","domain":"ADDRESS_TYPE","listSeq":8,"subCodes":[],"systemDataFlag":"N"}""")
  }

  @Nested
  @DisplayName("GET /domains")
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
  @DisplayName("GET /domains/codes")
  internal inner class GetCodesBuDomainTest {
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
  internal inner class GetReferenceCodesOrProfileTypesTest {

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
