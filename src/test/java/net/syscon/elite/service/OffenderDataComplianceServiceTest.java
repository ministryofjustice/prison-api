package net.syscon.elite.service;

import com.microsoft.applicationinsights.TelemetryClient;
import net.syscon.elite.api.model.OffenderNumber;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.OffenderDeletionRepository;
import net.syscon.elite.repository.OffenderRepository;
import net.syscon.elite.service.OffenderDataComplianceService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OffenderDataComplianceServiceTest {

    private static final String OFFENDER_NUMBER = "A1234AA";
    private static final String OFFENDER_ID = "123";

    @Mock
    private OffenderRepository offenderRepository;

    @Mock
    private OffenderDeletionRepository offenderDeletionRepository;

    @Mock
    private TelemetryClient telemetryClient;

    private OffenderDataComplianceService service;

    @Before
    public void setUp() {
        service = new OffenderDataComplianceService(offenderRepository, offenderDeletionRepository, telemetryClient);
    }

    @Test
    public void deleteOffender() {
        when(offenderDeletionRepository.deleteOffender(OFFENDER_NUMBER)).thenReturn(Set.of(OFFENDER_ID));

        service.deleteOffender(OFFENDER_NUMBER);

        verify(telemetryClient).trackEvent("OffenderDelete",
                Map.of("offenderNo", OFFENDER_NUMBER, "count", "1"), null);
    }

    @Test
    public void getOffenderNumbers() {

        var pageRequest = new PageRequest(0L, 1L);

        when(offenderRepository.listAllOffenders(pageRequest))
                .thenReturn(new Page<>(List.of(new OffenderNumber(OFFENDER_NUMBER)), 1L, pageRequest));

        assertThat(service.getOffenderNumbers(0L, 1L).getItems())
                .containsExactly(new OffenderNumber(OFFENDER_NUMBER));
    }
}