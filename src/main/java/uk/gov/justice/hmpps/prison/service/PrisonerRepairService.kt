package uk.gov.justice.hmpps.prison.service

import com.microsoft.applicationinsights.TelemetryClient
import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ExternalMovementRepository
import uk.gov.justice.hmpps.prison.web.config.trackEvent

@Component
class PrisonerRepairService(
  private val externalMovementRepository: ExternalMovementRepository,
  private val telemetryClient: TelemetryClient,
) {
  @Transactional
  fun updateMovementsForRestrictedPatients(bookingId: Long) {
    val movements: MutableList<ExternalMovement> = externalMovementRepository.findAllByOffenderBooking_BookingId(bookingId)

    val sortedMovements = movements.sortedByDescending { it.movementSequence }.filter { it.isActive }
    if (sortedMovements.size != 2) throw IllegalStateException("Found ${sortedMovements.size} active movements, expecting 2")

    val releaseMovement = sortedMovements[0]
    if (releaseMovement.movementType.code != "REL") {
      throw IllegalStateException("Movement ${releaseMovement.movementSequence} is not a REL, found ${releaseMovement.movementType.code} instead")
    }
    if (releaseMovement.movementReason.code != "CR") {
      throw IllegalStateException("Movement ${releaseMovement.movementSequence} is not a CR, found ${releaseMovement.movementReason.code} instead")
    }

    val hospitalMovement = sortedMovements[1]
    if (hospitalMovement.movementType.code != "REL") {
      throw IllegalStateException("Movement ${hospitalMovement.movementSequence} is not a REL, found ${hospitalMovement.movementType.code} instead")
    }
    if (hospitalMovement.movementReason.code != "HP") {
      throw IllegalStateException("Movement ${hospitalMovement.movementSequence} is not a HP, found ${hospitalMovement.movementReason.code} instead")
    }
    if (hospitalMovement.movementSequence.toInt() != movements.size - 1) {
      throw IllegalStateException("Hospital release is not the penultimate movement, has ${hospitalMovement.movementSequence} instead")
    }

    // now update the second movement to be inactive
    hospitalMovement.isActive = false

    telemetryClient.trackEvent(
      "prison-api-update-restricted-patient-movement",
      mapOf(
        "prisonNumber" to releaseMovement.offenderBooking.offender.nomsId,
        "bookingId" to bookingId.toString(),
        "movementSequence" to hospitalMovement.movementSequence.toString(),
      ),
    )
  }
}
