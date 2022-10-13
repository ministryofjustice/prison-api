package uk.gov.justice.hmpps.prison.service.digitalwarrant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.hmpps.prison.api.model.digitalwarrant.CourtCase;
import uk.gov.justice.hmpps.prison.api.model.digitalwarrant.Offence;
import uk.gov.justice.hmpps.prison.api.model.digitalwarrant.Sentence;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.LegalCaseType;
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

import static java.util.Arrays.asList;

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
    private OffenceResultRepository offenceResultRepository;

    @Autowired
    private SentenceTermRepository sentenceTermRepository;

    @Transactional
    public Long createCourtCase(Long bookingId, CourtCase courtCase) {
        AgencyLocation agency = agencyLocationRepository.findById(courtCase.getAgencyId()).orElseThrow(EntityNotFoundException.withId(courtCase.getAgencyId()));
        LegalCaseType legalCaseType = legalCaseTypeReferenceCodeRepository.findById(LegalCaseType.pk(courtCase.getCaseType())).orElseThrow(EntityNotFoundException.withId(courtCase.getCaseType()));
        OffenderBooking booking = offenderBookingRepository.findByBookingId(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));

        Long sequence = offenderCourtCaseRepository.findAllByOffenderBooking_BookingId(bookingId).stream().max(Comparator.comparing(OffenderCourtCase::getCaseSeq)).map(occ -> occ.getCaseSeq() + 1).orElse(1L);
        OffenderCourtCase offenderCourtCase = OffenderCourtCase.builder()
            .caseInfoNumber(courtCase.getCaseInfoNumber())
            .legalCaseType(legalCaseType)
            .agencyLocation(agency)
            .beginDate(courtCase.getBeginDate())
            .offenderBooking(booking)
            .caseSeq(sequence)
            .build();

        return offenderCourtCaseRepository.save(offenderCourtCase).getId();
    }

    @Transactional
    public Long createOffenderOffence(Long bookingId, Offence offenderOffence) {
        uk.gov.justice.hmpps.prison.repository.jpa.model.Offence offence = offenceRepository.findById((new PK(offenderOffence.getOffenceCode(), offenderOffence.getOffenceStatue()))).orElseThrow(EntityNotFoundException.withId(offenderOffence.getOffenceCode() + " " + offenderOffence.getOffenceStatue()));
        OffenderCourtCase courtCase = offenderCourtCaseRepository.findById(offenderOffence.getCourtCaseId()).orElseThrow(EntityNotFoundException.withId(offenderOffence.getCourtCaseId()));
        OffenderBooking booking = offenderBookingRepository.findByBookingId(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));
        String offenceResultCode = offenderOffence.isGuilty() ? OffenceResultRepository.IMPRISONMENT : OffenceResultRepository.NOT_GUILTY;
        OffenceResult result = offenceResultRepository.findById(offenceResultCode).orElseThrow(EntityNotFoundException.withId(offenceResultCode));

        OffenderCharge offenderCharge = OffenderCharge.builder()
            .offence(offence)
            .dateOfOffence(offenderOffence.getOffenceDate())
            .endDate(offenderOffence.getOffenceEndDate())
            .offenderCourtCase(courtCase)
            .offenderBooking(booking)
            .resultCodeOne(result)
            .mostSeriousFlag("N")
            .build();

        return offenderChargeRepository.save(offenderCharge).getId();
    }

    @Transactional
    public Integer createOffenderSentence(Long bookingId, Sentence sentence) {
        OffenderCourtCase courtCase = offenderCourtCaseRepository.findById(sentence.getCourtCaseId()).orElseThrow(EntityNotFoundException.withId(sentence.getCourtCaseId()));
        OffenderBooking booking = offenderBookingRepository.findByBookingId(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));
        OffenderCharge offenderCharge = offenderChargeRepository.findById(sentence.getOffenderChargeId()).orElseThrow(EntityNotFoundException.withId(sentence.getOffenderChargeId()));
        SentenceCalcType sentenceCalcType = sentenceCalcTypeRepository.findById(new SentenceCalcType.PK(sentence.getSentenceType(), sentence.getSentenceCategory())).orElseThrow(EntityNotFoundException.withId(sentence.getSentenceType() + " " + sentence.getSentenceCategory()));

        //TODO is this the correct way to set sequence?
        Integer sequence = offenderSentenceRepository.findByOffenderBooking_BookingId(bookingId).stream().max(Comparator.comparing(OffenderSentence::getSequence)).map(os -> os.getSequence() + 1).orElse(1);

        OffenderSentence offenderSentence = OffenderSentence.builder()
            .id(new OffenderSentence.PK(booking.getBookingId(), sequence))
            .calculationType(sentenceCalcType)
            .sentenceStartDate(sentence.getSentenceDate())
            .courtCase(courtCase)
            .status("A")
        .build();

        offenderSentence =  offenderSentenceRepository.save(offenderSentence);

        SentenceTerm term = SentenceTerm.builder()
            .days(sentence.getDays())
            .weeks(sentence.getWeeks())
            .months(sentence.getMonths())
            .years(sentence.getYears())
            .sentenceTermCode("IMP")
            .startDate(sentence.getSentenceDate())
            .id(new SentenceTerm.PK(booking.getBookingId(), offenderSentence.getSequence(), 1))
            .build();

        sentenceTermRepository.save(term);

        OffenderSentenceCharge offenderSentenceCharge = OffenderSentenceCharge.builder()
            .id(new OffenderSentenceCharge.PK(booking.getBookingId(), offenderSentence.getSequence(), offenderCharge.getId()))
            .build();

        offenderSentenceChargeRepository.save(offenderSentenceCharge);

        return offenderSentence.getSequence();
    }
}
