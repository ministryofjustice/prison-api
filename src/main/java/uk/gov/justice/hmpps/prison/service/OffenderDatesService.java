package uk.gov.justice.hmpps.prison.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.RequestToUpdateOffenderDates;
import uk.gov.justice.hmpps.prison.api.model.SentenceCalcDates;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CalcReasonType;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalculation;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.ReferenceCodeRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class OffenderDatesService {

    private final OffenderBookingRepository offenderBookingRepository;
    private final StaffUserAccountRepository staffUserAccountRepository;
    private final ReferenceCodeRepository<CalcReasonType> calcReasonTypeReferenceCodeRepository;
    private final Clock clock;

    @Transactional
    public SentenceCalcDates updateOffenderKeyDates(Long bookingId, RequestToUpdateOffenderDates requestToUpdateOffenderDates) {
        final var offenderBooking = offenderBookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));

        final var calculationDate = requestToUpdateOffenderDates.getCalculationDateTime() != null
            ? requestToUpdateOffenderDates.getCalculationDateTime()
            : LocalDateTime.now(clock);

        final var calcReasonCode = "UPDATE";
        final var calcReason = calcReasonTypeReferenceCodeRepository.findById(CalcReasonType.pk(calcReasonCode))
            .orElseThrow(EntityNotFoundException.withId(calcReasonCode));

        final var staffUserAccount = staffUserAccountRepository.findById(requestToUpdateOffenderDates.getSubmissionUser())
            .orElseThrow(EntityNotFoundException.withId(requestToUpdateOffenderDates.getSubmissionUser()));

        final var keyDatesFromPayload = requestToUpdateOffenderDates.getKeyDates();

        final var sentenceCalculation =
            SentenceCalculation.builder()
                .offenderBooking(offenderBooking)
                .calcReasonType(calcReason)
                .calculationDate(calculationDate)
                .comments("CRD calculation ID: " + requestToUpdateOffenderDates.getCalculationUuid())
                .staff(staffUserAccount.getStaff())
                .recordedUser(staffUserAccount)
                .recordedDateTime(calculationDate)
                .hdcedCalculatedDate(keyDatesFromPayload.getHomeDetentionCurfewEligibilityDate())
                .etdCalculatedDate(keyDatesFromPayload.getEarlyTermDate())
                .mtdCalculatedDate(keyDatesFromPayload.getMidTermDate())
                .ltdCalculatedDate(keyDatesFromPayload.getLateTermDate())
                .dprrdCalculatedDate(keyDatesFromPayload.getDtoPostRecallReleaseDate())
                .ardCalculatedDate(keyDatesFromPayload.getAutomaticReleaseDate())
                .crdCalculatedDate(keyDatesFromPayload.getConditionalReleaseDate())
                .pedCalculatedDate(keyDatesFromPayload.getParoleEligibilityDate())
                .npdCalculatedDate(keyDatesFromPayload.getNonParoleDate())
                .ledCalculatedDate(keyDatesFromPayload.getLicenceExpiryDate())
                .prrdCalculatedDate(keyDatesFromPayload.getPostRecallReleaseDate())
                .sedCalculatedDate(keyDatesFromPayload.getSentenceExpiryDate())
                .tusedCalculatedDate(keyDatesFromPayload.getTopupSupervisionExpiryDate())
                .effectiveSentenceEndDate(keyDatesFromPayload.getEffectiveSentenceEndDate())
                .effectiveSentenceLength(keyDatesFromPayload.getSentenceLength())
                .judiciallyImposedSentenceLength(keyDatesFromPayload.getSentenceLength())
                .build();
        offenderBooking.addSentenceCalculation(sentenceCalculation);
        return offenderBooking.getSentenceCalcDates(Optional.of(sentenceCalculation));
    }
}
