package uk.gov.justice.hmpps.nomis.datacompliance.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.repository.OffenderDeletionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderDeletionCompleteEvent;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.OffenderDeletionEventPusher;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OffenderDeletionService {

    private final OffenderDeletionRepository offenderDeletionRepository;
    private final OffenderDeletionEventPusher offenderDeletionEventPusher;
    private final TelemetryClient telemetryClient;

    @Transactional
    public void deleteOffender(final String offenderNumber, final Long referralId) {

        // TODO GDPR-70 Conduct final double-check that the record hasn't been updated before deleting

        final var offenderIds = offenderDeletionRepository.deleteOffender(offenderNumber);

        offenderDeletionEventPusher.sendDeletionCompleteEvent(new OffenderDeletionCompleteEvent(offenderNumber, referralId));

        telemetryClient.trackEvent("OffenderDelete",
                Map.of("offenderNo", offenderNumber, "count", String.valueOf(offenderIds.size())), null);
    }
}
