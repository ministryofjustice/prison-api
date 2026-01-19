package uk.gov.justice.hmpps.prison.service.transformers;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.prison.api.model.AssignedLivingUnit;
import uk.gov.justice.hmpps.prison.api.model.InmateDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenceHistoryDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenderIdentifier;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceTerms;
import uk.gov.justice.hmpps.prison.api.model.PhysicalAttributes;
import uk.gov.justice.hmpps.prison.api.model.ProfileInformation;
import uk.gov.justice.hmpps.prison.api.model.RecallCalc;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCharge;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImprisonmentStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderPhysicalAttributes;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderProfileDetail;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ReferenceCode;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceTerm;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderChargeRepository;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.hmpps.prison.util.DateTimeConverter.getAge;

@Component
@AllArgsConstructor
public class OffenderTransformer {

    private final Clock clock;
    private final OffenderChargeTransformer offenderChargeTransformer;
    private final OffenderChargeRepository offenderChargeRepository;

    public InmateDetail transformWithoutBooking(final Offender offender) {
        return buildOffender(offender, null).build();
    }

    public InmateDetail transform(final OffenderBooking latestBooking) {
        final var offenderBuilder = buildOffender(latestBooking.getOffender(), latestBooking.getLatestPhysicalAttributes());
        final var allConvictedOffences = getAllConvictedOffences(latestBooking.getOffender().getRootOffenderId());
        final var activeConvictedOffences = getActiveConvictedOffences(latestBooking.getBookingId());
        final var sentenceTerms = latestBooking.getActiveFilteredSentenceTerms(Collections.emptyList());
        final var imprisonmentStatus = latestBooking.getActiveImprisonmentStatus().map(OffenderImprisonmentStatus::getImprisonmentStatus).orElse(null);

        return offenderBuilder
            .activeFlag(latestBooking.isActive())
            .inOutStatus(latestBooking.getInOutStatus())
            .statusReason(latestBooking.getStatusReason())
            .agencyId(latestBooking.getLocation().getId())
            .bookingId(latestBooking.getBookingId())
            .bookingNo(latestBooking.getBookNumber())
            .assignedLivingUnit(AssignedLivingUnit.builder()
                .agencyId(latestBooking.getLocation().getId())
                .agencyName(LocationProcessor.formatLocation(latestBooking.getLocation().getDescription()))
                .description(latestBooking.getAssignedLivingUnit() != null ? RegExUtils.replaceFirst(latestBooking.getAssignedLivingUnit().getDescription(), "^[A-Z|a-z|0-9]+\\-", "") : null)
                .locationId(latestBooking.getAssignedLivingUnit() != null ? latestBooking.getAssignedLivingUnit().getLocationId() : null)
                .build())
            .alerts(latestBooking.getAlerts().stream().map(OffenderAlertTransformer::transformForBooking).toList())
            .alertsCodes(latestBooking.getAlertCodes())
            .activeAlertCount(latestBooking.getActiveAlertCount())
            .inactiveAlertCount(latestBooking.getAlerts().size() - latestBooking.getActiveAlertCount())
            .assignedLivingUnitId(latestBooking.getAssignedLivingUnit() != null ? latestBooking.getAssignedLivingUnit().getLocationId() : null)
            .sentenceTerms(sentenceTerms)
            .sentenceDetail(latestBooking.getSentenceCalcDates())
            .profileInformation(latestBooking.getActiveProfileDetails().stream()
                .sorted(Comparator.comparing(OffenderProfileDetail::getListSequence))
                .map(pd -> ProfileInformation.builder()
                    .type(pd.getId().getType().getType())
                    .question(pd.getId().getType().getDescription())
                    .resultValue(pd.getCode() != null ? pd.getCode().getDescription() : pd.getProfileCode())
                    .build()).toList())
            .legalStatus(latestBooking.getLegalStatus())
            .imprisonmentStatus(imprisonmentStatus != null ? imprisonmentStatus.getStatus() : null)
            .imprisonmentStatusDescription(imprisonmentStatus != null ? imprisonmentStatus.getDescription() : null)
            .convictedStatus(imprisonmentStatus != null ? latestBooking.getConvictedStatus(): null)
            .offenceHistory(allConvictedOffences)
            .recall(RecallCalc.calculate(latestBooking.getBookingId(), latestBooking.getLegalStatus(), activeConvictedOffences, sentenceTerms))
            .receptionDate(latestBooking.getBookingBeginDate().toLocalDate())
            .locationDescription(latestBooking.getLocationDescription())
            .latestLocationId(latestBooking.getLatestLocationId())
            .latestPrisonLocationId(latestBooking.getLatestPrisonLocationId())
            .build()
            .deriveStatus()
            .splitStatusReason()
            .updateReligion();
    }

