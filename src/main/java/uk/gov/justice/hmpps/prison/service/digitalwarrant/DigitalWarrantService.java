package uk.gov.justice.hmpps.prison.service.digitalwarrant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.hmpps.prison.api.model.digitalwarrant.CourtCase;
import uk.gov.justice.hmpps.prison.api.model.digitalwarrant.Offence;
import uk.gov.justice.hmpps.prison.api.model.digitalwarrant.Sentence;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CaseStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEvent;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtEventCharge;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourtOrder;
import uk.gov.justice.hmpps.prison.repository.jpa.model.EventStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.LegalCaseType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offence.PK;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenceResult;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCharge;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCourtCase;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSentence;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderSentenceCharge;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalcType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceTerm;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventChargeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtEventRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.CourtOrderRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenceRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenceResultRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderChargeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderCourtCaseRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSentenceChargeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderSentenceRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.SentenceCalcTypeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.SentenceTermRepository;
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException;

import javax.transaction.Transactional;

import java.util.Comparator;

@Service
public class DigitalWarrantService {
    @Autowired
    private OffenderCourtCaseRepository offenderCourtCaseRepository;
    @Autowired
    private OffenderChargeRepository offenderChargeRepository;
    @Autowired
    private OffenderSentenceRepository offenderSentenceRepository;
    @Autowired
    private AgencyLocationRepository agencyLocationRepository;
    @Autowired
    private OffenceRepository offenceRepository;
    @Autowired
    private OffenderBookingRepository offenderBookingRepository;
    @Autowired
    private SentenceCalcTypeRepository sentenceCalcTypeRepository;
    @Autowired
    private OffenderSentenceChargeRepository offenderSentenceChargeRepository;
    @Autowired
    private ReferenceCodeRepository<LegalCaseType> legalCaseTypeReferenceCodeRepository;

    @Autowired
    private ReferenceCodeRepository<MovementReason> movementReasonReferenceCodeRepository;

    @Autowired
    private ReferenceCodeRepository<CaseStatus> caseStatusReferenceCodeRepository;
    @Autowired
    private ReferenceCodeRepository<EventStatus> eventStatusReferenceCodeRepository;
    @Autowired
    private OffenceResultRepository offenceResultRepository;

    @Autowired
    private SentenceTermRepository sentenceTermRepository;

    @Autowired
    private CourtOrderRepository courtOrderRepository;

    @Autowired
    private CourtEventRepository courtEventRepository;

    @Autowired
    private CourtEventChargeRepository courtEventChargeRepository;
    @Transactional
    public Long createCourtCase(Long bookingId, CourtCase courtCase) {
        var agency = agencyLocationRepository.findById(courtCase.getAgencyId()).orElseThrow(EntityNotFoundException.withIdAndClass(courtCase.getAgencyId(), CourtCase.class));
        var legalCaseType = legalCaseTypeReferenceCodeRepository.findById(LegalCaseType.pk(courtCase.getCaseType())).orElseThrow(EntityNotFoundException.withIdAndClass(courtCase.getCaseType(), LegalCaseType.class));
        var booking = offenderBookingRepository.findByBookingId(bookingId).orElseThrow(EntityNotFoundException.withIdAndClass(bookingId, OffenderBooking.class));
        var caseStatus = caseStatusReferenceCodeRepository.findById(CaseStatus.pk("A")).orElseThrow(EntityNotFoundException.withIdAndClass(bookingId, CaseStatus.class));

        var sequence = offenderCourtCaseRepository.findAllByOffenderBooking_BookingId(bookingId).stream().max(Comparator.comparing(OffenderCourtCase::getCaseSeq)).map(occ -> occ.getCaseSeq() + 1).orElse(1L);
        var offenderCourtCase = OffenderCourtCase.builder()
            .caseInfoNumber(courtCase.getCaseInfoNumber())
            .legalCaseType(legalCaseType)
            .agencyLocation(agency)
            .beginDate(courtCase.getBeginDate())
            .offenderBooking(booking)
            .caseSeq(sequence)
            .caseStatus(caseStatus)
            .build();

        var movementReason = movementReasonReferenceCodeRepository.findById(MovementReason.pk(courtCase.getHearingType())).orElseThrow(EntityNotFoundException.withIdAndClass(courtCase.getCaseType(), MovementReason.class));
        var eventStatus = eventStatusReferenceCodeRepository.findById(EventStatus.COMPLETED).orElseThrow(EntityNotFoundException.withIdAndClass(EventStatus.COMPLETED.getCode(), EventStatus.class));
        var courtEvent = CourtEvent.builder()
            .offenderBooking(booking)
            .courtEventType(movementReason)
            .eventStatus(eventStatus)
            .startTime(courtCase.getBeginDate().atTime(10, 0))
            .eventDate(courtCase.getBeginDate())
            .courtLocation(agency)
            .offenderCourtCase(offenderCourtCase)
            .build();

        courtEventRepository.save(courtEvent);

        return offenderCourtCaseRepository.save(offenderCourtCase).getId();
    }

