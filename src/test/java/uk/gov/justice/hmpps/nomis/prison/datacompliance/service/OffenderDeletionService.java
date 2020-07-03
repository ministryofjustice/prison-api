package uk.gov.justice.hmpps.nomis.prison.datacompliance.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.nomis.prison.datacompliance.events.publishers.DataComplianceEventPusher;
import uk.gov.justice.hmpps.nomis.prison.datacompliance.events.publishers.dto.OffenderDeletionComplete;
import uk.gov.justice.hmpps.prison.repository.OffenderDeletionRepository;

import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OffenderDeletionService {

    private final OffenderDeletionRepository offenderDeletionRepository;
    private final DataComplianceEventPusher dataComplianceEventPusher;
    private final TelemetryClient telemetryClient;

    public void deleteOffender(final String offenderNumber, final Long referralId) {

        // TODO GDPR-70 Conduct final double-check that the record hasn't been updated before deleting

        final var offenderIds = offenderDeletionRepository.deleteOffender(offenderNumber);

        dataComplianceEventPusher.send(new OffenderDeletionComplete(offenderNumber, referralId));

        telemetryClient.trackEvent("OffenderDelete",
                Map.of("offenderNo", offenderNumber, "count", String.valueOf(offenderIds.size())), null);
    }
}
