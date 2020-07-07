package uk.gov.justice.hmpps.prison.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.OffenderNonAssociation;
import uk.gov.justice.hmpps.prison.api.model.OffenderNonAssociationDetails;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderNonAssociationDetail;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderNonAssociationDetailRepository;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;

import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
@Validated
@Slf4j
public class OffenderNonAssociationsService {

    private final OffenderNonAssociationDetailRepository repository;

    @VerifyBookingAccess
    public OffenderNonAssociationDetails retrieve(final long bookingId) {
        log.debug("Fetching non-associations for booking id '{}'", bookingId);

        final var nonAssociations = repository.findAllByOffenderBooking_BookingId(bookingId)
                .stream()
                .map(this::transform)
                .collect(Collectors.toList());

        log.debug("'{}' non-associations found for booking '{}'", nonAssociations.size(), bookingId);

        return new OffenderNonAssociationDetails(nonAssociations);
    }

    private uk.gov.justice.hmpps.prison.api.model.OffenderNonAssociationDetail transform(final OffenderNonAssociationDetail detail) {
        return uk.gov.justice.hmpps.prison.api.model.OffenderNonAssociationDetail.builder()
                .offenderNomsId(detail.getOffender().getNomsId())
                .firstName(detail.getOffender().getFirstName())
                .lastName(detail.getOffender().getLastName())
                .effectiveDate(detail.getEffectiveDate())
                .expiryDate(detail.getExpiryDate())
                .comments(detail.getComments())
                .authorisedBy(detail.getAuthorizedBy())
                .reasonCode(detail.getNonAssociationReason().getCode())
                .reasonDescription(detail.getNonAssociationReason().getDescription())
                .typeCode(detail.getNonAssociationType().getCode())
                .typeDescription(detail.getNonAssociationType().getDescription())
                .agencyDescription(detail.getAgencyDescription().orElse(null))
                .assignedLivingUnitDescription(detail.getAssignedLivingUnitDescription().orElse(null))
                .offenderNonAssociation(OffenderNonAssociation.builder()
                        .offenderNomsId(detail.getNonAssociation().getNsOffender().getNomsId())
                        .firstName(detail.getNonAssociation().getNsOffender().getFirstName())
                        .lastName(detail.getNonAssociation().getNsOffender().getLastName())
                        .reasonCode(detail.getNonAssociation().getRecipNonAssociationReason().getCode())
                        .reasonDescription(detail.getNonAssociation().getRecipNonAssociationReason().getDescription())
                        .agencyDescription(detail.getNonAssociation().getAgencyDescription().orElse(null))
                        .assignedLivingUnitDescription(detail.getNonAssociation().getAssignedLivingUnitDescription().orElse(null))
                        .build())
                .build();
    }
}
