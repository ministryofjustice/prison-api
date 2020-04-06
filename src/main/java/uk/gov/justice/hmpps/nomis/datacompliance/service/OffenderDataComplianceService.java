package uk.gov.justice.hmpps.nomis.datacompliance.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.OffenderNumber;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.OffenderDeletionRepository;
import net.syscon.elite.repository.OffenderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderPendingDeletionEvent;
import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderPendingDeletionEvent.Booking;
import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderPendingDeletionEvent.OffenderWithBookings;
import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderPendingDeletionReferralCompleteEvent;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.OffenderDeletionEventPusher;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderAliasPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.OffenderAliasPendingDeletionRepository;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.OffenderPendingDeletionRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;

@Service
@RequiredArgsConstructor
@Slf4j
public class OffenderDataComplianceService {

    private final OffenderRepository offenderRepository;
    private final OffenderDeletionRepository offenderDeletionRepository;
    private final OffenderPendingDeletionRepository offenderPendingDeletionRepository;
    private final OffenderAliasPendingDeletionRepository offenderAliasPendingDeletionRepository;
    private final TelemetryClient telemetryClient;
    private final OffenderDeletionEventPusher offenderDeletionEventPusher;

    @Transactional
    public void deleteOffender(final String offenderNumber) {

        final var offenderIds = offenderDeletionRepository.deleteOffender(offenderNumber);

        telemetryClient.trackEvent("OffenderDelete",
                Map.of("offenderNo", offenderNumber, "count", String.valueOf(offenderIds.size())), null);
    }

    public Page<OffenderNumber> getOffenderNumbers(long offset, long limit) {
        return offenderRepository.listAllOffenders(new PageRequest(offset, limit));
    }

    public CompletableFuture<Void> acceptOffendersPendingDeletionRequest(final String requestId,
                                                                         final LocalDateTime from,
                                                                         final LocalDateTime to) {
        return CompletableFuture.supplyAsync(() -> getOffendersPendingDeletion(from, to))

                .thenAccept(offenders -> offenders.forEach(offenderNumber ->
                        offenderDeletionEventPusher.sendPendingDeletionEvent(
                                generateOffenderPendingDeletionEvent(offenderNumber))))

                .thenRun(() -> offenderDeletionEventPusher.sendReferralCompleteEvent(
                        new OffenderPendingDeletionReferralCompleteEvent(requestId)));
    }

    private OffenderPendingDeletionEvent generateOffenderPendingDeletionEvent(final OffenderNumber offenderNumber) {

        final var offenderAliases = offenderAliasPendingDeletionRepository
                .findOffenderAliasPendingDeletionByOffenderNumber(offenderNumber.getOffenderNumber());

        checkState(!offenderAliases.isEmpty(), "Offender not found: '%s'", offenderNumber.getOffenderNumber());

        final var rootOffenderAlias = offenderAliases.stream()
                .filter(alias -> Objects.equals(alias.getOffenderId(), alias.getRootOffenderId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        format("Cannot find root offender alias for '%s'", offenderNumber.getOffenderNumber())));

        return transform(offenderNumber, rootOffenderAlias, offenderAliases);
    }

    private List<OffenderNumber> getOffendersPendingDeletion(final LocalDateTime from,
                                                             final LocalDateTime to) {
        return offenderPendingDeletionRepository
                .getOffendersDueForDeletionBetween(from.toLocalDate(), to.toLocalDate())
                .stream()
                .map(this::transform)
                .collect(toList());
    }

    private OffenderNumber transform(final OffenderPendingDeletion entity) {

        return OffenderNumber.builder()
                .offenderNumber(entity.getOffenderNumber())
                .build();
    }

    private OffenderPendingDeletionEvent transform(final OffenderNumber offenderNumber,
                                                   final OffenderAliasPendingDeletion rootOffenderAlias,
                                                   final Collection<OffenderAliasPendingDeletion> offenderAliases) {
        return OffenderPendingDeletionEvent.builder()
                .offenderIdDisplay(offenderNumber.getOffenderNumber())
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
