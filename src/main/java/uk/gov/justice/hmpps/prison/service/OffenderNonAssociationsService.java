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
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderNonAssociationDetail;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderBookingRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderRepository;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;

import java.util.Collection;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
@Validated
@Slf4j
public class OffenderNonAssociationsService {

    private final OffenderBookingRepository bookingRepository;
    private final OffenderRepository offenderRepository;

    @VerifyBookingAccess
    public OffenderNonAssociationDetails retrieve(final Long bookingId) {
        log.debug("Fetching non-associations for booking id '{}'", bookingId);

        final var booking = bookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException.withMessage("Offender booking with id %d not found.", bookingId));

        final var nonAssociations = booking.getNonAssociationDetails()
                .stream()
                .map(this::transform)
                .toList();

        log.debug("'{}' non-association(s) found for booking '{}'", nonAssociations.size(), bookingId);

        return OffenderNonAssociationDetails.builder()
                .offenderNo(booking.getOffender().getNomsId())
                .firstName(WordUtils.capitalizeFully(booking.getOffender().getFirstName()))
                .lastName(WordUtils.capitalizeFully(booking.getOffender().getLastName()))
                .agencyDescription(booking.getLocation().getDescription())
                .assignedLivingUnitId(Optional.ofNullable(booking.getAssignedLivingUnit()).map(AgencyInternalLocation::getLocationId).orElse(null))
                .assignedLivingUnitDescription(Optional.ofNullable(booking.getAssignedLivingUnit()).map(AgencyInternalLocation::getDescription).orElse(null))
                .nonAssociations(nonAssociations)
                .build();
    }

    @VerifyOffenderAccess
    public OffenderNonAssociationDetails retrieveByOffenderNo(final String offenderNo) {
        log.debug("Fetching non-associations for offender no '{}'", offenderNo);

        final var offender = offenderRepository.findOffenderByNomsIdWithNonAssociations(offenderNo)
            .orElseThrow(EntityNotFoundException.withMessage("Offender no %s not found.", offenderNo));

        final var nonAssociations = offender.getBookings().stream().map(OffenderBooking::getNonAssociationDetails)
            .flatMap(Collection::stream)
            .map(this::transform)
            .toList();

        log.debug("'{}' non-association(s) found for offender '{}'", nonAssociations.size(), offenderNo);

        return OffenderNonAssociationDetails.builder()
            .offenderNo(offender.getNomsId())
            .firstName(WordUtils.capitalizeFully(offender.getFirstName()))
            .lastName(WordUtils.capitalizeFully(offender.getLastName()))
            .agencyDescription(offender.getLatestBooking().orElseThrow().getLocation().getDescription())
            .assignedLivingUnitId(Optional.ofNullable(offender.getLatestBooking().orElseThrow().getAssignedLivingUnit()).map(AgencyInternalLocation::getLocationId).orElse(null))
            .assignedLivingUnitDescription(Optional.ofNullable(offender.getLatestBooking().orElseThrow().getAssignedLivingUnit()).map(AgencyInternalLocation::getDescription).orElse(null))
            .nonAssociations(nonAssociations)
            .build();
    }

    private uk.gov.justice.hmpps.prison.api.model.OffenderNonAssociationDetail transform(final OffenderNonAssociationDetail detail) {
        return uk.gov.justice.hmpps.prison.api.model.OffenderNonAssociationDetail.builder()
                .effectiveDate(detail.getEffectiveDate())
                .expiryDate(detail.getExpiryDate())
                .comments(detail.getComments())
                .authorisedBy(detail.getAuthorizedBy())
                .reasonCode(detail.getNonAssociationReason().getCode())
                .reasonDescription(detail.getNonAssociationReason().getDescription())
                .typeCode(detail.getNonAssociationType().getCode())
                .typeDescription(detail.getNonAssociationType().getDescription())
                .offenderNonAssociation(OffenderNonAssociation.builder()
                        .offenderNo(detail.getNonAssociation().getNsOffender().getNomsId())
                        .firstName(WordUtils.capitalizeFully(detail.getNonAssociation().getNsOffender().getFirstName()))
                        .lastName(WordUtils.capitalizeFully(detail.getNonAssociation().getNsOffender().getLastName()))
                        .reasonCode(Optional.ofNullable(detail.getNonAssociation().getRecipNonAssociationReason()).map(NonAssociationReason::getCode).orElse(null))
                        .reasonDescription(Optional.ofNullable(detail.getNonAssociation().getRecipNonAssociationReason()).map(NonAssociationReason::getDescription).orElse(null))
                        .agencyDescription(detail.getNonAssociation().getNsAgencyDescription().orElse(null))
                        .assignedLivingUnitDescription(detail.getNonAssociation().getNsAssignedLivingUnitDescription().orElse(null))
                        .assignedLivingUnitId(detail.getNonAssociation().getNsAssignedLivingUnitId().orElse(null))
                        .build())
                .build();
    }
}
