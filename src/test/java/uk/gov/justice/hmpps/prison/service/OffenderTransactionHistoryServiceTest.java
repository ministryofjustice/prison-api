package uk.gov.justice.hmpps.prison.service;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.OffenderTransactionHistoryDto;
import uk.gov.justice.hmpps.prison.repository.OffenderTransactionRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OffenderTransactionHistoryServiceTest {

    @Mock
    private OffenderTransactionRepository repository;

    private OffenderTransactionHistoryService service;

    @BeforeEach
    public void setUp() {
        service = new OffenderTransactionHistoryService(repository);
    }

    @Test
    public void when_TransactionHistoryIsRequested_Then_CallRepository() {

        final Long  offenderId = Long.parseLong("123");
        final Optional<String> accountCode  = Optional.of("SPENDS");
        final LocalDate fromDate = LocalDate.now().minusDays(7);
        final LocalDate toDate = LocalDate.now();

        final List<OffenderTransactionHistory> txnItem = Collections.emptyList();
        when(repository.findForGivenAccountType(offenderId, accountCode.get(), fromDate, toDate)).thenReturn(txnItem);

        List<OffenderTransactionHistoryDto> histories = service.getTransactionHistory(offenderId, accountCode, fromDate, toDate);

        verify(repository, times(1)).findForGivenAccountType(offenderId, accountCode.get(), fromDate, toDate);

        assertThat(histories).isNotNull();
        assertThat(histories.size()).isEqualTo(0);
    }

    @Test
    public void when_TransactionHistoryIsRequested_And_OneHistoryItem_Then_ReturnHistoryWithOneItem() {

        final Long  offenderId = Long.parseLong("123");
        final Optional<String> accountCode  = Optional.of("SPENDS");
        final LocalDate fromDate = LocalDate.now().minusDays(7);
        final LocalDate toDate = LocalDate.now();

        final List<OffenderTransactionHistory> txnItem = Lists.newArrayList(OffenderTransactionHistory.builder().build());
        when(repository.findForGivenAccountType(offenderId, "SPENDS", fromDate, toDate)).thenReturn(txnItem);

        List<OffenderTransactionHistoryDto> histories = service.getTransactionHistory(offenderId, accountCode, fromDate, toDate);

        verify(repository, times(1)).findForGivenAccountType(offenderId, accountCode.get(), fromDate, toDate);

        assertThat(histories).isNotNull();
        assertThat(histories.size()).isEqualTo(1);
    }
}