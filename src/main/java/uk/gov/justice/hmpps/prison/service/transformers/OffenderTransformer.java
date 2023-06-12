package uk.gov.justice.hmpps.prison.service.transformers;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.prison.api.model.AssignedLivingUnit;
import uk.gov.justice.hmpps.prison.api.model.InmateDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenderIdentifier;
import uk.gov.justice.hmpps.prison.api.model.OffenderSentenceTerms;
import uk.gov.justice.hmpps.prison.api.model.PhysicalAttributes;
import uk.gov.justice.hmpps.prison.api.model.ProfileInformation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.SentenceTerm;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.hmpps.prison.util.DateTimeConverter.getAge;

@Component
@AllArgsConstructor
public class OffenderTransformer {

    private final Clock clock;

    public InmateDetail transform(final Offender offender) {
        return offender.getLatestBooking()
            .map(this::transform)
            .orElse(buildOffender(offender).build());
    }

    public InmateDetail transform(final OffenderBooking latestBooking) {
        final var offenderBuilder = buildOffender(latestBooking.getOffender());

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
            .sentenceTerms(latestBooking.getActiveFilteredSentenceTerms(Collections.emptyList()))
            .sentenceDetail(latestBooking.getSentenceCalcDates())
            .profileInformation(latestBooking.getActiveProfileDetails().stream()
                .filter(pd -> pd.getCode() != null)
                .map(pd -> ProfileInformation.builder()
                    .type(pd.getId().getType().getType())
                    .question(pd.getId().getType().getDescription())
                    .resultValue(pd.getCode().getDescription())
                    .build()).toList())
            .build()
            .deriveStatus()
            .splitStatusReason();
    }

    private InmateDetail.InmateDetailBuilder buildOffender(final Offender offender) {
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
                .build()).toList())
            .physicalAttributes(PhysicalAttributes.builder()
                .sexCode(offender.getGender().getCode())
                .gender(offender.getGender().getDescription())
                .raceCode(offender.getEthnicity() != null ? offender.getEthnicity().getCode() : null)
                .ethnicity(offender.getEthnicity() != null ? offender.getEthnicity().getDescription() : null)
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
