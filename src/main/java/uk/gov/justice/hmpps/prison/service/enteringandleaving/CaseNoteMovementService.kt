package uk.gov.justice.hmpps.prison.service.enteringandleaving

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseNoteSubType
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseNoteType
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason.DISCHARGE_TO_PSY_HOSPITAL
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCaseNote
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderCaseNoteRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import java.time.LocalDateTime

@Service
class CaseNoteMovementService(
  private val caseNoteRepository: OffenderCaseNoteRepository,
  private val caseNoteTypeReferenceCodeRepository: ReferenceCodeRepository<CaseNoteType>,
  private val caseNoteSubTypeReferenceCodeRepository: ReferenceCodeRepository<CaseNoteSubType>,
  staffUserAccountRepository: StaffUserAccountRepository,
  authenticationFacade: AuthenticationFacade,
) : StaffAwareMovementService(
  staffUserAccountRepository = staffUserAccountRepository,
  authenticationFacade = authenticationFacade,
) {
  fun createGenerateAdmissionNote(booking: OffenderBooking, transferMovement: ExternalMovement) {
    createMovementCaseNote(
      booking = booking,
      typeCode = "TRANSFER",
      subTypeCode = "FROMTOL",
      note = "Offender admitted to ${transferMovement.toAgency.description} for reason: ${transferMovement.movementReason.description} from ${transferMovement.fromAgency.description}.",
      movementTime = transferMovement.movementTime,
    )
  }

  fun createReleaseNote(booking: OffenderBooking, movement: ExternalMovement) {
    createMovementCaseNote(
      booking = booking,
      typeCode = "PRISON",
      subTypeCode = "RELEASE",
      note = releaseNoteText(movement.movementReason, movement.fromAgency, movement.toAgency),
      movementTime = movement.movementTime,
    )
  }

  private fun releaseNoteText(movementReason: MovementReason, fromLocation: AgencyLocation, toLocation: AgencyLocation) =
    when {
      movementReason.code == DISCHARGE_TO_PSY_HOSPITAL.code && toLocation.id != AgencyLocation.OUT ->
        "Transferred from ${fromLocation.description} for reason: Moved to psychiatric hospital ${toLocation.description}."
      else ->
        "Released from ${fromLocation.description} for reason: ${movementReason.description}."
    }

  private fun createMovementCaseNote(booking: OffenderBooking, typeCode: String, subTypeCode: String, note: String, movementTime: LocalDateTime) {
    val staff = getLoggedInStaff().getOrThrow().staff
    val type = getType(typeCode).getOrThrow()
    val subType = getSubType(subTypeCode).getOrThrow()

    val caseNote = OffenderCaseNote.builder()
      .offenderBooking(booking)
      .occurrenceDate(movementTime.toLocalDate())
      .occurrenceDateTime(movementTime)
      .type(type)
      .subType(subType)
      .caseNoteText(note)
      .agencyLocation(booking.location)
      .author(staff)
      .noteSourceCode("AUTO")
      .build()

    caseNoteRepository.save(caseNote)
  }

  private fun getType(typeCode: String): Result<CaseNoteType> =
    caseNoteTypeReferenceCodeRepository.findByIdOrNull(CaseNoteType.pk(typeCode))?.let { Result.success(it) }
      ?: Result.failure(EntityNotFoundException.withId(typeCode))

  private fun getSubType(subTypeCode: String): Result<CaseNoteSubType> =
    caseNoteSubTypeReferenceCodeRepository.findByIdOrNull(CaseNoteSubType.pk(subTypeCode))?.let { Result.success(it) }
      ?: Result.failure(EntityNotFoundException.withId(subTypeCode))
}
