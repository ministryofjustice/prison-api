package uk.gov.justice.hmpps.nomis.datacompliance.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.OffenderNumber;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderPendingDeletionEvent;
import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderPendingDeletionReferralCompleteEvent;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.OffenderPendingDeletionEventPusher;
import net.syscon.elite.repository.OffenderDeletionRepository;
import net.syscon.elite.repository.OffenderRepository;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.model.OffenderPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.repository.jpa.repository.OffenderPendingDeletionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Slf4j
public class OffenderDataComplianceService {

    private final OffenderRepository offenderRepository;
    private final OffenderDeletionRepository offenderDeletionRepository;
    private final OffenderPendingDeletionRepository offenderPendingDeletionRepository;
    private final TelemetryClient telemetryClient;
    private final OffenderPendingDeletionEventPusher offenderPendingDeletionEventPusher;

    @Transactional
    public void deleteOffender(final String offenderNumber) {

        final var offenderIds = offenderDeletionRepository.deleteOffender(offenderNumber);

        telemetryClient.trackEvent("OffenderDelete",
                Map.of("offenderNo", offenderNumber, "count", String.valueOf(offenderIds.size())), null);
    }

    public Page<OffenderNumber> getOffenderNumbers(long offset, long limit) {
        return offenderRepository.listAllOffenders(new PageRequest(offset, limit));
    }

    public Future<Void> acceptOffendersPendingDeletionRequest(final String requestId,
                                                              final LocalDateTime from,
                                                              final LocalDateTime to) {
        return CompletableFuture.supplyAsync(() -> getOffendersPendingDeletion(from, to))

                .thenAccept(offenders -> offenders.forEach(offenderNumber ->
                        offenderPendingDeletionEventPusher.sendPendingDeletionEvent(
                                generateOffenderPendingDeletionEvent(offenderNumber))))

                .thenRun(() -> offenderPendingDeletionEventPusher.sendReferralCompleteEvent(
                        new OffenderPendingDeletionReferralCompleteEvent(requestId)));
    }

    private OffenderPendingDeletionEvent generateOffenderPendingDeletionEvent(final OffenderNumber offenderNumber) {

        // TODO GDPR-88 populate the rest of the event using request from db

        return OffenderPendingDeletionEvent.builder()
                .offenderIdDisplay(offenderNumber.getOffenderNumber())
                .build();
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
}
