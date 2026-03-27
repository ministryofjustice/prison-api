package uk.gov.justice.hmpps.prison.service

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.OffenderTransactionHistoryDto
import uk.gov.justice.hmpps.prison.repository.jpa.model.CourseActivity
import uk.gov.justice.hmpps.prison.repository.jpa.model.Offender
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderCourseAttendance
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionDetails
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderTransactionHistory
import uk.gov.justice.hmpps.prison.repository.jpa.model.PaymentType
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderTransactionHistoryRepository
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.stream.Collectors

@ExtendWith(MockitoExtension::class)
class OffenderTransactionHistoryServiceTest {
  private val repository: OffenderTransactionHistoryRepository = mock()

  private val entityManager: EntityManager = mock()

  private val service: OffenderTransactionHistoryService = OffenderTransactionHistoryService(
    "GBP",
    repository,
    entityManager,
  )

  companion object {
    const val OFFENDER_NO: String = "A1111AA"
    val OFFENDER: Offender = Offender.builder().nomsId(OFFENDER_NO).rootOffenderId(2L).id(3L).build()
    val OFFENDER_TRANSACTION: OffenderTransactionHistory =
      OffenderTransactionHistory.builder().offender(OFFENDER).entryDescription("Some description").entryAmount(
        BigDecimal.valueOf(1.0),
      ).agencyId("MDI").build()
    val TRANSACTION_OUT: OffenderTransactionHistory =
      OFFENDER_TRANSACTION.toBuilder().postingType("DR").accountType("SPND").transactionType("OUT").build()
    val TRANSACTION_IN: OffenderTransactionHistory =
      OFFENDER_TRANSACTION.toBuilder().postingType("CR").accountType("REG").transactionType("IN").build()
  }

  @Nested
  inner class CorrectParameters {
    @Test
    fun testGetTransactionHistoryThrowException_offenderIdIsNull() {
      Assertions.assertThatThrownBy {
        val nomisId: String? = null
        val accountCode = "spends"
        val fromDate = LocalDate.now().minusDays(7)
        val toDate = LocalDate.now()
        service.getTransactionHistory(nomisId, accountCode, fromDate, toDate, null)
      }
        .isInstanceOf(NullPointerException::class.java)
        .hasMessage("offenderNo can't be null")
    }

    @Test
    fun testGetTransactionHistoryThrowException_toDateIsBeforeFromDate() {
      Assertions.assertThatThrownBy {
        val accountCode = "spends"
        val fromDate = LocalDate.now().minusDays(7)
        val toDate = LocalDate.now().minusDays(8)
        service.getTransactionHistory(OFFENDER_NO, accountCode, fromDate, toDate, null)
      }
        .isInstanceOf(IllegalStateException::class.java)
        .hasMessage("toDate can't be before fromDate")
    }

    @Test
    fun testGetTransactionHistory_ThrowException_FromDateIsTomorrow() {
      Assertions.assertThatThrownBy {
        val accountCode = "spends"
        val fromDate = LocalDate.now().plusDays(1)
        val toDate = LocalDate.now().plusDays(2)
        service.getTransactionHistory(OFFENDER_NO, accountCode, fromDate, toDate, null)
      }
        .isInstanceOf(IllegalStateException::class.java)
        .hasMessage("fromDate can't be in the future")
    }

    @Test
    fun testGetTransactionHistoryThrowException_ToDateIs2DaysInFuture() {
      Assertions.assertThatThrownBy {
        val accountCode = "spends"
        val fromDate = LocalDate.now()
        val toDate = LocalDate.now().plusDays(2)
        service.getTransactionHistory(OFFENDER_NO, accountCode, fromDate, toDate, null)
      }
        .isInstanceOf(IllegalStateException::class.java)
        .hasMessage("toDate can't be in the future")
    }

    @Test
    fun testGetTransactionHistoryThrowException_TypoInAccountCode() {
      Assertions.assertThatThrownBy {
        val accountCode = "spendss"
        val fromDate = LocalDate.now()
        val toDate = LocalDate.now()
        service.getTransactionHistory(OFFENDER_NO, accountCode, fromDate, toDate, null)
      }
        .isInstanceOf(IllegalStateException::class.java)
        .hasMessage("Unknown account-code spendss")
    }

    @Test
    fun testGetTransactionHistory_CallsRepositoryWithCorrectParameters() {
      val histories =
        service.getTransactionHistory(OFFENDER_NO, null, null, null, null)

      verify(repository, times(1)).findByOffenderNomsId(OFFENDER_NO)

      assertThat(histories).isNotNull()
      assertThat(histories.size).isEqualTo(0)
    }
  }