    @Transactional
    public Long createOffenderOffence(Long bookingId, Offence offenderOffence) {
        var offence = offenceRepository.findById((new PK(offenderOffence.getOffenceCode(), offenderOffence.getOffenceStatue()))).orElseThrow(EntityNotFoundException.withIdAndClass(offenderOffence.getOffenceCode() + " " + offenderOffence.getOffenceStatue(), Offence.class));
        var courtCase = offenderCourtCaseRepository.findById(offenderOffence.getCourtCaseId()).orElseThrow(EntityNotFoundException.withIdAndClass(offenderOffence.getCourtCaseId(), OffenderCourtCase.class));
        var booking = offenderBookingRepository.findByBookingId(bookingId).orElseThrow(EntityNotFoundException.withIdAndClass(bookingId, OffenderBooking.class));
        var offenceResultCode = offenderOffence.isGuilty() ? OffenceResultRepository.IMPRISONMENT : OffenceResultRepository.NOT_GUILTY;
        var result = offenceResultRepository.findById(offenceResultCode).orElseThrow(EntityNotFoundException.withIdAndClass(offenceResultCode, OffenceResult.class));

        var offenderCharge = OffenderCharge.builder()
            .offence(offence)
            .dateOfOffence(offenderOffence.getOffenceDate())
            .endDate(offenderOffence.getOffenceEndDate())
            .offenderCourtCase(courtCase)
            .offenderBooking(booking)
            .resultCodeOne(result)
            .mostSeriousFlag("N")
            .pleaCode("G")
            .build();

        offenderCharge = offenderChargeRepository.save(offenderCharge);

        var courtEventCharge = CourtEventCharge.builder()
            .offenderCharge(offenderCharge)
            .courtEvent(courtCase.getCourtEvents().get(0))
            .build();

        courtEventChargeRepository.save(courtEventCharge);

        return offenderCharge.getId();
    }

    @Transactional
    public Integer createOffenderSentence(Long bookingId, Sentence sentence) {
        var courtCase = offenderCourtCaseRepository.findById(sentence.getCourtCaseId()).orElseThrow(EntityNotFoundException.withIdAndClass(sentence.getCourtCaseId(), CourtCase.class));
        var booking = offenderBookingRepository.findByBookingId(bookingId).orElseThrow(EntityNotFoundException.withIdAndClass(bookingId, OffenderBooking.class));
        var offenderCharge = offenderChargeRepository.findById(sentence.getOffenderChargeId()).orElseThrow(EntityNotFoundException.withIdAndClass(sentence.getOffenderChargeId(), OffenderCharge.class));
        var sentenceCalcType = sentenceCalcTypeRepository.findById(new SentenceCalcType.PK(sentence.getSentenceType(), sentence.getSentenceCategory())).orElseThrow(EntityNotFoundException.withIdAndClass(sentence.getSentenceType() + " " + sentence.getSentenceCategory(), SentenceCalcType.class));

        var courtOrder = CourtOrder.builder()
            .courtCase(courtCase)
            .issuingCourt(courtCase.getAgencyLocation())
            .courtDate(sentence.getSentenceDate())
            .offenderBooking(booking)
            .orderType("AUTO")
            //TODO ORDER STATUS
            .build();

        courtOrder = courtOrderRepository.save(courtOrder);

        var sequence = offenderSentenceRepository.findByOffenderBooking_BookingId(bookingId).stream().max(Comparator.comparing(OffenderSentence::getSequence)).map(os -> os.getSequence() + 1).orElse(1);

        var offenderSentence = OffenderSentence.builder()
            .id(new OffenderSentence.PK(booking.getBookingId(), sequence))
            .lineSequence(Long.valueOf(sequence))
            .calculationType(sentenceCalcType)
            .sentenceStartDate(sentence.getSentenceDate())
            .courtCase(courtCase)
            .courtOrder(courtOrder)
            .status("A")
        .build();

        offenderSentence =  offenderSentenceRepository.save(offenderSentence);

        var term = SentenceTerm.builder()
            .days(sentence.getDays())
            .weeks(sentence.getWeeks())
            .months(sentence.getMonths())
            .years(sentence.getYears())
            .sentenceTermCode("IMP")
            .startDate(sentence.getSentenceDate())
            .id(new SentenceTerm.PK(booking.getBookingId(), offenderSentence.getSequence(), 1))
            .build();

        sentenceTermRepository.save(term);

        var offenderSentenceCharge = OffenderSentenceCharge.builder()
            .id(new OffenderSentenceCharge.PK(booking.getBookingId(), offenderSentence.getSequence(), offenderCharge.getId()))
            .build();

        offenderSentenceChargeRepository.save(offenderSentenceCharge);

        var movementReason = movementReasonReferenceCodeRepository.findById(MovementReason.SENTENCING).orElseThrow(EntityNotFoundException.withIdAndClass(MovementReason.SENTENCING.getCode(), MovementReason.class));
        var eventStatus = eventStatusReferenceCodeRepository.findById(EventStatus.COMPLETED).orElseThrow(EntityNotFoundException.withIdAndClass(EventStatus.COMPLETED.getCode(), EventStatus.class));
        var courtEvent = CourtEvent.builder()
            .offenderBooking(booking)
            .courtEventType(movementReason)
            .eventStatus(eventStatus)
            .startTime(sentence.getSentenceDate().atTime(10, 0))
            .eventDate(sentence.getSentenceDate())
            .courtLocation(courtCase.getAgencyLocation())
            .offenderCourtCase(courtCase)
            .build();

        courtEventRepository.save(courtEvent);

        var courtEventCharge = CourtEventCharge.builder()
            .offenderCharge(offenderCharge)
            .courtEvent(courtEvent)
            .build();

        courtEventChargeRepository.save(courtEventCharge);

        return offenderSentence.getSequence();
    }

}
