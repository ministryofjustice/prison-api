@file:Suppress("ktlint:filename")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.security.test.context.support.WithMockUser
import uk.gov.justice.hmpps.prison.util.builders.dsl.NomisDataBuilder

@WithMockUser
class OffenderResourceTimelineIntTest : ResourceTest() {

  @Nested
  @DisplayName("GET /api/offenders/{offenderNo}/prison-timeline")
  inner class GetPrisonTimeline {
    lateinit var offenderNo: String
    lateinit var offenderNoWithNoBooking: String

    @BeforeEach
    fun setUp() {
      if (!::offenderNo.isInitialized) {
        NomisDataBuilder(testDataContext).build {
          offenderNo = offender(lastName = "DUBOIS") {
            booking {}
          }.nomsId
          offenderNoWithNoBooking = offender(lastName = "MATES") {
          }.nomsId
        }
      }
    }

    @Nested
    inner class Security {
      @Test
      fun `access forbidden when no role`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", offenderNo)
          .headers(setAuthorisation(listOf()))
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", offenderNo)
          .headers(setAuthorisation(listOf("ROLE_BANANAS")))
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access unauthorised with no auth token`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", offenderNo)
          .exchange()
          .expectStatus().isUnauthorized
      }
    }

    @Nested
    inner class Validation {
      @Test
      fun `404 when offender not found`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", "Z1234ZZ")
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isNotFound
      }

      @Test
      fun `404 when offender has no booking`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", offenderNoWithNoBooking)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isNotFound
      }

      @Test
      fun `200 when offender has booking`() {
        webTestClient.get().uri("/api/offenders/{nomsId}/prison-timeline", offenderNo)
          .headers(setAuthorisation(listOf("ROLE_VIEW_PRISONER_DATA")))
          .exchange()
          .expectStatus().isOk
      }
    }
  }
}