  @Nested
  inner class Sorting {
    @Test
    fun testSortedByEntryDateDescending() {
      val transaction =
        OffenderTransactionHistory.builder().offender(OFFENDER).postingType("CR").agencyId("LEI").build()

      val event = OffenderCourseAttendance
        .builder()
        .eventId(1L)
        .courseActivity(CourseActivity.builder().description("Wing cleaner").build())
        .build()

      transaction.relatedTransactionDetails = listOf(
        OffenderTransactionDetails.builder()
          .id(1L)
          .event(event)
          .calendarDate(LocalDate.now())
          .bonusPay(BigDecimal.valueOf(3.00))
          .pieceWork(BigDecimal.valueOf(2.00))
          .payAmount(BigDecimal.valueOf(1.00))
          .transactionEntrySequence(1L)
          .transactionId(1L)
          .build(),
      )

      whenever(
        repository.findByOffenderNomsId(
          anyString(),
        ),
      ).thenReturn(
        mutableListOf(
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
            .build(),
        ),
      )

      val histories =
        service.getTransactionHistory(OFFENDER_NO, null, null, null, null)

      assertThat(histories).isNotNull()
      assertThat(histories.size).isEqualTo(9)

      // Date: now, Seq: 3, 2, 1
      assertThat(histories[0])
        .extracting("transactionId", "entryDate", "transactionEntrySequence")
        .containsExactlyInAnyOrder(1L, LocalDate.now(), 3L)

      assertThat(histories[1])
        .extracting("transactionId", "entryDate", "transactionEntrySequence")
        .containsExactlyInAnyOrder(2L, LocalDate.now(), 2L)

      assertThat(histories[2])
        .extracting("transactionId", "entryDate", "transactionEntrySequence")
        .containsExactlyInAnyOrder(3L, LocalDate.now(), 1L)

      // Date: now - 1 day, Seq: 3, 2, 1
      assertThat(histories[3])
        .extracting("transactionId", "entryDate", "transactionEntrySequence")
        .containsExactlyInAnyOrder(4L, LocalDate.now().minusDays(1), 3L)

      assertThat(histories[3])
        .extracting("transactionId", "entryDate", "transactionEntrySequence")
        .containsExactlyInAnyOrder(4L, LocalDate.now().minusDays(1), 3L)

      assertThat(histories[4])
        .extracting("transactionId", "entryDate", "transactionEntrySequence")
        .containsExactlyInAnyOrder(5L, LocalDate.now().minusDays(1), 2L)

      assertThat(histories[5])
        .extracting("transactionId", "entryDate", "transactionEntrySequence")
        .containsExactlyInAnyOrder(6L, LocalDate.now().minusDays(1), 1L)

      // Date: now - 2 days, Seq: 3, 2, 1
      assertThat(histories[6])
        .extracting("transactionId", "entryDate", "transactionEntrySequence")
        .containsExactlyInAnyOrder(7L, LocalDate.now().minusDays(2), 3L)

      assertThat(histories[7])
        .extracting("transactionId", "entryDate", "transactionEntrySequence")
        .containsExactlyInAnyOrder(8L, LocalDate.now().minusDays(2), 2L)

      assertThat(histories[8])
        .extracting("transactionId", "entryDate", "transactionEntrySequence")
        .containsExactlyInAnyOrder(9L, LocalDate.now().minusDays(2), 1L)
    }
  }

