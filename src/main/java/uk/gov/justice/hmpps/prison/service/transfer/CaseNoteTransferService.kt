package uk.gov.justice.hmpps.prison.service.transfer

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseNoteSubType
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseNoteType
import uk.gov.justice.hmpps.prison.repository.jpa.model.ExternalMovement
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCaseNote
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderCaseNoteRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException

@Service
class CaseNoteTransferService(
  private val caseNoteRepository: OffenderCaseNoteRepository,
  private val caseNoteTypeReferenceCodeRepository: ReferenceCodeRepository<CaseNoteType>,
  private val caseNoteSubTypeReferenceCodeRepository: ReferenceCodeRepository<CaseNoteSubType>,
  staffUserAccountRepository: StaffUserAccountRepository,
  authenticationFacade: AuthenticationFacade,
) : StaffAwareTransferService(
  staffUserAccountRepository = staffUserAccountRepository,
  authenticationFacade = authenticationFacade
) {
  fun createGenerateAdmissionNote(booking: OffenderBooking, transferMovement: ExternalMovement) {
    val staff = getLoggedInStaff().getOrThrow().staff
    val type = getType().getOrThrow()
    val subType = getSubType().getOrThrow()
    val note =
      "Offender admitted to ${transferMovement.toAgency.description} for reason: ${transferMovement.movementReason.description} from ${transferMovement.fromAgency.description}."

    val caseNote = OffenderCaseNote(
      /* id = */ null,
      /* offenderBooking = */ booking,
      /* occurrenceDate = */ transferMovement.movementTime.toLocalDate(),
      /* occurrenceDateTime = */ transferMovement.movementTime,
      /* type = */ type,
      /* subType = */ subType,
      /* caseNoteText = */ note,
      /* amendmentFlag = */ false,
      /* agencyLocation = */ booking.location,
      /* author = */ staff,
      /* noteSourceCode = */ "AUTO",
      /* dateCreation = */ null,
      /* timeCreation = */ null
    )

    caseNoteRepository.save(caseNote)
  }

  private fun getType(): Result<CaseNoteType> =
    caseNoteTypeReferenceCodeRepository.findByIdOrNull(CaseNoteType.pk("TRANSFER"))?.let { Result.success(it) }
      ?: Result.failure(EntityNotFoundException.withId("TRANSFER"))

  private fun getSubType(): Result<CaseNoteSubType> =
    caseNoteSubTypeReferenceCodeRepository.findByIdOrNull(CaseNoteSubType.pk("FROMTOL"))?.let { Result.success(it) }
      ?: Result.failure(EntityNotFoundException.withId("FROMTOL"))
}
