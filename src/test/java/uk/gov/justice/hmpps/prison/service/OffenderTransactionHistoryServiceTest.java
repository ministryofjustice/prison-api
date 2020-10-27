package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.OffenderTransactionHistoryDto;
import uk.gov.justice.hmpps.prison.repository.OffenderTransactionHistoryRepository;
import uk.gov.justice.hmpps.prison.service.transformers.OffenderTransactionHistoryTransformer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;
import java.util.Collections;
import java.util.Arrays;

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
        service = new OffenderTransactionHistoryService("GBP", repository);
    }

    @Test
    public void When_getTransactionHistory_And_AccountCodeIsGiven_Then_CallRepositoryWithAccountCode() {

        final Long  offenderId = Long.parseLong("123");
        final Optional<String> accountCode  = Optional.of("spends");
        final Optional<LocalDate> fromDateOpl = Optional.of(LocalDate.now().minusDays(7));
        final Optional<LocalDate> toDateOpl = Optional.of(LocalDate.now());

        final List<uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory> txnItem = Collections.emptyList();
        when(repository.findForGivenAccountType(offenderId, "SPND", fromDateOpl.get(), toDateOpl.get())).thenReturn(txnItem);

        List<OffenderTransactionHistoryDto> histories = service.getTransactionHistory(offenderId, accountCode, fromDateOpl, toDateOpl);

        verify(repository, times(1)).findForGivenAccountType(offenderId, "SPND", fromDateOpl.get(), toDateOpl.get());

        assertThat(histories).isNotNull();
        assertThat(histories.size()).isEqualTo(0);
    }

    @Test
    public void When_getTransactionHistory_And_AccountCodeIsMissing_Then_CallRepositoryWithoutAccountCode() {

        final Long  offenderId = Long.parseLong("123");
        final Optional<String> accountCode  = Optional.empty();
        final Optional<LocalDate> fromDateOpl = Optional.of(LocalDate.now().minusDays(7));
        final Optional<LocalDate> toDateOpl = Optional.of(LocalDate.now());

        final List<uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory> txnItem = Collections.emptyList();
        when(repository.findForAllAccountTypes(offenderId, fromDateOpl.get(), toDateOpl.get())).thenReturn(txnItem);

        List<OffenderTransactionHistoryDto> histories = service.getTransactionHistory(offenderId, accountCode, fromDateOpl, toDateOpl);

        verify(repository, times(1)).findForAllAccountTypes(offenderId, fromDateOpl.get(), toDateOpl.get());

        assertThat(histories).isNotNull();
        assertThat(histories.size()).isEqualTo(0);
    }


    @Test()
    public void When_getTransactionHistory_And_OffenderIdIsNull_Then_ThrowException() {

        Throwable exception = assertThrows(NullPointerException.class, () -> {
            final Long  offenderId = null;
            final Optional<String> accountCode = Optional.of("spends");
            final Optional<LocalDate> fromDateOpl = Optional.of(LocalDate.now().minusDays(7));
            final Optional<LocalDate> toDateOpl = Optional.of(LocalDate.now());
            service.getTransactionHistory(offenderId, accountCode, fromDateOpl, toDateOpl);
        });

        assertEquals("offender-id can't be null", exception.getMessage());
    }

    @Test()
    public void When_getTransactionHistory_And_AccountCodeIsNull_Then_ThrowException() {

        Throwable exception = assertThrows(NullPointerException.class, () -> {
            final Long  offenderId = Long.parseLong("123");;
            final Optional<String> accountCode  = null;
            final Optional<LocalDate> fromDateOpl = Optional.of(LocalDate.now().minusDays(7));
            final Optional<LocalDate> toDateOpl = Optional.of(LocalDate.now());
            service.getTransactionHistory(offenderId, accountCode, fromDateOpl, toDateOpl);
        });

        assertEquals("accountCode optional can't be null", exception.getMessage());
    }

    @Test()
    public void When_getTransactionHistory_And_FromDateIsNull_Then_ThrowException() {

        Throwable exception = assertThrows(NullPointerException.class, () -> {
            final Long  offenderId = Long.parseLong("123");;
            final Optional<String> accountCode = Optional.of("spends");
            final Optional<LocalDate> fromDateOpl = null;
            final Optional<LocalDate> toDateOpl = Optional.of(LocalDate.now());
            service.getTransactionHistory(offenderId, accountCode, fromDateOpl, toDateOpl);
        });

        assertEquals("fromDate optional can't be null", exception.getMessage());
    }

    @Test()
    public void When_getTransactionHistory_And_ToDateIsNull_Then_ThrowException() {

        Throwable exception = assertThrows(NullPointerException.class, () -> {
            final Long  offenderId = Long.parseLong("123");;
            final Optional<String> accountCode = Optional.of("spends");
            final Optional<LocalDate> fromDateOpl = Optional.of(LocalDate.now().minusDays(7));
            final Optional<LocalDate> toDateOpl = null;
            service.getTransactionHistory(offenderId, accountCode, fromDateOpl, toDateOpl);
        });

        assertEquals("toDate optional can't be null", exception.getMessage());
    }

    @Test()
    public void When_getTransactionHistory_And_ToDateIsBeforeFromDate_Then_ThrowException() {

        Throwable exception = assertThrows(IllegalStateException.class, () -> {
            final Long  offenderId = Long.parseLong("123");;
            final Optional<String> accountCode = Optional.of("spends");
            final Optional<LocalDate> fromDateOpl = Optional.of(LocalDate.now().minusDays(7));
            final Optional<LocalDate> toDateOpl = Optional.of(LocalDate.now().minusDays(8));
            service.getTransactionHistory(offenderId, accountCode, fromDateOpl, toDateOpl);
        });

        assertEquals("toDate can't be before fromDate", exception.getMessage());
    }

    @Test()
    public void When_getTransactionHistory_And_FromDateIsTomorrow_Then_ThrowException() {

        Throwable exception = assertThrows(IllegalStateException.class, () -> {
            final Long  offenderId = Long.parseLong("123");;
            final Optional<String> accountCode = Optional.of("spends");
            final Optional<LocalDate> fromDateOpl = Optional.of(LocalDate.now().plusDays(1));
            final Optional<LocalDate> toDateOpl = Optional.of(LocalDate.now().plusDays(2));
            service.getTransactionHistory(offenderId, accountCode, fromDateOpl, toDateOpl);
        });

        assertEquals("fromDate can't be in the future", exception.getMessage());
    }

    @Test()
    public void When_getTransactionHistory_And_ToDateIs2DaysInFuture_Then_ThrowException() {

        Throwable exception = assertThrows(IllegalStateException.class, () -> {
            final Long  offenderId = Long.parseLong("123");;
            final Optional<String> accountCode = Optional.of("spends");
            final Optional<LocalDate> fromDateOpl = Optional.of(LocalDate.now());
            final Optional<LocalDate> toDateOpl = Optional.of(LocalDate.now().plusDays(2));
            service.getTransactionHistory(offenderId, accountCode, fromDateOpl, toDateOpl);
        });

        assertEquals("toDate can't be in the future", exception.getMessage());
    }

    @Test
    public void When_getTransactionHistory_And_DatesDefaultToNow_Then_CallRepositoryWithoutAccountCode() {

        final Long  offenderId = Long.parseLong("123");
        final Optional<String> accountCode  = Optional.of("spends");
        final Optional<LocalDate> fromDateOpl = Optional.of(LocalDate.now());
        final Optional<LocalDate> toDateOpl = Optional.of(LocalDate.now());

        final List<uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory> txnItem = Collections.emptyList();
        when(repository.findForAllAccountTypes(offenderId, fromDateOpl.get(), toDateOpl.get())).thenReturn(txnItem);

        List<OffenderTransactionHistoryDto> histories = service.getTransactionHistory(offenderId, accountCode, fromDateOpl, toDateOpl);

        verify(repository, times(1)).findForAllAccountTypes(offenderId, fromDateOpl.get(), toDateOpl.get());

        assertThat(histories).isNotNull();
        assertThat(histories.size()).isEqualTo(0);
    }

    @Test
    public void When_getTransactionHistory_And_TypoInAccountCode_Then_ThrowException() {

        Throwable exception = assertThrows(IllegalStateException.class, () -> {
            final Long  offenderId = Long.parseLong("123");;
            final Optional<String> accountCode = Optional.of("spendss");
            final Optional<LocalDate> fromDateOpl = Optional.of(LocalDate.now());
            final Optional<LocalDate> toDateOpl = Optional.of(LocalDate.now());
            service.getTransactionHistory(offenderId, accountCode, fromDateOpl, toDateOpl);
        });

        assertEquals("Unknown account-code spendss", exception.getMessage());
    }

    @Test
    public void When_getTransactionHistory_ShouldBe_SortedByEntryDateDescending() {

        final Long  offenderId = Long.parseLong("123");
        final Optional<String> accountCode  = Optional.empty();
        final Optional<LocalDate> fromDateOpl = Optional.of(LocalDate.now());
        final Optional<LocalDate> toDateOpl = Optional.of(LocalDate.now());

        var firstSeq3 = uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory.builder()
                .entryDate(LocalDate.now().minusDays(2))
                .entryAmount(BigDecimal.ONE)
                .transactionEntrySequence(3L)
                .build();
        var firstSeq2 = uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory.builder()
                .entryDate(LocalDate.now().minusDays(2))
                .entryAmount(BigDecimal.ONE)
                .transactionEntrySequence(2L)
                .build();
        var firstSeq1 = uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory.builder()
                .entryDate(LocalDate.now().minusDays(2))
                .entryAmount(BigDecimal.ONE)
                .transactionEntrySequence(1L)
                .build();

        var secondSeq2 = uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory.builder()
                .entryDate(LocalDate.now().minusDays(1))
                .entryAmount(BigDecimal.ONE)
                .transactionEntrySequence(2L)
                .build();
        var secondSeq3 = uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory.builder()
                .entryDate(LocalDate.now().minusDays(1))
                .entryAmount(BigDecimal.ONE)
                .transactionEntrySequence(3L)
                .build();
        var secondSeq1 = uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory.builder()
                .entryDate(LocalDate.now().minusDays(1))
                .entryAmount(BigDecimal.ONE)
                .transactionEntrySequence(1L)
                .build();

        var thirdSeq1 = uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory.builder()
                .entryDate(LocalDate.now())
                .entryAmount(BigDecimal.ONE)
                .transactionEntrySequence(1L)
                .build();
        var thirdSeq3 = uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory.builder()
                .entryDate(LocalDate.now())
                .entryAmount(BigDecimal.ONE)
                .transactionEntrySequence(3L)
                .build();
        var thirdSeq2 = uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory.builder()
                .entryDate(LocalDate.now())
                .entryAmount(BigDecimal.ONE)
                .transactionEntrySequence(2L)
                .build();

        var firstDtoSeq3 = OffenderTransactionHistoryTransformer.transform(firstSeq3);
        var firstDtoSeq2 = OffenderTransactionHistoryTransformer.transform(firstSeq2);
        var firstDtoSeq1 = OffenderTransactionHistoryTransformer.transform(firstSeq1);

        var secondDtoSeq2 = OffenderTransactionHistoryTransformer.transform(secondSeq2);
        var secondDtoSeq3 = OffenderTransactionHistoryTransformer.transform(secondSeq3);
        var secondDtoSeq1 = OffenderTransactionHistoryTransformer.transform(secondSeq1);

        var thirdDto = OffenderTransactionHistoryTransformer.transform(thirdSeq1);
        var thirdDtoSeq3 = OffenderTransactionHistoryTransformer.transform(thirdSeq3);
        var thirdDtoSeq2 = OffenderTransactionHistoryTransformer.transform(thirdSeq2);

        final List<uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory> txnItem = Arrays.asList(secondSeq2, firstSeq3, secondSeq3, secondSeq1, firstSeq2, thirdSeq1, thirdSeq3, firstSeq1, thirdSeq2);

        when(repository.findForAllAccountTypes(offenderId, fromDateOpl.get(), toDateOpl.get())).thenReturn(txnItem);

        List<OffenderTransactionHistoryDto> histories = service.getTransactionHistory(offenderId, accountCode, fromDateOpl, toDateOpl);

        verify(repository, times(1)).findForAllAccountTypes(offenderId, fromDateOpl.get(), toDateOpl.get());

        assertThat(histories).isNotNull();
        assertThat(histories.size()).isEqualTo(9);

        /* verify */
        assertThat(histories.get(0)).isEqualTo(thirdDto);
        assertThat(histories.get(1)).isEqualTo(thirdDtoSeq2);
        assertThat(histories.get(2)).isEqualTo(thirdDtoSeq3);

        /* verify */
        assertThat(histories.get(3)).isEqualTo(secondDtoSeq1);
        assertThat(histories.get(4)).isEqualTo(secondDtoSeq2);
        assertThat(histories.get(5)).isEqualTo(secondDtoSeq3);

        /* verify */
        assertThat(histories.get(6)).isEqualTo(firstDtoSeq1);
        assertThat(histories.get(7)).isEqualTo(firstDtoSeq2);
        assertThat(histories.get(8)).isEqualTo(firstDtoSeq3);
    }
}