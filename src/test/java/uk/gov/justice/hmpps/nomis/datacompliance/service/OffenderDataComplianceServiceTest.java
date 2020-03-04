package uk.gov.justice.hmpps.nomis.datacompliance.service;

import com.microsoft.applicationinsights.TelemetryClient;
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

    private static final String REQUEST_ID = "123";
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
    private OffenderPendingDeletionRepository offenderPendingDeletionRepository;

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
                offenderPendingDeletionRepository,
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

        when(offenderPendingDeletionRepository
                .getOffendersDueForDeletionBetween(WINDOW_START.toLocalDate(), WINDOW_END.toLocalDate()))
                .thenReturn(List.of(
                        new OffenderPendingDeletion(OFFENDER_NUMBER_1),
                        new OffenderPendingDeletion(OFFENDER_NUMBER_2)));

        service.acceptOffendersPendingDeletionRequest(REQUEST_ID, WINDOW_START, WINDOW_END).get();

        verify(eventPusher).sendPendingDeletionEvent(expectedPendingDeletionEvent(OFFENDER_NUMBER_1));
        verify(eventPusher).sendPendingDeletionEvent(expectedPendingDeletionEvent(OFFENDER_NUMBER_2));
        verify(eventPusher).sendReferralCompleteEvent(expectedReferralCompleteEvent(REQUEST_ID));
        verifyNoMoreInteractions(eventPusher);
    }

    private OffenderPendingDeletionEvent expectedPendingDeletionEvent(final String offenderNumber) {
        return OffenderPendingDeletionEvent.builder()
                .offenderIdDisplay(offenderNumber)
                .build();
    }

    private OffenderPendingDeletionReferralCompleteEvent expectedReferralCompleteEvent(final String requestId) {
        return new OffenderPendingDeletionReferralCompleteEvent(requestId);
    }
}