    private @NotNull List<OffenceHistoryDetail> getAllConvictedOffences(Long rootOffenderId) {
        return offenderChargeRepository.findChargesByRootOffenderId(rootOffenderId).stream()
            .sorted(Comparator.comparing(OffenderCharge::getId))
            .map(offenderChargeTransformer::convert)
            .filter(OffenceHistoryDetail::convicted)
            .toList();
    }

    private @NotNull List<OffenceHistoryDetail> getActiveConvictedOffences(Long bookingId) {
        return offenderChargeRepository.findByOffenderBooking_BookingId(bookingId).stream()
            .filter(oc -> "A".equals(oc.getChargeStatus()))
            .filter(oc -> "A".equals(oc.getOffenderCourtCase().getCaseStatus().map(ReferenceCode::getCode).orElse(null)))
            .sorted(Comparator.comparing(OffenderCharge::getId))
            .map(offenderChargeTransformer::convert)
            .filter(OffenceHistoryDetail::convicted)
            .toList();
    }

    private InmateDetail.InmateDetailBuilder buildOffender(final Offender offender, @Nullable OffenderPhysicalAttributes physicalAttributes) {
        return InmateDetail.builder()
            .offenderNo(offender.getNomsId())
            .offenderId(offender.getId())
            .rootOffenderId(offender.getRootOffenderId())
            .lastName(offender.getLastName())
            .firstName(offender.getFirstName())
            .middleName(StringUtils.trimToNull(StringUtils.trimToEmpty(offender.getMiddleName()) + " " + StringUtils.trimToEmpty(offender.getMiddleName2())))
            .dateOfBirth(offender.getBirthDate())
            .age(getAge(offender.getBirthDate(), LocalDate.now(clock)))
            .profileInformation(null)
            .identifiers(offender.getLatestIdentifiers().stream().map(oi -> OffenderIdentifier.builder()
                .offenderNo(offender.getNomsId())
                .caseloadType(oi.getCaseloadType())
                .type(oi.getIdentifierType())
                .value(oi.getIdentifier())
                .issuedDate(oi.getIssuedDate())
                .whenCreated(oi.getCreateDateTime())
                .issuedAuthorityText(oi.getIssuedAuthorityText())
                .offenderId(oi.getOffender().getId())
                .build()).toList())
            .physicalAttributes(PhysicalAttributes.builder()
                .sexCode(offender.getGender().getCode())
                .gender(offender.getGender().getDescription())
                .raceCode(offender.getEthnicity() != null ? offender.getEthnicity().getCode() : null)
                .ethnicity(offender.getEthnicity() != null ? offender.getEthnicity().getDescription() : null)
                .heightCentimetres(physicalAttributes != null ? physicalAttributes.getHeightCentimetres() : null)
                .heightMetres(physicalAttributes != null&& physicalAttributes.getHeightCentimetres() != null ? BigDecimal.valueOf(physicalAttributes.getHeightCentimetres()).movePointLeft(2) : null)
                .heightFeet(physicalAttributes != null ? physicalAttributes.getHeightFeet() : null)
                .heightInches(physicalAttributes != null ? physicalAttributes.getHeightInches() : null)
                .weightKilograms(physicalAttributes != null ? physicalAttributes.getWeightKgs() : null)
                .weightPounds(physicalAttributes != null ? physicalAttributes.getWeightPounds() : null)
                .build());
    }

    public static List<OffenderSentenceTerms> filterSentenceTerms(List<SentenceTerm> terms, List<String> filterBySentenceTermCodes) {
        final var sentenceTermCodes = (filterBySentenceTermCodes == null || filterBySentenceTermCodes.isEmpty()) ? List.of("IMP") : filterBySentenceTermCodes;
        return terms
            .stream()
            .filter(term -> "A".equals(term.getOffenderSentence().getStatus()))
            .filter(term -> sentenceTermCodes.contains(term.getSentenceTermCode()))
            .map(SentenceTerm::getSentenceSummary)
            .collect(toList());
    }
}
