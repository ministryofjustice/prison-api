package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.hmpps.prison.api.model.OffenderTeamAssignmentResponse
import java.time.LocalDate

@Sql("/sql/offender_team_assignments_init.sql")
@Sql(scripts = ["/sql/offender_team_assignments_clean.sql"], executionPhase = AFTER_TEST_METHOD)
class OffenderTeamAssignmentResourceTest : ResourceTest() {
  private val expected = listOf(
    OffenderTeamAssignmentResponse(-20, "AAA", -90001, LocalDate.of(2020, 1, 1)),
    OffenderTeamAssignmentResponse(-1, "AAA", -90001, LocalDate.of(2020, 2, 2)),
    OffenderTeamAssignmentResponse(-1, "ZZZ", -90001, LocalDate.of(2020, 3, 3)),
  )

  @Test
  fun `requires authentication`() {
    webTestClient.get().uri("/api/offender-team-assignments")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `requires global prisoner read permission`() {
    webTestClient.get().uri("/api/offender-team-assignments")
      .headers(setClientAuthorisation(listOf()))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `requires read scope even with the global role`() {
    val token = jwtAuthenticationHelper.createJwtAccessToken(
      roles = listOf("VIEW_PRISONER_DATA"),
      scope = listOf("write"),
    )
    webTestClient.get().uri("/api/offender-team-assignments")
      .headers { it.setBearerAuth(token) }
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `returns flat assignments across bookings including inactive teams`() {
    getPage()
      .jsonPath("content.length()").isEqualTo(3)
      .jsonPath("content[0]").isEqualTo(
        mapOf("bookingId" to -20, "functionType" to "AAA", "teamId" to -90001, "assignmentDate" to "2020-01-01"),
      )
      .jsonPath("number").isEqualTo(0)
      .jsonPath("size").isEqualTo(500)
      .jsonPath("totalElements").isEqualTo(3)
      .jsonPath("totalPages").isEqualTo(1)
      .jsonPath("last").isEqualTo(true)
  }

  @Test
  fun `looping through pages returns every row once in fixed composite key order`() {
    val actual = mutableListOf<OffenderTeamAssignmentResponse>()
    var page = 0
    do {
      val body = getPage("?page=$page&size=2&sort=functionType,desc")
        .jsonPath("number").isEqualTo(page)
        .jsonPath("totalPages").isEqualTo(2)
        .jsonPath("last").isEqualTo(page == 1)
        .returnResult().responseBody!!
      val json = jsonMapper.readTree(body)
      for (index in 0 until json["content"].size()) {
        actual += jsonMapper.treeToValue(json["content"][index], OffenderTeamAssignmentResponse::class.java)
      }
      val last = json["last"].booleanValue()
      page++
    } while (!last && page < 10)

    assertThat(page).isEqualTo(2)
    assertThat(actual).containsExactlyElementsOf(expected)
  }

  @Test
  fun `exactly full final page reports completion`() {
    getPage("?size=3")
      .jsonPath("content.length()").isEqualTo(3)
      .jsonPath("numberOfElements").isEqualTo(3)
      .jsonPath("last").isEqualTo(true)
  }

  @Test
  fun `page beyond the end is empty`() {
    getPage("?page=2&size=2")
      .jsonPath("content").isEmpty
      .jsonPath("number").isEqualTo(2)
      .jsonPath("totalElements").isEqualTo(3)
      .jsonPath("last").isEqualTo(true)
  }

  @Test
  fun `empty table returns a completed empty page`() {
    dataLoader.jdbcTemplate.update("DELETE FROM OFFENDER_TEAM_ASSIGNMENTS WHERE TEAM_ID = -90001")
    getPage()
      .jsonPath("content").isEmpty
      .jsonPath("totalElements").isEqualTo(0)
      .jsonPath("last").isEqualTo(true)
  }

  @ParameterizedTest
  @ValueSource(strings = ["page=-1", "size=0", "size=-1", "page=invalid", "size=invalid"])
  fun `rejects invalid pagination`(query: String) {
    webTestClient.get().uri("/api/offender-team-assignments?$query")
      .headers(setClientAuthorisation(listOf("VIEW_PRISONER_DATA")))
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `caps page size at the configured maximum`() {
    getPage("?size=10000")
      .jsonPath("size").isEqualTo(5000)
      .jsonPath("content.length()").isEqualTo(3)
  }

  private fun getPage(query: String = ""): WebTestClient.BodyContentSpec = webTestClient.get()
    .uri("/api/offender-team-assignments$query")
    .headers(setClientAuthorisation(listOf("VIEW_PRISONER_DATA")))
    .exchange()
    .expectStatus().isOk
    .expectBody()
}