  @Nested
  inner class Mapping {
    @Test
    fun testMapsTransactions() {
      val createDateTime = LocalDateTime.now()

      whenever(
        repository.findByOffenderNomsId(
          anyString(),
        ),
      )
        .thenReturn(
          listOf(
            TRANSACTION_OUT.toBuilder()
              .transactionId(1L)
              .referenceNumber("12343/1")
              .transactionEntrySequence(1L)
              .entryDate(LocalDate.now())
              .createDatetime(createDateTime)
              .build(),
          ),
        )

      val fromDate = LocalDate.now()
      val toDate = LocalDate.now()

      val transaction = service.getTransactionHistory(OFFENDER_NO, null, fromDate, toDate, null)
        .stream()
        .findFirst()
        .orElseThrow()

      assertThat(transaction.offenderId).isEqualTo(3L)
      assertThat(transaction.transactionId).isEqualTo(1L)
      assertThat(transaction.transactionEntrySequence).isEqualTo(1L)
      assertThat(transaction.entryDate).isEqualTo(LocalDate.now())
      assertThat(transaction.transactionType).isEqualTo("OUT")
      assertThat(transaction.entryDescription).isEqualTo("Some description")
      assertThat(transaction.referenceNumber).isEqualTo("12343/1")
      assertThat(transaction.currency).isEqualTo("GBP")
      assertThat(transaction.penceAmount).isEqualTo(100L)
      assertThat(transaction.accountType).isEqualTo("SPND")
      assertThat(transaction.postingType).isEqualTo("DR")
      assertThat(transaction.offenderNo).isEqualTo(OFFENDER_NO)
      assertThat(transaction.agencyId).isEqualTo("MDI")
      assertThat(transaction.createDateTime).isEqualTo(createDateTime)
      assertThat(transaction.relatedOffenderTransactions).isEmpty()
    }

    @Test
    fun testMapsRelatedTransactionsForPaidActivityWork() {
      val courseActivity = CourseActivity.builder().description("Wing cleaner").build()
      val courseAttendance = OffenderCourseAttendance.builder().eventId(2L).courseActivity(courseActivity).build()
      val relatedTransaction = OffenderTransactionDetails.builder()
        .id(1L)
        .payAmount(BigDecimal.valueOf(1.0))
        .bonusPay(BigDecimal.valueOf(3.0))
        .pieceWork(BigDecimal.valueOf(2.0))
        .calendarDate(LocalDate.now())
        .transactionId(1L)
        .event(courseAttendance)
        .transactionEntrySequence(1L)
        .build()

      whenever(
        repository.findByOffenderNomsId(
          anyString(),
        ),
      )
        .thenReturn(
          listOf(
            TRANSACTION_IN.toBuilder()
              .entryDate(LocalDate.now())
              .createDatetime(LocalDateTime.now())
              .relatedTransactionDetails(listOf(relatedTransaction))
              .build(),
          ),
        )

      val fromDate = LocalDate.now()
      val toDateOpl = LocalDate.now()

      val relatedTransactionDetail = service.getTransactionHistory(OFFENDER_NO, null, fromDate, toDateOpl, null)
        .stream()
        .flatMap { transaction: OffenderTransactionHistoryDto ->
          transaction.relatedOffenderTransactions.stream()
        }
        .findFirst()
        .orElseThrow()

      assertThat(relatedTransactionDetail.id).isEqualTo(1)
      assertThat(relatedTransactionDetail.payAmount).isEqualTo(100)
      assertThat(relatedTransactionDetail.bonusPay).isEqualTo(300)
      assertThat(relatedTransactionDetail.pieceWork).isEqualTo(200)
      assertThat(relatedTransactionDetail.calendarDate).isEqualTo(LocalDate.now())
      assertThat(relatedTransactionDetail.eventId).isEqualTo(2L)
      assertThat(relatedTransactionDetail.payTypeCode).isEqualTo("SESSION")
      assertThat(relatedTransactionDetail.paymentDescription).isEqualTo("Wing cleaner")
    }

    @Test
    fun testMapsRelatedTransactionsForOtherPaid() {
      val relatedTransaction = OffenderTransactionDetails.builder()
        .id(1L)
        .payAmount(BigDecimal.valueOf(1.0))
        .bonusPay(BigDecimal.valueOf(3.0))
        .pieceWork(BigDecimal.valueOf(2.0))
        .calendarDate(LocalDate.now())
        .transactionId(1L)
        .noneActivityPaymentType(PaymentType("UNEMPLOYMENT", "Unemployment"))
        .transactionEntrySequence(1L)
        .build()

      whenever(repository.findByOffenderNomsId(anyString()))
        .thenReturn(
          listOf(
            TRANSACTION_IN.toBuilder()
              .entryDate(LocalDate.now())
              .createDatetime(LocalDateTime.now())
              .relatedTransactionDetails(listOf(relatedTransaction))
              .build(),
          ),
        )

      val fromDate = LocalDate.now()
      val toDateOpl = LocalDate.now()

      val relatedTransactionDetail = service.getTransactionHistory(OFFENDER_NO, null, fromDate, toDateOpl, null)
        .stream()
        .flatMap { transaction: OffenderTransactionHistoryDto ->
          transaction.relatedOffenderTransactions.stream()
        }
        .findFirst()
        .orElseThrow()

      assertThat(relatedTransactionDetail.id).isEqualTo(1)
      assertThat(relatedTransactionDetail.payAmount).isEqualTo(100)
      assertThat(relatedTransactionDetail.bonusPay).isEqualTo(300)
      assertThat(relatedTransactionDetail.pieceWork).isEqualTo(200)
      assertThat(relatedTransactionDetail.calendarDate).isEqualTo(LocalDate.now())
      assertThat(relatedTransactionDetail.eventId as? Any).isNull()
      assertThat(relatedTransactionDetail.payTypeCode).isEqualTo("UNEMPLOYMENT")
      assertThat(relatedTransactionDetail.transactionEntrySequence).isEqualTo(1L)
      assertThat(relatedTransactionDetail.transactionId).isEqualTo(1)
      assertThat(relatedTransactionDetail.paymentDescription).isEqualTo("Unemployment")
    }
  }

