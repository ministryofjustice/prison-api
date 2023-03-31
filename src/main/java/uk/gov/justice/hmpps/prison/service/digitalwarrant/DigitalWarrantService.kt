package uk.gov.justice.hmpps.prison.service.digitalwarrant

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.hmpps.prison.api.model.digitalwarrant.CourtDateResult
import uk.gov.justice.hmpps.prison.api.model.digitalwarrant.WarrantCharge
import uk.gov.justice.hmpps.prison.api.model.digitalwarrant.WarrantCourtCase
import uk.gov.justice.hmpps.prison.api.model.digitalwarrant.WarrantSentence
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEventCharge
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtOrder
import uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.ImprisonmentStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.LegalCaseType
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offence
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenceResult
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCharge
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCourtCase
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImprisonmentStatus
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSentence
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSentenceCharge
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalcType
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceTerm
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventChargeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtOrderRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ImprisonmentStatusRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenceRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenceResultRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderChargeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderCourtCaseRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSentenceChargeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSentenceRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.SentenceCalcTypeRepository
import uk.gov.justice.hmpps.prison.repository.jpa.repository.SentenceTermRepository
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class DigitalWarrantService(
  private val offenderCourtCaseRepository: OffenderCourtCaseRepository,
  private val offenderChargeRepository: OffenderChargeRepository,
  private val offenderSentenceRepository: OffenderSentenceRepository,
  private val agencyLocationRepository: AgencyLocationRepository,
  private val offenceRepository: OffenceRepository,
  private val offenderBookingRepository: OffenderBookingRepository,
  private val sentenceCalcTypeRepository: SentenceCalcTypeRepository,
  private val offenderSentenceChargeRepository: OffenderSentenceChargeRepository,
  private val legalCaseTypeReferenceCodeRepository: ReferenceCodeRepository<LegalCaseType>,
  private val movementReasonReferenceCodeRepository: ReferenceCodeRepository<MovementReason>,
  private val caseStatusReferenceCodeRepository: ReferenceCodeRepository<CaseStatus>,
  private val eventStatusReferenceCodeRepository: ReferenceCodeRepository<EventStatus>,
  private val offenceResultRepository: OffenceResultRepository,
  private val sentenceTermRepository: SentenceTermRepository,
  private val courtOrderRepository: CourtOrderRepository,
  private val courtEventRepository: CourtEventRepository,
  private val courtEventChargeRepository: CourtEventChargeRepository,
  private val imprisonmentStatusRepository: ImprisonmentStatusRepository
) {

  @Transactional
  fun createCourtCase(bookingId: Long, courtCase: WarrantCourtCase): Long {
    val agency = agencyLocationRepository.findById(courtCase.agencyId).orElseThrow(EntityNotFoundException.withIdAndClass(courtCase.agencyId, WarrantCourtCase::class.java))
    val legalCaseType = legalCaseTypeReferenceCodeRepository.findById(LegalCaseType.pk(courtCase.caseType)).orElseThrow(EntityNotFoundException.withIdAndClass(courtCase.caseType, LegalCaseType::class.java))
    val booking = offenderBookingRepository.findByBookingId(bookingId).orElseThrow(EntityNotFoundException.withIdAndClass(bookingId, OffenderBooking::class.java))
    val caseStatus = caseStatusReferenceCodeRepository.findById(CaseStatus.pk("A")).orElseThrow(EntityNotFoundException.withIdAndClass("A", CaseStatus::class.java))
    val sequence = offenderCourtCaseRepository.findAllByOffenderBooking_BookingId(bookingId).stream().max(Comparator.comparing { obj: OffenderCourtCase -> obj.caseSeq }).map { occ: OffenderCourtCase -> occ.caseSeq + 1 }.orElse(1)
    val offenderCourtCase = OffenderCourtCase()
      .withCaseInfoNumber(courtCase.caseInfoNumber)
      .withLegalCaseType(legalCaseType)
      .withAgencyLocation(agency)
      .withBeginDate(courtCase.beginDate)
      .withOffenderBooking(booking)
      .withCaseSeq(sequence)
      .withCaseStatus(caseStatus)
    val movementReason = movementReasonReferenceCodeRepository.findById(MovementReason.pk(courtCase.hearingType)).orElseThrow(EntityNotFoundException.withIdAndClass(courtCase.caseType, MovementReason::class.java))
    val eventStatus = eventStatusReferenceCodeRepository.findById(EventStatus.COMPLETED).orElseThrow(EntityNotFoundException.withIdAndClass(EventStatus.COMPLETED.code, EventStatus::class.java))
    val courtEvent = CourtEvent()
      .withOffenderBooking(booking)
      .withCourtEventType(movementReason)
      .withEventStatus(eventStatus)
      .withStartTime(courtCase.beginDate.atTime(10, 0))
      .withEventDate(courtCase.beginDate)
      .withCourtLocation(agency)
      .withOffenderCourtCase(offenderCourtCase)
    courtEventRepository.save(courtEvent)
    return offenderCourtCaseRepository.save(offenderCourtCase).id
  }

  @Transactional
  fun createCharge(bookingId: Long?, charge: WarrantCharge): Long {
    val offence = offenceRepository.findById(Offence.PK(charge.offenceCode, charge.offenceStatue)).orElseThrow(EntityNotFoundException.withIdAndClass(charge.offenceCode + " " + charge.offenceStatue, WarrantCharge::class.java))
    val courtCase = offenderCourtCaseRepository.findById(charge.courtCaseId).orElseThrow(EntityNotFoundException.withIdAndClass(charge.courtCaseId, OffenderCourtCase::class.java))
    val booking = offenderBookingRepository.findByBookingId(bookingId).orElseThrow(EntityNotFoundException.withIdAndClass(bookingId!!, OffenderBooking::class.java))
    val offenceResultCode = if (charge.isGuilty) OffenceResultRepository.IMPRISONMENT else OffenceResultRepository.NOT_GUILTY
    val result = offenceResultRepository.findById(offenceResultCode).orElseThrow(EntityNotFoundException.withIdAndClass(offenceResultCode, OffenceResult::class.java))
    val otherCharges = offenderChargeRepository.findByOffenderBooking_BookingId(bookingId)
    val mostSeriousCharge = otherCharges.stream().filter { charge: OffenderCharge -> charge.mostSeriousFlag == "Y" }.findFirst()
    val mostSerious = mostSeriousCharge.map { mostSerious: OffenderCharge -> offence.isMoreSeriousThan(mostSerious.offence) }.orElse(true)
    var offenderCharge = OffenderCharge()
      .withOffence(offence)
      .withDateOfOffence(charge.offenceDate)
      .withEndDate(charge.offenceEndDate)
      .withOffenderCourtCase(courtCase)
      .withOffenderBooking(booking)
      .withResultCodeOne(result)
      .withResultCodeOneIndicator("F")
      .withMostSeriousFlag(if (mostSerious) "Y" else "N")
      .withPleaCode("G")
    offenderCharge = offenderChargeRepository.save(offenderCharge)
    if (mostSerious && mostSeriousCharge.isPresent) {
      mostSeriousCharge.get().mostSeriousFlag = "N"
      offenderChargeRepository.save(mostSeriousCharge.get())
    }
    return offenderCharge.id
  }

  @Transactional
  fun createOffenderSentence(bookingId: Long, sentence: WarrantSentence): Int {
    val courtCase = offenderCourtCaseRepository.findById(sentence.courtCaseId).orElseThrow(EntityNotFoundException.withIdAndClass(sentence.courtCaseId, WarrantCourtCase::class.java))
    val booking = offenderBookingRepository.findByBookingId(bookingId).orElseThrow(EntityNotFoundException.withIdAndClass(bookingId, OffenderBooking::class.java))
    val offenderCharge = offenderChargeRepository.findById(sentence.offenderChargeId).orElseThrow(EntityNotFoundException.withIdAndClass(sentence.offenderChargeId, OffenderCharge::class.java))
    val sentenceCalcType = sentenceCalcTypeRepository.findById(SentenceCalcType.PK(sentence.sentenceType, sentence.sentenceCategory)).orElseThrow(EntityNotFoundException.withIdAndClass(sentence.sentenceType + " " + sentence.sentenceCategory, SentenceCalcType::class.java))
    val movementReason = movementReasonReferenceCodeRepository.findById(MovementReason.SENTENCING).orElseThrow(EntityNotFoundException.withIdAndClass(MovementReason.SENTENCING.code, MovementReason::class.java))
    val eventStatus = eventStatusReferenceCodeRepository.findById(EventStatus.COMPLETED).orElseThrow(EntityNotFoundException.withIdAndClass(EventStatus.COMPLETED.code, EventStatus::class.java))
    val result = offenceResultRepository.findById(OffenceResultRepository.IMPRISONMENT).orElseThrow(EntityNotFoundException.withIdAndClass(OffenceResultRepository.IMPRISONMENT, OffenceResult::class.java))
    val courtEvent = CourtEvent()
      .withOffenderBooking(booking)
      .withCourtEventType(movementReason)
      .withEventStatus(eventStatus)
      .withStartTime(sentence.sentenceDate.atTime(10, 0))
      .withEventDate(sentence.sentenceDate)
      .withCourtLocation(courtCase.agencyLocation)
      .withOffenderCourtCase(courtCase)
      .withOutcomeReasonCode(result)
    courtEventRepository.save(courtEvent)
    val courtEventCharge = CourtEventCharge(offenderCharge, courtEvent)
    courtEventChargeRepository.save(courtEventCharge)
    var courtOrder = CourtOrder()
      .withCourtCase(courtCase)
      .withIssuingCourt(courtCase.agencyLocation)
      .withCourtDate(sentence.sentenceDate)
      .withOffenderBooking(booking)
      .withOrderType("AUTO")
      .withOrderStatus("A")
      .withCourtEvent(courtEvent)
    courtOrder = courtOrderRepository.save(courtOrder)
    val sequence = offenderSentenceRepository.findByOffenderBooking_BookingId(bookingId).stream().max(Comparator.comparing { obj: OffenderSentence -> obj.sequence }).map { os: OffenderSentence -> os.sequence + 1 }.orElse(1)
    var offenderSentence = OffenderSentence()
      .withId(OffenderSentence.PK(booking.bookingId, sequence))
      .withLineSequence(java.lang.Long.valueOf(sequence.toLong()))
      .withCalculationType(sentenceCalcType)
      .withSentenceStartDate(sentence.sentenceDate)
      .withCourtCase(courtCase)
      .withCourtOrder(courtOrder)
      .withStatus("A")
    offenderSentence = offenderSentenceRepository.save(offenderSentence)
    val term = SentenceTerm()
      .withDays(sentence.days)
      .withWeeks(sentence.weeks)
      .withMonths(sentence.months)
      .withYears(sentence.years)
      .withSentenceTermCode("IMP")
      .withStartDate(sentence.sentenceDate)
      .withId(SentenceTerm.PK(booking.bookingId, offenderSentence.sequence, 1))
    sentenceTermRepository.save(term)
    val offenderSentenceCharge = OffenderSentenceCharge()
      .withId(OffenderSentenceCharge.PK(booking.bookingId, offenderSentence.sequence, offenderCharge.id))
    offenderSentenceChargeRepository.save(offenderSentenceCharge)
    val status = imprisonmentStatusRepository.findByStatusAndActive(sentence.sentenceType, true)
      .orElseGet {
        imprisonmentStatusRepository.findByStatusAndActive(ImprisonmentStatusRepository.ADULT_IMPRISONMENT_WITHOUT_OPTION, true)
          .orElseThrow(EntityNotFoundException.withIdAndClass(sentence.sentenceType, ImprisonmentStatus::class.java))
      }
    booking.setImprisonmentStatus(
      OffenderImprisonmentStatus()
        .withImprisonmentStatus(status)
        .withAgyLocId(booking.location.id),
      LocalDateTime.now(),
    )
    return offenderSentence.sequence
  }

  fun getCourtDateResults(offenderId: String): List<CourtDateResult> {
    return courtEventChargeRepository.findByOffender(offenderId).map {
      val event = it.eventAndCharge.courtEvent
      val charge = it.eventAndCharge.offenderCharge
      CourtDateResult(
        event.id,
        event.eventDate,
        event.outcomeReasonCode?.code,
        event.outcomeReasonCode?.description,
        event.outcomeReasonCode?.dispositionCode,
        WarrantCharge(
          charge.id,
          charge.offence.code,
          charge.offence.statute.code,
          charge.offence.description,
          charge.dateOfOffence,
          charge.endDate,
          charge.pleaCode == "G",
          charge.offenderCourtCase.id,
          charge.offenderCourtCase.caseInfoNumber,
          charge.offenderCourtCase.agencyLocation?.description,
          charge.offenderSentenceCharges.firstOrNull()?.offenderSentence?.sequence,
          charge.offenderSentenceCharges.firstOrNull()?.offenderSentence?.courtOrder?.courtDate,
          charge.resultCodeOne?.description,
        ),
        charge.offenderBooking.bookingId,
      )
    }
      .sortedBy { it.date }
  }
}
