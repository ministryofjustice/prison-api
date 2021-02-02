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
                final var accountCode ="spends";
                final var fromDate = LocalDate.now();
                final var toDate = LocalDate.now().plusDays(2);
                service.getTransactionHistory(OFFENDER_NO, accountCode, fromDate, toDate, null);
            })
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("toDate can't be in the future");
        }

        @Test
        public void testGetTransactionHistoryThrowException_TypoInAccountCode() {
            assertThatThrownBy(() ->{
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

            final var offender = Offender.builder().nomsId(OFFENDER_NO).id(1L).build();
            final var transaction =  OffenderTransactionHistory.builder().offender(offender).postingType("CR").agencyId("LEI").build();

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
        final Offender offender = Offender.builder().nomsId(OFFENDER_NO).rootOffenderId(2L).id(3L).build();
        final OffenderTransactionHistory baseOffenderTransactionHistory = OffenderTransactionHistory.builder().offender(offender).entryDescription("Some description").agencyId("MDI").transactionEntrySequence(1L).build();
        final OffenderTransactionHistory out = baseOffenderTransactionHistory.toBuilder().postingType("DR").accountType("SPENDS").build();
        final OffenderTransactionHistory in = baseOffenderTransactionHistory.toBuilder().postingType("CR").accountType("REG").build();

        @Test
        public void testCalculateRunningBalance() {
            final var allTransactions = List.of(
                in.toBuilder()
                    .accountType("SPENDS")
                    .createDatetime(LocalDateTime.of(2000,10,10, 0,0,0))
                    .entryAmount(BigDecimal.valueOf(2))
                    .build(),
                in.toBuilder()
                    .accountType("SPENDS")
                    .createDatetime(LocalDateTime.of(2000,11,10, 0,0,0))
                    .entryAmount(BigDecimal.valueOf(3))
                    .build(),
                out.toBuilder()
                    .accountType("SPENDS")
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

        @Test
        public void testCalculateRunningBalance_OffenderMovedThroughMultiplePrions() {
            final var allTransactions = List.of(
                in.toBuilder()
                    .transactionId(1L)
                    .createDatetime(LocalDateTime.of(2000,10,10, 0,0,0))
                    .agencyId("LEI")
                    .accountType("SPENDS")
                    .entryAmount(BigDecimal.valueOf(2))
                    .build(),
                in.toBuilder()
                    .transactionId(2L)
                    .createDatetime(LocalDateTime.of(2000,11,10, 0,0,0))
                    .entryAmount(BigDecimal.valueOf(3))
                    .agencyId("LEI")
                    .accountType("SPENDS")
                    .build(),
                in.toBuilder()
                    .transactionId(3L)
                    .createDatetime(LocalDateTime.of(2002,1,1,0,0,0))
                    .entryAmount(BigDecimal.valueOf(5))
                    .agencyId("MDI")
                    .accountType("SPENDS")
                    .build(),
                in.toBuilder()
                    .transactionId(4L)
                    .createDatetime(LocalDateTime.of(2002,2,1,0,0,0))
                    .entryAmount(BigDecimal.valueOf(5))
                    .agencyId("MDI")
                    .accountType("SPENDS")
                    .build(),
                in.toBuilder()
                    .transactionId(5L)
                    .createDatetime(LocalDateTime.of(2002,3,1,0,0,0))
                    .entryAmount(BigDecimal.valueOf(1))
                    .agencyId("LEI")
                    .accountType("SPENDS")
                    .build()
            );
            when(repository.findByOffender_NomsId(any())).thenReturn(allTransactions);

            final var transactions =
                service.getTransactionHistory(OFFENDER_NO, null, null, null,null);


            assertThat(transactions.get(0).getCurrentBalance()).isEqualTo(600L);
            assertThat(transactions.get(1).getCurrentBalance()).isEqualTo(1000L);
            assertThat(transactions.get(2).getCurrentBalance()).isEqualTo(500L);
            assertThat(transactions.get(3).getCurrentBalance()).isEqualTo(500L);
            assertThat(transactions.get(4).getCurrentBalance()).isEqualTo(200L);
        }


        @Test
        public void testCalculateRunningBalance_GroupedByAgencyAndAccountType() {
            final var allTransactions = List.of(
                in.toBuilder()
                    .transactionId(1L)
                    .createDatetime(LocalDateTime.of(2000,10,10, 0,0,0))
                    .entryDate(LocalDate.of(2000,10, 10))
                    .agencyId("LEI")
                    .accountType("SPND")
                    .entryAmount(BigDecimal.valueOf(2))
                    .build(),
                in.toBuilder()
                    .transactionId(2L)
                    .createDatetime(LocalDateTime.of(2000,11,10, 0,0,0))
                    .entryDate(LocalDate.of(2000,11, 10))
                    .entryAmount(BigDecimal.valueOf(3))
                    .agencyId("LEI")
                    .accountType("REG")
                    .build(),
                in.toBuilder()
                    .transactionId(3L)
                    .createDatetime(LocalDateTime.of(2002,1,1,0,0,0))
                    .entryDate(LocalDate.of(2002,1, 1))
                    .entryAmount(BigDecimal.valueOf(5))
                    .agencyId("LEI")
                    .accountType("SPND")
                    .build(),
                in.toBuilder()
                    .transactionId(4L)
                    .createDatetime(LocalDateTime.of(2002,2,1,0,0,0))
                    .entryDate(LocalDate.of(2002,2, 1))
                    .entryAmount(BigDecimal.valueOf(5))
                    .agencyId("MDI")
                    .accountType("REG")
                    .build(),
                in.toBuilder()
                    .transactionId(5L)
                    .createDatetime(LocalDateTime.of(2002,3,1,0,0,0))
                    .entryDate(LocalDate.of(2002,3, 1))
                    .entryAmount(BigDecimal.valueOf(1))
                    .agencyId("MDI")
                    .accountType("REG")
                    .build()
            );
            when(repository.findByOffender_NomsId(any())).thenReturn(allTransactions);

            final var transactions =
                service.getTransactionHistory(OFFENDER_NO, null, null, null,null);

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