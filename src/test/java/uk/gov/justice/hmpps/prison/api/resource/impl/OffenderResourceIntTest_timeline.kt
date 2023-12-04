@file:Suppress("ktlint:filename")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import uk.gov.justice.hmpps.prison.util.builders.dsl.NomisDataBuilder

/**
 * KOTLIN
 */
@WithMockUser
class OffenderResourceTimelineIntTest : ResourceTest() {
  @Autowired
  private lateinit var nomisDataBuilder: NomisDataBuilder

  @Nested
  @DisplayName("GET /api/offenders/{offenderNo}/prison-timeline")
  inner class GetPrisonTimeline {
    lateinit var offenderNo: String

    @BeforeEach
    fun setUp() {
      nomisDataBuilder.build {
        offenderNo = offender(lastName = "DUBOIS") {
          booking {
          }
        }.nomsId
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
  }
}
