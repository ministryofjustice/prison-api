package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import uk.gov.justice.hmpps.prison.dsl.NomisDataBuilder
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.SMOKE_TEST
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken.SYSTEM_USER_READ_WRITE
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository
import uk.gov.justice.hmpps.prison.service.InmateService
import uk.gov.justice.hmpps.prison.service.SmokeTestHelperService.Companion.SMOKE_TEST_PRISON_ID
import uk.gov.justice.hmpps.prison.util.builders.OffenderBuilder

class SmokeTestHelperResourceIntTest : ResourceTest() {

  @Autowired
  private lateinit var builder: NomisDataBuilder

  @Autowired
  private lateinit var offenderRepository: OffenderRepository

  @Autowired
  private lateinit var inmateService: InmateService

  @Nested
  inner class OffenderStatus {
    @Test
    @DisplayName("requires ROLE_SMOKE_TEST")
    fun requiresCorrectRole() {
      webTestClient.put()
        .uri("/api/smoketest/offenders/A1234AA/status")
        .headers { it.setBearerAuth(authTokenHelper.getToken(SYSTEM_USER_READ_WRITE)) }
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    @DisplayName("not found")
    fun notFound() {
      webTestClient.put()
        .uri("/api/smoketest/offenders/NOT_AN_OFFENDER/status")
        .headers { it.setBearerAuth(authTokenHelper.getToken(SMOKE_TEST)) }
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    @DisplayName("will set offender status to IN")
    fun willSetOffenderStatus() {
      val offenderToRecall = "Z0023ZZ"
      val outOffender = inmateService.findOffender(offenderToRecall, false, false)
      assertThat(outOffender.inOutStatus).isEqualTo("OUT")
      assertThat(outOffender.agencyId).isEqualTo("OUT")

      webTestClient.put()
        .uri("/api/smoketest/offenders/$offenderToRecall/status")
        .headers { it.setBearerAuth(authTokenHelper.getToken(SMOKE_TEST)) }
        .exchange()
        .expectStatus().isOk

      val inOffender = inmateService.findOffender(offenderToRecall, false, false)
      assertThat(inOffender.inOutStatus).isEqualTo("IN")
      assertThat(inOffender.agencyId).isEqualTo(SMOKE_TEST_PRISON_ID)

      // tidy up - revert to original OUT status
      webTestClient.put()
        .uri("/api/smoketest/offenders/$offenderToRecall/release")
        .headers { it.setBearerAuth(authTokenHelper.getToken(SMOKE_TEST)) }
        .exchange()
        .expectStatus().isOk
      val outAgainOffender = inmateService.findOffender(offenderToRecall, false, false)
      assertThat(outAgainOffender.inOutStatus).isEqualTo("OUT")
      assertThat(outAgainOffender.agencyId).isEqualTo("OUT")
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
      val prisonerNo = builder.build {
        offender {
          booking { }
        }.offenderNo
      }.offenders.first().offenderNo

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

      val prisoner = offenderRepository.findRootOffenderByNomsId(prisonerNo).orElseThrow()
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
