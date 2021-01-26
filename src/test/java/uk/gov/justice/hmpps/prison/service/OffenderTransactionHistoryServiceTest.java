package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionDetails;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTransactionHistoryRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    @Nested
    public class CorrectParameters {
        @Test
        public void testGetTransactionHistoryThrowException_offenderIdIsNull() {

            Throwable exception = assertThrows(NullPointerException.class, () -> {
                final String nomisId = null;
                final var accountCode = "spends";
                final var fromDate = LocalDate.now().minusDays(7);
                final var toDate = LocalDate.now();
                service.getTransactionHistory(nomisId, accountCode, fromDate, toDate, null);
            });

            assertEquals("offenderNo can't be null", exception.getMessage());
        }

        @Test
        public void testGetTransactionHistoryThrowException_toDateIsBeforeFromDate() {

            Throwable exception = assertThrows(IllegalStateException.class, () -> {
                final var accountCode = "spends";
                final var fromDate = LocalDate.now().minusDays(7);
                final var toDate = LocalDate.now().minusDays(8);
                service.getTransactionHistory(OFFENDER_NO, accountCode, fromDate, toDate, null);
            });

            assertEquals("toDate can't be before fromDate", exception.getMessage());
        }

        @Test
        public void testGetTransactionHistory_ThrowException_FromDateIsTomorrow() {

            Throwable exception = assertThrows(IllegalStateException.class, () -> {
                final var accountCode = "spends";
                final var fromDate = LocalDate.now().plusDays(1);
                final var toDate = LocalDate.now().plusDays(2);
                service.getTransactionHistory(OFFENDER_NO, accountCode, fromDate, toDate, null);
            });

            assertEquals("fromDate can't be in the future", exception.getMessage());
        }

        @Test
        public void testGetTransactionHistoryThrowException_ToDateIs2DaysInFuture() {

            Throwable exception = assertThrows(IllegalStateException.class, () -> {
                final var accountCode ="spends";
                final var fromDate = LocalDate.now();
                final var toDate = LocalDate.now().plusDays(2);
                service.getTransactionHistory(OFFENDER_NO, accountCode, fromDate, toDate, null);
            });

            assertEquals("toDate can't be in the future", exception.getMessage());
        }

        @Test
        public void testGetTransactionHistoryThrowException_TypoInAccountCode() {

            Throwable exception = assertThrows(IllegalStateException.class, () -> {
                final var accountCode = "spendss";
                final LocalDate fromDate = LocalDate.now();
                final LocalDate toDate = LocalDate.now();
                service.getTransactionHistory(OFFENDER_NO, accountCode, fromDate, toDate, null);
            });

            assertEquals("Unknown account-code spendss", exception.getMessage());
        }

        @Test
        public void testGetTransactionHistory_CallsRepositoryWithCorrectParameters() {
            final var histories =
                service.getTransactionHistory(OFFENDER_NO, null, null, null, null);

            verify(repository, times(1)).findByOffender_NomsId(OFFENDER_NO);

            assertThat(histories).isNotNull();
            assertThat(histories.size()).isEqualTo(0);
        }
    }

    @Nested
    public class Sorting {
        @Test
        public void testSortedByEntryDateDescending() {

            final var offender = Offender.builder().nomsId(OFFENDER_NO).id(1L).build();
            final var transaction =  OffenderTransactionHistory.builder().offender(offender).postingType("CR").build();

            when(repository.findByOffender_NomsId(anyString())).thenReturn(List.of(
                transaction.toBuilder()
                    .createDatetime(LocalDateTime.now())
                    .entryDate(LocalDate.now())
                    .entryAmount(BigDecimal.ONE)
                    .transactionEntrySequence(1L)
                    .transactionId(3L)
                    .build(),
                transaction.toBuilder()
                    .createDatetime(LocalDateTime.now())
                    .entryDate(LocalDate.now())
                    .entryAmount(BigDecimal.ONE)
                    .transactionEntrySequence(2L)
                    .transactionId(2L)
                    .build(),
                transaction.toBuilder()
                    .createDatetime(LocalDateTime.now())
                    .entryDate(LocalDate.now())
                    .entryAmount(BigDecimal.ONE)
                    .transactionEntrySequence(3L)
                    .transactionId(1L)
                    .build(),
                transaction.toBuilder()
                    .createDatetime(LocalDateTime.now().minusDays(1))
                    .entryDate(LocalDate.now().minusDays(1))
                    .entryAmount(BigDecimal.ONE)
                    .transactionEntrySequence(1L)
                    .transactionId(6L)
                    .build(),
                transaction.toBuilder()
                    .createDatetime(LocalDateTime.now().minusDays(1))
                    .entryDate(LocalDate.now().minusDays(1))
                    .entryAmount(BigDecimal.ONE)
                    .transactionEntrySequence(2L)
                    .transactionId(5L)
                    .build(),
                transaction.toBuilder()
                    .createDatetime(LocalDateTime.now().minusDays(1))
                    .entryDate(LocalDate.now().minusDays(1))
                    .entryAmount(BigDecimal.ONE)
                    .transactionEntrySequence(3L)
                    .transactionId(4L)
                    .build(),
                transaction.toBuilder()
                    .createDatetime(LocalDateTime.now().minusDays(2))
                    .entryDate(LocalDate.now().minusDays(2))
                    .entryAmount(BigDecimal.ONE)
                    .transactionEntrySequence(1L)
                    .transactionId(9L)
                    .build(),
                transaction.toBuilder()
                    .createDatetime(LocalDateTime.now().minusDays(2))
                    .entryDate(LocalDate.now().minusDays(2))
                    .entryAmount(BigDecimal.ONE)
                    .transactionEntrySequence(2L)
                    .transactionId(8L)
                    .build(),
                transaction.toBuilder()
                    .createDatetime(LocalDateTime.now().minusDays(2))
                    .entryDate(LocalDate.now().minusDays(2))
                    .entryAmount(BigDecimal.ONE)
                    .transactionEntrySequence(3L)
                    .transactionId(7L)
                    .build()
            ));

            final var histories =
                service.getTransactionHistory(OFFENDER_NO, null, null, null, null);

            assertThat(histories).isNotNull();
            assertThat(histories.size()).isEqualTo(9);


            // Date: now, Seq: 3, 2, 1
            assertThat(histories.get(0).getTransactionId()).isEqualTo(1L);
            assertThat(histories.get(0).getEntryDate()).isEqualTo(LocalDate.now());
            assertThat(histories.get(0).getTransactionEntrySequence()).isEqualTo(3L);

            assertThat(histories.get(1).getTransactionId()).isEqualTo(2L);
            assertThat(histories.get(1).getEntryDate()).isEqualTo(LocalDate.now());
            assertThat(histories.get(1).getTransactionEntrySequence()).isEqualTo(2L);

            assertThat(histories.get(2).getTransactionId()).isEqualTo(3L);
            assertThat(histories.get(2).getEntryDate()).isEqualTo(LocalDate.now());
            assertThat(histories.get(2).getTransactionEntrySequence()).isEqualTo(1L);

            // Date: now - 1 day, Seq: 3, 2, 1
            assertThat(histories.get(3).getTransactionId()).isEqualTo(4L);
            assertThat(histories.get(3).getEntryDate()).isEqualTo(LocalDate.now().minusDays(1));
            assertThat(histories.get(3).getTransactionEntrySequence()).isEqualTo(3L);

            assertThat(histories.get(4).getTransactionId()).isEqualTo(5L);
            assertThat(histories.get(4).getEntryDate()).isEqualTo(LocalDate.now().minusDays(1));
            assertThat(histories.get(4).getTransactionEntrySequence()).isEqualTo(2L);

            assertThat(histories.get(5).getTransactionId()).isEqualTo(6L);
            assertThat(histories.get(5).getEntryDate()).isEqualTo(LocalDate.now().minusDays(1));
            assertThat(histories.get(5).getTransactionEntrySequence()).isEqualTo(1L);

            // Date: now - 2 days, Seq: 3, 2, 1
            assertThat(histories.get(6).getTransactionId()).isEqualTo(7L);
            assertThat(histories.get(6).getEntryDate()).isEqualTo(LocalDate.now().minusDays(2));
            assertThat(histories.get(6).getTransactionEntrySequence()).isEqualTo(3L);

            assertThat(histories.get(7).getTransactionId()).isEqualTo(8L);
            assertThat(histories.get(7).getEntryDate()).isEqualTo(LocalDate.now().minusDays(2));
            assertThat(histories.get(7).getTransactionEntrySequence()).isEqualTo(2L);

            assertThat(histories.get(8).getTransactionId()).isEqualTo(9L);
            assertThat(histories.get(8).getEntryDate()).isEqualTo(LocalDate.now().minusDays(2));
            assertThat(histories.get(8).getTransactionEntrySequence()).isEqualTo(1L);

        }
    }

    @Nested
    public class Mapping {
        @Test
        public void testGetTransactionHistory_MapsCorrectly() {
            when(repository.findByOffender_NomsId(anyString()))
                .thenReturn(List.of(offenderTransactionHistoryEntry()));

            final var fromDate = LocalDate.now();
            final var toDate = LocalDate.now();

            final var transaction = service.getTransactionHistory(OFFENDER_NO, null, fromDate, toDate, null)
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
        public void testGetTransactionHistory_MapsRelatedTransactionDetailsCorrectly() {
            when(repository.findByOffender_NomsId(anyString()))
                .thenReturn(List.of(
                    withRelatedTransactionDetails(offenderTransactionHistoryEntry())
                ));

            final var fromDate = LocalDate.now();
            final var toDateOpl =LocalDate.now();

            final var relatedTransactionDetail = service.getTransactionHistory(OFFENDER_NO, null, fromDate, toDateOpl, null)
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

    }

    @Nested
    public class Filtering {
        final Offender offender = Offender.builder().nomsId(OFFENDER_NO).rootOffenderId(2L).id(3L).build();
        final OffenderTransactionHistory baseOffenderTransactionHistory = OffenderTransactionHistory.builder().offender(offender).entryDescription("Some description").agencyId("MDI").build();
        final OffenderTransactionHistory out = baseOffenderTransactionHistory.toBuilder().postingType("DR").accountType("SPND").transactionType("OUT").build();
        final OffenderTransactionHistory in = baseOffenderTransactionHistory.toBuilder().postingType("CR").accountType("REG").transactionType("IN").build();

        @Test
        public void testFilterByDateRange() {
            final var allTransactions = List.of(
                in.toBuilder()
                    .entryDate(LocalDate.of(2000,10,10))
                    .createDatetime(LocalDateTime.of(2000,10,10,0,0))
                    .entryAmount(BigDecimal.valueOf(2))
                    .build(),
                in.toBuilder()
                    .entryDate(LocalDate.of(2000,11,10))
                    .createDatetime(LocalDateTime.of(2000,11,10,0,0))
                    .entryAmount(BigDecimal.valueOf(3))
                    .build(),
                out.toBuilder()
                    .entryDate(LocalDate.of(2001,11,10))
                    .createDatetime(LocalDateTime.of(2001,11,10,0,0))
                    .entryAmount(BigDecimal.valueOf(5))
                    .build()
            );

            when(repository.findByOffender_NomsId(anyString())).thenReturn(allTransactions);

            final var transactions = service.getTransactionHistory(OFFENDER_NO, null,
                LocalDate.of(2000,10,11), null,null);

            assertThat(transactions.size()).isEqualTo(2);
            assertThat(transactions.get(0).getEntryDate()).isEqualTo(LocalDate.of(2001,11,10));
            assertThat(transactions.get(1).getEntryDate()).isEqualTo(LocalDate.of(2000,11,10));
        }

        @Test
        public void testFilterByAccountType() {
            final var allTransactions = List.of(
                in.toBuilder()
                    .createDatetime(LocalDateTime.of(2000,10,10,0,0))
                    .entryAmount(BigDecimal.valueOf(2))
                    .build(),
                in.toBuilder()
                    .createDatetime(LocalDateTime.of(2000,11,10,0,0))
                    .entryAmount(BigDecimal.valueOf(3))
                    .build(),
                out.toBuilder()
                    .createDatetime(LocalDateTime.of(2001,11,10,0,0))
                    .entryAmount(BigDecimal.valueOf(5))
                    .build()
            );

            when(repository.findByOffender_NomsId(anyString())).thenReturn(allTransactions);

            final var transactions = service
                .getTransactionHistory(OFFENDER_NO, "SPENDS", null, null,null);

            assertThat(transactions.size()).isEqualTo(1);
            assertThat(transactions.get(0).getAccountType()).isEqualTo("SPND");
        }

        @Test
        public void testFilterTransactionType() {
            final var allTransactions = List.of(
                in.toBuilder()
                    .createDatetime(LocalDateTime.of(2000,10,10,0,0))
                    .entryAmount(BigDecimal.valueOf(2))
                    .build(),
                in.toBuilder()
                    .createDatetime(LocalDateTime.of(2000,11,10,0,0))
                    .entryAmount(BigDecimal.valueOf(3))
                    .build(),
                out.toBuilder()
                    .createDatetime(LocalDateTime.of(2001,11,10,0,0))
                    .entryAmount(BigDecimal.valueOf(5))
                    .transactionType("OUT")
                    .build()
            );

            when(repository.findByOffender_NomsId(any())).thenReturn(allTransactions);

            final var transactions = service
                .getTransactionHistory(OFFENDER_NO, null, null, null,"OUT");

            assertThat(transactions.size()).isEqualTo(1);
            assertThat(transactions.get(0).getPenceAmount()).isEqualTo(500L);
        }
    }

    @Nested
    public class RunningBalance{
        @Test
        public void testGetTransactionHistory_CalculateRunningBalance() {
            final var offender = Offender.builder().nomsId(OFFENDER_NO).rootOffenderId(2L).id(3L).build();

            final var baseOffenderTransactionHistory = OffenderTransactionHistory.builder().offender(offender).entryDescription("Some description").agencyId("MDI").transactionEntrySequence(1L).build();
            final var out = baseOffenderTransactionHistory.toBuilder().postingType("DR").accountType("SPENDS").build();
            final var in = baseOffenderTransactionHistory.toBuilder().postingType("CR").accountType("REG").build();

            final var allTransactions = List.of(
                in.toBuilder()
                    .createDatetime(LocalDateTime.of(2000,10,10, 0,0,0))
                    .entryAmount(BigDecimal.valueOf(2))
                    .build(),
                in.toBuilder()
                    .createDatetime(LocalDateTime.of(2000,11,10, 0,0,0))
                    .entryAmount(BigDecimal.valueOf(3))
                    .build(),
                out.toBuilder()
                    .createDatetime(LocalDateTime.of(2001,12,10,0,0,0))
                    .entryAmount(BigDecimal.valueOf(5))
                    .build()
            );

            when(repository.findByOffender_NomsId(any())).thenReturn(allTransactions);

            final var transactions =
                service.getTransactionHistory(OFFENDER_NO, null, null, null,null);

            assertThat(transactions.size()).isEqualTo(3);
            assertThat(transactions.get(0).getCurrentBalance()).isEqualTo(0L);
            assertThat(transactions.get(1).getCurrentBalance()).isEqualTo(500L);
            assertThat(transactions.get(2).getCurrentBalance()).isEqualTo(200L);
        }
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