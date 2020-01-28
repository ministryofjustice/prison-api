package net.syscon.elite.service;

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

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OffenderDataComplianceService {

    private final OffenderRepository offenderRepository;
    private final OffenderDeletionRepository offenderDeletionRepository;
    private final TelemetryClient telemetryClient;

    @Transactional
    public void deleteOffender(final String offenderNumber) {

        final var offenderIds = offenderDeletionRepository.deleteOffender(offenderNumber);

        telemetryClient.trackEvent("OffenderDelete",
                Map.of("offenderNo", offenderNumber, "count", String.valueOf(offenderIds.size())), null);
    }

    public Page<OffenderNumber> getOffenderNumbers(long offset, long limit) {
        return offenderRepository.listAllOffenders(new PageRequest(offset, limit));
    }
}