  @Nested
  inner class Filtering {
    @Test
    fun testFilterByDateRange() {
      val allTransactions = listOf(
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
          .build(),
      )

      whenever(
        repository.findByOffenderNomsId(
          anyString(),
        ),
      ).thenReturn(allTransactions)

      val transactions = service.getTransactionHistory(
        OFFENDER_NO,
        null,
        LocalDate.of(2000, 10, 11),
        null,
        null,
      )

      assertThat(transactions.size).isEqualTo(2)
      assertThat(transactions[0].entryDate).isEqualTo(LocalDate.now())
      assertThat(transactions[1].entryDate).isEqualTo(LocalDate.of(2000, 11, 10))
    }

    // THIS ONE FAILED
    @Test
    fun testFilterByAccountType() {
      val allTransactions = listOf(
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
          .build(),
      )

      whenever(
        repository.findByOffenderNomsId(anyString()),
      ).thenReturn(allTransactions)

      val transactions = service
        .getTransactionHistory(OFFENDER_NO, "SPENDS", null, null, null)

      assertThat(transactions.size).isEqualTo(1)
      assertThat(transactions[0].accountType).isEqualTo("SPND")
    }

    @Test
    fun testFilterTransactionType() {
      val allTransactions = listOf(
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
          .build(),
      )

      whenever(repository.findByOffenderNomsId(anyString())).thenReturn(allTransactions)

      val transactions = service
        .getTransactionHistory(OFFENDER_NO, null, null, null, "OUT")

      assertThat(transactions.size).isEqualTo(1)
      assertThat(transactions[0].penceAmount).isEqualTo(500L)
    }
  }

