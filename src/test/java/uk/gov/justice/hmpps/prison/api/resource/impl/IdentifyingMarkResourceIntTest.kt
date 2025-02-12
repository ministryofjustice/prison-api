package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.NORMAL_USER
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderIdentifyingMarkRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderImageRepository
import java.io.File
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

@ContextConfiguration(classes = [IdentifyingMarkResourceIntTest.TestClock::class])
class IdentifyingMarkResourceIntTest : ResourceTest() {

  @Autowired
  private lateinit var identifyingMarkRepository: OffenderIdentifyingMarkRepository

  @Autowired
  private lateinit var imageRepository: OffenderImageRepository

  @TestConfiguration
  internal class TestClock {
    @Bean
    fun clock(): Clock = Clock.fixed(
      LocalDateTime.of(2020, 1, 2, 3, 4, 5).atZone(ZoneId.systemDefault()).toInstant(),
      ZoneId.systemDefault(),
    )
  }

  @Nested
  @DisplayName("GET /api/identifying-marks/prisoner/{offenderId}")
  inner class GetAllIdentifyingMarksForLatestBooking {
    @Test
    fun `returns 401 when user does not have a token`() {
      webTestClient.get().uri("/api/identifying-marks/prisoner/A1234AA")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 if does not have override role`() {
      webTestClient.get().uri("/api/identifying-marks/prisoner/A1234AA")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 403 when client has incorrect role`() {
      webTestClient.get().uri("/api/identifying-marks/prisoner/A1234AA")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns success when client has override role ROLE_VIEW_PRISONER_DATA `() {
      webTestClient.get().uri("/api/identifying-marks/prisoner/A1234AA")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns 403 if not in user caseload`() {
      webTestClient.get().uri("/api/identifying-marks/prisoner/A1234AA")
        .headers(setAuthorisation("WAI_USER", listOf())).exchange().expectStatus().isForbidden
    }

    @Test
    fun `returns 403 if user has no caseloads`() {
      webTestClient.get().uri("/api/identifying-marks/prisoner/A1234AA")
        .headers(setAuthorisation("RO_USER", listOf())).exchange().expectStatus().isForbidden
    }

    @Test
    fun `returns 404 if prisoner not found`() {
      webTestClient.get().uri("/api/identifying-marks/prisoner/ZZ9999ZZ")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA"))).exchange().expectStatus().isNotFound
    }

    @Test
    fun `correctly returns all marks associated with the prisoner's latest booking`() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val httpEntity = createHttpEntity(token, null)
      val response = testRestTemplate.exchange(
        "/api/identifying-marks/prisoner/A1234AA",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )
      assertThatJsonFileAndStatus(response, 200, "identifying_marks_for_prisoner.json")
    }
  }

  @Nested
  @DisplayName("GET /api/identifying-marks/prisoner/{offenderId}/mark/{markId}")
  inner class GetIdentifyingMarkForLatestBooking {
    @Test
    fun `returns 401 when user does not have a token`() {
      webTestClient.get().uri("/api/identifying-marks/prisoner/A1234AA/mark/2")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 if does not have override role`() {
      webTestClient.get().uri("/api/identifying-marks/prisoner/A1234AA/mark/2")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 403 when client has incorrect role`() {
      webTestClient.get().uri("/api/identifying-marks/prisoner/A1234AA/mark/2")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns success when client has override role ROLE_VIEW_PRISONER_DATA `() {
      webTestClient.get().uri("/api/identifying-marks/prisoner/A1234AA/mark/2")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns 403 if not in user caseload`() {
      webTestClient.get().uri("/api/identifying-marks/prisoner/A1234AA/mark/2")
        .headers(setAuthorisation("WAI_USER", listOf())).exchange().expectStatus().isForbidden
    }

    @Test
    fun `returns 403 if user has no caseloads`() {
      webTestClient.get().uri("/api/identifying-marks/prisoner/A1234AA/mark/2")
        .headers(setAuthorisation("RO_USER", listOf())).exchange().expectStatus().isForbidden
    }

    @Test
    fun `returns 404 if prisoner not found`() {
      webTestClient.get().uri("/api/identifying-marks/prisoner/ZZ9999ZZ/mark/2")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA"))).exchange().expectStatus().isNotFound
    }

    @Test
    fun `returns 404 if mark not found`() {
      webTestClient.get().uri("/api/identifying-marks/prisoner/A1234AA/mark/999")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA"))).exchange().expectStatus().isNotFound
    }

    @Test
    fun `correctly returns specific mark associated with the prisoner's latest booking`() {
      val token = authTokenHelper.getToken(NORMAL_USER)
      val httpEntity = createHttpEntity(token, null)
      val response = testRestTemplate.exchange(
        "/api/identifying-marks/prisoner/A1234AA/mark/2",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )
      assertThatJsonFileAndStatus(response, 200, "identifying_mark_2.json")
    }
  }

  @Nested
  @DisplayName("GET /api/identifying-marks/photo/{photoId}")
  inner class GetImageContent {
    @Test
    fun `returns 401 when user does not have a token`() {
      webTestClient.get().uri("/api/identifying-marks/photo/-100")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 if does not have override role`() {
      webTestClient.get().uri("/api/identifying-marks/photo/-100")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 403 when client has incorrect role`() {
      webTestClient.get().uri("/api/identifying-marks/photo/-100")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns success when client has override role ROLE_VIEW_PRISONER_DATA `() {
      webTestClient.get().uri("/api/identifying-marks/photo/-100")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns 403 if not in user caseload`() {
      webTestClient.get().uri("/api/identifying-marks/photo/-100")
        .headers(setAuthorisation("WAI_USER", listOf())).exchange().expectStatus().isForbidden
    }

    @Test
    fun `returns 403 if user has no caseloads`() {
      webTestClient.get().uri("/api/identifying-marks/photo/-100")
        .headers(setAuthorisation("RO_USER", listOf())).exchange().expectStatus().isForbidden
    }

    @Test
    fun `returns 404 if mark not found`() {
      webTestClient.get().uri("/api/identifying-marks/photo/-999")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA"))).exchange().expectStatus().isNotFound
    }

    @Test
    fun `correctly returns image content`() {
      val token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA)
      val httpEntity = createHttpEntity(token, null)
      val response = testRestTemplate.exchange(
        "/api/identifying-marks/photo/-100",
        GET,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )
      Assertions.assertThat(response.body).isNotEmpty()
    }
  }

  @Nested
  @DisplayName("POST /api/identifying-marks/prisoner/{offenderId}/mark/{markId}/photo")
  inner class AddPhotoToMark {

    @Test
    fun `returns 401 when user does not have a token`() {
      webTestClient.post().uri("/api/identifying-marks/prisoner/A1234AA/mark/2/photo")
        .body(multiPartFormRequest())
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 if does not have override role`() {
      webTestClient.post().uri("/api/identifying-marks/prisoner/A1234AA/mark/2/photo")
        .headers(setClientAuthorisation(listOf()))
        .body(multiPartFormRequest())
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 403 when client has incorrect role`() {
      webTestClient.post().uri("/api/identifying-marks/prisoner/A1234AA/mark/2/photo")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .body(multiPartFormRequest())
        .exchange()
        .expectStatus().isForbidden
    }

    fun `returns success when client has override role ROLE_VIEW_PRISONER_DATA `() {
      webTestClient.post().uri("/api/identifying-marks/prisoner/A1234AA/mark/2/photo")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .body(multiPartFormRequest())
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns 403 if not in user caseload`() {
      webTestClient.post().uri("/api/identifying-marks/prisoner/A1234AA/mark/2/photo")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .body(multiPartFormRequest())
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 403 if user has no caseloads`() {
      webTestClient.post().uri("/api/identifying-marks/prisoner/A1234AA/mark/2/photo")
        .headers(setAuthorisation("RO_USER", listOf()))
        .body(multiPartFormRequest())
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 404 if prisoner not found`() {
      webTestClient.post().uri("/api/identifying-marks/prisoner/ZZ9999ZZ/mark/2/photo")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .body(multiPartFormRequest())
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `returns 404 if mark not found`() {
      webTestClient.post().uri("/api/identifying-marks/prisoner/A1234AA/mark/999/photo")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .body(multiPartFormRequest())
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `Adds the photo to the mark`() {
      val token = authTokenHelper.getToken(VIEW_PRISONER_DATA)
      val parameters: MultiValueMap<String, Any> = LinkedMultiValueMap()
      parameters.add("file", FileSystemResource(File(javaClass.getResource("/images/image.jpg")!!.file)))
      val httpEntity = createHttpEntity(
        token,
        parameters,
        contentType = MediaType.MULTIPART_FORM_DATA_VALUE,
      )

      val response = testRestTemplate.exchange(
        "/api/identifying-marks/prisoner/A1234AB/mark/1/photo",
        POST,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )

      assertThatJsonFileAndStatus(response, 200, "identifying_mark_add_photo.json")
    }

    private fun multiPartFormRequest(): BodyInserters.MultipartInserter = LinkedMultiValueMap<String, FileSystemResource>()
      .apply { add("file", FileSystemResource(File(javaClass.getResource("/images/image.jpg")!!.file))) }
      .let { BodyInserters.fromMultipartData(it) }
  }
}
