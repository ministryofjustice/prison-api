package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.Customization
import org.skyscreamer.jsonassert.JSONCompareMode
import org.skyscreamer.jsonassert.comparator.CustomComparator
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.NORMAL_USER
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.VIEW_PRISONER_DATA
import java.io.File

class IdentifyingMarkResourceIntTest : ResourceTest() {

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
      println(response)
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
        contentType = MULTIPART_FORM_DATA_VALUE,
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

  @Nested
  @DisplayName("PUT /api/identifying-marks/prisoner/{offenderId}/mark/{markId}")
  inner class UpdateExistingMark {
    private val updateRequest = """
      {
        "markType": "SCAR",
        "bodyPart": "ARM",
        "side": "R",
        "partOrientation": "LOW",
        "comment": "Old wound"
      }
    """.trimIndent()

    @Test
    fun `returns 401 when user does not have a token`() {
      webTestClient.put().uri("/api/identifying-marks/prisoner/A1234AB/mark/2")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(updateRequest)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 if does not have override role`() {
      webTestClient.put().uri("/api/identifying-marks/prisoner/A1234AB/mark/2")
        .headers(setClientAuthorisation(listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(updateRequest)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 403 when client has incorrect role`() {
      webTestClient.put().uri("/api/identifying-marks/prisoner/A1234AB/mark/2")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(updateRequest)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 403 if not in user caseload`() {
      webTestClient.put().uri("/api/identifying-marks/prisoner/A1234AB/mark/2")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(updateRequest)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 403 if user has no caseloads`() {
      webTestClient.put().uri("/api/identifying-marks/prisoner/A1234AB/mark/2")
        .headers(setAuthorisation("RO_USER", listOf()))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(updateRequest)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 404 if prisoner not found`() {
      webTestClient.put().uri("/api/identifying-marks/prisoner/ZZ9999ZZ/mark/2")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(updateRequest)
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `returns 404 if mark not found`() {
      webTestClient.put().uri("/api/identifying-marks/prisoner/A1234AA/mark/999")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(updateRequest)
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `Updates the existing mark`() {
      val token = authTokenHelper.getToken(VIEW_PRISONER_DATA)
      val httpEntity = createHttpEntity(token, updateRequest)

      val response = testRestTemplate.exchange(
        "/api/identifying-marks/prisoner/A1234AB/mark/2",
        PUT,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )

      assertThatJsonFileAndStatus(response, 200, "identifying_mark_updated.json")
    }
  }

  @Nested
  @DisplayName("POST /api/identifying-marks/prisoner/{offenderId}/mark/{markId}")
  inner class CreateNewMark {

    @Test
    fun `returns 401 when user does not have a token`() {
      webTestClient.post().uri("/api/identifying-marks/prisoner/A1234AC/mark")
        .bodyValue(multiPartBodyBuilder().build())
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `returns 403 if does not have override role`() {
      webTestClient.post().uri("/api/identifying-marks/prisoner/A1234AC/mark")
        .headers(setClientAuthorisation(listOf()))
        .bodyValue(multiPartBodyBuilder().build())
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 403 when client has incorrect role`() {
      webTestClient.post().uri("/api/identifying-marks/prisoner/A1234AC/mark")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .bodyValue(multiPartBodyBuilder().build())
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 403 if not in user caseload`() {
      webTestClient.post().uri("/api/identifying-marks/prisoner/A1234AC/mark")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .bodyValue(multiPartBodyBuilder().build())
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 403 if user has no caseloads`() {
      webTestClient.post().uri("/api/identifying-marks/prisoner/A1234AC/mark")
        .headers(setAuthorisation("RO_USER", listOf()))
        .bodyValue(multiPartBodyBuilder().build())
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 404 if prisoner not found`() {
      webTestClient.post().uri("/api/identifying-marks/prisoner/ZZ9999ZZ/mark")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .bodyValue(multiPartBodyBuilder().build())
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `Creates a new mark without an image`() {
      val token = authTokenHelper.getToken(VIEW_PRISONER_DATA)
      val parameters: MultiValueMap<String, Any> = LinkedMultiValueMap()
      parameters.add("bodyPart", "LEG")
      parameters.add("markType", "TAT")
      parameters.add("side", "R")
      parameters.add("partOrientation", "LOW")
      parameters.add("comment", "Some comment")

      val httpEntity = createHttpEntity(
        token,
        parameters,
        contentType = MULTIPART_FORM_DATA_VALUE,
      )

      val response = testRestTemplate.exchange(
        "/api/identifying-marks/prisoner/A1234AC/mark",
        POST,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )

      assertThatStatus(response, 200)
      val bodyAsJsonContent = getBodyAsJsonContent<Any>(response)
      Assertions.assertThat(bodyAsJsonContent).isEqualToJson(
        "identifying_mark_created_without_image.json",
        CustomComparator(JSONCompareMode.STRICT, Customization("createdAt") { _, _ -> true }),
      )
    }

    @Test
    fun `Creates a new mark with an image`() {
      val token = authTokenHelper.getToken(VIEW_PRISONER_DATA)
      val parameters: MultiValueMap<String, Any> = LinkedMultiValueMap()
      parameters.add("file", FileSystemResource(File(javaClass.getResource("/images/image.jpg")!!.file)))
      parameters.add("bodyPart", "LEG")
      parameters.add("markType", "TAT")
      parameters.add("side", "R")
      parameters.add("partOrientation", "LOW")
      parameters.add("comment", "Some comment")

      val httpEntity = createHttpEntity(
        token,
        parameters,
        contentType = MULTIPART_FORM_DATA_VALUE,
      )

      val response = testRestTemplate.exchange(
        "/api/identifying-marks/prisoner/A1234AD/mark",
        POST,
        httpEntity,
        object : ParameterizedTypeReference<String?>() {},
      )

      assertThatStatus(response, 200)
      val bodyAsJsonContent = getBodyAsJsonContent<Any>(response)
      Assertions.assertThat(bodyAsJsonContent).isEqualToJson(
        "identifying_mark_created_with_image.json",
        CustomComparator(JSONCompareMode.STRICT, Customization("createdAt") { _, _ -> true }),
      )
    }

    private fun multiPartBodyBuilder(): MultipartBodyBuilder {
      val bodyBuilder = MultipartBodyBuilder()
      bodyBuilder.part("file", FileSystemResource(File(javaClass.getResource("/images/image.jpg")!!.file)))
        .header("Content-Disposition", "form-data; name=file; filename=image.jpg")
      bodyBuilder.part("bodyPart", "LEG")
      bodyBuilder.part("markType", "TAT")
      bodyBuilder.part("side", "R")
      bodyBuilder.part("partOrientation", "LOW")
      bodyBuilder.part("comment", "Some comment")

      return bodyBuilder
    }
  }
}