  @Nested
  inner class RunningBalance {
    @Test
    fun testCalculateRunningBalance() {
      val allTransactions = listOf(
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
          .build(),
      )

      whenever(repository.findByOffenderNomsId(anyString())).thenReturn(allTransactions)

      val transactions =
        service.getTransactionHistory(OFFENDER_NO, null, null, null, null)

      assertThat(transactions.size).isEqualTo(3)
      assertThat(transactions[0].currentBalance).isEqualTo(0L)
      assertThat(transactions[1].currentBalance).isEqualTo(500L)
      assertThat(transactions[2].currentBalance).isEqualTo(200L)
    }

    @Test
    fun testCalculateRunningBalance_OffenderMovedThroughMultiplePrions() {
      val allTransactions = listOf(
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
          .build(),
      )
      whenever(repository.findByOffenderNomsId(anyString())).thenReturn(allTransactions)

      val transactions =
        service.getTransactionHistory(OFFENDER_NO, null, null, null, null)

      assertThat(transactions[0].currentBalance).isEqualTo(600L)
      assertThat(transactions[1].currentBalance).isEqualTo(1000L)
      assertThat(transactions[2].currentBalance).isEqualTo(500L)
      assertThat(transactions[3].currentBalance).isEqualTo(500L)
      assertThat(transactions[4].currentBalance).isEqualTo(200L)
    }

    @Test
    fun testCalculateRunningBalance_GroupedByAgencyAndAccountType() {
      val allTransactions = listOf(
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
          .build(),
      )
      whenever(repository.findByOffenderNomsId(anyString())).thenReturn(allTransactions)

      val transactions =
        service.getTransactionHistory(OFFENDER_NO, null, null, null, null)

      // MDI - REG
      assertThat(transactions[0])
        .extracting("currentBalance", "agencyId", "accountType")
        .containsExactlyInAnyOrder(600L, "MDI", "REG")

      assertThat(transactions[1])
        .extracting("currentBalance", "agencyId", "accountType")
        .containsExactlyInAnyOrder(500L, "MDI", "REG")

      // LEI - SPENDS
      assertThat(transactions[2])
        .extracting("currentBalance", "agencyId", "accountType")
        .containsExactlyInAnyOrder(700L, "LEI", "SPND")

      assertThat(transactions[3])
        .extracting("currentBalance", "agencyId", "accountType")
        .containsExactlyInAnyOrder(300L, "LEI", "REG")

      // LEI - REG
      assertThat(transactions[4])
        .extracting("currentBalance", "agencyId", "accountType")
        .containsExactlyInAnyOrder(200L, "LEI", "SPND")
    }
  }

