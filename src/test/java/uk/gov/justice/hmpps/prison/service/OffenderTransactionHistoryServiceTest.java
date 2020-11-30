package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.OffenderTransactionHistoryDto;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionDetails;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTransactionHistoryRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OffenderTransactionHistoryServiceTest {

    public static final String OFFENDER_NO = "A1111AA";

    @Mock
    private OffenderTransactionHistoryRepository repository;

    private OffenderTransactionHistoryService service;

    @BeforeEach
    public void setUp() {
        service = new OffenderTransactionHistoryService("GBP", repository);
    }

    @Test
    public void When_getTransactionHistory_And_AccountCodeIsGiven_Then_CallRepositoryWithAccountCode() {

        final Optional<String> accountCode = Optional.of("spends");
        final Optional<LocalDate> fromDateOpl = Optional.of(LocalDate.now().minusDays(7));
        final Optional<LocalDate> toDateOpl = Optional.of(LocalDate.now());

        final List<OffenderTransactionHistory> txnItem = Collections.emptyList();
        when(repository.findForGivenAccountType(OFFENDER_NO, "SPND", fromDateOpl.get(), toDateOpl.get())).thenReturn(txnItem);

        List<OffenderTransactionHistoryDto> histories = service.getTransactionHistory(OFFENDER_NO, accountCode, fromDateOpl, toDateOpl);

        verify(repository, times(1)).findForGivenAccountType(OFFENDER_NO, "SPND", fromDateOpl.get(), toDateOpl.get());

        assertThat(histories).isNotNull();
        assertThat(histories.size()).isEqualTo(0);
    }

    @Test
    public void When_getTransactionHistory_And_AccountCodeIsMissing_Then_CallRepositoryWithoutAccountCode() {

        final Optional<String> accountCode = Optional.empty();
        final Optional<LocalDate> fromDateOpl = Optional.of(LocalDate.now().minusDays(7));
        final Optional<LocalDate> toDateOpl = Optional.of(LocalDate.now());

        final List<OffenderTransactionHistory> txnItem = Collections.emptyList();
        when(repository.findForAllAccountTypes(OFFENDER_NO, fromDateOpl.get(), toDateOpl.get())).thenReturn(txnItem);

        List<OffenderTransactionHistoryDto> histories = service.getTransactionHistory(OFFENDER_NO, accountCode, fromDateOpl, toDateOpl);

        verify(repository, times(1)).findForAllAccountTypes(OFFENDER_NO, fromDateOpl.get(), toDateOpl.get());

        assertThat(histories).isNotNull();
        assertThat(histories.size()).isEqualTo(0);
    }


    @Test
    public void When_getTransactionHistory_And_OffenderIdIsNull_Then_ThrowException() {

        Throwable exception = assertThrows(NullPointerException.class, () -> {
            final String nomisId = null;
            final Optional<String> accountCode = Optional.of("spends");
            final Optional<LocalDate> fromDateOpl = Optional.of(LocalDate.now().minusDays(7));
            final Optional<LocalDate> toDateOpl = Optional.of(LocalDate.now());
            service.getTransactionHistory(nomisId, accountCode, fromDateOpl, toDateOpl);
        });

        assertEquals("offenderNo can't be null", exception.getMessage());
    }

    @Test
    public void When_getTransactionHistory_And_AccountCodeIsNull_Then_ThrowException() {

        Throwable exception = assertThrows(NullPointerException.class, () -> {
            final Optional<String> accountCode = null;
            final Optional<LocalDate> fromDateOpl = Optional.of(LocalDate.now().minusDays(7));
            final Optional<LocalDate> toDateOpl = Optional.of(LocalDate.now());
            service.getTransactionHistory(OFFENDER_NO, accountCode, fromDateOpl, toDateOpl);
        });

        assertEquals("accountCode optional can't be null", exception.getMessage());
    }

    @Test
    public void When_getTransactionHistory_And_FromDateIsNull_Then_ThrowException() {

        Throwable exception = assertThrows(NullPointerException.class, () -> {
            final Optional<String> accountCode = Optional.of("spends");
            final Optional<LocalDate> fromDateOpl = null;
            final Optional<LocalDate> toDateOpl = Optional.of(LocalDate.now());
            service.getTransactionHistory(OFFENDER_NO, accountCode, fromDateOpl, toDateOpl);
        });

        assertEquals("fromDate optional can't be null", exception.getMessage());
    }

    @Test
    public void When_getTransactionHistory_And_ToDateIsNull_Then_ThrowException() {

        Throwable exception = assertThrows(NullPointerException.class, () -> {
            final Optional<String> accountCode = Optional.of("spends");
            final Optional<LocalDate> fromDateOpl = Optional.of(LocalDate.now().minusDays(7));
            final Optional<LocalDate> toDateOpl = null;
            service.getTransactionHistory(OFFENDER_NO, accountCode, fromDateOpl, toDateOpl);
        });

        assertEquals("toDate optional can't be null", exception.getMessage());
    }

    @Test
    public void When_getTransactionHistory_And_ToDateIsBeforeFromDate_Then_ThrowException() {

        Throwable exception = assertThrows(IllegalStateException.class, () -> {
            final Optional<String> accountCode = Optional.of("spends");
            final Optional<LocalDate> fromDateOpl = Optional.of(LocalDate.now().minusDays(7));
            final Optional<LocalDate> toDateOpl = Optional.of(LocalDate.now().minusDays(8));
            service.getTransactionHistory(OFFENDER_NO, accountCode, fromDateOpl, toDateOpl);
        });

        assertEquals("toDate can't be before fromDate", exception.getMessage());
    }

    @Test
    public void When_getTransactionHistory_And_FromDateIsTomorrow_Then_ThrowException() {

        Throwable exception = assertThrows(IllegalStateException.class, () -> {
            final Optional<String> accountCode = Optional.of("spends");
            final Optional<LocalDate> fromDateOpl = Optional.of(LocalDate.now().plusDays(1));
            final Optional<LocalDate> toDateOpl = Optional.of(LocalDate.now().plusDays(2));
            service.getTransactionHistory(OFFENDER_NO, accountCode, fromDateOpl, toDateOpl);
        });

        assertEquals("fromDate can't be in the future", exception.getMessage());
    }

    @Test
    public void When_getTransactionHistory_And_ToDateIs2DaysInFuture_Then_ThrowException() {

        Throwable exception = assertThrows(IllegalStateException.class, () -> {
            final Optional<String> accountCode = Optional.of("spends");
            final Optional<LocalDate> fromDateOpl = Optional.of(LocalDate.now());
            final Optional<LocalDate> toDateOpl = Optional.of(LocalDate.now().plusDays(2));
            service.getTransactionHistory(OFFENDER_NO, accountCode, fromDateOpl, toDateOpl);
        });

        assertEquals("toDate can't be in the future", exception.getMessage());
    }

    @Test
    public void When_getTransactionHistory_And_DatesDefaultToNow_Then_CallRepositoryWithoutAccountCode() {

        final Optional<String> accountCode = Optional.of("spends");
        final Optional<LocalDate> fromDateOpl = Optional.of(LocalDate.now());
        final Optional<LocalDate> toDateOpl = Optional.of(LocalDate.now());

        final List<OffenderTransactionHistory> txnItem = Collections.emptyList();
        when(repository.findForAllAccountTypes(OFFENDER_NO, fromDateOpl.get(), toDateOpl.get())).thenReturn(txnItem);

        List<OffenderTransactionHistoryDto> histories = service.getTransactionHistory(OFFENDER_NO, accountCode, fromDateOpl, toDateOpl);

        verify(repository, times(1)).findForAllAccountTypes(OFFENDER_NO, fromDateOpl.get(), toDateOpl.get());

        assertThat(histories).isNotNull();
        assertThat(histories.size()).isEqualTo(0);
    }

    @Test
    public void When_getTransactionHistory_And_TypoInAccountCode_Then_ThrowException() {

        Throwable exception = assertThrows(IllegalStateException.class, () -> {
            final Optional<String> accountCode = Optional.of("spendss");
            final Optional<LocalDate> fromDateOpl = Optional.of(LocalDate.now());
            final Optional<LocalDate> toDateOpl = Optional.of(LocalDate.now());
            service.getTransactionHistory(OFFENDER_NO, accountCode, fromDateOpl, toDateOpl);
        });

        assertEquals("Unknown account-code spendss", exception.getMessage());
    }

    @Test
    public void When_getTransactionHistory_ShouldBe_SortedByEntryDateDescending() {

        final Optional<String> accountCode = Optional.empty();
        final var fromDateOpl = Optional.of(LocalDate.now());
        final var toDateOpl = Optional.of(LocalDate.now());
        final var offender = Offender.builder().nomsId(OFFENDER_NO).id(1L).build();

        when(repository.findForAllAccountTypes(OFFENDER_NO, fromDateOpl.get(), toDateOpl.get())).thenReturn(List.of(
            OffenderTransactionHistory.builder()
                .entryDate(LocalDate.now())
                .entryAmount(BigDecimal.ONE)
                .transactionEntrySequence(1L)
                .offender(offender)
                .build(),
            OffenderTransactionHistory.builder()
                .entryDate(LocalDate.now())
                .entryAmount(BigDecimal.ONE)
                .transactionEntrySequence(2L)
                .offender(offender)
                .build(),
            OffenderTransactionHistory.builder()
                .entryDate(LocalDate.now())
                .entryAmount(BigDecimal.ONE)
                .transactionEntrySequence(3L)
                .offender(offender)
                .build(),
            OffenderTransactionHistory.builder()
                .entryDate(LocalDate.now().minusDays(1))
                .entryAmount(BigDecimal.ONE)
                .transactionEntrySequence(1L)
                .offender(offender)
                .build(),
            OffenderTransactionHistory.builder()
                .entryDate(LocalDate.now().minusDays(1))
                .entryAmount(BigDecimal.ONE)
                .transactionEntrySequence(2L)
                .offender(offender)
                .build(),
            OffenderTransactionHistory.builder()
                .entryDate(LocalDate.now().minusDays(1))
                .entryAmount(BigDecimal.ONE)
                .transactionEntrySequence(3L)
                .offender(offender)
                .build(),
            OffenderTransactionHistory.builder()
                .entryDate(LocalDate.now().minusDays(2))
                .entryAmount(BigDecimal.ONE)
                .transactionEntrySequence(1L)
                .offender(offender)
                .build(),
            OffenderTransactionHistory.builder()
                .entryDate(LocalDate.now().minusDays(2))
                .entryAmount(BigDecimal.ONE)
                .transactionEntrySequence(2L)
                .offender(offender)
                .build(),
            OffenderTransactionHistory.builder()
                .entryDate(LocalDate.now().minusDays(2))
                .entryAmount(BigDecimal.ONE)
                .transactionEntrySequence(3L)
                .offender(offender)
                .build()
        ));

        final var histories = service.getTransactionHistory(OFFENDER_NO, accountCode, fromDateOpl, toDateOpl);

        verify(repository, times(1)).findForAllAccountTypes(OFFENDER_NO, fromDateOpl.get(), toDateOpl.get());

        assertThat(histories).isNotNull();
        assertThat(histories.size()).isEqualTo(9);

        /* verify */
        assertThat(histories.get(0)).isEqualTo(OffenderTransactionHistoryDto.builder()
            .entryDate(LocalDate.now())
            .penceAmount(100L)
            .transactionEntrySequence(3L)
            .offenderNo(OFFENDER_NO)
            .offenderId(offender.getId())
            .currency("GBP")
            .build());

        assertThat(histories.get(1)).isEqualTo(OffenderTransactionHistoryDto.builder()
            .entryDate(LocalDate.now())
            .penceAmount(100L)
            .transactionEntrySequence(2L)
            .offenderNo(OFFENDER_NO)
            .offenderId(offender.getId())
            .currency("GBP")
            .build());

        assertThat(histories.get(2)).isEqualTo(OffenderTransactionHistoryDto.builder()
            .entryDate(LocalDate.now())
            .penceAmount(100L)
            .transactionEntrySequence(1L)
            .offenderNo(OFFENDER_NO)
            .offenderId(offender.getId())
            .currency("GBP")
            .build());

        /* verify */
        assertThat(histories.get(3)).isEqualTo(OffenderTransactionHistoryDto.builder()
            .entryDate(LocalDate.now().minusDays(1))
            .penceAmount(100L)
            .transactionEntrySequence(3L)
            .offenderNo(OFFENDER_NO)
            .offenderId(offender.getId())
            .currency("GBP")
            .build());

        assertThat(histories.get(4)).isEqualTo(OffenderTransactionHistoryDto.builder()
            .entryDate(LocalDate.now().minusDays(1))
            .penceAmount(100L)
            .transactionEntrySequence(2L)
            .offenderNo(OFFENDER_NO)
            .offenderId(offender.getId())
            .currency("GBP")
            .build());

        assertThat(histories.get(5)).isEqualTo(OffenderTransactionHistoryDto.builder()
            .entryDate(LocalDate.now().minusDays(1))
            .penceAmount(100L)
            .transactionEntrySequence(1L)
            .offenderNo(OFFENDER_NO)
            .offenderId(offender.getId())
            .currency("GBP")
            .build());

        /* verify */
        assertThat(histories.get(6)).isEqualTo(OffenderTransactionHistoryDto.builder()
            .entryDate(LocalDate.now().minusDays(2))
            .penceAmount(100L)
            .transactionEntrySequence(3L)
            .offenderNo(OFFENDER_NO)
            .offenderId(offender.getId())
            .currency("GBP")
            .build());

        assertThat(histories.get(7)).isEqualTo(OffenderTransactionHistoryDto
            .builder()
            .entryDate(LocalDate.now().minusDays(2))
            .penceAmount(100L)
            .transactionEntrySequence(2L)
            .offenderNo(OFFENDER_NO)
            .offenderId(offender.getId())
            .currency("GBP")
            .build());

        assertThat(histories.get(8)).isEqualTo(OffenderTransactionHistoryDto
            .builder()
            .entryDate(LocalDate.now().minusDays(2))
            .penceAmount(100L)
            .transactionEntrySequence(1L)
            .offenderNo(OFFENDER_NO)
            .offenderId(offender.getId())
            .currency("GBP")
            .build());
    }

    @Test
    public void When_getTransactionHistory_ForAccountCode_Maps_Correctly() {
        when(repository.findForGivenAccountType(anyString(), anyString(), any(), any()))
            .thenReturn(List.of(offenderTransactionHistoryEntry()));

        final var accountCode = Optional.of("spends");
        final var fromDateOpl = Optional.of(LocalDate.now());
        final var toDateOpl = Optional.of(LocalDate.now());

        final var transaction = service.getTransactionHistory(OFFENDER_NO, accountCode, fromDateOpl, toDateOpl)
            .stream()
            .findFirst()
            .orElseThrow();

        assertThat(transaction.getOffenderId()).isEqualTo(3L);
        assertThat(transaction.getTransactionId()).isEqualTo(1L);
        assertThat(transaction.getTransactionEntrySequence()).isEqualTo(1L);
        assertThat(transaction.getEntryDate()).isEqualTo(LocalDate.now());
        assertThat(transaction.getTransactionType()).isEqualTo("OUT");
        assertThat(transaction.getEntryDescription()).isEqualTo("Some description");
        assertThat(transaction.getReferenceNumber()).isEqualTo("12343/1");
        assertThat(transaction.getCurrency()).isEqualTo("GBP");
        assertThat(transaction.getPenceAmount()).isEqualTo(200);
        assertThat(transaction.getAccountType()).isEqualTo("SPENDS");
        assertThat(transaction.getPostingType()).isEqualTo("DR");
        assertThat(transaction.getOffenderNo()).isEqualTo(OFFENDER_NO);
        assertThat(transaction.getAgencyId()).isEqualTo("MDI");
        assertThat(transaction.getRelatedOffenderTransactions()).isEmpty();
    }

    @Test
    public void When_getTransactionHistory_Maps_Correctly() {
        when(repository.findForAllAccountTypes(anyString(), any(), any()))
            .thenReturn(List.of(offenderTransactionHistoryEntry()));

        final var fromDateOpl = Optional.of(LocalDate.now());
        final var toDateOpl = Optional.of(LocalDate.now());

        final var transaction = service.getTransactionHistory(OFFENDER_NO, Optional.empty(), fromDateOpl, toDateOpl)
            .stream()
            .findFirst()
            .orElseThrow();

        assertThat(transaction.getOffenderId()).isEqualTo(3L);
        assertThat(transaction.getTransactionId()).isEqualTo(1L);
        assertThat(transaction.getTransactionEntrySequence()).isEqualTo(1L);
        assertThat(transaction.getEntryDate()).isEqualTo(LocalDate.now());
        assertThat(transaction.getTransactionType()).isEqualTo("OUT");
        assertThat(transaction.getEntryDescription()).isEqualTo("Some description");
        assertThat(transaction.getReferenceNumber()).isEqualTo("12343/1");
        assertThat(transaction.getCurrency()).isEqualTo("GBP");
        assertThat(transaction.getPenceAmount()).isEqualTo(200);
        assertThat(transaction.getAccountType()).isEqualTo("SPENDS");
        assertThat(transaction.getPostingType()).isEqualTo("DR");
        assertThat(transaction.getOffenderNo()).isEqualTo(OFFENDER_NO);
        assertThat(transaction.getAgencyId()).isEqualTo("MDI");
        assertThat(transaction.getRelatedOffenderTransactions()).isEmpty();
    }

    @Test
    public void When_getTransactionHistoryRelatedTransactionDetails_Correctly() {
        when(repository.findForAllAccountTypes(anyString(), any(), any()))
            .thenReturn(List.of(
                withRelatedTransactionDetails(offenderTransactionHistoryEntry())
            ));

        final var fromDateOpl = Optional.of(LocalDate.now());
        final var toDateOpl = Optional.of(LocalDate.now());

        final var relatedTransactionDetail = service.getTransactionHistory(OFFENDER_NO, Optional.empty(), fromDateOpl, toDateOpl)
            .stream()
            .flatMap(transaction -> transaction.getRelatedOffenderTransactions().stream())
            .findFirst()
            .orElseThrow();

        assertThat(relatedTransactionDetail.getId()).isEqualTo(1);
        assertThat(relatedTransactionDetail.getPayAmount()).isEqualTo(BigDecimal.valueOf(1.0));
        assertThat(relatedTransactionDetail.getBonusPay()).isEqualTo(BigDecimal.valueOf(3.0));
        assertThat(relatedTransactionDetail.getPieceWork()).isEqualTo(BigDecimal.valueOf(2.0));
        assertThat(relatedTransactionDetail.getCalendarDate()).isEqualTo(LocalDate.now());
        assertThat(relatedTransactionDetail.getEventId()).isEqualTo(1);
        assertThat(relatedTransactionDetail.getPayTypeCode()).isEqualTo("UNEMPLOYED");
        assertThat(relatedTransactionDetail.getTransactionEntrySequence()).isEqualTo(1);
        assertThat(relatedTransactionDetail.getTransactionId()).isEqualTo(1);
    }

    @Test
    public void When_getTransactionHistoryRelatedTransactionDetails_ForAccountCode_Correctly() {
        when(repository.findForGivenAccountType(anyString(), anyString(), any(), any()))
            .thenReturn(List.of(
                withRelatedTransactionDetails(offenderTransactionHistoryEntry())
            ));

        final var fromDateOpl = Optional.of(LocalDate.now());
        final var toDateOpl = Optional.of(LocalDate.now());

        final var relatedTransactionDetail = service.getTransactionHistory(OFFENDER_NO, Optional.of("SPENDS"), fromDateOpl, toDateOpl)
            .stream()
            .flatMap(transaction -> transaction.getRelatedOffenderTransactions().stream())
            .findFirst()
            .orElseThrow();

        assertThat(relatedTransactionDetail.getId()).isEqualTo(1);
        assertThat(relatedTransactionDetail.getPayAmount()).isEqualTo(BigDecimal.valueOf(1.0));
        assertThat(relatedTransactionDetail.getBonusPay()).isEqualTo(BigDecimal.valueOf(3.0));
        assertThat(relatedTransactionDetail.getPieceWork()).isEqualTo(BigDecimal.valueOf(2.0));
        assertThat(relatedTransactionDetail.getCalendarDate()).isEqualTo(LocalDate.now());
        assertThat(relatedTransactionDetail.getEventId()).isEqualTo(1);
        assertThat(relatedTransactionDetail.getPayTypeCode()).isEqualTo("UNEMPLOYED");
        assertThat(relatedTransactionDetail.getTransactionEntrySequence()).isEqualTo(1);
        assertThat(relatedTransactionDetail.getTransactionId()).isEqualTo(1);
    }

    private OffenderTransactionHistory offenderTransactionHistoryEntry() {
        return OffenderTransactionHistory
            .builder()
            .entryAmount(BigDecimal.valueOf(2.0))
            .postingType("DR")
            .entryDate(LocalDate.now())
            .offender(Offender.builder().nomsId(OFFENDER_NO).rootOffenderId(2L).id(3L).build())
            .entryDescription("Some description")
            .referenceNumber("12343/1")
            .transactionEntrySequence(1L)
            .transactionId(1L)
            .transactionType("OUT")
            .accountType("SPENDS")
            .agencyId("MDI")
            .build();
    }

    private OffenderTransactionHistory withRelatedTransactionDetails(final OffenderTransactionHistory offenderTransactionHistory) {
        return offenderTransactionHistory.toBuilder()
            .relatedTransactionDetails(List.of(OffenderTransactionDetails.builder()
                .id(1L)
                .calendarDate(LocalDate.now())
                .bonusPay(BigDecimal.valueOf(3.00))
                .pieceWork(BigDecimal.valueOf(2.00))
                .payAmount(BigDecimal.valueOf(1.00))
                .eventId(1)
                .payTypeCode("UNEMPLOYED")
                .transactionEntrySequence(1L)
                .transactionId(1L)
                .build()))
            .build();

    }
}