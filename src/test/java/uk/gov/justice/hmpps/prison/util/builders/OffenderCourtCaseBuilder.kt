package uk.gov.justice.hmpps.prison.util.builders

import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.LegalCaseType
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCourtCase
import uk.gov.justice.hmpps.prison.service.DataLoaderRepository
import java.time.LocalDate

class OffenderCourtCaseBuilder(var courtId: String = "COURT1") {
  fun save(offenderBookingId: Long, dataLoader: DataLoaderRepository): OffenderCourtCase {
    val offenderBooking = dataLoader.offenderBookingRepository.findByBookingId(offenderBookingId).orElseThrow()
    val court = dataLoader.agencyLocationRepository.findById(courtId).orElseThrow()
    val caseType = dataLoader.legalCourtCaseTypeRepository.findById(LegalCaseType.pk("A")).orElseThrow() // adult
    val caseStatus = dataLoader.courtCaseStatusRepository.findById(CaseStatus.pk("A")).orElseThrow() // active
    val nextCaseSequence = dataLoader.offenderCourtCaseRepository.findAllByOffenderBooking_BookingId(offenderBookingId).size + 1
    val beginDate = LocalDate.now()
    return dataLoader.offenderCourtCaseRepository.save(
      OffenderCourtCase(
        /* id = */ null,
        /* offenderBooking = */ offenderBooking,
        /* caseSeq = */ nextCaseSequence,
        /* beginDate = */ beginDate,
        /* agencyLocation = */ court,
        /* legalCaseType = */ caseType,
        /* caseInfoPrefix = */ null,
        /* caseInfoNumber = */ null,
        /* caseStatus = */ caseStatus,
        /* combinedCase = */ null,
        /* courtEvents = */ listOf(),
        /* sentences = */ listOf()
      )
    )
  }
}
