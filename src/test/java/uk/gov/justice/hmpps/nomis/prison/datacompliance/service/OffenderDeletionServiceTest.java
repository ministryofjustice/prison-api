package uk.gov.justice.hmpps.nomis.prison.datacompliance.service;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.nomis.prison.datacompliance.events.publishers.DataComplianceEventPusher;
import uk.gov.justice.hmpps.nomis.prison.datacompliance.events.publishers.dto.OffenderDeletionComplete;
import uk.gov.justice.hmpps.prison.repository.OffenderDeletionRepository;

import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OffenderDeletionServiceTest {

    private static final String OFFENDER_NUMBER = "A1234AA";
    private static final String OFFENDER_ID = "123";
    private static final Long REFERRAL_ID = 321L;

    @Mock
    private OffenderDeletionRepository offenderDeletionRepository;

    @Mock
    private DataComplianceEventPusher dataComplianceEventPusher;

    @Mock
    private TelemetryClient telemetryClient;

    private OffenderDeletionService service;

    @BeforeEach
    public void setUp() {
        service = new OffenderDeletionService(offenderDeletionRepository, dataComplianceEventPusher, telemetryClient);
    }

    @Test
    public void deleteOffender() {
        when(offenderDeletionRepository.deleteOffender(OFFENDER_NUMBER)).thenReturn(Set.of(OFFENDER_ID));

        service.deleteOffender(OFFENDER_NUMBER, REFERRAL_ID);

        verify(dataComplianceEventPusher).send(new OffenderDeletionComplete(OFFENDER_NUMBER, REFERRAL_ID));
        verify(telemetryClient).trackEvent("OffenderDelete", Map.of("offenderNo", OFFENDER_NUMBER, "count", "1"), null);
    }
}
