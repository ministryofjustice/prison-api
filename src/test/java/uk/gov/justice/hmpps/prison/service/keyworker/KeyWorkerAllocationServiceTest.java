package uk.gov.justice.hmpps.prison.service.keyworker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.OffenderKeyWorker;
import uk.gov.justice.hmpps.prison.repository.KeyWorkerAllocationRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
public class KeyWorkerAllocationServiceTest {
    @Mock
    private KeyWorkerAllocationRepository repository;

    private KeyWorkerAllocationService keyWorkerAllocationService;

    @BeforeEach
    public void init() {
        keyWorkerAllocationService = new KeyWorkerAllocationService(repository,
            1000);
    }

    @Test
    public void getAllocationHistoryByOffenderNos_SortsByOffenderNoReversedThenNewestAssignedDate() {

        final var offenderNos = List.of("off1", "off2");
        final var offender_A1234AA_NewestAllocation = OffenderKeyWorker.builder()
            .offenderNo("A1234AA")
            .assigned(LocalDateTime.now().minusDays(2))
            .build();
        final var offender_A1234AA_OlderAllocation = OffenderKeyWorker.builder()
            .offenderNo("A1234AA")
            .assigned(LocalDateTime.now().minusDays(3))
            .build();
        final var offender_B1234BB = OffenderKeyWorker.builder()
            .offenderNo("B1234BB")
            .assigned(LocalDateTime.now())
            .build();
        final var allocationHistory = List.of(offender_A1234AA_OlderAllocation, offender_B1234BB, offender_A1234AA_NewestAllocation);

        when(repository.getAllocationHistoryByOffenderNos(any())).thenReturn(allocationHistory);

        final var orderedAllocationHistory = keyWorkerAllocationService.getAllocationHistoryByOffenderNos(offenderNos);

        assertThat(orderedAllocationHistory).containsExactly(offender_B1234BB, offender_A1234AA_NewestAllocation, offender_A1234AA_OlderAllocation);
    }

    @Test
    public void getAllocationHistoryByOffenderNos_HandlesEmptyHistory() {

        when(repository.getAllocationHistoryByOffenderNos(any())).thenReturn(List.of());

        final var allocationHistory = keyWorkerAllocationService.getAllocationHistoryByOffenderNos(List.of("off1", "off2", "off3"));

        assertThat(allocationHistory).hasSize(0);
    }

    @Test
    public void getAllocationHistoryByOffenderNos_CallsKeyworkerRepoInBatches() {

        final var keyWorkerAllocationServiceWithSmallBatchSize = new KeyWorkerAllocationService(repository,
            2);

        keyWorkerAllocationServiceWithSmallBatchSize.getAllocationHistoryByOffenderNos(List.of("off1", "off2", "off3"));

        verify(repository, times(1)).getAllocationHistoryByOffenderNos(eq(List.of("off1", "off2")));
        verify(repository, times(1)).getAllocationHistoryByOffenderNos(eq(List.of("off3")));
    }
}
