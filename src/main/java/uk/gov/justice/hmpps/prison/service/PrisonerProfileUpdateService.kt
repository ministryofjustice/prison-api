package uk.gov.justice.hmpps.prison.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository

@Service
class PrisonerProfileUpdateService(
  private val offenderRepository: OffenderRepository,
  private val offenderBookingRepository: OffenderBookingRepository,
) {

  @Transactional
  fun updateBirthPlaceOfCurrentAlias(prisonerNumber: String, birthPlace: String?) {
    offenderBookingRepository.findLatestOffenderBookingByNomsId(prisonerNumber)
      .orElseThrow(
        EntityNotFoundException.withMessage(
          "Prisoner with prisonerNumber %s and existing booking not found",
          prisonerNumber,
        ),
      )

    offenderRepository.updateBirthPlaceOfCurrentAlias(prisonerNumber, birthPlace?.ifBlank { null })
  }
}
