package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.TransactionHistoryDto;
import uk.gov.justice.hmpps.prison.repository.FinanceRepository;
import uk.gov.justice.hmpps.prison.repository.OffenderTransactionHistoryRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.TransactionHistory;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OffenderTransactionHistoryServiceTest {
    @Mock
    private OffenderTransactionHistoryRepository repository;

    private OffenderTransactionHistoryService service;

    @BeforeEach
    public void setUp() {
        service = new OffenderTransactionHistoryService(repository);
    }

    @Test
    public void when_TransactionHistoryIsRequested_Then_CallRepository() {

        final String  prisonId = "123";
        final String nomisId  = "456";
        final String accountCode  = "SPENDS";
        final LocalDate fromDate = LocalDate.now().minusDays(7);
        final LocalDate toDate = LocalDate.now();

        final List<TransactionHistory> txnItem = Collections.emptyList();

        when(repository.getTransactionsHistory(prisonId, nomisId, accountCode, fromDate, toDate)).thenReturn(txnItem);

        List<TransactionHistory> histeries = service.getTransactionHistory(prisonId, nomisId, accountCode, fromDate, toDate);

        verify(repository, times(1)).getTransactionsHistory(prisonId, nomisId, accountCode, fromDate, toDate);

        assertThat(histeries).isNotNull();
        assertThat(histeries.size()).isEqualTo(0);
    }

    @Test
    public void when_TransactionHistoryIsRequested_And_OneHistoryItem_Then_ReturnHistoryWithOneItem() {

        final String prisonId = "123";
        final String nomisId  = "456";
        final String accountCode  = "SPENDS";
        final LocalDate fromDate = LocalDate.now().minusDays(7);
        final LocalDate toDate = LocalDate.now();

        final List<TransactionHistory> txnItem = Lists.newArrayList(TransactionHistory.builder().build());

        when(repository.getTransactionsHistory(prisonId, nomisId, accountCode, fromDate, toDate)).thenReturn(txnItem);

        List<TransactionHistory> histeries = service.getTransactionHistory(prisonId, nomisId, accountCode, fromDate, toDate);

        verify(repository, times(1)).getTransactionsHistory(prisonId, nomisId, accountCode, fromDate, toDate);

        assertThat(histeries).isNotNull();
        assertThat(histeries.size()).isEqualTo(1);
    }
}
