package uk.gov.justice.hmpps.prison.api.resource.impl

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters

class UserResourceTest : ResourceTest() {

  @Nested
  @DisplayName("GET /api/users/me/caseNoteTypes")
  inner class CaseNoteTypes {
    @Test
    fun `Retrieve valid case note types for current user`() {
      webTestClient.get()
        .uri("/api/users/me/caseNoteTypes")
        .headers(setAuthorisation("ITAG_USER", emptyList()))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(11)
        .jsonPath("$[*].subCodes.length()")
        .value<List<Int>> { assertThat(it).contains(7, 6, 2, 2, 2, 4, 4, 1, 4, 1, 3) }
    }
  }

  @Nested
  @DisplayName("GET /api/users/me/locations")
  inner class Locations {
    /**
     *  Return locations for logged in staff users based on user-related context information (e.g. number of caseloads/agencies):
     *     - only institution-level locations are returned for a logged in staff user with multiple caseloads (and, hence, multiple agencies)
     *     - only institution-level locations are returned for a logged in staff user with single caseload associated with multiple agencies
     *     - institution and wing-level locations are returned for a logged in staff user with a single caseload associated with single agency
     */
    @ParameterizedTest
    @MethodSource("uk.gov.justice.hmpps.prison.api.resource.impl.UserResourceTest#locationsTable")
    fun `Retrieve user locations`(table: LocationRow) {
      webTestClient.get()
        .uri("/api/users/me/locations?include-non-residential-locations=${table.nonResidential}")
        .headers(setAuthorisation(table.token, emptyList()))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$[*].agencyId").value<List<String>> {
          assertThat(it).containsExactlyInAnyOrderElementsOf(table.agencies)
        }
        .jsonPath("$[*].description").value<List<String>> {
          assertThat(it).containsExactlyInAnyOrderElementsOf(table.descriptions)
        }
        .jsonPath("$[*].locationPrefix").value<List<String>> {
          assertThat(it).containsExactlyInAnyOrderElementsOf(table.prefixes)
        }
    }
  }

  @Nested
  @DisplayName("GET /api/users/me/roles")
  inner class Roles {
    @ParameterizedTest
    @MethodSource("uk.gov.justice.hmpps.prison.api.resource.impl.UserResourceTest#rolesTableAll")
    fun `As a logged in user I can find out all my roles`(table: RoleRow) {
      webTestClient.get()
        .uri("/api/users/me/roles?allRoles=true")
        .headers(setAuthorisation(table.token, emptyList()))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$[*].roleCode").value<List<String>> {
          assertThat(it).containsExactlyInAnyOrderElementsOf(table.roles)
        }
    }

    @ParameterizedTest
    @MethodSource("uk.gov.justice.hmpps.prison.api.resource.impl.UserResourceTest#rolesTable")
    fun `As a logged in user I can find out just my api roles`(table: RoleRow) {
      webTestClient.get()
        .uri("/api/users/me/roles")
        .headers(setAuthorisation(table.token, emptyList()))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$[*].roleCode").value<List<String>> {
          assertThat(it).containsExactlyInAnyOrderElementsOf(table.roles)
        }
    }
  }

  @Nested
  @DisplayName("PUT /api/users/me/activeCaseLoad")
  inner class ActiveCaseLoad {

    @Test
    fun `should update active caseload for current user`() {
      webTestClient.put()
        .uri("/api/users/me/activeCaseLoad")
        .headers(setAuthorisation("ITAG_USER", emptyList()))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .body(BodyInserters.fromValue("""{"caseLoadId":"LEI","description":"Leeds HMP"}"""))
        .exchange()
        .expectStatus().isOk
        .expectBody().isEmpty
    }

    @Test
    fun `should not allow setting active caseload to a caseload not in users list`() {
      webTestClient.put()
        .uri("/api/users/me/activeCaseLoad")
        .headers(setAuthorisation("ITAG_USER", emptyList()))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .body(BodyInserters.fromValue("""{"caseLoadId":"WWI","description":"Disallowed prison)"}"""))
        .exchange()
        .expectStatus().isForbidden
        .expectBody()
        .json("""{"status":403,"userMessage":"The user does not have access to the caseLoadId = WWI"}""")
    }
  }

  @Nested
  @DisplayName("POST /api/users/list")
  inner class UserList {
    @Test
    fun `should get list of users`() {
      webTestClient.post()
        .uri("/api/users/list")
        .headers(setClientAuthorisation(listOf("ROLE_STAFF_SEARCH")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .body(BodyInserters.fromValue("""["ITAG_USER","RO_USER"]"""))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json(
          """
          [{ "staffId":-5,"username":"RO_USER",  "firstName":"Ro", "lastName":"User","accountStatus":"ACTIVE","lockedFlag":false,"expiredFlag":false,"active":true },
           { "staffId":-2,"username":"ITAG_USER","firstName":"Api","lastName":"User","accountStatus":"ACTIVE","lockedFlag":false,"expiredFlag":false,"active":true,"activeCaseLoadId":"LEI" }]
          """.trimIndent(),
        )
    }

    @Test
    fun `should get forbidden with auth-user token`() {
      webTestClient.post()
        .uri("/api/users/list")
        .headers(setAuthorisation("ITAG_USER", emptyList()))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .body(BodyInserters.fromValue("""["ITAG_USER","API_USER","RO_USER"]"""))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should get forbidden when wrong role`() {
      webTestClient.post()
        .uri("/api/users/list")
        .headers(setClientAuthorisation(listOf("ROLE_OTHER")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .body(BodyInserters.fromValue("""["ITAG_USER","API_USER","RO_USER"]"""))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `A list of staff users by usernames can be retrieved`() {
      webTestClient.post()
        .uri("/api/users/list")
        .headers(setAuthorisation("ITAG_USER", listOf("ROLE_STAFF_SEARCH")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .body(BodyInserters.fromValue("""["JBRIEN","RENEGADE"]"""))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$[*].username").value<List<String>> {
          assertThat(it).containsExactlyInAnyOrderElementsOf(listOf("JBRIEN", "RENEGADE"))
        }
    }
  }

  @Nested
  @DisplayName("PUT /api/users/add/default/{caseload}")
  inner class AddDefaultCaseLoad {
    @Test
    fun `should add default caseload for current user`() {
      webTestClient.put()
        .uri("/api/users/add/default/MDI")
        .headers(setClientAuthorisation(listOf("ROLE_MAINTAIN_ACCESS_ROLES")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("""{ "caseload":"MDI", "numUsersEnabled":0 }""")
    }

    @Test
    fun `should return forbidden when wrong role`() {
      webTestClient.put()
        .uri("/api/users/add/default/MDI")
        .headers(setClientAuthorisation(listOf("ROLE_OTHER")))
        .exchange()
        .expectStatus().isForbidden
    }
  }

  private companion object {
    @JvmStatic
    fun rolesTableAll(): List<RoleRow> {
      return listOf(
        RoleRow(
          "ITAG_USER",
          listOf(
            "BXI_WING_OFF",
            "LEI_WING_OFF",
            "MDI_WING_OFF",
            "NWEB_ACCESS_ROLE_ADMIN",
            "NWEB_KW_ADMIN",
            "NWEB_OMIC_ADMIN",
            "SYI_WING_OFF",
            "WAI_WING_OFF",
            "NWEB_MAINTAIN_ACCESS_ROLES",
            "NWEB_MAINTAIN_ACCESS_ROLES_ADMIN",
          ),
        ),
        RoleRow(
          "API_TEST_USER",
          listOf("MUL_WING_OFF", "NWEB_KW_ADMIN", "NWEB_OMIC_ADMIN"),
        ),
      )
    }

    @JvmStatic
    fun rolesTable(): List<RoleRow> {
      return listOf(
        RoleRow(
          "ITAG_USER",
          listOf("KW_ADMIN", "OMIC_ADMIN", "ACCESS_ROLE_ADMIN", "MAINTAIN_ACCESS_ROLES", "MAINTAIN_ACCESS_ROLES_ADMIN"),
        ),
        RoleRow(
          "API_TEST_USER",
          listOf("KW_ADMIN", "OMIC_ADMIN"),
        ),
        RoleRow(
          "RO_USER",
          listOf("VIEW_PRISONER_DATA", "LICENCE_RO"),
        ),
      )
    }

    @JvmStatic
    fun locationsTable(): List<LocationRow> {
      return listOf(
        LocationRow(
          "ITAG_USER",
          false,
          9,
          listOf("LEI", "LEI", "LEI", "LEI", "LEI", "LEI", "LEI", "LEI", "LEI"),
          listOf("Block A", "C", "D", "E", "F", "H", "I", "Leeds", "S"),
          listOf("LEI", "LEI-A", "LEI-C", "LEI-D", "LEI-E", "LEI-F", "LEI-H", "LEI-I", "LEI-S"),
        ),
        LocationRow(
          "API_TEST_USER",
          false,
          10,
          listOf("BXI", "LEI", "LEI", "LEI", "LEI", "LEI", "LEI", "LEI", "LEI", "LEI"),
          listOf("Block A", "Brixton", "C", "D", "E", "F", "H", "I", "Leeds", "S"),
          listOf("BXI", "LEI", "LEI-A", "LEI-C", "LEI-D", "LEI-E", "LEI-F", "LEI-H", "LEI-I", "LEI-S"),
        ),
        LocationRow(
          "ITAG_USER",
          true,
          13,
          listOf("LEI", "LEI", "LEI", "LEI", "LEI", "LEI", "LEI", "LEI", "LEI", "LEI", "LEI", "LEI", "LEI"),
          listOf("Block A", "C", "Court", "Cswap", "D", "E", "F", "H", "I", "Leeds", "Recp", "S", "Tap"),
          listOf(
            "LEI",
            "LEI-A",
            "LEI-C",
            "LEI-COURT",
            "LEI-CSWAP",
            "LEI-D",
            "LEI-E",
            "LEI-F",
            "LEI-H",
            "LEI-I",
            "LEI-RECP",
            "LEI-S",
            "LEI-TAP",
          ),
        ),
        LocationRow(
          "API_TEST_USER",
          true,
          14,
          listOf("BXI", "LEI", "LEI", "LEI", "LEI", "LEI", "LEI", "LEI", "LEI", "LEI", "LEI", "LEI", "LEI", "LEI"),
          listOf("Block A", "Brixton", "C", "Court", "Cswap", "D", "E", "F", "H", "I", "Leeds", "Recp", "S", "Tap"),
          listOf(
            "BXI",
            "LEI",
            "LEI-A",
            "LEI-C",
            "LEI-COURT",
            "LEI-CSWAP",
            "LEI-D",
            "LEI-E",
            "LEI-F",
            "LEI-H",
            "LEI-I",
            "LEI-RECP",
            "LEI-S",
            "LEI-TAP",
          ),
        ),
      )
    }
  }

  data class RoleRow(
    val token: String,
    val roles: List<String>,
  )

  data class LocationRow(
    val token: String,
    val nonResidential: Boolean,
    val number: Int,
    val agencies: List<String>,
    val descriptions: List<String>,
    val prefixes: List<String>,
  )
}
