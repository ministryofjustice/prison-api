package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.SMOKE_TEST
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.SYSTEM_USER_READ_WRITE
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderImprisonmentStatusRepository
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class SmokeTestHelperResourceIntTest : ResourceTest() {
  @Autowired
  private lateinit var repository: OffenderImprisonmentStatusRepository

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
    assertThat(repository.findByOffenderBookingId(bookingId)).hasSize(1)

    // WHEN I setup the smoke test data
    webTestClient.post()
      .uri("/api/smoketest/offenders/A1234AA/imprisonment-status")
      .headers { it.setBearerAuth(authTokenHelper.getToken(SMOKE_TEST)) }
      .exchange()
      .expectStatus().isOk

    // THEN I have two imprisonment statuses
    val statuses = repository.findByOffenderBookingId(bookingId)
    assertThat(statuses).hasSize(2)
    assertThat(statuses[0].isActiveLatestStatus).isFalse()
    assertThat(statuses[0].expiryDate).isCloseTo(LocalDateTime.now(), within(60, ChronoUnit.SECONDS))
    assertThat(statuses[1].expiryDate).isNull()
    assertThat(statuses[1].isActiveLatestStatus).isTrue()
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
