package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.core.io.FileSystemResource
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import java.io.File
import java.time.LocalDate

class ImageResourceIntTest : ResourceTest() {
  @Nested
  @DisplayName("GET /images/offenders/{offenderNo}")
  inner class GetOffender {

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/images/offenders/A1234AA")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if does not have role`() {
      webTestClient.get().uri("/api/images/offenders/A1234AA")
        .headers(setClientAuthorisation(emptyList()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun getImagesByOffender() {
      webTestClient.get().uri("/api/images/offenders/A1234AA")
        .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA"))).exchange()
        .expectStatus().isOk
        .expectBody().json(
          """
        [
          {
            "imageId":-1,
            "active": true,
            "captureDate":"2008-08-27",
            "captureDateTime":"2008-08-27T00:00:00",
            "imageView":"FACE",
            "imageOrientation":"FRONT",
            "imageType":"OFF_BKG"
          }
        ]
          """.trimIndent(),
        )
    }

    @Test
    fun getImagesByOffenderReturnsNotFound() {
      webTestClient.get().uri("/api/images/offenders/UNKNOWN")
        .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA"))).exchange()
        .expectStatus().isNotFound
    }
  }

  @Nested
  @DisplayName("GET /images/{imageId}")
  inner class GetImage {
    @Test
    fun getImage() {
      webTestClient.get().uri("/api/images/-1")
        .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA"))).exchange()
        .expectStatus().isOk
        .expectBody().json(
          """
        {
          "imageId":-1,
          "captureDate":"2008-08-27",
          "imageView":"FACE",
          "imageOrientation":"FRONT",
          "imageType":"OFF_BKG"
        }
          """.trimIndent(),
        )
    }

    @Test
    fun getImageReturnsNotFound() {
      webTestClient.get().uri("/api/images/9999")
        .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA"))).exchange()
        .expectStatus().isNotFound
    }
  }

  @Nested
  @DisplayName("GET /images/{imageId}/data")
  inner class GetData {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/images/-1/data")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if does not have role`() {
      webTestClient.get().uri("/api/images/-1/data")
        .headers(setClientAuthorisation(emptyList()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun getImageData() {
      webTestClient.get().uri("/api/images/-1/data")
        .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .accept(MediaType.IMAGE_JPEG)
        .exchange()
        .expectStatus().isOk
        .expectHeader().contentType(MediaType.IMAGE_JPEG)
    }

    @Test
    fun getImageDataReturnsNotFound() {
      webTestClient.get().uri("/api/images/9999/data")
        .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .accept(MediaType.IMAGE_JPEG)
        .exchange()
        .expectStatus().isNotFound
    }
  }

  @Nested
  @DisplayName("POST /images/offenders/{offenderNo}")
  inner class PutImage {
    @Test
    fun putImageReturnsForbiddenForIncorrectRoles() {
      webTestClient.post().uri("/api/images/offenders/A1234AA")
        .headers(setClientAuthorisation(listOf("ROLE_WRONG")))
        .body(generateMultiPartFormRequestWeb())
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun putImageReturnsNotFoundForInvalidOffender() {
      webTestClient.post().uri("/api/images/offenders/A9999XX")
        .headers(setClientAuthorisation(listOf("ROLE_IMAGE_UPLOAD")))
        .body(generateMultiPartFormRequestWeb())
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun putImageUploadsAndStoresScaledImages() {
      webTestClient.post().uri("/api/images/offenders/A1234AI")
        .headers(setAuthorisation("ITAG_USER", listOf("ROLE_IMAGE_UPLOAD")))
        .body(generateMultiPartFormRequestWeb())
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.imageId").value<Int> { assertThat(it).isGreaterThan(0) }
        .jsonPath("$.captureDate").value<String> { assertAfterYesterday(it) }
        .jsonPath("$.imageType").isEqualTo("OFF_BKG")
    }

    private fun assertAfterYesterday(it: String) {
      assertThat(LocalDate.parse(it)).isAfter(LocalDate.now().minusDays(1))
    }

    private fun generateMultiPartFormRequestWeb(): BodyInserters.MultipartInserter =
      LinkedMultiValueMap<String, FileSystemResource>()
        .apply { add("file", FileSystemResource(File(javaClass.getResource("/images/image.jpg")!!.file))) }
        .let { BodyInserters.fromMultipartData(it) }
  }
}
