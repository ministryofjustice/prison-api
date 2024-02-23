@file:Suppress("ktlint:standard:filename", "ClassName")

package uk.gov.justice.hmpps.prison.api.resource.impl

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import uk.gov.justice.hmpps.prison.dsl.NomisDataBuilder
import uk.gov.justice.hmpps.prison.dsl.OffenderId
import java.time.LocalDateTime

private const val REMAND_REASON = "N"

@WithMockUser
class OffenderResourceIntTest_addressesIntTest : ResourceTest() {
  @Autowired
  private lateinit var builder: NomisDataBuilder

  @Nested
  @DisplayName("GET /api/offenders/{offenderNo}/addresses")
  inner class GetAddressesByPrisonerNumber {
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class Security {
      private lateinit var activePrisoner: OffenderId
      private lateinit var inactivePrisoner: OffenderId

      @BeforeAll
      fun setUp() {
        builder.build {
          activePrisoner = offender(lastName = "DUBOIS") {
            booking(
              prisonId = "MDI",
              bookingInTime = LocalDateTime.parse("2023-07-19T10:00:00"),
              movementReasonCode = REMAND_REASON,
            ) {}
          }
          inactivePrisoner = offender(lastName = "FRANZ") {
            booking(
              prisonId = "MDI",
              bookingInTime = LocalDateTime.parse("2023-07-19T10:00:00"),
              movementReasonCode = REMAND_REASON,
            ) {
              release()
            }
          }
        }
      }

      @AfterAll
      fun deletePrisoner() {
        builder.deletePrisoner(activePrisoner.offenderNo)
        builder.deletePrisoner(inactivePrisoner.offenderNo)
      }

      @Nested
      @DisplayName("when using client credentials")
      inner class ClientCredentials {

        @Test
        fun `access forbidden when no role`() {
          webTestClient.get().uri("/api/offenders/{nomsId}/addresses", activePrisoner.offenderNo)
            .headers(setClientAuthorisation(listOf()))
            .exchange()
            .expectStatus().isForbidden
        }

        @Test
        fun `access forbidden with wrong role`() {
          webTestClient.get().uri("/api/offenders/{nomsId}/addresses", activePrisoner.offenderNo)
            .headers(setClientAuthorisation(listOf("ROLE_BANANAS")))
            .exchange()
            .expectStatus().isForbidden
        }

        @Test
        fun `access unauthorised with no auth token`() {
          webTestClient.get().uri("/api/offenders/{nomsId}/addresses", activePrisoner.offenderNo)
            .exchange()
            .expectStatus().isUnauthorized
        }

        @Test
        fun `can view active prisoner with correct role`() {
          webTestClient.get().uri("/api/offenders/{nomsId}/addresses", activePrisoner.offenderNo)
            .headers(setClientAuthorisation(listOf("VIEW_PRISONER_DATA")))
            .exchange()
            .expectStatus().isOk
        }

        @Test
        fun `can not view inactive prisoner with even with the correct role`() {
          webTestClient.get().uri("/api/offenders/{nomsId}/addresses", inactivePrisoner.offenderNo)
            .headers(setClientAuthorisation(listOf("VIEW_PRISONER_DATA")))
            .exchange()
            .expectStatus().isNotFound
        }
      }

      @Nested
      @DisplayName("when using user credentials")
      inner class UserCredentials {

        @Test
        fun `can view active prisoner when in users caseload without any roles`() {
          webTestClient.get().uri("/api/offenders/{nomsId}/addresses", activePrisoner.offenderNo)
            .headers(setAuthorisation(listOf()))
            .exchange()
            .expectStatus().isOk
        }

        @Test
        fun `can not view inactive prisoner or address when user has not got INACTIVE_BOOKINGS role`() {
          webTestClient.get().uri("/api/offenders/{nomsId}", inactivePrisoner.offenderNo)
            .headers(setAuthorisation(listOf()))
            .exchange()
            .expectStatus().isNotFound
          webTestClient.get().uri("/api/offenders/{nomsId}/addresses", inactivePrisoner.offenderNo)
            .headers(setAuthorisation(listOf()))
            .exchange()
            .expectStatus().isNotFound
        }

        @Test
        fun `still can not view inactive prisoner's address even when the user has INACTIVE_BOOKINGS role`() {
          webTestClient.get().uri("/api/offenders/{nomsId}", inactivePrisoner.offenderNo)
            .headers(setAuthorisation(listOf("INACTIVE_BOOKINGS")))
            .exchange()
            .expectStatus().isOk
          webTestClient.get().uri("/api/offenders/{nomsId}/addresses", inactivePrisoner.offenderNo)
            .headers(setAuthorisation(listOf("INACTIVE_BOOKINGS")))
            .exchange()
            .expectStatus().isNotFound
        }
      }
    }
  }
}
