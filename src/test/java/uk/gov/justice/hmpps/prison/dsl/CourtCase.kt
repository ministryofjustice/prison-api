package uk.gov.justice.hmpps.prison.dsl

import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.prison.api.model.CourtHearing
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.LegalCaseType
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCourtCase
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderCourtCaseRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import java.time.LocalDate
import java.time.LocalDateTime

@DslMarker
annotation class CourtCaseDslMarker

@NomisDataDslMarker
interface CourtCaseDsl {
  @CourtHearingDslMarker
  fun hearing(
    fromPrisonLocation: String = "LEI",
    toCourtLocation: String = "COURT1",
    courtHearingDateTime: LocalDateTime = LocalDateTime.now().plusHours(1),
    comments: String = "court appearance",
  ): CourtHearing
}

@Component
class CourtCaseBuilderRepository(
  val offenderBookingRepository: OffenderBookingRepository,
  val agencyLocationRepository: AgencyLocationRepository,
  val legalCourtCaseTypeRepository: ReferenceCodeRepository<LegalCaseType>,
  val courtCaseStatusRepository: ReferenceCodeRepository<CaseStatus>,
  val offenderCourtCaseRepository: OffenderCourtCaseRepository,
) {
  fun save(
    bookingId: Long,
    courtId: String,
  ): OffenderCourtCase {
    val offenderBooking = offenderBookingRepository.findByBookingId(bookingId).orElseThrow()
    val court = agencyLocationRepository.findById(courtId).orElseThrow()
    val caseType = legalCourtCaseTypeRepository.findById(LegalCaseType.pk("A")).orElseThrow() // adult
    val caseStatus = courtCaseStatusRepository.findById(CaseStatus.pk("A")).orElseThrow() // active
    val nextCaseSequence = offenderCourtCaseRepository.findAllByOffenderBooking_BookingId(bookingId).size + 1
    val beginDate = LocalDate.now()
    return offenderCourtCaseRepository.save(
      OffenderCourtCase
        .builder()
        .caseStatus(caseStatus)
        .offenderBooking(offenderBooking)
        .caseSeq(nextCaseSequence)
        .beginDate(beginDate)
        .agencyLocation(court)
        .legalCaseType(caseType)
        .courtEvents(emptyList())
        .sentences(emptyList())
        .build(),
    )
  }
}

@Component
class CourtCaseBuilder(
  private val repository: CourtCaseBuilderRepository,
  private val courtHearingBuilder: CourtHearingBuilder,
) : CourtCaseDsl {
  private lateinit var courtCase: OffenderCourtCase
  fun build(
    offenderBookingId: OffenderBookingId,
    courtId: String,
  ) = repository.save(
    bookingId = offenderBookingId.bookingId,
    courtId = courtId,
  ).also {
    courtCase = it
  }

  override fun hearing(
    fromPrisonLocation: String,
    toCourtLocation: String,
    courtHearingDateTime: LocalDateTime,
    comments: String,
  ): CourtHearing =
    courtHearingBuilder.build(
      bookingId = courtCase.offenderBooking.bookingId,
      courtCase = courtCase,
      fromPrisonLocation = fromPrisonLocation,
      toCourtLocation = toCourtLocation,
      courtHearingDateTime = courtHearingDateTime,
      comments = comments,
    )
}
