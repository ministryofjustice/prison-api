package uk.gov.justice.hmpps.prison.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.WordUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.OffenderNonAssociation;
import uk.gov.justice.hmpps.prison.api.model.OffenderNonAssociationDetails;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.NonAssociationReason;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderNonAssociationDetail;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderNonAssociationDetailRepository;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
@Validated
@Slf4j
public class OffenderNonAssociationsService {

    private final OffenderBookingRepository bookingRepository;
    private final OffenderNonAssociationDetailRepository offenderNonAssociationDetailRepository;
    private final Clock clock;

    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "VIEW_PRISONER_DATA"} )
    public OffenderNonAssociationDetails retrieveByOffenderNo(final String offenderNo, final boolean currentPrisonOnly, final boolean excludeInactive) {
        log.debug("Fetching non-associations for offender no '{}'", offenderNo);
        final var now = LocalDate.now(clock);

        final var latestBooking = bookingRepository.findWithDetailsByOffenderNomsIdAndBookingSequence(offenderNo, 1)
            .orElseThrow(EntityNotFoundException.withMessage("Booking not found for offender %s.", offenderNo));
        final List<OffenderNonAssociationDetail> nonAssociations
            = offenderNonAssociationDetailRepository.findAllByOffenderBooking_Offender_NomsIdOrderByEffectiveDateAsc(offenderNo)
                .stream().filter(
                    na -> !excludeInactive || ((na.getExpiryDate() == null || !na.getExpiryDate().isBefore(now)) && !na.getEffectiveDate().isAfter(now)))
            .filter(na -> !currentPrisonOnly || (na.getNonAssociation().getNsAgencyId().isPresent() && latestBooking.getLocation().getId().equals(na.getNonAssociation().getNsAgencyId().get())))
            .toList();

        log.debug("'{}' non-association(s) found for offender '{}'", nonAssociations.size(), offenderNo);

        return getOffenderNonAssociationDetails(latestBooking, nonAssociations);
    }

    private OffenderNonAssociationDetails getOffenderNonAssociationDetails(OffenderBooking booking, List<OffenderNonAssociationDetail> nonAssociations) {
        final Offender offender = booking.getOffender();
        return OffenderNonAssociationDetails.builder()
            .offenderNo(offender.getNomsId())
            .firstName(WordUtils.capitalizeFully(offender.getFirstName()))
            .lastName(WordUtils.capitalizeFully(offender.getLastName()))
            .agencyDescription(booking.getLocation().getDescription())
            .agencyId(booking.getLocation().getId())
            .assignedLivingUnitId(Optional.ofNullable(booking.getAssignedLivingUnit()).map(AgencyInternalLocation::getLocationId).orElse(null))
            .assignedLivingUnitDescription(Optional.ofNullable(booking.getAssignedLivingUnit()).map(AgencyInternalLocation::getDescription).orElse(null))
            .nonAssociations(nonAssociations.stream().map(this::transform).toList())
            .build();
    }

    private uk.gov.justice.hmpps.prison.api.model.OffenderNonAssociationDetail transform(final OffenderNonAssociationDetail detail) {
        return uk.gov.justice.hmpps.prison.api.model.OffenderNonAssociationDetail.builder()
                .effectiveDate(detail.getEffectiveDate() != null ? detail.getEffectiveDate().atStartOfDay() : null)
                .expiryDate(detail.getExpiryDate() != null ? detail.getExpiryDate().atStartOfDay() : null)
                .comments(detail.getComments())
                .authorisedBy(detail.getAuthorizedBy())
                .reasonCode(detail.getNonAssociationReason().getCode())
                .reasonDescription(detail.getNonAssociationReason().getDescription())
                .typeCode(detail.getNonAssociationType().getCode())
                .typeDescription(detail.getNonAssociationType().getDescription())
                .offenderNonAssociation(OffenderNonAssociation.builder()
                        .offenderNo(detail.getNsOffender().getNomsId())
                        .firstName(WordUtils.capitalizeFully(detail.getNsOffender().getFirstName()))
                        .lastName(WordUtils.capitalizeFully(detail.getNsOffender().getLastName()))
                        .reasonCode(Optional.ofNullable(detail.getNonAssociation().getRecipNonAssociationReason()).map(NonAssociationReason::getCode).orElse(null))
                        .reasonDescription(Optional.ofNullable(detail.getNonAssociation().getRecipNonAssociationReason()).map(NonAssociationReason::getDescription).orElse(null))
                        .agencyDescription(detail.getNonAssociation().getNsAgencyDescription().orElse(null))
                        .agencyId(detail.getNonAssociation().getNsAgencyId().orElse(null))
                        .assignedLivingUnitDescription(detail.getNonAssociation().getNsAssignedLivingUnitDescription().orElse(null))
                        .assignedLivingUnitId(detail.getNonAssociation().getNsAssignedLivingUnitId().orElse(null))
                        .build())
                .build();
    }
}
