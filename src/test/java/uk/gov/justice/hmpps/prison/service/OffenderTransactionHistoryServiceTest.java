package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourseActivity;
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCourseAttendance;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionDetails;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory;
import uk.gov.justice.hmpps.prison.repository.jpa.model.PaymentType;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTransactionHistoryRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    final Offender OFFENDER = Offender.builder().nomsId(OFFENDER_NO).rootOffenderId(2L).id(3L).build();
    final OffenderTransactionHistory OFFENDER_TRANSACTION = OffenderTransactionHistory.builder().offender(OFFENDER).entryDescription("Some description").entryAmount(BigDecimal.valueOf(1.0)).agencyId("MDI").build();
    final OffenderTransactionHistory TRANSACTION_OUT = OFFENDER_TRANSACTION.toBuilder().postingType("DR").accountType("SPND").transactionType("OUT").build();
    final OffenderTransactionHistory TRANSACTION_IN = OFFENDER_TRANSACTION.toBuilder().postingType("CR").accountType("REG").transactionType("IN").build();

    @Nested
    public class CorrectParameters {
        @Test
        public void testGetTransactionHistoryThrowException_offenderIdIsNull() {
            assertThatThrownBy(() -> {
                final String nomisId = null;
                final var accountCode = "spends";
                final var fromDate = LocalDate.now().minusDays(7);
                final var toDate = LocalDate.now();
                service.getTransactionHistory(nomisId, accountCode, fromDate, toDate, null);
            })
                .isInstanceOf(NullPointerException.class)
                .hasMessage("offenderNo can't be null");
        }

        @Test
        public void testGetTransactionHistoryThrowException_toDateIsBeforeFromDate() {
            assertThatThrownBy(() -> {
                final var accountCode = "spends";
                final var fromDate = LocalDate.now().minusDays(7);
                final var toDate = LocalDate.now().minusDays(8);
                service.getTransactionHistory(OFFENDER_NO, accountCode, fromDate, toDate, null);
            })
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("toDate can't be before fromDate");
        }

        @Test
        public void testGetTransactionHistory_ThrowException_FromDateIsTomorrow() {
            assertThatThrownBy(() -> {
                final var accountCode = "spends";
                final var fromDate = LocalDate.now().plusDays(1);
                final var toDate = LocalDate.now().plusDays(2);
                service.getTransactionHistory(OFFENDER_NO, accountCode, fromDate, toDate, null);
            })
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("fromDate can't be in the future");
        }

        @Test
        public void testGetTransactionHistoryThrowException_ToDateIs2DaysInFuture() {
            assertThatThrownBy(() -> {
                final var accountCode = "spends";
                final var fromDate = LocalDate.now();
                final var toDate = LocalDate.now().plusDays(2);
                service.getTransactionHistory(OFFENDER_NO, accountCode, fromDate, toDate, null);
            })
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("toDate can't be in the future");
        }

        @Test
        public void testGetTransactionHistoryThrowException_TypoInAccountCode() {
            assertThatThrownBy(() -> {
                final var accountCode = "spendss";
                final LocalDate fromDate = LocalDate.now();
                final LocalDate toDate = LocalDate.now();
                service.getTransactionHistory(OFFENDER_NO, accountCode, fromDate, toDate, null);
            })
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Unknown account-code spendss");
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
            final var transaction = OffenderTransactionHistory.builder().offender(OFFENDER).postingType("CR").agencyId("LEI").build();

            final var event = OffenderCourseAttendance
                .builder()
                .eventId(1L)
                .courseActivity(CourseActivity.builder().description("Wing cleaner").build())
                .build();

            transaction.setRelatedTransactionDetails(List.of(OffenderTransactionDetails.builder()
                .id(1L)
                .event(event)
                .calendarDate(LocalDate.now())
                .bonusPay(BigDecimal.valueOf(3.00))
                .pieceWork(BigDecimal.valueOf(2.00))
                .payAmount(BigDecimal.valueOf(1.00))
                .transactionEntrySequence(1L)
                .transactionId(1L)
                .build()));

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
            assertThat(histories.get(0)).extracting("transactionId", "entryDate", "transactionEntrySequence")
                .containsExactlyInAnyOrder(1L, LocalDate.now(), 3L);

            assertThat(histories.get(1)).extracting("transactionId", "entryDate", "transactionEntrySequence")
                .containsExactlyInAnyOrder(2L, LocalDate.now(), 2L);

            assertThat(histories.get(2)).extracting("transactionId", "entryDate", "transactionEntrySequence")
                .containsExactlyInAnyOrder(3L, LocalDate.now(), 1L);

            // Date: now - 1 day, Seq: 3, 2, 1
            assertThat(histories.get(3)).extracting("transactionId", "entryDate", "transactionEntrySequence")
                .containsExactlyInAnyOrder(4L, LocalDate.now().minusDays(1), 3L);

            assertThat(histories.get(3)).extracting("transactionId", "entryDate", "transactionEntrySequence")
                .containsExactlyInAnyOrder(4L, LocalDate.now().minusDays(1), 3L);

            assertThat(histories.get(4)).extracting("transactionId", "entryDate", "transactionEntrySequence")
                .containsExactlyInAnyOrder(5L, LocalDate.now().minusDays(1), 2L);

            assertThat(histories.get(5)).extracting("transactionId", "entryDate", "transactionEntrySequence")
                .containsExactlyInAnyOrder(6L, LocalDate.now().minusDays(1), 1L);

            // Date: now - 2 days, Seq: 3, 2, 1
            assertThat(histories.get(6)).extracting("transactionId", "entryDate", "transactionEntrySequence")
                .containsExactlyInAnyOrder(7L, LocalDate.now().minusDays(2), 3L);

            assertThat(histories.get(7)).extracting("transactionId", "entryDate", "transactionEntrySequence")
                .containsExactlyInAnyOrder(8L, LocalDate.now().minusDays(2), 2L);

            assertThat(histories.get(8)).extracting("transactionId", "entryDate", "transactionEntrySequence")
                .containsExactlyInAnyOrder(9L, LocalDate.now().minusDays(2), 1L);
        }
    }

    @Nested
    public class Mapping {
        @Test
        public void testMapsTransactions() {
            final var createDateTime = LocalDateTime.now();

            when(repository.findByOffender_NomsId(anyString()))
                .thenReturn(List.of(
                    TRANSACTION_OUT.toBuilder()
                        .transactionId(1L)
                        .referenceNumber("12343/1")
                        .transactionEntrySequence(1L)
                        .entryDate(LocalDate.now())
                        .createDatetime(createDateTime)
                        .build()
                ));

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
            assertThat(transaction.getPenceAmount()).isEqualTo(100L);
            assertThat(transaction.getAccountType()).isEqualTo("SPND");
            assertThat(transaction.getPostingType()).isEqualTo("DR");
            assertThat(transaction.getOffenderNo()).isEqualTo(OFFENDER_NO);
            assertThat(transaction.getAgencyId()).isEqualTo("MDI");
            assertThat(transaction.getCreateDateTime()).isEqualTo(createDateTime);
            assertThat(transaction.getRelatedOffenderTransactions()).isEmpty();
        }

        @Test
        public void testMapsRelatedTransactionsForPaidActivityWork() {
            final var courseActivity = CourseActivity.builder().description("Wing cleaner").build();
            final var courseAttendance = OffenderCourseAttendance.builder().eventId(2L).courseActivity(courseActivity).build();
            final var relatedTransaction = OffenderTransactionDetails.builder()
                .id(1L)
                .payAmount(BigDecimal.valueOf(1.0))
                .bonusPay(BigDecimal.valueOf(3.0))
                .pieceWork(BigDecimal.valueOf(2.0))
                .calendarDate(LocalDate.now())
                .transactionId(1L)
                .event(courseAttendance)
                .transactionEntrySequence(1L)
                .build();


            when(repository.findByOffender_NomsId(anyString()))
                .thenReturn(List.of(
                    TRANSACTION_IN.toBuilder()
                        .entryDate(LocalDate.now())
                        .createDatetime(LocalDateTime.now())
                        .relatedTransactionDetails(List.of(relatedTransaction))
                        .build()
                ));

            final var fromDate = LocalDate.now();
            final var toDateOpl = LocalDate.now();

            final var relatedTransactionDetail = service.getTransactionHistory(OFFENDER_NO, null, fromDate, toDateOpl, null)
                .stream()
                .flatMap(transaction -> transaction.getRelatedOffenderTransactions().stream())
                .findFirst()
                .orElseThrow();

            assertThat(relatedTransactionDetail.getId()).isEqualTo(1);
            assertThat(relatedTransactionDetail.getPayAmount()).isEqualTo(100);
            assertThat(relatedTransactionDetail.getBonusPay()).isEqualTo(300);
            assertThat(relatedTransactionDetail.getPieceWork()).isEqualTo(200);
            assertThat(relatedTransactionDetail.getCalendarDate()).isEqualTo(LocalDate.now());
            assertThat(relatedTransactionDetail.getEventId()).isEqualTo(2L);
            assertThat(relatedTransactionDetail.getPayTypeCode()).isEqualTo("SESSION");
            assertThat(relatedTransactionDetail.getPaymentDescription()).isEqualTo("Wing cleaner");
        }

        @Test
        public void testMapsRelatedTransactionsForOtherPaid() {
            final var relatedTransaction = OffenderTransactionDetails.builder()
                .id(1L)
                .payAmount(BigDecimal.valueOf(1.0))
                .bonusPay(BigDecimal.valueOf(3.0))
                .pieceWork(BigDecimal.valueOf(2.0))
                .calendarDate(LocalDate.now())
                .transactionId(1L)
                .noneActivityPaymentType(new PaymentType("UNEMPLOYMENT", "Unemployment"))
                .transactionEntrySequence(1L)
                .build();

            when(repository.findByOffender_NomsId(anyString()))
                .thenReturn(List.of(
                    TRANSACTION_IN.toBuilder()
                        .entryDate(LocalDate.now())
                        .createDatetime(LocalDateTime.now())
                        .relatedTransactionDetails(List.of(relatedTransaction))
                        .build()
                ));

            final var fromDate = LocalDate.now();
            final var toDateOpl = LocalDate.now();

            final var relatedTransactionDetail = service.getTransactionHistory(OFFENDER_NO, null, fromDate, toDateOpl, null)
                .stream()
                .flatMap(transaction -> transaction.getRelatedOffenderTransactions().stream())
                .findFirst()
                .orElseThrow();

            assertThat(relatedTransactionDetail.getId()).isEqualTo(1);
            assertThat(relatedTransactionDetail.getPayAmount()).isEqualTo(100);
            assertThat(relatedTransactionDetail.getBonusPay()).isEqualTo(300);
            assertThat(relatedTransactionDetail.getPieceWork()).isEqualTo(200);
            assertThat(relatedTransactionDetail.getCalendarDate()).isEqualTo(LocalDate.now());
            assertThat(relatedTransactionDetail.getEventId()).isNull();
            assertThat(relatedTransactionDetail.getPayTypeCode()).isEqualTo("UNEMPLOYMENT");
            assertThat(relatedTransactionDetail.getTransactionEntrySequence()).isEqualTo(1L);
            assertThat(relatedTransactionDetail.getTransactionId()).isEqualTo(1);
            assertThat(relatedTransactionDetail.getPaymentDescription()).isEqualTo("Unemployment");
        }
    }

    @Nested
    public class Filtering {
        @Test
        public void testFilterByDateRange() {
            final var allTransactions = List.of(
                TRANSACTION_IN.toBuilder()
                    .entryDate(LocalDate.of(2000, 10, 10))
                    .createDatetime(LocalDateTime.of(2000, 10, 10, 0, 0))
                    .entryAmount(BigDecimal.valueOf(2))
                    .build(),
                TRANSACTION_IN.toBuilder()
                    .entryDate(LocalDate.of(2000, 11, 10))
                    .createDatetime(LocalDateTime.of(2000, 11, 10, 0, 0))
                    .entryAmount(BigDecimal.valueOf(3))
                    .build(),
                TRANSACTION_OUT.toBuilder()
                    .entryDate(LocalDate.now())
                    .createDatetime(LocalDateTime.now())
                    .entryAmount(BigDecimal.valueOf(5))
                    .build()
            );

            when(repository.findByOffender_NomsId(anyString())).thenReturn(allTransactions);

            final var transactions = service.getTransactionHistory(OFFENDER_NO, null,
                LocalDate.of(2000, 10, 11), null, null);

            assertThat(transactions.size()).isEqualTo(2);
            assertThat(transactions.get(0).getEntryDate()).isEqualTo(LocalDate.now());
            assertThat(transactions.get(1).getEntryDate()).isEqualTo(LocalDate.of(2000, 11, 10));
        }

        @Test
        public void testFilterByAccountType() {
            final var allTransactions = List.of(
                TRANSACTION_IN.toBuilder()
                    .createDatetime(LocalDateTime.of(2000, 10, 10, 0, 0))
                    .entryAmount(BigDecimal.valueOf(2))
                    .build(),
                TRANSACTION_IN.toBuilder()
                    .createDatetime(LocalDateTime.of(2000, 11, 10, 0, 0))
                    .entryAmount(BigDecimal.valueOf(3))
                    .build(),
                TRANSACTION_OUT.toBuilder()
                    .createDatetime(LocalDateTime.of(2001, 11, 10, 0, 0))
                    .entryAmount(BigDecimal.valueOf(5))
                    .build()
            );

            when(repository.findByOffender_NomsId(anyString())).thenReturn(allTransactions);

            final var transactions = service
                .getTransactionHistory(OFFENDER_NO, "SPENDS", null, null, null);

            assertThat(transactions.size()).isEqualTo(1);
            assertThat(transactions.get(0).getAccountType()).isEqualTo("SPND");
        }

        @Test
        public void testFilterTransactionType() {
            final var allTransactions = List.of(
                TRANSACTION_IN.toBuilder()
                    .createDatetime(LocalDateTime.of(2000, 10, 10, 0, 0))
                    .entryAmount(BigDecimal.valueOf(2))
                    .build(),
                TRANSACTION_IN.toBuilder()
                    .createDatetime(LocalDateTime.of(2000, 11, 10, 0, 0))
                    .entryAmount(BigDecimal.valueOf(3))
                    .build(),
                TRANSACTION_OUT.toBuilder()
                    .createDatetime(LocalDateTime.of(2001, 11, 10, 0, 0))
                    .entryAmount(BigDecimal.valueOf(5))
                    .transactionType("OUT")
                    .build()
            );

            when(repository.findByOffender_NomsId(any())).thenReturn(allTransactions);

            final var transactions = service
                .getTransactionHistory(OFFENDER_NO, null, null, null, "OUT");

            assertThat(transactions.size()).isEqualTo(1);
            assertThat(transactions.get(0).getPenceAmount()).isEqualTo(500L);
        }
    }

    @Nested
    public class RunningBalance {
        @Test
        public void testCalculateRunningBalance() {
            final var allTransactions = List.of(
                TRANSACTION_IN.toBuilder()
                    .accountType("SPENDS")
                    .createDatetime(LocalDateTime.of(2000, 10, 10, 0, 0, 0))
                    .entryAmount(BigDecimal.valueOf(2))
                    .build(),
                TRANSACTION_IN.toBuilder()
                    .accountType("SPENDS")
                    .createDatetime(LocalDateTime.of(2000, 11, 10, 0, 0, 0))
                    .entryAmount(BigDecimal.valueOf(3))
                    .build(),
                TRANSACTION_OUT.toBuilder()
                    .accountType("SPENDS")
                    .createDatetime(LocalDateTime.of(2001, 12, 10, 0, 0, 0))
                    .entryAmount(BigDecimal.valueOf(5))
                    .build()
            );

            when(repository.findByOffender_NomsId(any())).thenReturn(allTransactions);

            final var transactions =
                service.getTransactionHistory(OFFENDER_NO, null, null, null, null);

            assertThat(transactions.size()).isEqualTo(3);
            assertThat(transactions.get(0).getCurrentBalance()).isEqualTo(0L);
            assertThat(transactions.get(1).getCurrentBalance()).isEqualTo(500L);
            assertThat(transactions.get(2).getCurrentBalance()).isEqualTo(200L);
        }

        @Test
        public void testCalculateRunningBalance_OffenderMovedThroughMultiplePrions() {
            final var allTransactions = List.of(
                TRANSACTION_IN.toBuilder()
                    .transactionId(1L)
                    .createDatetime(LocalDateTime.of(2000, 10, 10, 0, 0, 0))
                    .agencyId("LEI")
                    .accountType("SPENDS")
                    .entryAmount(BigDecimal.valueOf(2))
                    .build(),
                TRANSACTION_IN.toBuilder()
                    .transactionId(2L)
                    .createDatetime(LocalDateTime.of(2000, 11, 10, 0, 0, 0))
                    .entryAmount(BigDecimal.valueOf(3))
                    .agencyId("LEI")
                    .accountType("SPENDS")
                    .build(),
                TRANSACTION_IN.toBuilder()
                    .transactionId(3L)
                    .createDatetime(LocalDateTime.of(2002, 1, 1, 0, 0, 0))
                    .entryAmount(BigDecimal.valueOf(5))
                    .agencyId("MDI")
                    .accountType("SPENDS")
                    .build(),
                TRANSACTION_IN.toBuilder()
                    .transactionId(4L)
                    .createDatetime(LocalDateTime.of(2002, 2, 1, 0, 0, 0))
                    .entryAmount(BigDecimal.valueOf(5))
                    .agencyId("MDI")
                    .accountType("SPENDS")
                    .build(),
                TRANSACTION_IN.toBuilder()
                    .transactionId(5L)
                    .createDatetime(LocalDateTime.of(2002, 3, 1, 0, 0, 0))
                    .entryAmount(BigDecimal.valueOf(1))
                    .agencyId("LEI")
                    .accountType("SPENDS")
                    .build()
            );
            when(repository.findByOffender_NomsId(any())).thenReturn(allTransactions);

            final var transactions =
                service.getTransactionHistory(OFFENDER_NO, null, null, null, null);


            assertThat(transactions.get(0).getCurrentBalance()).isEqualTo(600L);
            assertThat(transactions.get(1).getCurrentBalance()).isEqualTo(1000L);
            assertThat(transactions.get(2).getCurrentBalance()).isEqualTo(500L);
            assertThat(transactions.get(3).getCurrentBalance()).isEqualTo(500L);
            assertThat(transactions.get(4).getCurrentBalance()).isEqualTo(200L);
        }


        @Test
        public void testCalculateRunningBalance_GroupedByAgencyAndAccountType() {
            final var allTransactions = List.of(
                TRANSACTION_IN.toBuilder()
                    .transactionId(1L)
                    .createDatetime(LocalDateTime.of(2000, 10, 10, 0, 0, 0))
                    .entryDate(LocalDate.of(2000, 10, 10))
                    .agencyId("LEI")
                    .accountType("SPND")
                    .entryAmount(BigDecimal.valueOf(2))
                    .build(),
                TRANSACTION_IN.toBuilder()
                    .transactionId(2L)
                    .createDatetime(LocalDateTime.of(2000, 11, 10, 0, 0, 0))
                    .entryDate(LocalDate.of(2000, 11, 10))
                    .entryAmount(BigDecimal.valueOf(3))
                    .agencyId("LEI")
                    .accountType("REG")
                    .build(),
                TRANSACTION_IN.toBuilder()
                    .transactionId(3L)
                    .createDatetime(LocalDateTime.of(2002, 1, 1, 0, 0, 0))
                    .entryDate(LocalDate.of(2002, 1, 1))
                    .entryAmount(BigDecimal.valueOf(5))
                    .agencyId("LEI")
                    .accountType("SPND")
                    .build(),
                TRANSACTION_IN.toBuilder()
                    .transactionId(4L)
                    .createDatetime(LocalDateTime.of(2002, 2, 1, 0, 0, 0))
                    .entryDate(LocalDate.of(2002, 2, 1))
                    .entryAmount(BigDecimal.valueOf(5))
                    .agencyId("MDI")
                    .accountType("REG")
                    .build(),
                TRANSACTION_IN.toBuilder()
                    .transactionId(5L)
                    .createDatetime(LocalDateTime.of(2002, 3, 1, 0, 0, 0))
                    .entryDate(LocalDate.of(2002, 3, 1))
                    .entryAmount(BigDecimal.valueOf(1))
                    .agencyId("MDI")
                    .accountType("REG")
                    .build()
            );
            when(repository.findByOffender_NomsId(any())).thenReturn(allTransactions);

            final var transactions =
                service.getTransactionHistory(OFFENDER_NO, null, null, null, null);

            //MDI - REG
            assertThat(transactions.get(0))
                .extracting("currentBalance", "agencyId", "accountType")
                .containsExactlyInAnyOrder(600L, "MDI", "REG");

            assertThat(transactions.get(1))
                .extracting("currentBalance", "agencyId", "accountType")
                .containsExactlyInAnyOrder(500L, "MDI", "REG");

            // LEI - SPENDS
            assertThat(transactions.get(2))
                .extracting("currentBalance", "agencyId", "accountType")
                .containsExactlyInAnyOrder(700L, "LEI", "SPND");

            assertThat(transactions.get(3))
                .extracting("currentBalance", "agencyId", "accountType")
                .containsExactlyInAnyOrder(300L, "LEI", "REG");

            // LEI - REG
            assertThat(transactions.get(4))
                .extracting("currentBalance", "agencyId", "accountType")
                .containsExactlyInAnyOrder(200L, "LEI", "SPND");
        }
    }

    @Nested
    public class RelatedTransactionRunningBalance {
        @Test
        public void testCalculateRunningBalanceOverMultipleDays() {
            final var yesterdaysDate = LocalDateTime.now().minusDays(1);
            final var todaysDate = LocalDateTime.now();

            final var yesterdaysRelatedTransactionAmount = 5;
            final var yesterdaysOutAmount = 10;
            final var todaysRelatedTransactionAmount = 20;
            final var todaysOutAmount = 30;

            final var currentBalanceAfterYesterdaysIn = yesterdaysRelatedTransactionAmount;
            final var currentBalanceAfterYesterday = currentBalanceAfterYesterdaysIn - yesterdaysOutAmount;
            final var currentBalanceAfterTodaysIn = currentBalanceAfterYesterday + todaysRelatedTransactionAmount;

            final var yesterdaysRelatedTransaction = makeRelatedTransaction(yesterdaysRelatedTransactionAmount, yesterdaysDate.toLocalDate(), 1, 1, 1);
            final var todaysRelatedTransaction = makeRelatedTransaction(todaysRelatedTransactionAmount, todaysDate.toLocalDate(), 2, 2, 1);

            final var batchTransactionYesterdayIn = makeBatchTransactionIn(yesterdaysDate.minusHours(1), yesterdaysRelatedTransactionAmount, List.of(yesterdaysRelatedTransaction));
            final var batchTransactionYesterdayOut = makeBatchTransactionOut(yesterdaysDate, yesterdaysOutAmount);
            final var batchTransactionTodayIn = makeBatchTransactionIn(todaysDate.minusHours(1), todaysRelatedTransactionAmount, List.of(todaysRelatedTransaction));
            final var batchTransactionTodayOut = makeBatchTransactionOut(todaysDate, todaysOutAmount);

            when(repository.findByOffender_NomsId(anyString()))
                .thenReturn(List.of(
                    batchTransactionTodayIn, batchTransactionTodayOut,
                    batchTransactionYesterdayIn, batchTransactionYesterdayOut
                ));

            final var relatedTransactionDetails = service.getTransactionHistory(OFFENDER_NO, null, yesterdaysDate.toLocalDate(), todaysDate.toLocalDate(), null)
                .stream()
                .flatMap(transaction -> transaction.getRelatedOffenderTransactions().stream()).collect(toList());

            assertThat(relatedTransactionDetails.size()).isEqualTo(2L);
            assertThat(relatedTransactionDetails.get(0).getId()).isEqualTo(2L);
            assertThat(relatedTransactionDetails.get(0).getCurrentBalance()).isEqualTo(currentBalanceAfterTodaysIn * 100);
            assertThat(relatedTransactionDetails.get(1).getId()).isEqualTo(1L);
            assertThat(relatedTransactionDetails.get(1).getCurrentBalance()).isEqualTo(currentBalanceAfterYesterdaysIn * 100);
        }

        @Test
        public void testCalculateRunningBalanceWithMultipleRelatedTransactions() {
            final var todaysDate = LocalDateTime.now();

            final var firstRelatedTransactionAmount = 5;
            final var secondRelatedTransactionAmount = 10;
            final var finalBalance = firstRelatedTransactionAmount + secondRelatedTransactionAmount;

            final var firstRelatedTransaction = makeRelatedTransaction(firstRelatedTransactionAmount, todaysDate.toLocalDate(), 1, 1, 1);
            final var secondRelatedTransaction = makeRelatedTransaction(secondRelatedTransactionAmount, todaysDate.toLocalDate(), 2, 2, 2);

            final var batchTransactionToday = makeBatchTransactionIn(todaysDate, finalBalance,
                List.of(secondRelatedTransaction, firstRelatedTransaction));

            when(repository.findByOffender_NomsId(anyString()))
                .thenReturn(List.of(batchTransactionToday));

            final var relatedTransactionDetails = service.getTransactionHistory(OFFENDER_NO, null, todaysDate.minusDays(1).toLocalDate(), todaysDate.toLocalDate(), null)
                .stream()
                .flatMap(transaction -> transaction.getRelatedOffenderTransactions().stream()).collect(toList());

            assertThat(relatedTransactionDetails.size()).isEqualTo(2L);
            assertThat(relatedTransactionDetails.get(0).getId()).isEqualTo(2L);
            assertThat(relatedTransactionDetails.get(0).getCurrentBalance()).isEqualTo(finalBalance * 100);
            assertThat(relatedTransactionDetails.get(1).getId()).isEqualTo(1L);
            assertThat(relatedTransactionDetails.get(1).getCurrentBalance()).isEqualTo(firstRelatedTransactionAmount * 100);
        }

        private OffenderTransactionHistory makeBatchTransactionIn(final LocalDateTime batchTransactionTime, final int entryAmount, final List<OffenderTransactionDetails> relatedTransactions) {
            return OFFENDER_TRANSACTION.toBuilder()
                .postingType("CR")
                .accountType("SPND")
                .transactionType("IN")
                .entryDate(LocalDate.now())
                .entryAmount(BigDecimal.valueOf(entryAmount))
                .createDatetime(batchTransactionTime)
                .relatedTransactionDetails(relatedTransactions)
                .build();
        }

        private OffenderTransactionHistory makeBatchTransactionOut(final LocalDateTime batchTransactionTime, final int entryAmount) {
            return OFFENDER_TRANSACTION.toBuilder()
                .postingType("DR")
                .accountType("SPND")
                .transactionType("OUT")
                .entryDate(LocalDate.now())
                .entryAmount(BigDecimal.valueOf(entryAmount))
                .createDatetime(batchTransactionTime)
                .build();
        }

        private OffenderTransactionDetails makeRelatedTransaction(final int payAmount, final LocalDate transactionTime, final long transactionId, final long eventId, final long entrySequence) {
            final var courseActivity = CourseActivity.builder().description("Wing cleaner").build();
            final var courseAttendance = OffenderCourseAttendance.builder().eventId(eventId).courseActivity(courseActivity).build();
            return OffenderTransactionDetails.builder()
                .id(transactionId)
                .payAmount(BigDecimal.valueOf(payAmount))
                .bonusPay(BigDecimal.valueOf(0.0))
                .pieceWork(BigDecimal.valueOf(0.0))
                .calendarDate(transactionTime)
                .transactionId(transactionId)
                .event(courseAttendance)
                .transactionEntrySequence(entrySequence)
                .build();
        }
    }
}