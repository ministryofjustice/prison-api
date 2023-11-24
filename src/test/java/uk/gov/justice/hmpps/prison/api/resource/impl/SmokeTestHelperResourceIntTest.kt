package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.SMOKE_TEST
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.SYSTEM_USER_READ_WRITE
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderImprisonmentStatusRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.util.builders.OffenderBuilder
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class SmokeTestHelperResourceIntTest : ResourceTest() {
  @Autowired
  private lateinit var offenderImprisonmentStatusRepository: OffenderImprisonmentStatusRepository

  @Autowired
  private lateinit var offenderRepository: OffenderRepository

  @Nested
  inner class ImprisonmentStatus {
    @Test
    @DisplayName("requires ROLE_SMOKE_TEST")
    fun requiresCorrectRole() {
      webTestClient.post()
        .uri("/api/smoketest/offenders/A1234AA/imprisonment-status")
        .headers { it.setBearerAuth(authTokenHelper.getToken(SYSTEM_USER_READ_WRITE)) }
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    @DisplayName("not found")
    fun notFound() {
      webTestClient.post()
        .uri("/api/smoketest/offenders/NOT_AN_OFFENDER/imprisonment-status")
        .headers { it.setBearerAuth(authTokenHelper.getToken(SMOKE_TEST)) }
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    @DisplayName("will add new imprisonment status to active booking")
    fun willAddImprisonmentStatus() {
      // GIVEN the offender booking as a single imprisonment status
      val bookingId = -1L
      assertThat(offenderImprisonmentStatusRepository.findByOffenderBookingId(bookingId)).hasSize(1)

      // WHEN I setup the smoke test data
      webTestClient.post()
        .uri("/api/smoketest/offenders/A1234AA/imprisonment-status")
        .headers { it.setBearerAuth(authTokenHelper.getToken(SMOKE_TEST)) }
        .exchange()
        .expectStatus().isOk

      // THEN I have two imprisonment statuses
      val statuses = offenderImprisonmentStatusRepository.findByOffenderBookingId(bookingId)
      assertThat(statuses).hasSize(2)
      assertThat(statuses[0].isActiveLatestStatus).isFalse()
      assertThat(statuses[0].expiryDate).isCloseTo(LocalDateTime.now(), within(60, ChronoUnit.SECONDS))
      assertThat(statuses[1].expiryDate).isNull()
      assertThat(statuses[1].isActiveLatestStatus).isTrue()
    }
  }

  @Nested
  inner class UpdateDetails {
    private fun createPrisoner() =
      OffenderBuilder(bookingBuilders = arrayOf(), firstName = "Bob", lastName = "Bailey")
        .save(testDataContext).offenderNo

    @Test
    fun `requires ROLE_SMOKE_TEST`() {
      webTestClient.post()
        .uri("/api/smoketest/offenders/A1234AA/details")
        .headers {
          it.setBearerAuth(authTokenHelper.getToken(SYSTEM_USER_READ_WRITE))
          it.contentType = MediaType.APPLICATION_JSON
        }
        .bodyValue(
          """ {
          "firstName": "John",
          "lastName": "Smith"
        } 
          """.trimIndent(),
        )
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `not found`() {
      webTestClient.post()
        .uri("/api/smoketest/offenders/NOT_AN_OFFENDER/details")
        .headers {
          it.setBearerAuth(authTokenHelper.getToken(SMOKE_TEST))
          it.contentType = MediaType.APPLICATION_JSON
        }
        .bodyValue(
          """ {
          "firstName": "John",
          "lastName": "Smith"
        } 
          """.trimIndent(),
        )
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `will change prisoner name`() {
      val prisonerNo = createPrisoner()

      webTestClient.post()
        .uri("/api/smoketest/offenders/$prisonerNo/details")
        .headers {
          it.setBearerAuth(authTokenHelper.getToken(SMOKE_TEST))
          it.contentType = MediaType.APPLICATION_JSON
        }
        .bodyValue(
          """ {
          "firstName": "John",
          "lastName": "Smith"
        } 
          """.trimIndent(),
        )
        .exchange()
        .expectStatus().isOk

      val prisoner = offenderRepository.findOffenderByNomsId(prisonerNo).orElseThrow()
      assertThat(prisoner.firstName).isEqualTo("JOHN")
      assertThat(prisoner.lastName).isEqualTo("SMITH")
    }

    @Test
    fun `will validate required fields`() {
      webTestClient.post()
        .uri("/api/smoketest/offenders/ANY/details")
        .headers {
          it.setBearerAuth(authTokenHelper.getToken(SMOKE_TEST))
          it.contentType = MediaType.APPLICATION_JSON
        }
        .bodyValue(
          """ {
        } 
          """.trimIndent(),
        )
        .exchange()
        .expectStatus().isBadRequest
    }
  }

  @Nested
  inner class ReleasePrisoner {
    @Test
    @DisplayName("requires ROLE_SMOKE_TEST")
    fun requiresCorrectRole() {
      webTestClient.put()
        .uri("/api/smoketest/offenders/A1060AA/release")
        .headers { it.setBearerAuth(authTokenHelper.getToken(SYSTEM_USER_READ_WRITE)) }
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    @DisplayName("not found")
    fun notFound() {
      webTestClient.put()
        .uri("/api/smoketest/offenders/NOT_AN_OFFENDER/release")
        .headers { it.setBearerAuth(authTokenHelper.getToken(SMOKE_TEST)) }
        .exchange()
        .expectStatus().isNotFound
    }
  }

  @Nested
  inner class RecallPrisoner {
    @Test
    @DisplayName("requires ROLE_SMOKE_TEST")
    fun requiresCorrectRole() {
      webTestClient.put()
        .uri("/api/smoketest/offenders/A1060AA/recall")
        .headers { it.setBearerAuth(authTokenHelper.getToken(SYSTEM_USER_READ_WRITE)) }
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    @DisplayName("not found")
    fun notFound() {
      webTestClient.put()
        .uri("/api/smoketest/offenders/NOT_AN_OFFENDER/recall")
        .headers { it.setBearerAuth(authTokenHelper.getToken(SMOKE_TEST)) }
        .exchange()
        .expectStatus().isNotFound
    }
  }

  @Nested
  inner class ReleaseAndRecallPrisoner {
    @Test
    @DisplayName("release and recall the prisoner")
    fun willReleaseAndRecallPrisoner() {
      webTestClient.put()
        .uri("/api/smoketest/offenders/A1060AA/release")
        .headers { it.setBearerAuth(authTokenHelper.getToken(SMOKE_TEST)) }
        .exchange()
        .expectStatus().isOk
      webTestClient.put()
        .uri("/api/smoketest/offenders/A1060AA/recall")
        .headers { it.setBearerAuth(authTokenHelper.getToken(SMOKE_TEST)) }
        .exchange()
        .expectStatus().isOk
    }
  }
}
