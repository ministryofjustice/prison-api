package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.OffenderTransactionHistoryDto;
import uk.gov.justice.hmpps.prison.repository.OffenderTransactionHistoryRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
    public void When_getTransactionHistory_And_AccountCodeIsGiven_Then_CallRepositoryWithAccountCode() {

        final Long  offenderId = Long.parseLong("123");
        final Optional<String> accountCode  = Optional.of("spends");
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
    public void When_getTransactionHistory_And_AccountCodeIsMissing_Then_CallRepositoryWithoutAccountCode() {

        final Long  offenderId = Long.parseLong("123");
        final Optional<String> accountCode  = Optional.empty();
        final LocalDate fromDate = LocalDate.now().minusDays(7);
        final LocalDate toDate = LocalDate.now();

        final List<OffenderTransactionHistory> txnItem = Collections.emptyList();
        when(repository.findForAllAccountTypes(offenderId, fromDate, toDate)).thenReturn(txnItem);

        List<OffenderTransactionHistoryDto> histories = service.getTransactionHistory(offenderId, accountCode, fromDate, toDate);

        verify(repository, times(1)).findForAllAccountTypes(offenderId, fromDate, toDate);

        assertThat(histories).isNotNull();
        assertThat(histories.size()).isEqualTo(0);
    }


    @Test()
    public void When_getTransactionHistory_And_OffenderIdIsNull_Then_ThrowException() {

        Throwable exception = assertThrows(NullPointerException.class, () -> {
            final Long  offenderId = null;
            final Optional<String> accountCode = Optional.of("spends");
            final LocalDate fromDate = LocalDate.now().minusDays(7);
            final LocalDate toDate = LocalDate.now();
            service.getTransactionHistory(offenderId, accountCode, fromDate, toDate);
        });

        assertEquals("offender-id can't be null", exception.getMessage());
    }

    @Test()
    public void When_getTransactionHistory_And_AccountCodeIsNull_Then_ThrowException() {

        Throwable exception = assertThrows(NullPointerException.class, () -> {
            final Long  offenderId = Long.parseLong("123");;
            final Optional<String> accountCode  = null;
            final LocalDate fromDate = LocalDate.now().minusDays(7);
            final LocalDate toDate = LocalDate.now();
            service.getTransactionHistory(offenderId, accountCode, fromDate, toDate);
        });

        assertEquals("accountCode optional can't be null", exception.getMessage());
    }

    @Test()
    public void When_getTransactionHistory_And_FromDateIsNull_Then_ThrowException() {

        Throwable exception = assertThrows(NullPointerException.class, () -> {
            final Long  offenderId = Long.parseLong("123");;
            final Optional<String> accountCode = Optional.of("spends");
            final LocalDate fromDate = null;
            final LocalDate toDate = LocalDate.now();
            service.getTransactionHistory(offenderId, accountCode, fromDate, toDate);
        });

        assertEquals("fromDate can't be null", exception.getMessage());
    }

    @Test()
    public void When_getTransactionHistory_And_ToDateIsNull_Then_ThrowException() {

        Throwable exception = assertThrows(NullPointerException.class, () -> {
            final Long  offenderId = Long.parseLong("123");;
            final Optional<String> accountCode = Optional.of("spends");
            final LocalDate fromDate = LocalDate.now().minusDays(7);
            final LocalDate toDate = null;
            service.getTransactionHistory(offenderId, accountCode, fromDate, toDate);
        });

        assertEquals("toDate can't be null", exception.getMessage());
    }

    @Test()
    public void When_getTransactionHistory_And_ToDateIsBeforeFromDate_Then_ThrowException() {

        Throwable exception = assertThrows(IllegalStateException.class, () -> {
            final Long  offenderId = Long.parseLong("123");;
            final Optional<String> accountCode = Optional.of("spends");
            final LocalDate fromDate = LocalDate.now().minusDays(7);
            final LocalDate toDate = LocalDate.now().minusDays(8);
            service.getTransactionHistory(offenderId, accountCode, fromDate, toDate);
        });

        assertEquals("toDate can't be before fromDate", exception.getMessage());
    }

    @Test()
    public void When_getTransactionHistory_And_FromDateIsTomorrow_Then_ThrowException() {

        Throwable exception = assertThrows(IllegalStateException.class, () -> {
            final Long  offenderId = Long.parseLong("123");;
            final Optional<String> accountCode = Optional.of("spends");
            final LocalDate fromDate = LocalDate.now().plusDays(1);
            final LocalDate toDate = LocalDate.now().plusDays(2);
            service.getTransactionHistory(offenderId, accountCode, fromDate, toDate);
        });

        assertEquals("fromDate can't be in the future", exception.getMessage());
    }

    @Test()
    public void When_getTransactionHistory_And_ToDateIs2DaysInFuture_Then_ThrowException() {

        Throwable exception = assertThrows(IllegalStateException.class, () -> {
            final Long  offenderId = Long.parseLong("123");;
            final Optional<String> accountCode = Optional.of("spends");
            final LocalDate fromDate = LocalDate.now();
            final LocalDate toDate = LocalDate.now().plusDays(2);
            service.getTransactionHistory(offenderId, accountCode, fromDate, toDate);
        });

        assertEquals("toDate can't be in the future", exception.getMessage());
    }

    @Test
    public void When_getTransactionHistory_And_DatesDefaultToNow_Then_CallRepositoryWithoutAccountCode() {

        final Long  offenderId = Long.parseLong("123");
        final Optional<String> accountCode  = Optional.of("spends");
        final LocalDate fromDate = LocalDate.now();
        final LocalDate toDate = LocalDate.now();

        final List<OffenderTransactionHistory> txnItem = Collections.emptyList();
        when(repository.findForAllAccountTypes(offenderId, fromDate, toDate)).thenReturn(txnItem);

        List<OffenderTransactionHistoryDto> histories = service.getTransactionHistory(offenderId, accountCode, fromDate, toDate);

        verify(repository, times(1)).findForAllAccountTypes(offenderId, fromDate, toDate);

        assertThat(histories).isNotNull();
        assertThat(histories.size()).isEqualTo(0);
    }
}