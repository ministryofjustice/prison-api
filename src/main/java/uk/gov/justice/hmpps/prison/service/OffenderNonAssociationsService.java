package uk.gov.justice.hmpps.prison.service;

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
@Validated
@Slf4j
public class OffenderNonAssociationsService {

    private final OffenderNonAssociationDetailRepository repository;

    public OffenderNonAssociationsService(final OffenderNonAssociationDetailRepository repository) {
        this.repository = repository;
    }

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
                .effectiveDate(detail.getEffectiveDate())
                .expiryDate(detail.getExpiryDate())
                .comments(detail.getComments())
                .authorisedBy(detail.getAuthorizedBy())
                .reasonCode(detail.getNonAssociationReason().getCode())
                .reasonDescription(detail.getNonAssociationReason().getDescription())
                .typeCode(detail.getNonAssociationType().getCode())
                .typeDescription(detail.getNonAssociationType().getDescription())
                .offenderNonAssociation(OffenderNonAssociation.builder()
                        .offenderNomsId(detail.getNonAssociation().getNsOffender().getNomsId())
                        .reasonCode(detail.getNonAssociation().getNonAssociationReason().getCode())
                        .reasonDescription(detail.getNonAssociation().getNonAssociationReason().getDescription())
                        .build())
                .build();
    }
}
