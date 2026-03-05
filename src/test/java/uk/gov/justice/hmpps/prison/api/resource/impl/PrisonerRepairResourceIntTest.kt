package uk.gov.justice.hmpps.prison.api.resource.impl

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.transaction.TestTransaction
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.dsl.NomisDataBuilder
import uk.gov.justice.hmpps.prison.dsl.OffenderBookingId
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementDirection
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType
import java.time.LocalDateTime

class PrisonerRepairResourceIntTest(
  @Autowired private val builder: NomisDataBuilder,
) : ResourceTest() {

  @Nested
  @DisplayName("GET /api/prisoner-repair/{bookingId}/restricted-patient-movements")
  open inner class RepairRestrictedPatientMovements {
    @Test
    fun `should return 401 without an auth token`() {
      webTestClient.post().uri("/api/prisoner-repair/123456/restricted-patient-movements")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 403 with no roles`() {
      webTestClient.post().uri("/api/prisoner-repair/123456/restricted-patient-movements")
        .headers(setAuthorisation(listOf()))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return 403 with wrong role`() {
      webTestClient.post().uri("/api/prisoner-repair/123456/restricted-patient-movements")
        .headers(setAuthorisation(listOf("ROLE_SYSTEM_USER")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `should return bad request if prisoner not found`() {
      webTestClient.post().uri("/api/prisoner-repair/991199/restricted-patient-movements")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__PRISONER_REPAIR__RW")))
        .exchange()
        .expectStatus().isBadRequest
        .expectBody().jsonPath("userMessage").isEqualTo("Found 0 active movements, expecting 2")
    }

    @Test
    @Transactional
    open fun `should update the restricted patient movement`() {
      lateinit var booking: OffenderBookingId

      builder.build {
        offender {
          booking = booking(bookingInTime = LocalDateTime.parse("2025-05-18T08:00:00")) {
            release(releaseTime = LocalDateTime.parse("2025-05-19T10:00:00"))
            recall(recallTime = LocalDateTime.parse("2025-05-20T22:00:00"))
          }
        }
      }
      val offenderBooking = dataLoader.offenderBookingRepository.findByBookingId(booking.bookingId).orElseThrow()
      val movements = offenderBooking.externalMovements
      movements[1].apply {
        movementReason = MovementReason("HP", "Hospital Release")
        isActive = true
      }
      movements[2].apply {
        movementReason = MovementReason("CR", "Conditional Release")
        movementType = MovementType.of(MovementType.REL)
        movementDirection = MovementDirection.OUT
        isActive = true
      }
      assertThat(movements.map { it.isActive }).containsExactly(false, true, true)
      dataLoader.offenderBookingRepository.save(offenderBooking)
      TestTransaction.flagForCommit()
      TestTransaction.end()
      TestTransaction.start()

      webTestClient.post().uri("/api/prisoner-repair/${booking.bookingId}/restricted-patient-movements")
        .headers(setAuthorisation(listOf("ROLE_PRISON_API__PRISONER_REPAIR__RW")))
        .exchange()
        .expectStatus().isOk

      // now check the data is okay
      webTestClient.get()
        .uri("/api/movements/booking/{bookingId}", booking.bookingId)
        .headers(setClientAuthorisation(listOf("VIEW_PRISONER_DATA")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo("3")
        .jsonPath("[0].sequence").isEqualTo(1)
        .jsonPath("[0].movementType").isEqualTo("ADM")
        .jsonPath("[0].directionCode").isEqualTo("IN")
        .jsonPath("[1].sequence").isEqualTo(2)
        .jsonPath("[1].movementType").isEqualTo("REL")
        .jsonPath("[1].movementReasonCode").isEqualTo("HP")
        .jsonPath("[1].directionCode").isEqualTo("OUT")
        .jsonPath("[2].sequence").isEqualTo(3)
        .jsonPath("[2].movementType").isEqualTo("REL")
        .jsonPath("[2].movementReasonCode").isEqualTo("CR")
        .jsonPath("[2].directionCode").isEqualTo("OUT")

      // and check the active flag
      val savedOffenderBooking = dataLoader.offenderBookingRepository.findByBookingId(booking.bookingId).orElseThrow()
      assertThat(savedOffenderBooking.externalMovements.map { it.isActive }).containsExactly(false, false, true)
    }
  }
}
