package uk.gov.justice.hmpps.prison.api.resource.impl

import net.javacrumbs.jsonunit.assertj.JsonAssertions
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.util.UriBuilder
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken

class ReferenceDataResourceTest : ResourceTest() {
  @Autowired
  private lateinit var jdbcTemplate: JdbcTemplate

  @Test
  fun testCreateANewSubReferenceType() {
    try {
      val token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER)
      val httpEntity = createHttpEntity(
        token,
        """{    "description": "TASK_TEST1",    "expiredDate": "2018-07-19",    "activeFlag": "N",    "listSeq": 88,    "parentCode": "GEN",    "parentDomain": "TASK_TYPE"}""",
      )

      val response = testRestTemplate.exchange(
        "/api/reference-domains/domains/{domain}/codes/{code}",
        HttpMethod.POST,
        httpEntity,
        object : ParameterizedTypeReference<String>() {},
        "TASK_SUBTYPE",
        "TEST1",
      )

      assertThatStatus<String>(response, 200)
      JsonAssertions.assertThatJson(response.body.orEmpty())
        .isEqualTo("""{domain:"TASK_SUBTYPE",code:"TEST1",description:"TASK_TEST1",parentDomain:"TASK_TYPE",parentCode:"GEN",activeFlag:"N",listSeq:88,systemDataFlag:"Y",expiredDate:"2018-07-19","subCodes":[]}""")
    } finally {
      val deleteSql = "DELETE FROM REFERENCE_CODES WHERE domain = ? and code = ?"
      Assertions.assertThat(jdbcTemplate.update(deleteSql, "TASK_SUBTYPE", "TEST1")).isEqualTo(1)
    }
  }

  @Test
  fun testUpdateASubReferenceTypeToActive() {
    val token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER)

    val httpEntity = createHttpEntity(
      token,
      """{    "description": "Amended Type",    "activeFlag": "Y",    "systemDataFlag": "N",    "listSeq": 999,    "parentCode": "ATR",    "parentDomain": "TASK_TYPE"}""",
    )

    val response = testRestTemplate.exchange(
      "/api/reference-domains/domains/{domain}/codes/{code}",
      HttpMethod.PUT,
      httpEntity,
      object : ParameterizedTypeReference<String>() {},
      "TASK_SUBTYPE",
      "ATRCC",
    )

    assertThatStatus<String>(response, 200)

    JsonAssertions.assertThatJson(response.body.orEmpty())
      .isEqualTo("{domain:\"TASK_SUBTYPE\",code:\"ATRCC\",description:\"Amended Type\",parentDomain:\"TASK_TYPE\",parentCode:\"ATR\",activeFlag:\"Y\",listSeq:999,systemDataFlag:\"N\",\"subCodes\":[]}")
  }

  @Test
  fun testUpdateASubReferenceTypeToInactive() {
    val token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER)

    val httpEntity = createHttpEntity(
      token,
      """{    "description": "Alcohol Rehab - community -changed",    "activeFlag": "N",    "systemDataFlag": "Y",    "expiredDate": "2019-07-19",    "listSeq": 10,    "parentCode": "ATR",    "parentDomain": "TASK_TYPE"}""",
    )

    val response = testRestTemplate.exchange(
      "/api/reference-domains/domains/{domain}/codes/{code}",
      HttpMethod.PUT,
      httpEntity,
      object : ParameterizedTypeReference<String>() {},
      "TASK_SUBTYPE",
      "AREH-C",
    )

    assertThatStatus<String>(response, 200)

    JsonAssertions.assertThatJson(response.body.orEmpty())
      .isEqualTo("""{domain:"TASK_SUBTYPE",code:"AREH-C",description:"Alcohol Rehab - community -changed",parentDomain:"TASK_TYPE",parentCode:"ATR",activeFlag:"N",listSeq:10,expiredDate: "2019-07-19",systemDataFlag:"Y","subCodes":[]}""")
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
      val domainAt = "$[?(@.domain == '%s')]"
      val domainPropertyAt = "$[?(@.domain == '%s')].%s"

      webTestClient.get()
        .uri("/api/reference-domains/domains")
        .headers(setAuthorisation(listOf("ROLE_SYSTEM")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath(domainAt, "ETHNICITY").isNotEmpty()
        .jsonPath(domainAt, "SKL_SUB_TYPE").isNotEmpty()
        .jsonPath(domainPropertyAt, "ETHNICITY", "description").isEqualTo("Ethnicity")
        .jsonPath(domainPropertyAt, "ETHNICITY", "domainStatus").isEqualTo("ACTIVE")
        .jsonPath(domainPropertyAt, "ETHNICITY", "ownerCode").isEqualTo("ADMIN")
        .jsonPath(domainPropertyAt, "ETHNICITY", "applnCode").isEqualTo("OMS")
        .jsonPath(domainPropertyAt, "SKL_SUB_TYPE", "parentDomain").isEqualTo("STAFF_SKILLS")
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
      val codeAt = "$[?(@.code == '%s')]"
      val codePropertyAt = "$[?(@.code == '%s')].%s"

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
        .jsonPath(codeAt, "SCON").isNotEmpty()
        .jsonPath(codePropertyAt, "SCON", "domain").isEqualTo("VISIT_TYPE")
        .jsonPath(codePropertyAt, "SCON", "description").isEqualTo("Social Contact")
        .jsonPath(codePropertyAt, "SCON", "activeFlag").isEqualTo("Y")
        .jsonPath(codePropertyAt, "SCON", "systemDataFlag").isEqualTo("N")
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
