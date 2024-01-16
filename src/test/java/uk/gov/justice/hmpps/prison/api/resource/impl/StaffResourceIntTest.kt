package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import org.springframework.http.MediaType

class StaffResourceIntTest : ResourceTest() {

  @Nested
  @DisplayName("GET /api/staff/{staffId}")
  inner class StaffDetails {
    @ParameterizedTest
    @MethodSource("uk.gov.justice.hmpps.prison.api.resource.impl.StaffResourceIntTest#staffDetailsTable")
    fun `Find staff member using staff id`(table: StaffDetailsRow) {
      webTestClient.get()
        .uri("/api/staff/${table.staffId}")
        .headers(setClientAuthorisation(listOf("STAFF_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.firstName").isEqualTo(table.firstName)
        .jsonPath("$.lastName").isEqualTo(table.lastName)
        .jsonPath("$.gender").isEqualTo(table.gender)
        .jsonPath("$.dateOfBirth").isEqualTo(table.dob)
    }

    @Test
    fun `Find staff member using staff id that does not exist`() {
      webTestClient.get()
        .uri("/api/staff/-9999")
        .headers(setClientAuthorisation(listOf("STAFF_SEARCH")))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `cannot get another staff members details`() {
      webTestClient.get()
        .uri("/api/staff/-2")
        .headers(setAuthorisation("EXOFF5", emptyList()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `can get another staff members details with role`() {
      webTestClient.get()
        .uri("/api/staff/-2")
        .headers(setAuthorisation("EXOFF5", listOf("ROLE_STAFF_SEARCH")))
        .exchange()
        .expectStatus().isOk
    }
  }

  @Nested
  @DisplayName("GET /api/staff/{staffId}/emails")
  inner class StaffEmails {
    @Test
    fun `Find staff member emails using staff id -1`() {
      webTestClient.get()
        .uri("/api/staff/-1/emails")
        .headers(setClientAuthorisation(listOf("STAFF_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("$[0]").isEqualTo("prison-api-user@test.com")
    }

    @Test
    fun `Find staff member emails using staff id -2`() {
      webTestClient.get()
        .uri("/api/staff/-2/emails")
        .headers(setClientAuthorisation(listOf("STAFF_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(2)
        .jsonPath("$[0]").isEqualTo("itaguser@other.com")
        .jsonPath("$[1]").isEqualTo("itaguser@syscon.net")
    }

    @Test
    fun `Find staff member emails using staff id -7`() {
      webTestClient.get()
        .uri("/api/staff/-7/emails")
        .headers(setClientAuthorisation(listOf("STAFF_SEARCH")))
        .exchange()
        .expectStatus().isNoContent
    }

    @Test
    fun `Find staff member emails with staff id not found`() {
      webTestClient.get()
        .uri("/api/staff/99999/emails")
        .headers(setClientAuthorisation(listOf("STAFF_SEARCH")))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `cannot get another staff members details`() {
      webTestClient.get()
        .uri("/api/staff/{staffId}/emails", -2)
        .headers(setAuthorisation("EXOFF5", emptyList()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `can get another staff members details with role`() {
      webTestClient.get()
        .uri("/api/staff/{staffId}/emails", -2)
        .headers(setAuthorisation("EXOFF5", listOf("ROLE_STAFF_SEARCH")))
        .exchange()
        .expectStatus().isOk
    }
  }

  @Nested
  @DisplayName("GET /api/staff/roles/{agencyId}/role/{role}")
  inner class StaffIsRole {
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
        .jsonPath("[*].staffId")
        .value<List<Int>> { assertThat(it).containsExactlyInAnyOrder(-1, -4, -11, -12) }
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
        .jsonPath("[*].staffId").value<List<Int>> { assertThat(it).containsExactlyInAnyOrder(-1, -4) }
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
        .jsonPath("[*].staffId").value<List<Int>> { assertThat(it).containsExactlyInAnyOrder(-1, -4) }
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
    fun `should return 403 if does not have override role`() {
      webTestClient.get()
        .uri("/api/staff/roles/BMI/role/KW")
        .headers(setClientAuthorisation(listOf("")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden
        .expectBody()
        .jsonPath("userMessage")
        .isEqualTo("Client not authorised to access agency with id BMI due to missing override role, or agency inactive")

      verify(telemetryClient).trackEvent(eq("ClientUnauthorisedAgencyAccess"), any(), isNull())
    }

    @Test
    fun `should return not found if agency does not exist`() {
      webTestClient.get()
        .uri("/api/staff/roles/XYZ/role/KW")
        .headers(setClientAuthorisation(listOf("STAFF_SEARCH")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNotFound
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

  @Nested
  @DisplayName("GET /api/staff/{staffId}/{agencyId}/roles")
  inner class StaffRoles {
    @Test
    fun `Should find roles for staff member`() {
      webTestClient.get()
        .uri("/api/staff/-2/LEI/roles")
        .headers(setAuthorisation("ITAG_USER", emptyList()))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(1)
        .jsonPath("$[0].role").isEqualTo("OS")
    }

    @ParameterizedTest
    @MethodSource("uk.gov.justice.hmpps.prison.api.resource.impl.StaffResourceIntTest#staffRolesTable")
    fun `List all active job roles for staff member at an agency`(table: StaffRolesRow) {
      webTestClient.get()
        .uri("/api/staff/${table.staffId}/${table.agencyId}/roles")
        .headers(setClientAuthorisation(listOf("STAFF_SEARCH")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("""[{"role":"${table.role}","roleDescription":"${table.roleDescription}"}]""")
    }
  }

  @Nested
  @DisplayName("GET /api/staff/{staffId}/{agencyId}/roles/{roleType}")
  inner class HasRole {
    //  -2 is a KW at BXI
    // -10 is a KW at SYI
    @Test
    fun `Should find role for staff member`() {
      assertThat(
        webTestClient.get()
          .uri("/api/staff/-2/BXI/roles/KW")
          .headers(setAuthorisation("ITAG_USER", listOf("")))
          .exchange()
          .expectStatus().isOk
          .expectBody(String::class.java)
          .returnResult()
          .responseBody,
      ).isEqualTo("true")
    }

    @Test
    fun `Should find KW role for staff member`() {
      assertThat(
        webTestClient.get()
          .uri("/api/staff/-1/LEI/roles/KW")
          .headers(setAuthorisation("PRISON_API_USER", emptyList()))
          .accept(MediaType.APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk
          .expectBody(String::class.java)
          .returnResult()
          .responseBody,
      ).isEqualTo("true")
    }

    @Test
    fun `Should not find role for staff member`() {
      assertThat(
        webTestClient.get()
          .uri("/api/staff/-2/LEI/roles/POM")
          .headers(setAuthorisation("ITAG_USER", listOf("")))
          .exchange()
          .expectStatus().isOk
          .expectBody(String::class.java)
          .returnResult()
          .responseBody,
      ).isEqualTo("false")
    }

    @Test
    fun `No access to find role for staff member at different agency`() {
      webTestClient.get()
        .uri("/api/staff/-2/RNI/roles/KW")
        .headers(setAuthorisation("PRISON_API_USER", listOf("")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `Should find role for staff member at different agency with override role`() {
      assertThat(
        webTestClient.get()
          .uri("/api/staff/-2/RNI/roles/KW")
          .headers(setAuthorisation("ITAG_USER", listOf("ROLE_STAFF_SEARCH")))
          .exchange()
          .expectStatus().isOk
          .expectBody(String::class.java)
          .returnResult()
          .responseBody,
      ).isEqualTo("false")
    }

    @Test
    fun `Should find role for staff member with override role`() {
      assertThat(
        webTestClient.get()
          .uri("/api/staff/-10/SYI/roles/KW")
          .headers(setClientAuthorisation(listOf("ROLE_STAFF_SEARCH")))
          .exchange()
          .expectStatus().isOk
          .expectBody(String::class.java)
          .returnResult()
          .responseBody,
      ).isEqualTo("true")
    }

    @Test
    fun `Should be able to check role for staff member at inactive agency`() {
      // PRISON_ANALYST_LOCAL -28 has the ghost establishment (inactive) in their caseload
      assertThat(
        webTestClient.get()
          .uri("/api/staff/-28/ZZGHI/roles/KW")
          .headers(setAuthorisation("PRISON_ANALYST_LOCAL", listOf("")))
          .exchange()
          .expectStatus().isOk
          .expectBody(String::class.java)
          .returnResult()
          .responseBody,
      ).isEqualTo("false")
    }
  }

  private companion object {
    @JvmStatic
    fun staffDetailsTable(): List<StaffDetailsRow> {
      return listOf(
        StaffDetailsRow(-1, "PRISON", "USER", "F", "1970-01-01"),
        StaffDetailsRow(-2, "API", "USER", "M", "1970-02-01"),
        StaffDetailsRow(-3, "CA", "USER", "M", "1970-03-01"),
        StaffDetailsRow(-5, "RO", "USER", "M", "1970-05-01"),
        StaffDetailsRow(-6, "DM", "USER", "M", "1970-06-01"),
      )
    }

    @JvmStatic
    fun staffRolesTable(): List<StaffRolesRow> {
      return listOf(
        StaffRolesRow(-2, "LEI", "OS", "Offender Supervisor"),
        StaffRolesRow(-1, "LEI", "KW", "Key Worker"),
        StaffRolesRow(-2, "BXI", "KW", "Key Worker"),
      )
    }
  }

  data class StaffDetailsRow(
    val staffId: Long,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val dob: String,
  )

  data class StaffRolesRow(
    val staffId: Long,
    val agencyId: String,
    val role: String,
    val roleDescription: String,
  )
}
