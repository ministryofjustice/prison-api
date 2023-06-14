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
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;

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

    @VerifyBookingAccess
    public OffenderNonAssociationDetails retrieve(final Long bookingId) {
        log.debug("Fetching non-associations for booking id '{}'", bookingId);

        final var booking = bookingRepository.findById(bookingId).orElseThrow(EntityNotFoundException.withMessage("Offender booking with id %d not found.", bookingId));

        final var nonAssociations = offenderNonAssociationDetailRepository.findAllByOffenderBooking_BookingIdOrderByEffectiveDateAsc(bookingId);

        log.debug("'{}' non-association(s) found for booking '{}'", nonAssociations.size(), bookingId);

        return getOffenderNonAssociationDetails(booking, nonAssociations);
    }

    @VerifyOffenderAccess
    public OffenderNonAssociationDetails retrieveByOffenderNo(final String offenderNo) {
        log.debug("Fetching non-associations for offender no '{}'", offenderNo);

        final List<OffenderNonAssociationDetail> nonAssociations
            = offenderNonAssociationDetailRepository.findAllByOffenderBooking_Offender_NomsIdOrderByEffectiveDateAsc(offenderNo);

        log.debug("'{}' non-association(s) found for offender '{}'", nonAssociations.size(), offenderNo);

        final var latestBooking = bookingRepository.findWithDetailsByOffenderNomsIdAndBookingSequence(offenderNo, 1)
            .orElseThrow(EntityNotFoundException.withMessage("Offender no %s not found.", offenderNo));

        return getOffenderNonAssociationDetails(latestBooking, nonAssociations);
    }

    private OffenderNonAssociationDetails getOffenderNonAssociationDetails(OffenderBooking booking, List<OffenderNonAssociationDetail> nonAssociations) {
        final Offender offender = booking.getOffender();
        return OffenderNonAssociationDetails.builder()
            .offenderNo(offender.getNomsId())
            .firstName(WordUtils.capitalizeFully(offender.getFirstName()))
            .lastName(WordUtils.capitalizeFully(offender.getLastName()))
            .agencyDescription(booking.getLocation().getDescription())
            .assignedLivingUnitId(Optional.ofNullable(booking.getAssignedLivingUnit()).map(AgencyInternalLocation::getLocationId).orElse(null))
            .assignedLivingUnitDescription(Optional.ofNullable(booking.getAssignedLivingUnit()).map(AgencyInternalLocation::getDescription).orElse(null))
            .nonAssociations(nonAssociations.stream().map(this::transform).toList())
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
                        .offenderNo(detail.getNsOffender().getNomsId())
                        .firstName(WordUtils.capitalizeFully(detail.getNsOffender().getFirstName()))
                        .lastName(WordUtils.capitalizeFully(detail.getNsOffender().getLastName()))
                        .reasonCode(Optional.ofNullable(detail.getNonAssociation().getRecipNonAssociationReason()).map(NonAssociationReason::getCode).orElse(null))
                        .reasonDescription(Optional.ofNullable(detail.getNonAssociation().getRecipNonAssociationReason()).map(NonAssociationReason::getDescription).orElse(null))
                        .agencyDescription(detail.getNonAssociation().getNsAgencyDescription().orElse(null))
                        .assignedLivingUnitDescription(detail.getNonAssociation().getNsAssignedLivingUnitDescription().orElse(null))
                        .assignedLivingUnitId(detail.getNonAssociation().getNsAssignedLivingUnitId().orElse(null))
                        .build())
                .build();
    }
}
