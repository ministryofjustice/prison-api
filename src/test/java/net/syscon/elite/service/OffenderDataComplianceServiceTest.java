package net.syscon.elite.service;

import com.microsoft.applicationinsights.TelemetryClient;
import net.syscon.elite.api.model.OffenderNumber;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.events.publishers.OffenderPendingDeletionEventPusher;
import net.syscon.elite.repository.OffenderDeletionRepository;
import net.syscon.elite.repository.OffenderRepository;
import net.syscon.elite.repository.jpa.model.OffenderToDelete;
import net.syscon.elite.repository.jpa.repository.DataComplianceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OffenderDataComplianceServiceTest {

    private static final LocalDateTime WINDOW_START = LocalDateTime.now();
    private static final LocalDateTime WINDOW_END = WINDOW_START.plusDays(1);

    private static final String OFFENDER_NUMBER_1 = "A1234AA";
    private static final String OFFENDER_NUMBER_2 = "B4321BB";
    private static final String OFFENDER_ID = "123";

    @Mock
    private OffenderRepository offenderRepository;

    @Mock
    private OffenderDeletionRepository offenderDeletionRepository;

    @Mock
    private DataComplianceRepository dataComplianceRepository;

    @Mock
    private TelemetryClient telemetryClient;

    @Mock
    private OffenderPendingDeletionEventPusher eventPusher;

    private OffenderDataComplianceService service;

    @BeforeEach
    public void setUp() {
        service = new OffenderDataComplianceService(
                offenderRepository,
                offenderDeletionRepository,
                dataComplianceRepository,
                telemetryClient,
                eventPusher);
    }

    @Test
    public void deleteOffender() {
        when(offenderDeletionRepository.deleteOffender(OFFENDER_NUMBER_1)).thenReturn(Set.of(OFFENDER_ID));

        service.deleteOffender(OFFENDER_NUMBER_1);

        verify(telemetryClient).trackEvent("OffenderDelete",
                Map.of("offenderNo", OFFENDER_NUMBER_1, "count", "1"), null);
    }

    @Test
    public void getOffenderNumbers() {

        var pageRequest = new PageRequest(0L, 1L);

        when(offenderRepository.listAllOffenders(pageRequest))
                .thenReturn(new Page<>(List.of(new OffenderNumber(OFFENDER_NUMBER_1)), 1L, pageRequest));

        assertThat(service.getOffenderNumbers(0L, 1L).getItems())
                .containsExactly(new OffenderNumber(OFFENDER_NUMBER_1));
    }

    @Test
    public void acceptOffendersPendingDeletion() throws Exception {

        when(dataComplianceRepository
                .getOffendersDueForDeletionBetween(WINDOW_START.toLocalDate(), WINDOW_END.toLocalDate()))
                .thenReturn(List.of(
                        new OffenderToDelete(OFFENDER_NUMBER_1),
                        new OffenderToDelete(OFFENDER_NUMBER_2)));

        service.acceptOffendersPendingDeletionRequest(WINDOW_START, WINDOW_END).get();

        verify(eventPusher).sendEvent(OFFENDER_NUMBER_1);
        verify(eventPusher).sendEvent(OFFENDER_NUMBER_2);
        verifyNoMoreInteractions(eventPusher);
    }
}
