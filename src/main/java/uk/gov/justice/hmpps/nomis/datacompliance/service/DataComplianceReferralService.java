package uk.gov.justice.hmpps.nomis.datacompliance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.syscon.prison.api.model.OffenderNumber;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.DataComplianceEventPusher;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletion.Booking;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletion.OffenderWithBookings;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletionReferralComplete;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderAliasPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderChargePendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.OffenderAliasPendingDeletionRepository;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.OffenderPendingDeletionRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableList;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DataComplianceReferralService {

    private final OffenderPendingDeletionRepository offenderPendingDeletionRepository;
    private final OffenderAliasPendingDeletionRepository offenderAliasPendingDeletionRepository;
    private final DataComplianceEventPusher dataComplianceEventPusher;

    public CompletableFuture<Void> acceptOffendersPendingDeletionRequest(final Long batchId,
                                                                         final LocalDateTime from,
                                                                         final LocalDateTime to,
                                                                         final Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> getOffendersPendingDeletion(from, to, pageable))

                .thenApply(offenders -> {
                    offenders.forEach(offenderNumber ->
                            dataComplianceEventPusher.send(
                                    generateOffenderPendingDeletionEvent(offenderNumber, batchId)));

                    return offenders;
                })

                .thenAccept(offenders -> dataComplianceEventPusher.send(
                        new OffenderPendingDeletionReferralComplete(
                                batchId, (long) offenders.getNumberOfElements(), offenders.getTotalElements())));
    }

    private OffenderPendingDeletion generateOffenderPendingDeletionEvent(final OffenderNumber offenderNumber,
                                                                         final Long batchId) {

        final var offenderAliases = offenderAliasPendingDeletionRepository
                .findOffenderAliasPendingDeletionByOffenderNumber(offenderNumber.getOffenderNumber());

        checkState(!offenderAliases.isEmpty(), "Offender not found: '%s'", offenderNumber.getOffenderNumber());

        final var rootOffenderAlias = offenderAliases.stream()
                .filter(alias -> Objects.equals(alias.getOffenderId(), alias.getRootOffenderId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        format("Cannot find root offender alias for '%s'", offenderNumber.getOffenderNumber())));

        return transform(offenderNumber, rootOffenderAlias, offenderAliases, batchId);
    }

    private Page<OffenderNumber> getOffendersPendingDeletion(final LocalDateTime from,
                                                             final LocalDateTime to,
                                                             final Pageable pageable) {
        return offenderPendingDeletionRepository
                .getOffendersDueForDeletionBetween(from.toLocalDate(), to.toLocalDate(), pageable)
                .map(this::transform);
    }

    private OffenderNumber transform(final uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderPendingDeletion entity) {

        return OffenderNumber.builder()
                .offenderNumber(entity.getOffenderNumber())
                .build();
    }

    private OffenderPendingDeletion transform(final OffenderNumber offenderNumber,
                                              final OffenderAliasPendingDeletion rootOffenderAlias,
                                              final Collection<OffenderAliasPendingDeletion> offenderAliases,
                                              final Long batchId) {
        return OffenderPendingDeletion.builder()
                .offenderIdDisplay(offenderNumber.getOffenderNumber())
                .batchId(batchId)
                .firstName(rootOffenderAlias.getFirstName())
                .middleName(rootOffenderAlias.getMiddleName())
                .lastName(rootOffenderAlias.getLastName())
                .birthDate(rootOffenderAlias.getBirthDate())
                .offenders(offenderAliases.stream()
                        .map(this::transform)
                        .collect(toUnmodifiableList()))
                .build();
    }

    private OffenderWithBookings transform(final OffenderAliasPendingDeletion alias) {
        return OffenderWithBookings.builder()
                .offenderId(alias.getOffenderId())
                .bookings(alias.getOffenderBookings().stream()
                        .map(booking -> Booking.builder()
                                .offenderBookId(booking.getBookingId())
                                .offenceCodes(booking.getOffenderCharges().stream()
                                        .map(OffenderChargePendingDeletion::getOffenceCode)
                                        .collect(toSet()))
                                .build())
                        .collect(toUnmodifiableList()))
                .build();
    }
}
