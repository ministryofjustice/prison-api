package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import uk.gov.justice.hmpps.prison.helper.builder.GangBuilder

class GangResourceTest : ResourceTest() {

  @Autowired
  lateinit var gangBuilder: GangBuilder

  @BeforeEach
  internal fun setup() {
    gangBuilder.initGangs()
  }

  @AfterEach
  internal fun tearDown() {
    gangBuilder.teardown()
  }

  @Test
  fun `Test that endpoint returns a summary list of gang non-associations`() {
    webTestClient.get()
      .uri("/api/gang/non-associations/A1234AD")
      .headers(
        setAuthorisation(
          listOf("ROLE_VIEW_GANG"),
        ),
      )
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("member.offenderNo").isEqualTo("A1234AD")
      .jsonPath("currentGangs.size()").isEqualTo("1")
      .jsonPath("gangNonAssociations.size()").isEqualTo("2")
  }

  @Test
  fun `Test that endpoint returns forbidden without role`() {
    webTestClient.get()
      .uri("/api/gang/non-associations/A1234AD")
      .headers(
        setAuthorisation(
          listOf("ROLE_DUMMY"),
        ),
      )
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `Test that endpoint returns a 404 when not found`() {
    webTestClient.get()
      .uri("/api/gang/non-associations/XXXXXXX")
      .headers(
        setAuthorisation(
          listOf("ROLE_VIEW_GANG"),
        ),
      )
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isNotFound
  }
}
