package uk.gov.justice.hmpps.prison.api.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import uk.gov.justice.hmpps.prison.api.resource.impl.ResourceTest
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class InfoIntTest : ResourceTest() {
  @Test
  fun testInfoPageContainsGitInformation() {
    val token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER)
    val response: ResponseEntity<String> = testRestTemplate.exchange(
      "/info",
      HttpMethod.GET,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThat<HttpStatusCode>(response.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    )!!.extractingJsonPathStringValue("git.commit.id").isNotBlank()
  }

  @Test
  fun testInfoPageReportsVersion() {
    val token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER)
    val response: ResponseEntity<String> = testRestTemplate.exchange(
      "/info",
      HttpMethod.GET,
      createHttpEntity(token, null),
      object : ParameterizedTypeReference<String>() {
      },
    )

    assertThat<HttpStatusCode>(response.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(
      getBodyAsJsonContent<Any>(
        response,
      ),
    )!!.extractingJsonPathStringValue("build.version")
      .startsWith(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE))
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
