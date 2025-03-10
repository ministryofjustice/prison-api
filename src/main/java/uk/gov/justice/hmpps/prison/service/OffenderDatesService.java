package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.ImmutableMap;
import com.microsoft.applicationinsights.TelemetryClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.prison.api.model.LatestTusedData;
import uk.gov.justice.hmpps.prison.api.model.OffenderCalculatedKeyDates;
import uk.gov.justice.hmpps.prison.api.model.RequestToUpdateOffenderDates;
import uk.gov.justice.hmpps.prison.api.model.SentenceCalcDates;
import uk.gov.justice.hmpps.prison.repository.SentenceCalculationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceCalculation;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.StaffUserAccountRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class OffenderDatesService {

    private static final String ZERO_LENGTH = "00/00/00";
    private static final String DEFAULT_REASON = "UPDATE";
    private final SentenceCalculationRepository sentenceCalculationRepository;
    private final OffenderBookingRepository offenderBookingRepository;
    private final StaffUserAccountRepository staffUserAccountRepository;
    private final TelemetryClient telemetryClient;
    private final Clock clock;

    @Transactional
    public SentenceCalcDates updateOffenderKeyDates(Long bookingId, RequestToUpdateOffenderDates requestToUpdateOffenderDates) {
        final var offenderBooking = offenderBookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));

        final var calculationDate = requestToUpdateOffenderDates.getCalculationDateTime() != null
            ? requestToUpdateOffenderDates.getCalculationDateTime()
            : LocalDateTime.now(clock);

        final var staffUserAccount = staffUserAccountRepository.findById(requestToUpdateOffenderDates.getSubmissionUser())
            .orElseThrow(EntityNotFoundException.withId(requestToUpdateOffenderDates.getSubmissionUser()));

        final var keyDatesFromPayload = requestToUpdateOffenderDates.getKeyDates();
        SentenceCalculation sentenceCalculation;
        if (!requestToUpdateOffenderDates.isNoDates()) {
            sentenceCalculation =
                SentenceCalculation.builder()
                    .offenderBooking(offenderBooking)
                    .reasonCode(isBlank(requestToUpdateOffenderDates.getReason()) ? DEFAULT_REASON : requestToUpdateOffenderDates.getReason())
                    .calculationDate(calculationDate)
                    .comments(
                        isBlank(requestToUpdateOffenderDates.getComment()) ?
                            "The information shown was calculated using the Calculate Release Dates service. The calculation ID is: " + requestToUpdateOffenderDates.getCalculationUuid()
                            : requestToUpdateOffenderDates.getComment()
                    )
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
                    .ersedOverridedDate(keyDatesFromPayload.getEarlyRemovalSchemeEligibilityDate())
                    .hdcadOverridedDate(keyDatesFromPayload.getHomeDetentionCurfewApprovedDate())
                    .tariffOverridedDate(keyDatesFromPayload.getTariffDate())
                    .tersedOverridedDate(keyDatesFromPayload.getTariffExpiredRemovalSchemeEligibilityDate())
                    .apdOverridedDate(keyDatesFromPayload.getApprovedParoleDate())
                    .rotlOverridedDate(keyDatesFromPayload.getReleaseOnTemporaryLicenceDate())
                    .judiciallyImposedSentenceLength(keyDatesFromPayload.getSentenceLength())
                    .build();
            offenderBooking.addSentenceCalculation(sentenceCalculation);
        } else {
            sentenceCalculation = SentenceCalculation.builder()
                .offenderBooking(offenderBooking)
                .reasonCode(isBlank(requestToUpdateOffenderDates.getReason()) ? DEFAULT_REASON : requestToUpdateOffenderDates.getReason())
                .calculationDate(calculationDate)
                .effectiveSentenceLength(ZERO_LENGTH)
                .comments(
                    isBlank(requestToUpdateOffenderDates.getComment()) ?
                        "The information shown was calculated using the Calculate Release Dates service. The calculation ID is: " + requestToUpdateOffenderDates.getCalculationUuid()
                        : requestToUpdateOffenderDates.getComment()
                )
                .staff(staffUserAccount.getStaff())
                .recordedUser(staffUserAccount)
                .recordedDateTime(calculationDate).build();
            offenderBooking.addSentenceCalculation(sentenceCalculation);
        }

        telemetryClient.trackEvent("OffenderKeyDatesUpdated",
            ImmutableMap.of(
                "bookingId", bookingId.toString(),
                "calculationUuid", requestToUpdateOffenderDates.getCalculationUuid().toString(),
                "submissionUser", requestToUpdateOffenderDates.getSubmissionUser()
            ), null);

        return offenderBooking.getSentenceCalcDates(Optional.of(sentenceCalculation));
    }

    public OffenderCalculatedKeyDates getOffenderKeyDates(Long bookingId) {
        final var offenderBooking = offenderBookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException.withId(bookingId));
        final var sentenceCalculation = offenderBooking.getLatestCalculation().orElseThrow(EntityNotFoundException.withId(bookingId));

        return getOffenderCalculatedKeyDates(sentenceCalculation);
    }

    public OffenderCalculatedKeyDates getOffenderKeyDatesByOffenderSentCalcId(Long offenderSentCalcId) {
        final var sentenceCalculation = sentenceCalculationRepository.findById(offenderSentCalcId).orElseThrow(EntityNotFoundException.withId(offenderSentCalcId));

        return getOffenderCalculatedKeyDates(sentenceCalculation);
    }

    private OffenderCalculatedKeyDates getOffenderCalculatedKeyDates(SentenceCalculation sentenceCalculation) {
        return OffenderCalculatedKeyDates.offenderCalculatedKeyDates()
            .homeDetentionCurfewEligibilityDate(sentenceCalculation.getHomeDetentionCurfewEligibilityDate())
            .earlyTermDate(sentenceCalculation.getEarlyTermDate())
            .midTermDate(sentenceCalculation.getMidTermDate())
            .lateTermDate(sentenceCalculation.getLateTermDate())
            .dtoPostRecallReleaseDate(sentenceCalculation.getDtoPostRecallReleaseDate())
            .automaticReleaseDate(sentenceCalculation.getAutomaticReleaseDate())
            .conditionalReleaseDate(sentenceCalculation.getConditionalReleaseDate())
            .paroleEligibilityDate(sentenceCalculation.getParoleEligibilityDate())
            .nonParoleDate(sentenceCalculation.getNonParoleDate())
            .licenceExpiryDate(sentenceCalculation.getLicenceExpiryDate())
            .postRecallReleaseDate(sentenceCalculation.getPostRecallReleaseDate())
            .sentenceExpiryDate(sentenceCalculation.getSentenceExpiryDate())
            .topupSupervisionExpiryDate(sentenceCalculation.getTopupSupervisionExpiryDate())
            .effectiveSentenceEndDate(sentenceCalculation.getEffectiveSentenceEndDate())
            .sentenceLength(sentenceCalculation.getEffectiveSentenceLength())
            .earlyRemovalSchemeEligibilityDate(sentenceCalculation.getErsedOverridedDate())
            .homeDetentionCurfewApprovedDate(sentenceCalculation.getHdcadOverridedDate())
            .tariffDate(sentenceCalculation.getTariffOverridedDate())
            .tariffExpiredRemovalSchemeEligibilityDate(sentenceCalculation.getTersedOverridedDate())
            .approvedParoleDate(sentenceCalculation.getApdOverridedDate())
            .releaseOnTemporaryLicenceDate(sentenceCalculation.getRotlOverridedDate())
            .judiciallyImposedSentenceLength(sentenceCalculation.getJudiciallyImposedSentenceLength())
            .comment(sentenceCalculation.getComments())
            .reasonCode(sentenceCalculation.getReasonCode())
            .calculatedAt(sentenceCalculation.getCalculationDate())
            .build();
    }

    public LatestTusedData getLatestTusedDataFromNomsId(String nomsId) {
        return offenderBookingRepository.findLatestTusedDataFromNomsId(nomsId)
            .orElseThrow(EntityNotFoundException.withId(nomsId));
    }
}
