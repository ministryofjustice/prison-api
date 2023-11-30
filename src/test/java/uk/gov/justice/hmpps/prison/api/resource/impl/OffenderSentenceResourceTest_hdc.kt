package uk.gov.justice.hmpps.prison.api.resource.impl

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import uk.gov.justice.hmpps.prison.api.model.HdcChecks
import uk.gov.justice.hmpps.prison.service.curfews.OffenderCurfewService
import java.time.LocalDate

class OffenderSentenceResourceTest_hdc : ResourceTest() {

  @Autowired
  private lateinit var offenderCurfewService: OffenderCurfewService

  @Nested
  @DisplayName("GET /api/offender-sentences/booking/{bookingId}/home-detention-curfews/latest")
  inner class LatestCurfewForBooking {
    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.get().uri("/api/offender-sentences/booking/-35/home-detention-curfews/latest")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if no override role`() {
      webTestClient.get().uri("/api/offender-sentences/booking/-35/home-detention-curfews/latest")
        .headers(setClientAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 200 for client if booking not found`() {
      webTestClient.get().uri("/api/offender-sentences/booking/-99999/home-detention-curfews/latest")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -99999 not found.")
    }

    @Test
    fun `should return 403 if has client has ROLE_SYSTEM_USER role`() {
      webTestClient.get().uri("/api/offender-sentences/booking/-35/home-detention-curfews/latest")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 200 when client has override role ROLE_VIEW_PRISONER_DATA`() {
      webTestClient.get().uri("/api/offender-sentences/booking/-35/home-detention-curfews/latest")
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `returns 404 if user has no caseloads`() {
      webTestClient.get().uri("/api/offender-sentences/booking/-35/home-detention-curfews/latest")
        .headers(setAuthorisation("RO_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -35 not found.")
    }

    @Test
    fun `returns 404 if not in user caseload`() {
      webTestClient.get().uri("/api/offender-sentences/booking/-35/home-detention-curfews/latest")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -35 not found.")
      verify(telemetryClient).trackEvent(eq("UserUnauthorisedBookingAccess"), any(), isNull())
    }

    @Test
    fun `returns 404 if booking not found`() {
      webTestClient.get().uri("/api/offender-sentences/booking/-99999/home-detention-curfews/latest")
        .headers(setAuthorisation("WAI_USER", listOf()))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Offender booking with id -99999 not found.")
    }

    @Test
    fun `should return success when user has booking in caseload`() {
      webTestClient.get().uri("/api/offender-sentences/booking/-7/home-detention-curfews/latest")
        .headers(setAuthorisation(listOf()))
        .exchange()
        .expectStatus().isOk
    }
  }

  @Nested
  @DisplayName("POST /api/offender-sentences/home-detention-curfews/latest")
  inner class GetLatestCurfews {

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.post().uri("/api/offender-sentences/home-detention-curfews/latest")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue("[ -1, -2, -3 ]").exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if no override role`() {
      webTestClient.post().uri("/api/offender-sentences/home-detention-curfews/latest")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setClientAuthorisation(listOf()))
        .bodyValue("[ -1, -2, -3 ]").exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return 403 if as role ROLE_SYSTEM_USER`() {
      webTestClient.post().uri("/api/offender-sentences/home-detention-curfews/latest")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .bodyValue("[ -1, -2, -3 ]").exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 200 when client has override role ROLE_VIEW_PRISONER_DATA`() {
      webTestClient.post().uri("/api/offender-sentences/home-detention-curfews/latest")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .bodyValue("[ -1, -2, -3, 7 ]").exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("length()").isEqualTo(3)
    }

    @Test
    fun `returns empty body if booking not found`() {
      webTestClient.post().uri("/api/offender-sentences/home-detention-curfews/latest")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setClientAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
        .bodyValue("[-99999]").exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("length()").isEqualTo(0)
    }
  }

  @Nested
  @DisplayName("PUT /api/offender-sentences/booking/{bookingId}/home-detention-curfews/latest/checks-passed")
  inner class SetHDCChecks {
    private val hdcCheck =
      """
        {
          "date": "2018-01-31",
          "passed": true
        }
      """

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.put().uri("/api/offender-sentences/booking/-35/home-detention-curfews/latest/checks-passed")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(hdcCheck).exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if no override role`() {
      webTestClient.put().uri("/api/offender-sentences/booking/-35/home-detention-curfews/latest/checks-passed")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setClientAuthorisation(listOf()))
        .bodyValue(hdcCheck).exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 404 if booking not found`() {
      webTestClient.put().uri("/api/offender-sentences/booking/-99999/home-detention-curfews/latest/checks-passed")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setClientAuthorisation(listOf("ROLE_MAINTAIN_HDC")))
        .bodyValue(hdcCheck).exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("No 'latest' Home Detention Curfew found for bookingId -99999")
    }

    @Test
    fun `returns 400 if missing passed value`() {
      webTestClient.put().uri("/api/offender-sentences/booking/-99999/home-detention-curfews/latest/checks-passed")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setClientAuthorisation(listOf("ROLE_MAINTAIN_HDC")))
        .bodyValue(
          """
            {
              "date": "2018-01-31"
            }
          """,
        ).exchange()
        .expectStatus().isBadRequest
        .expectBody().jsonPath("userMessage").isEqualTo("Field: passed - must not be null")
    }

