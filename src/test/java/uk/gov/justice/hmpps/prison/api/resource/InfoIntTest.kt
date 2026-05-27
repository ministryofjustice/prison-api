package uk.gov.justice.hmpps.prison.api.resource

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.info.BuildProperties
import uk.gov.justice.hmpps.prison.api.resource.impl.ResourceTest
import kotlin.text.get

class InfoIntTest(
  @Autowired private val buildProperties: BuildProperties,
) : ResourceTest() {
  @Test
  fun `Info page is accessible`() {
    webTestClient.get()
      .uri("/info")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("build.name").isEqualTo("prison-api")
  }

  @Test
  fun testInfoPageContainsGitInformation() {
    webTestClient.get()
      .uri("/info")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("git.commit.id").isNotEmpty
  }

  @Test
  fun `Info page reports version`() {
    webTestClient.get().uri("/info")
      .exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("build.version").isEqualTo(buildProperties.version)
  }

  @Test
  fun testReadInfoWithCachePopulated() {
    webTestClient.get()
      .uri("/api/reference-domains/domains/{domain}", "TASK_TYPE")
      .headers(setAuthorisation(listOf()))
      .exchange()
      .expectStatus().isOk

    webTestClient.get().uri("/info").exchange().expectStatus().isOk
  }
}
