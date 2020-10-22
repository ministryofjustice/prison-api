package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.TransactionHistory;
import uk.gov.justice.hmpps.prison.api.model.TransactionHistoryItem;
import uk.gov.justice.hmpps.prison.repository.FinanceRepository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NomisApiServiceTest {
    @Mock
    private FinanceRepository financeRepository;

    private NomisApiService service;

    @BeforeEach
    public void setUp() {
        service = new NomisApiService(financeRepository);
    }

    @Test
    public void when_TransactionHistoryIsRequested_Then_CallRepository() {

        final String  prisonId = "123";
        final String nomisId  = "456";
        final String accountCode  = "SPENDS";
        final LocalDate fromDate = LocalDate.now().minusDays(7);
        final LocalDate toDate = LocalDate.now();

        final List<TransactionHistoryItem> txnItem = Collections.emptyList();

        when(financeRepository.getTransactionsHistory(prisonId, nomisId, accountCode, fromDate, toDate)).thenReturn(txnItem);

        TransactionHistory actualResult = service.getTransactionsHistory(prisonId, nomisId, accountCode, fromDate, toDate);

        verify(financeRepository, times(1)).getTransactionsHistory(prisonId, nomisId, accountCode, fromDate, toDate);

        assertThat(actualResult.items).isNotNull();
        assertThat(actualResult.items.size()).isEqualTo(0);
    }

    @Test
    public void when_TransactionHistoryIsRequested_And_OneHistoryItem_Then_ReturnHistoryWithOneItem() {

        final String prisonId = "123";
        final String nomisId  = "456";
        final String accountCode  = "SPENDS";
        final LocalDate fromDate = LocalDate.now().minusDays(7);
        final LocalDate toDate = LocalDate.now();

        final List<TransactionHistoryItem> txnItem = Lists.newArrayList(TransactionHistoryItem.builder().build());

        when(financeRepository.getTransactionsHistory(prisonId, nomisId, accountCode, fromDate, toDate)).thenReturn(txnItem);

        TransactionHistory actualResult = service.getTransactionsHistory(prisonId, nomisId, accountCode, fromDate, toDate);

        verify(financeRepository, times(1)).getTransactionsHistory(prisonId, nomisId, accountCode, fromDate, toDate);

        assertThat(actualResult.items).isNotNull();
        assertThat(actualResult.items.size()).isEqualTo(1);
    }
}