  @Nested
  inner class RelatedTransactionRunningBalance {
    @Test
    fun testCalculateRunningBalanceOverMultipleDays() {
      val yesterdaysDate = LocalDateTime.now().minusDays(1)
      val todaysDate = LocalDateTime.now()

      val yesterdaysRelatedTransactionAmount = 5
      val yesterdaysOutAmount = 10
      val todaysRelatedTransactionAmount = 20
      val todaysOutAmount = 30

      val currentBalanceAfterYesterday = yesterdaysRelatedTransactionAmount - yesterdaysOutAmount
      val currentBalanceAfterTodaysIn = currentBalanceAfterYesterday + todaysRelatedTransactionAmount

      val yesterdaysRelatedTransaction =
        makeRelatedTransaction(yesterdaysRelatedTransactionAmount, yesterdaysDate.toLocalDate(), 1, 1, 1)
      val todaysRelatedTransaction =
        makeRelatedTransaction(todaysRelatedTransactionAmount, todaysDate.toLocalDate(), 2, 2, 1)

      val batchTransactionYesterdayIn = makeBatchTransactionIn(
        yesterdaysDate.minusHours(1),
        yesterdaysRelatedTransactionAmount,
        listOf(yesterdaysRelatedTransaction),
      )
      val batchTransactionYesterdayOut = makeBatchTransactionOut(yesterdaysDate, yesterdaysOutAmount)
      val batchTransactionTodayIn = makeBatchTransactionIn(
        todaysDate.minusHours(1),
        todaysRelatedTransactionAmount,
        listOf(todaysRelatedTransaction),
      )
      val batchTransactionTodayOut = makeBatchTransactionOut(todaysDate, todaysOutAmount)

      whenever(repository.findByOffenderNomsId(anyString()))
        .thenReturn(
          listOf(
            batchTransactionTodayIn,
            batchTransactionTodayOut,
            batchTransactionYesterdayIn,
            batchTransactionYesterdayOut,
          ),
        )

      val relatedTransactionDetails = service.getTransactionHistory(
        OFFENDER_NO,
        null,
        yesterdaysDate.toLocalDate(),
        todaysDate.toLocalDate(),
        null,
      )
        .stream()
        .flatMap { transaction: OffenderTransactionHistoryDto ->
          transaction.relatedOffenderTransactions.stream()
        }.collect(
          Collectors.toList(),
        )

      assertThat(relatedTransactionDetails.size).isEqualTo(2L)
      assertThat(relatedTransactionDetails[0]!!.id).isEqualTo(2L)
      assertThat(relatedTransactionDetails[0]!!.currentBalance)
        .isEqualTo((currentBalanceAfterTodaysIn * 100).toLong())
      assertThat(relatedTransactionDetails[1]!!.id).isEqualTo(1L)
      assertThat(relatedTransactionDetails[1]!!.currentBalance)
        .isEqualTo((yesterdaysRelatedTransactionAmount * 100).toLong())
    }

    @Test
    fun testCalculateRunningBalanceWithMultipleRelatedTransactions() {
      val todaysDate = LocalDateTime.now()

      val firstRelatedTransactionAmount = 5
      val secondRelatedTransactionAmount = 10
      val finalBalance = firstRelatedTransactionAmount + secondRelatedTransactionAmount

      val firstRelatedTransaction =
        makeRelatedTransaction(firstRelatedTransactionAmount, todaysDate.toLocalDate(), 1, 1, 1)
      val secondRelatedTransaction =
        makeRelatedTransaction(secondRelatedTransactionAmount, todaysDate.toLocalDate(), 2, 2, 2)

      val batchTransactionToday = makeBatchTransactionIn(
        todaysDate,
        finalBalance,
        listOf(secondRelatedTransaction, firstRelatedTransaction),
      )

      whenever(
        repository.findByOffenderNomsId(
          anyString(),
        ),
      )
        .thenReturn(listOf(batchTransactionToday))

      val relatedTransactionDetails = service.getTransactionHistory(
        OFFENDER_NO,
        null,
        todaysDate.minusDays(1).toLocalDate(),
        todaysDate.toLocalDate(),
        null,
      )
        .stream()
        .flatMap { transaction: OffenderTransactionHistoryDto ->
          transaction.relatedOffenderTransactions.stream()
        }.collect(
          Collectors.toList(),
        )

      assertThat(relatedTransactionDetails.size).isEqualTo(2L)
      assertThat(relatedTransactionDetails[0]!!.id).isEqualTo(2L)
      assertThat(relatedTransactionDetails[0]!!.currentBalance)
        .isEqualTo((finalBalance * 100).toLong())
      assertThat(relatedTransactionDetails[1]!!.id).isEqualTo(1L)
      assertThat(relatedTransactionDetails[1]!!.currentBalance)
        .isEqualTo((firstRelatedTransactionAmount * 100).toLong())
    }

    @Test
    fun testRelatedTransactionsOrderedByLatestFirstThenTxnIdDesc() {
      val todaysDate = LocalDateTime.now()

      val todaysLastRelatedTransactionAmount = 5
      val todaysFirstRelatedTransactionAmount = 10
      val yesterdaysRelatedTransactionAmount = 20
      val finalBalance =
        todaysLastRelatedTransactionAmount + todaysFirstRelatedTransactionAmount + yesterdaysRelatedTransactionAmount

      val todaysLastRelatedTransaction =
        makeRelatedTransaction(todaysLastRelatedTransactionAmount, todaysDate.toLocalDate(), 33, 1, 1)
      val todaysFirstRelatedTransaction =
        makeRelatedTransaction(todaysFirstRelatedTransactionAmount, todaysDate.toLocalDate(), 22, 1, 1)
      val yesterdaysRelatedTransaction = makeRelatedTransaction(
        yesterdaysRelatedTransactionAmount,
        todaysDate.minusDays(1).toLocalDate(),
        44,
        1,
        1,
      )

      val batchTransactionToday = makeBatchTransactionIn(
        todaysDate,
        finalBalance,
        listOf(
          todaysFirstRelatedTransaction,
          yesterdaysRelatedTransaction,
          todaysLastRelatedTransaction,
        ),
      )

      whenever(
        repository.findByOffenderNomsId(
          anyString(),
        ),
      )
        .thenReturn(listOf(batchTransactionToday))

      val relatedTransactionDetails = service.getTransactionHistory(
        OFFENDER_NO,
        null,
        todaysDate.minusDays(1).toLocalDate(),
        todaysDate.toLocalDate(),
        null,
      )
        .stream()
        .flatMap { transaction: OffenderTransactionHistoryDto ->
          transaction.relatedOffenderTransactions.stream()
        }.collect(
          Collectors.toList(),
        )

      assertThat(relatedTransactionDetails.size).isEqualTo(3L)
      assertThat(relatedTransactionDetails[0].id).isEqualTo(33L)
      assertThat(relatedTransactionDetails[0].currentBalance)
        .isEqualTo((finalBalance * 100).toLong())
      assertThat(relatedTransactionDetails[1].id).isEqualTo(22L)
      assertThat(relatedTransactionDetails[1].currentBalance)
        .isEqualTo(((finalBalance - todaysLastRelatedTransactionAmount) * 100).toLong())
      assertThat(relatedTransactionDetails[2].id).isEqualTo(44L)
      assertThat(relatedTransactionDetails[2].currentBalance)
        .isEqualTo((yesterdaysRelatedTransactionAmount * 100).toLong())
    }

    private fun makeBatchTransactionIn(
      batchTransactionTime: LocalDateTime,
      entryAmount: Int,
      relatedTransactions: List<OffenderTransactionDetails>,
    ): OffenderTransactionHistory = OFFENDER_TRANSACTION.toBuilder()
      .postingType("CR")
      .accountType("SPND")
      .transactionType("IN")
      .entryDate(LocalDate.now())
      .entryAmount(BigDecimal.valueOf(entryAmount.toLong()))
      .createDatetime(batchTransactionTime)
      .relatedTransactionDetails(relatedTransactions)
      .build()

    private fun makeBatchTransactionOut(
      batchTransactionTime: LocalDateTime,
      entryAmount: Int,
    ): OffenderTransactionHistory = OFFENDER_TRANSACTION.toBuilder()
      .postingType("DR")
      .accountType("SPND")
      .transactionType("OUT")
      .entryDate(LocalDate.now())
      .entryAmount(BigDecimal.valueOf(entryAmount.toLong()))
      .createDatetime(batchTransactionTime)
      .build()

    private fun makeRelatedTransaction(
      payAmount: Int,
      transactionTime: LocalDate,
      transactionId: Long,
      eventId: Long,
      entrySequence: Long,
    ): OffenderTransactionDetails {
      val courseActivity = CourseActivity.builder().description("Wing cleaner").build()
      val courseAttendance =
        OffenderCourseAttendance.builder().eventId(eventId).courseActivity(courseActivity).build()
      return OffenderTransactionDetails.builder()
        .id(transactionId)
        .payAmount(BigDecimal.valueOf(payAmount.toLong()))
        .bonusPay(BigDecimal.valueOf(0.0))
        .pieceWork(BigDecimal.valueOf(0.0))
        .calendarDate(transactionTime)
        .transactionId(transactionId)
        .event(courseAttendance)
        .transactionEntrySequence(entrySequence)
        .build()
    }
  }
}
