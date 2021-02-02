package uk.gov.justice.hmpps.prison.service.transformers;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.prison.api.model.AssignedLivingUnit;
import uk.gov.justice.hmpps.prison.api.model.InmateDetail;
import uk.gov.justice.hmpps.prison.api.model.OffenderIdentifier;
import uk.gov.justice.hmpps.prison.api.model.PhysicalAttributes;
import uk.gov.justice.hmpps.prison.api.model.ProfileInformation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.service.support.LocationProcessor;

import java.time.Clock;
import java.time.LocalDate;
import java.util.stream.Collectors;

import static java.lang.String.format;
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
            .activeFlag(latestBooking.getActiveFlag().equalsIgnoreCase("Y"))
            .agencyId(latestBooking.getLocation().getId())
            .bookingId(latestBooking.getBookingId())
            .bookingNo(latestBooking.getBookNumber())
            .inOutStatus(latestBooking.getInOutStatus())
            .status(format("%s %s", latestBooking.getActiveFlag().equalsIgnoreCase("Y") ? "ACTIVE" : "INACTIVE", latestBooking.getInOutStatus()))
            .assignedLivingUnit(AssignedLivingUnit.builder()
                .agencyId(latestBooking.getLocation().getId())
                .agencyName(LocationProcessor.formatLocation(latestBooking.getLocation().getDescription()))
                .description(latestBooking.getAssignedLivingUnit() != null ? RegExUtils.replaceFirst(latestBooking.getAssignedLivingUnit().getDescription(), "^[A-Z|a-z|0-9]+\\-", "") : null)
                .locationId(latestBooking.getAssignedLivingUnit() != null ? latestBooking.getAssignedLivingUnit().getLocationId() : null)
                .build())
            .assignedLivingUnitId(latestBooking.getAssignedLivingUnit() != null ? latestBooking.getAssignedLivingUnit().getLocationId() : null)
            .profileInformation(latestBooking.getActiveProfileDetails().stream()
                .filter(pd -> pd.getCode() != null)
                .map(pd -> ProfileInformation.builder()
                .type(pd.getId().getType().getType())
                .question(pd.getId().getType().getDescription())
                .resultValue(pd.getCode().getDescription())
                .build()).collect(Collectors.toList()))
            .build();
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
                .build()).collect(Collectors.toList()))
            .physicalAttributes(PhysicalAttributes.builder()
                .sexCode(offender.getGender().getCode())
                .gender(offender.getGender().getDescription())
                .raceCode(offender.getEthnicity() != null ? offender.getEthnicity().getCode() : null)
                .ethnicity(offender.getEthnicity() != null ? offender.getEthnicity().getDescription() : null)
                .build());
    }
}