    @Test
    fun `returns 400 if missing date value`() {
      webTestClient.put().uri("/api/offender-sentences/booking/-99999/home-detention-curfews/latest/checks-passed")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setClientAuthorisation(listOf("ROLE_MAINTAIN_HDC")))
        .bodyValue(
          """
            {
               "passed": true
            }
          """,
        ).exchange()
        .expectStatus().isBadRequest
        .expectBody().jsonPath("userMessage").isEqualTo("Field: date - must not be null")
    }

    @Test
    fun `returns 400 if invalid date value`() {
      webTestClient.put().uri("/api/offender-sentences/booking/-99999/home-detention-curfews/latest/checks-passed")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setClientAuthorisation(listOf("ROLE_MAINTAIN_HDC")))
        .bodyValue(
          """
            {
              "date": "xxxxxxxxxx",
              "passed": true
            }
          """,
        ).exchange()
        .expectStatus().isBadRequest
    }

    @Test
    fun `returns 400 if impossible date value`() {
      webTestClient.put().uri("/api/offender-sentences/booking/-35/home-detention-curfews/latest/checks-passed")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setClientAuthorisation(listOf("ROLE_MAINTAIN_HDC")))
        .bodyValue(
          """
            {
              "date": "2019-02-31",
              "passed": true
            }
          """,
        ).exchange()
        .expectStatus().isBadRequest
    }

    @Test
    fun `returns success if passed=true when client has override role ROLE_MAINTAIN_HDC`() {
      webTestClient.put().uri("/api/offender-sentences/booking/-35/home-detention-curfews/latest/checks-passed")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setClientAuthorisation(listOf("ROLE_MAINTAIN_HDC")))
        .bodyValue(
          """
            {
              "date": "2019-01-31",
              "passed": true
            }
          """,
        ).exchange()
        .expectStatus().isNoContent

      val latestHDC = offenderCurfewService.getLatestHomeDetentionCurfew(-35)
      assertThat(latestHDC.passed).isEqualTo(true)
      assertThat(latestHDC.checksPassedDate).isEqualTo("2019-01-31")
    }

    @Test
    fun `returns success if passed=false when client has override role ROLE_MAINTAIN_HDC`() {
      webTestClient.put().uri("/api/offender-sentences/booking/-35/home-detention-curfews/latest/checks-passed")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setClientAuthorisation(listOf("ROLE_MAINTAIN_HDC")))
        .bodyValue(
          """
            {
              "date": "2020-01-31",
              "passed": false
            }
          """,
        ).exchange()
        .expectStatus().isNoContent
      val latestHDC = offenderCurfewService.getLatestHomeDetentionCurfew(-35)
      assertThat(latestHDC.passed).isEqualTo(false)
      assertThat(latestHDC.checksPassedDate).isEqualTo("2020-01-31")
    }
  }

  @Nested
  @DisplayName("PUT /api/offender-sentences/booking/{bookingId}/home-detention-curfews/latest/approval-status")
  inner class SetHDCApprovalStatus {
    private val approvalStatus =
      """
        {
          "approvalStatus": "APPROVED",
          "date": "2018-01-31"
         }
       """

    @Test
    fun `should return 401 when user does not even have token`() {
      webTestClient.put().uri("/api/offender-sentences/booking/-36/home-detention-curfews/latest/approval-status")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .bodyValue(approvalStatus).exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 if no override role`() {
      webTestClient.put().uri("/api/offender-sentences/booking/-36/home-detention-curfews/latest/approval-status")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setClientAuthorisation(listOf()))
        .bodyValue(approvalStatus).exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `returns 404 if booking not found`() {
      webTestClient.put().uri("/api/offender-sentences/booking/-99999/home-detention-curfews/latest/approval-status")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setClientAuthorisation(listOf("ROLE_MAINTAIN_HDC")))
        .bodyValue(approvalStatus).exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("No 'latest' Home Detention Curfew found for bookingId -99999")
    }

    @Test
    fun `returns 400 if missing approvalStatus value`() {
      webTestClient.put().uri("/api/offender-sentences/booking/-99999/home-detention-curfews/latest/approval-status")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setClientAuthorisation(listOf("ROLE_MAINTAIN_HDC")))
        .bodyValue(
          """
            {
              "date": "2018-01-31"
            }
          """,
        ).exchange()
        .expectStatus().isBadRequest
        .expectBody().jsonPath("userMessage").isEqualTo("Field: approvalStatus - Value cannot be blank")
    }

    @Test
    fun `returns 400 if missing date value`() {
      webTestClient.put().uri("/api/offender-sentences/booking/-36/home-detention-curfews/latest/approval-status")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setClientAuthorisation(listOf("ROLE_MAINTAIN_HDC")))
        .bodyValue(
          """
            {
               "approvalStatus": "APPROVED"
            }
          """,
        ).exchange()
        .expectStatus().isBadRequest
        .expectBody().jsonPath("userMessage").isEqualTo("Field: date - must not be null")
    }

    @Test
    fun `returns 400 if invalid date value`() {
      webTestClient.put().uri("/api/offender-sentences/booking/-36/home-detention-curfews/latest/approval-status")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setClientAuthorisation(listOf("ROLE_MAINTAIN_HDC")))
        .bodyValue(
          """
            {
              "date": "xxxxxxxxxx",
              "approvalStatus": "APPROVED"
            }
          """,
        ).exchange()
        .expectStatus().isBadRequest
    }

    @Test
    fun `returns 400 if impossible date value`() {
      webTestClient.put().uri("/api/offender-sentences/booking/-36/home-detention-curfews/latest/approval-status")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setClientAuthorisation(listOf("ROLE_MAINTAIN_HDC")))
        .bodyValue(
          """
            {
              "date": "2018-02-31",
              "approvalStatus": "APPROVED"
            }
          """,
        ).exchange()
        .expectStatus().isBadRequest
    }

    @Test
    fun `returns success if approved when client has override role ROLE_MAINTAIN_HDC`() {
      offenderCurfewService.setHdcChecks(-36, HdcChecks(true, LocalDate.parse("2017-01-31")))
      webTestClient.put().uri("/api/offender-sentences/booking/-36/home-detention-curfews/latest/approval-status")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setClientAuthorisation(listOf("ROLE_MAINTAIN_HDC")))
        .bodyValue(approvalStatus).exchange()
        .expectStatus().isNoContent

      val latestHDC = offenderCurfewService.getLatestHomeDetentionCurfew(-36)
      assertThat(latestHDC.passed).isEqualTo(true)
      assertThat(latestHDC.refusedReason).isNull()
      assertThat(latestHDC.checksPassedDate).isEqualTo("2017-01-31")
      assertThat(latestHDC.approvalStatusDate).isEqualTo("2018-01-31")

      resetTestData()
    }

    @Test
    fun `returns success if refused when client has override role ROLE_MAINTAIN_HDC`() {
      offenderCurfewService.setHdcChecks(-36, HdcChecks(true, LocalDate.parse("2020-01-31")))

      webTestClient.put().uri("/api/offender-sentences/booking/-36/home-detention-curfews/latest/approval-status")
        .header("Content-Type", APPLICATION_JSON_VALUE)
        .headers(setClientAuthorisation(listOf("ROLE_MAINTAIN_HDC")))
        .bodyValue(
          """
            {
              "approvalStatus": "OPT_OUT",
              "refusedReason": "BREACH",
              "date": "2021-01-31"
            }
          """,
        ).exchange()
        .expectStatus().isNoContent

      val latestHDC = offenderCurfewService.getLatestHomeDetentionCurfew(-36)
      assertThat(latestHDC.approvalStatus).isEqualTo("OPT_OUT")
      assertThat(latestHDC.refusedReason).isEqualTo("BREACH")
      assertThat(latestHDC.checksPassedDate).isEqualTo("2020-01-31")
      assertThat(latestHDC.approvalStatusDate).isEqualTo("2021-01-31")

      resetTestData()
    }
  }

  private fun resetTestData() {
    webTestClient.delete()
      .uri("/api/offender-sentences/booking/-36/home-detention-curfews/latest/approval-status")
      .headers(setClientAuthorisation(listOf("ROLE_MAINTAIN_HDC")))
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isNoContent
    webTestClient.delete()
      .uri("/api/offender-sentences/booking/-36/home-detention-curfews/latest/checks-passed")
      .headers(setClientAuthorisation(listOf("ROLE_MAINTAIN_HDC")))
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isNoContent
  }

  @Nested
  @DisplayName("DELETE /api/offender-sentences/booking/{bookingId}/home-detention-curfews/latest/checks-passed")
  inner class ClearHDCChecksPassedFlag {
    @Test
    fun `should return 403 when client has SYSTEM_USER role`() {
      webTestClient.delete()
        .uri("/api/offender-sentences/booking/-2/home-detention-curfews/latest/checks-passed")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return success when client has MAINTAIN_HDC role`() {
      webTestClient.delete()
        .uri("/api/offender-sentences/booking/-2/home-detention-curfews/latest/checks-passed")
        .headers(setClientAuthorisation(listOf("ROLE_MAINTAIN_HDC")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNoContent
    }
  }

  @Nested
  @DisplayName("DELETE /api/offender-sentences/booking/{bookingId}/home-detention-curfews/latest/approval-status")
  inner class ClearHDCApprovalStatus {
    @Test
    fun `should return 403 when client has SYSTEM_USER role`() {
      webTestClient.delete()
        .uri("/api/offender-sentences/booking/-2/home-detention-curfews/latest/approval-status")
        .headers(setClientAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return success when client has MAINTAIN_HDC role`() {
      webTestClient.delete()
        .uri("/api/offender-sentences/booking/-2/home-detention-curfews/latest/approval-status")
        .headers(setClientAuthorisation(listOf("ROLE_MAINTAIN_HDC")))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isNoContent
    }
  }
}
