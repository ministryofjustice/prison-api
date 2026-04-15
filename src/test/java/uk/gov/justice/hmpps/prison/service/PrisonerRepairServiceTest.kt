@file:Suppress("ClassName")

package uk.gov.justice.hmpps.prison.service

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementTypeAndReason
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ExternalMovementRepository

internal class PrisonerRepairServiceTest {
  private val externalMovementRepository: ExternalMovementRepository = mock()
  private val telemetryClient: TelemetryClient = mock()

  private val service = PrisonerRepairService(
    externalMovementRepository,
    telemetryClient,
  )

  @Nested
  inner class updateMovementsForRestrictedPatients {
    private val booking = OffenderBooking().apply {
      bookingId = 12345
      offender = Offender().apply {
        nomsId = "A1234BC"
      }
    }
    private val hospitalRelease = ExternalMovement().apply {
      isActive = true
      movementSequence = 1
      movementReason = MovementTypeAndReason(MovementType.of(MovementType.REL), MovementReason.DISCHARGE_TO_PSY_HOSPITAL.code, "desc")
      offenderBooking = booking
    }
    private val conditionalRelease = ExternalMovement().apply {
      isActive = true
      movementSequence = 2
      movementReason = MovementTypeAndReason(MovementType.of(MovementType.REL), MovementReason.CONDITIONAL_RELEASE.code, "desc")
      offenderBooking = booking
    }

    @Nested
    inner class Validation {
      @Test
      fun `test update movements not enough active movements`() {
        whenever(externalMovementRepository.findAllByOffenderBooking_BookingId(any())).thenReturn(
          listOf(conditionalRelease, hospitalRelease.apply { isActive = false }),
        )

        assertThatThrownBy { service.updateMovementsForRestrictedPatients(12345) }
          .isInstanceOf(IllegalStateException::class.java)
          .hasMessage("Found 1 active movements, expecting 2")
      }

      @Test
      fun `test update movements last movement not a release`() {
        whenever(externalMovementRepository.findAllByOffenderBooking_BookingId(any())).thenReturn(
          listOf(
            ExternalMovement().apply {
              isActive = true
              movementSequence = 2
              movementReason = MovementTypeAndReason(MovementType("BOB", "Bob"), MovementReason.CONDITIONAL_RELEASE.code, "desc")
            },
            hospitalRelease,
          ),
        )

        assertThatThrownBy { service.updateMovementsForRestrictedPatients(12345) }
          .isInstanceOf(IllegalStateException::class.java)
          .hasMessage("Movement 2 is not a REL, found BOB instead")
      }

      @Test
      fun `test update movements last movement not a conditional release`() {
        whenever(externalMovementRepository.findAllByOffenderBooking_BookingId(any())).thenReturn(
          listOf(
            ExternalMovement().apply {
              isActive = true
              movementSequence = 2
              movementReason = MovementTypeAndReason(MovementType.of(MovementType.REL), MovementReason.SENTENCING.code, "desc")
            },
            hospitalRelease,
          ),
        )

        assertThatThrownBy { service.updateMovementsForRestrictedPatients(12345) }
          .isInstanceOf(IllegalStateException::class.java)
          .hasMessage("Movement 2 is not a CR, found SENT instead")
      }

      @Test
      fun `test update movements previous movement not a release`() {
        whenever(externalMovementRepository.findAllByOffenderBooking_BookingId(any())).thenReturn(
          listOf(
            conditionalRelease,
            ExternalMovement().apply {
              isActive = true
              movementSequence = 1
              movementReason = MovementTypeAndReason(MovementType("BOB", "Bob"), MovementReason.CONDITIONAL_RELEASE.code, "desc")
            },
          ),
        )

        assertThatThrownBy { service.updateMovementsForRestrictedPatients(12345) }
          .isInstanceOf(IllegalStateException::class.java)
          .hasMessage("Movement 1 is not a REL, found BOB instead")
      }

      @Test
      fun `test update movements previous movement not a release to hospital`() {
        whenever(externalMovementRepository.findAllByOffenderBooking_BookingId(any())).thenReturn(
          listOf(
            conditionalRelease,
            ExternalMovement().apply {
              isActive = true
              movementSequence = 1
              movementReason = MovementTypeAndReason(MovementType.of(MovementType.REL), MovementReason.SENTENCING.code, "desc")
            },
          ),
        )

        assertThatThrownBy { service.updateMovementsForRestrictedPatients(12345) }
          .isInstanceOf(IllegalStateException::class.java)
          .hasMessage("Movement 1 is not a HP, found SENT instead")
      }

      @Test
      fun `test hospital movement not penultimate movement`() {
        whenever(externalMovementRepository.findAllByOffenderBooking_BookingId(any())).thenReturn(
          listOf(
            conditionalRelease.apply {
              movementSequence = 5323
            },
            hospitalRelease.apply {
              movementSequence = 1234
            },
          ),
        )

        assertThatThrownBy { service.updateMovementsForRestrictedPatients(12345) }
          .isInstanceOf(IllegalStateException::class.java)
          .hasMessage("Hospital release is not the penultimate movement, has 1234 instead")
      }
    }

    @Nested
    inner class HappyPath {
      @Test
      fun `test update movements success sets release to inactive`() {
        whenever(externalMovementRepository.findAllByOffenderBooking_BookingId(any())).thenReturn(
          listOf(conditionalRelease, hospitalRelease),
        )
        service.updateMovementsForRestrictedPatients(12345)

        assertThat(hospitalRelease.isActive).isFalse
      }

      @Test
      fun `test update movements success creates telemetry event`() {
        whenever(externalMovementRepository.findAllByOffenderBooking_BookingId(any())).thenReturn(
          listOf(conditionalRelease, hospitalRelease),
        )
        service.updateMovementsForRestrictedPatients(12345)

        verify(telemetryClient).trackEvent(
          "prison-api-update-restricted-patient-movement",
          mapOf("prisonNumber" to "A1234BC", "bookingId" to "12345", "movementSequence" to "1"),
          null,
        )
      }
    }
  }
}
