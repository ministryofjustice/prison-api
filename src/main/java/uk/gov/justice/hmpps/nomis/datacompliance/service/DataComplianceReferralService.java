package uk.gov.justice.hmpps.nomis.datacompliance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.OffenderNumber;
import org.springframework.stereotype.Service;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletion.Booking;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletion.OffenderWithBookings;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletionReferralComplete;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.DataComplianceEventPusher;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderAliasPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.OffenderAliasPendingDeletionRepository;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.OffenderPendingDeletionRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataComplianceReferralService {

    private final OffenderPendingDeletionRepository offenderPendingDeletionRepository;
    private final OffenderAliasPendingDeletionRepository offenderAliasPendingDeletionRepository;
    private final DataComplianceEventPusher dataComplianceEventPusher;

    public CompletableFuture<Void> acceptOffendersPendingDeletionRequest(final Long batchId,
                                                                         final LocalDateTime from,
                                                                         final LocalDateTime to) {
        return CompletableFuture.supplyAsync(() -> getOffendersPendingDeletion(from, to))

                .thenAccept(offenders -> offenders.forEach(offenderNumber ->
                        dataComplianceEventPusher.sendPendingDeletionEvent(
                                generateOffenderPendingDeletionEvent(offenderNumber, batchId))))

                .thenRun(() -> dataComplianceEventPusher.sendReferralCompleteEvent(
                        new OffenderPendingDeletionReferralComplete(batchId)));
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

    private List<OffenderNumber> getOffendersPendingDeletion(final LocalDateTime from,
                                                             final LocalDateTime to) {
        return offenderPendingDeletionRepository
                .getOffendersDueForDeletionBetween(from.toLocalDate(), to.toLocalDate())
                .stream()
                .map(this::transform)
                .collect(toList());
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
                        .map(booking -> new Booking(booking.getBookingId()))
                        .collect(toUnmodifiableList()))
                .build();
    }
}
