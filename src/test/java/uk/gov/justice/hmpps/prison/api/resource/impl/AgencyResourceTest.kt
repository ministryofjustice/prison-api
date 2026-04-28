package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod.GET
import uk.gov.justice.hmpps.prison.api.model.Location
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.NORMAL_USER

class AgencyResourceTest : ResourceTest() {

  @Nested
  @DisplayName("GET /api/agencies")
  inner class GetAgencies {
    @Test
    fun testCanFindAgencyById() {
      val token = authTokenHelper.getToken(NORMAL_USER)

      val httpEntity = createHttpEntity(token, null)

      val response = testRestTemplate.exchange(
        "/api/agencies/LEI",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String>() {
        },
      )

      assertThatJsonFileAndStatus(response, 200, "single_agency.json")
    }

    @Test
    fun testCanFindAgencyWithAddress() {
      val token = authTokenHelper.getToken(NORMAL_USER)

      val httpEntity = createHttpEntity(token, null)

      val response = testRestTemplate.exchange(
        "/api/agencies/BMI?withAddresses=true",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String>() {
        },
      )

      assertThatJsonFileAndStatus(response, 200, "single_agency_with_address.json")
    }

    @Test
    fun `Retrieve agency details when excluding inactive agencies`() {
      webTestClient.get().uri("/api/agencies/ZZGHI?activeOnly=true")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `Retrieve agency details doesn't return inactive agencies by default`() {
      webTestClient.get().uri("/api/agencies/ZZGHI?activeOnly=true")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `Retrieve agency details when including inactive agencies`() {
      webTestClient.get().uri("/api/agencies/ZZGHI?activeOnly=false")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("agencyId").isEqualTo("ZZGHI")
        .jsonPath("description").isEqualTo("Ghost")
        .jsonPath("agencyType").isEqualTo("INST")
        .jsonPath("active").isEqualTo(false)
    }
  }

  @Nested
  @DisplayName("GET /api/agencies/type/{type}")
  inner class GetAgenciesByType {
    @Test
    fun testCanFindAgenciesByTypePlusInactive() {
      val token = authTokenHelper.getToken(NORMAL_USER)

      val httpEntity = createHttpEntity(token, null)

      val response = testRestTemplate.exchange(
        "/api/agencies/type/INST?activeOnly={activeOnly}",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String>() {
        },
        "false",
      )

      assertThatJsonFileAndStatus(response, 200, "inactive_agencies_by_type.json")
    }

    @Test
    fun testCanFindCourtsPlusAddresses() {
      val token = authTokenHelper.getToken(NORMAL_USER)

      val httpEntity = createHttpEntity(token, null)

      val response = testRestTemplate.exchange(
        "/api/agencies/type/CRT?withAddresses={withAddresses}",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String>() {
        },
        "true",
      )

      assertThatJsonFileAndStatus(response, 200, "courts_by_type.json")
    }
  }

  @Nested
  @DisplayName("GET /api/agencies/{agencyId}/receptionsWithCapacity")
  inner class GetAgencyReceptionCapacity {

    @Test
    fun testCanFindReceptionsWithCapacity() {
      val token = authTokenHelper.getToken(NORMAL_USER)

      val httpEntity = createHttpEntity(token, null)

      val response = testRestTemplate.exchange(
        "/api/agencies/LEI/receptionsWithCapacity",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String>() {
        },
      )
      assertThatJsonFileAndStatus(response, 200, "reception_with_capacity.json")
    }
  }

  @Nested
  @DisplayName("GET /api/agencies/{agencyId}/eventLocations")
  inner class GetAgencyEventLocations {
    @Test
    fun testGetEventLocationsForAPrison() {
      val token = authTokenHelper.getToken(AuthToken.REF_DATA_MAINTAINER)
      val httpEntity = createHttpEntity(token, null)

      val response = testRestTemplate.exchange(
        "/api/agencies/LEI/eventLocations",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<List<Location>>() {
        },
      )

      assertThat(response.body?.size).isEqualTo(14)
    }
  }
}
