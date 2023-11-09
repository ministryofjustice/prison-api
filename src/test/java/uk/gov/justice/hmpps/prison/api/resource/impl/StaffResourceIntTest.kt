package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import org.springframework.http.MediaType

class StaffResourceIntTest : ResourceTest() {

  @Nested
  @DisplayName("GET /api/staff/roles/{agencyId}/role/{role}")
  inner class StaffRoles {
    @Test
    fun `should return not found if agency does not exist for user`() {
      webTestClient.get()
        .uri("/api/staff/roles/XYZ/role/KW")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should return not found if not part of user caseload`() {
      webTestClient.get()
        .uri("/api/staff/roles/BMI/role/KW")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should return success with count of 0 if no staff`() {
      webTestClient.get()
        .uri("/api/staff/roles/WAI/role/OS")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(0)
    }

    @Test
    fun `should return success with count of 1 if staff`() {
      webTestClient.get()
        .uri("/api/staff/roles/SYI/role/KW")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("$[0].staffId").isEqualTo("-9")
    }

    @Test
    fun `should return success with count of 4 if multiple staff`() {
      webTestClient.get()
        .uri("/api/staff/roles/LEI/role/KW")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(4)
        .jsonPath("[*].staffId").value<List<Int>> { Assertions.assertThat(it).containsExactlyInAnyOrder(-1, -4, -11, -12) }
    }

    @Test
    fun `should return success with 0 results when passing in nameFilter parameter only`() {
      webTestClient.get()
        .uri("/api/staff/roles/LEI/role/KW?nameFilter=Ronald")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(0)
    }

    @Test
    fun `should return success with 2 results when passing in nameFilter with partial match`() {
      webTestClient.get()
        .uri("/api/staff/roles/LEI/role/KW?nameFilter=USE")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(2)
        .jsonPath("[*].staffId").value<List<Int>> { Assertions.assertThat(it).containsExactlyInAnyOrder(-1, -4) }
    }

    @Test
    fun `should return success with 2 results when passing in nameFilter variable case parameter only`() {
      webTestClient.get()
        .uri("/api/staff/roles/LEI/role/KW?nameFilter=User")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(2)
        .jsonPath("[*].staffId").value<List<Int>> { Assertions.assertThat(it).containsExactlyInAnyOrder(-1, -4) }
    }

    @Test
    fun `should return success with 0 results when passing in staffId parameter only`() {
      webTestClient.get()
        .uri("/api/staff/roles/LEI/role/KW?staffId=-999 ")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(0)
    }

    @Test
    fun `should return success with 1 result when passing in matching staffId parameter only`() {
      webTestClient.get()
        .uri("/api/staff/roles/LEI/role/KW?staffId=-1")
        .headers(setAuthorisation("ITAG_USER", listOf("")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("$[0].staffId").isEqualTo(-1)
    }

    @Test
    fun `should return not found if does not have override role`() {
      webTestClient.get()
        .uri("/api/staff/roles/BMI/role/KW")
        .headers(setClientAuthorisation(listOf("")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound

      verify(telemetryClient).trackEvent(eq("ClientUnauthorisedAgencyAccess"), any(), isNull())
    }

    @Test
    fun `should return not found if agency does not exist`() {
      webTestClient.get()
        .uri("/api/staff/roles/XYZ/role/KW")
        .headers(setClientAuthorisation(listOf("SYSTEM_USER")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `should return success if has SYSTEM_USER override role`() {
      webTestClient.get()
        .uri("/api/staff/roles/BMI/role/KW")
        .headers(setClientAuthorisation(listOf("SYSTEM_USER")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `should return success if has STAFF_SEARCH override role`() {
      webTestClient.get()
        .uri("/api/staff/roles/BMI/role/KW")
        .headers(setClientAuthorisation(listOf("STAFF_SEARCH")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk
    }
  }
}
