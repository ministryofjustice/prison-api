package uk.gov.justice.hmpps.nomis.datacompliance.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.repository.OffenderDeletionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.DataComplianceEventPusher;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderDeletionComplete;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OffenderDeletionService {

    private final OffenderDeletionRepository offenderDeletionRepository;
    private final DataComplianceEventPusher dataComplianceEventPusher;
    private final TelemetryClient telemetryClient;

    @Transactional
    public void deleteOffender(final String offenderNumber, final Long referralId) {

        // TODO GDPR-70 Conduct final double-check that the record hasn't been updated before deleting

        final var offenderIds = offenderDeletionRepository.deleteOffender(offenderNumber);

        dataComplianceEventPusher.send(new OffenderDeletionComplete(offenderNumber, referralId));

        telemetryClient.trackEvent("OffenderDelete",
                Map.of("offenderNo", offenderNumber, "count", String.valueOf(offenderIds.size())), null);
    }
}
