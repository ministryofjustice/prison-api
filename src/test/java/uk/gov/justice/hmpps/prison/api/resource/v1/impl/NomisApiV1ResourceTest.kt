package uk.gov.justice.hmpps.prison.api.resource.v1.impl

import com.google.common.collect.ImmutableSortedMap
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.hmpps.prison.api.model.v1.AccountBalance
import uk.gov.justice.hmpps.prison.api.model.v1.AccountTransaction
import uk.gov.justice.hmpps.prison.api.model.v1.ActiveOffender
import uk.gov.justice.hmpps.prison.api.model.v1.AvailableDates
import uk.gov.justice.hmpps.prison.api.model.v1.CodeDescription
import uk.gov.justice.hmpps.prison.api.model.v1.ContactList
import uk.gov.justice.hmpps.prison.api.model.v1.ContactPerson
import uk.gov.justice.hmpps.prison.api.model.v1.CreateTransaction
import uk.gov.justice.hmpps.prison.api.model.v1.Event
import uk.gov.justice.hmpps.prison.api.model.v1.Events
import uk.gov.justice.hmpps.prison.api.model.v1.Hold
import uk.gov.justice.hmpps.prison.api.model.v1.Image
import uk.gov.justice.hmpps.prison.api.model.v1.LiveRoll
import uk.gov.justice.hmpps.prison.api.model.v1.Offender
import uk.gov.justice.hmpps.prison.api.model.v1.OffenderId
import uk.gov.justice.hmpps.prison.api.model.v1.PaymentResponse
import uk.gov.justice.hmpps.prison.api.model.v1.StorePaymentRequest
import uk.gov.justice.hmpps.prison.api.model.v1.Transaction
import uk.gov.justice.hmpps.prison.api.model.v1.Transfer
import uk.gov.justice.hmpps.prison.api.model.v1.UnavailabilityReason
import uk.gov.justice.hmpps.prison.api.model.v1.VisitRestriction
import uk.gov.justice.hmpps.prison.api.model.v1.VisitSlotCapacity
import uk.gov.justice.hmpps.prison.api.model.v1.VisitSlots
import uk.gov.justice.hmpps.prison.api.resource.NomisApiV1Resource
import uk.gov.justice.hmpps.prison.repository.v1.model.TransferSP
import uk.gov.justice.hmpps.prison.repository.v1.model.TransferSP.TransactionSP
import uk.gov.justice.hmpps.prison.service.v1.NomisApiV1Service
import java.time.LocalDate
import java.time.LocalDateTime
import org.mockito.ArgumentMatchers.any as anyClass

class NomisApiV1ResourceTest {
  private val service: NomisApiV1Service = mock()
  private var nomisApiV1Resource: NomisApiV1Resource = NomisApiV1Resource(service)

  @Test
  fun transferTransaction() {
    whenever(
      service.transferTransaction(
        anyString(),
        anyString(),
        anyString(),
        anyString(),
        any(),
        any(),
        anyString(),
        anyString(),
      ),
    ).thenReturn(TransferSP(CodeDescription.safeNullBuild("someCode", "desc"), TransactionSP("someId")))
    val createTransaction = CreateTransaction()
    createTransaction.amount = 1234L
    createTransaction.clientUniqueRef = "clientRef"
    createTransaction.description = "desc"
    createTransaction.type = "type"
    createTransaction.clientTransactionId = "transId"
    val transfer = nomisApiV1Resource.transferTransaction("client", "previous", "nomis", createTransaction)
    assertThat(transfer)
      .isEqualTo(Transfer(CodeDescription.safeNullBuild("someCode", "desc"), Transaction("someId")))
  }

  @Test
  fun createTransaction() {
    whenever(
      service.createTransaction(
        anyString(),
        anyString(),
        anyString(),
        anyString(),
        any(),
        any(),
        anyString(),
        anyString(),
      ),
    ).thenReturn("someId")
    val createTransaction = CreateTransaction()
    createTransaction.amount = 1234L
    createTransaction.clientUniqueRef = "clientRef"
    createTransaction.description = "desc"
    createTransaction.type = "type"
    createTransaction.clientTransactionId = "transId"
    val transfer = nomisApiV1Resource.createTransaction("client", "previous", "nomis", createTransaction, false)
    assertThat(transfer).isEqualTo(Transaction("someId"))
  }

  @Test
  fun createTransaction_disabled() {
    Assertions.assertThatThrownBy { nomisApiV1Resource.createTransaction("client", "previous", "nomis", null, true) }
      .isInstanceOf(RuntimeException::class.java)
      .hasMessage("SDI-147: Create transaction currently disabled during unilink testing")
  }

  @Test
  fun offenderPssDetail() {
    val testEvent =
      Event.builder().id(0L).nomsId("A4014AE").prisonId("MDI").timestamp(LocalDateTime.now()).eventData("data").build()
    whenever(service.getOffenderPssDetail(anyString())).thenReturn(testEvent)
    val actualEvent = nomisApiV1Resource.getOffenderPssDetail("A1404AE")
    assertThat(actualEvent).isEqualTo(testEvent)
    verify(service).getOffenderPssDetail(anyString())
    verifyNoMoreInteractions(service)
  }

  @Test
  fun offenderDetail() {
    whenever(service.getOffender(anyString()))
      .thenReturn(Offender.builder().nomsId("A1404AE").build())
    val offender = nomisApiV1Resource.getOffender("A1404AE")
    assertThat(offender).extracting { obj: Offender -> obj.nomsId }
      .isEqualTo("A1404AE")
    verify(service).getOffender(anyString())
    verifyNoMoreInteractions(service)
  }

  @Test
  fun offenderImage() {
    whenever(service.getOffenderImage(anyString()))
      .thenReturn(Image.builder().image("ABCDEFGHI").build())
    val event = nomisApiV1Resource.getOffenderImage("A1404AE")
    assertThat(event).extracting { obj: Image -> obj.image }
      .isEqualTo("ABCDEFGHI")
    verify(service).getOffenderImage(anyString())
    verifyNoMoreInteractions(service)
  }

  @Test
  fun holds() {
    val holds = listOf(Hold(3L, "ref", "12345", "entry", null, 12345L, null))
    whenever(
      service.getHolds(
        anyString(),
        anyString(),
        anyString(),
        anyString(),
      ),
    ).thenReturn(holds)
    val transfer = nomisApiV1Resource.getHolds("client", "prison", "nomis", "ref")
    assertThat(transfer).isEqualTo(holds)
  }

  @Test
  fun events() {
    val events = listOf(Event("EVENT", 3L, "noms", "prison", LocalDateTime.now(), "entry"))
    whenever(
      service.getEvents(
        anyString(),
        any(),
        anyString(),
        any(),
        anyLong(),
      ),
    ).thenReturn(events)
    val transfer = nomisApiV1Resource.getOffenderEvents("client", null, "nomis", "2020-05-08", 50L)
    assertThat(transfer).isEqualTo(Events(events))
  }

  @Test
  fun liveRoll() {
    val liveRoll = listOf("bob", "joe")
    whenever(service.getLiveRoll(anyString())).thenReturn(liveRoll)
    val roll = nomisApiV1Resource.getLiveRoll("any")
    assertThat(roll).isEqualTo(LiveRoll(liveRoll))
  }

  @Test
  fun storePayment() {
    val request = StorePaymentRequest.builder().type("A_EARN").amount(1324L).clientTransactionId("CS123")
      .description("Earnings for May").build()
    val response = PaymentResponse.builder().message("Payment accepted").build()
    whenever(
      service.storePayment(
        anyString(),
        anyString(),
        anyString(),
        anyString(),
        any(),
        any(),
        anyString(),
      ),
    ).thenReturn(response)
    val result = nomisApiV1Resource.storePayment("prison", "noms", request)
    assertThat(result.message).isEqualToIgnoringCase("payment accepted")
    verify(service).storePayment(
      anyString(),
      anyString(),
      anyString(),
      anyString(),
      any(),
      any(),
      anyString(),
    )
    verifyNoMoreInteractions(service)
  }

  @Test
  fun balance() {
    val balanceResponse = AccountBalance.builder().cash(1234L).spends(5678L).savings(3434L).build()
    whenever(service.getAccountBalances(anyString(), anyString()))
      .thenReturn(balanceResponse)
    val result = nomisApiV1Resource.getAccountBalance("prison", "noms")
    assertThat(result.cash).isEqualTo(1234L)
    assertThat(result.spends).isEqualTo(5678L)
    assertThat(result.savings).isEqualTo(3434L)
    verify(service).getAccountBalances(anyString(), anyString())
    verifyNoMoreInteractions(service)
  }

  @Test
  fun accountTransactions() {
    val accountTransactions = listOf(
      AccountTransaction.builder()
        .id("1111-1")
        .description("Test transaction 1")
        .type(CodeDescription.safeNullBuild("A", "AAAAA"))
        .amount(1234L)
        .date(LocalDate.of(2019, 12, 1)).build(),
      AccountTransaction.builder()
        .id("2222-2")
        .description("Test transaction 2")
        .type(CodeDescription.safeNullBuild("B", "BBBBB"))
        .amount(4567L)
        .date(LocalDate.of(2019, 12, 1)).build(),
    )
    whenever(
      service.getAccountTransactions(
        anyString(),
        anyString(),
        anyString(),
        isNull(),
        isNull(),
      ),
    ).thenReturn(accountTransactions)
    val result = nomisApiV1Resource.getAccountTransactions("prison", "noms", "spends", null, null)
    assertThat(result.transactions).containsAll(accountTransactions)
    verify(service).getAccountTransactions(
      anyString(),
      anyString(),
      anyString(),
      isNull(),
      isNull(),
    )
    verifyNoMoreInteractions(service)
  }

  @Test
  fun transactionByClientUniqueRef() {
    val accountTransaction = AccountTransaction.builder()
      .id("1111-1")
      .description("Test transaction 1")
      .type(CodeDescription.safeNullBuild("A", "AAAAA"))
      .amount(1234L)
      .date(LocalDate.of(2019, 12, 1)).build()
    whenever(
      service.getTransactionByClientUniqueRef(
        anyString(),
        anyString(),
        anyString(),
      ),
    ).thenReturn(accountTransaction)
    val result = nomisApiV1Resource.getTransactionByClientUniqueRef("client", "prison", "nomis", "ref")
    assertThat(result).isEqualTo(accountTransaction)
    verify(service).getTransactionByClientUniqueRef("prison", "nomis", "client-ref")
    verifyNoMoreInteractions(service)
  }

  @Test
  fun activeOffender() {
    val activeOffender = ActiveOffender.builder()
      .found(true)
      .offender(OffenderId(1111111L))
      .build()
    whenever(
      service.getActiveOffender(
        anyString(),
        anyClass(LocalDate::class.java),
      ),
    ).thenReturn(activeOffender)
    val result = nomisApiV1Resource.getActiveOffender("A4014AE", LocalDate.of(1970, 1, 1))
    assertThat(result).isEqualTo(activeOffender)
    verify(service).getActiveOffender("A4014AE", LocalDate.of(1970, 1, 1))
    verifyNoMoreInteractions(service)
  }

  @Test
  fun availableDates() {
    val availableDates = AvailableDates.builder()
      .dates(listOf(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 2)))
      .build()
    whenever(
      service.getVisitAvailableDates(
        anyLong(),
        anyClass(LocalDate::class.java),
        anyClass(LocalDate::class.java),
      ),
    ).thenReturn(availableDates)
    val result =
      nomisApiV1Resource.getVisitAvailableDates(1111111L, LocalDate.of(2019, 1, 1), LocalDate.of(2019, 2, 1))
    assertThat(result).isEqualTo(availableDates)
    verify(service).getVisitAvailableDates(1111111L, LocalDate.of(2019, 1, 1), LocalDate.of(2019, 2, 1))
    verifyNoMoreInteractions(service)
  }

  @Test
  fun contactList() {
    val contactList = ContactList.builder()
      .contacts(
        listOf(
          ContactPerson.builder()
            .id(1234567L)
            .firstName("first")
            .middleName("middle")
            .lastName("last")
            .dateOfBirth(LocalDate.of(1970, 1, 1))
            .gender(CodeDescription.safeNullBuild("M", "Male"))
            .contactType(CodeDescription.safeNullBuild("\"S", "Social Family"))
            .approvedVisitor(true)
            .active(true)
            .relationshipType(CodeDescription.safeNullBuild("COU", "Cousin"))
            .restrictions(
              listOf(
                VisitRestriction.builder()
                  .restrictionType(CodeDescription.safeNullBuild("", ""))
                  .effectiveDate(LocalDate.of(2000, 1, 1))
                  .expiryDate(null)
                  .commentText("XXXXX")
                  .build(),
              ),
            )
            .build(),
        ),
      )
      .build()
    whenever(service.getVisitContactList(anyLong())).thenReturn(contactList)
    val result = nomisApiV1Resource.getVisitContactList(1111111L)
    assertThat(result).isEqualTo(contactList)
    verify(service).getVisitContactList(1111111L)
    verifyNoMoreInteractions(service)
  }

  @Test
  fun unavailableReasons() {
    val date = "2070-01-01"
    val returnMap = ImmutableSortedMap.of(date, UnavailabilityReason())
    whenever(service.getVisitUnavailability(anyLong(), anyString()))
      .thenReturn(returnMap)
    val result = nomisApiV1Resource.getVisitUnavailability(1234567L, date)
    assertThat(result).isEqualTo(returnMap)
    verify(service).getVisitUnavailability(1234567L, date)
    verifyNoMoreInteractions(service)
  }

  @Test
  fun visitSlotsWithCapacity() {
    val visitSlots = VisitSlots.builder()
      .slots(
        listOf(
          VisitSlotCapacity
            .builder()
            .time("2019-01-01T13:30/16:00")
            .capacity(402L)
            .maxGroups(999L)
            .maxAdults(999L)
            .groupsBooked(4L)
            .visitorsBooked(5L)
            .adultsBooked(6L)
            .build(),
        ),
      )
      .build()
    whenever(
      service.getVisitSlotsWithCapacity(
        anyString(),
        anyClass(LocalDate::class.java),
        anyClass(LocalDate::class.java),
      ),
    ).thenReturn(visitSlots)
    val result =
      nomisApiV1Resource.getVisitSlotsWithCapacity("MDI", LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 1))
    assertThat(result).isEqualTo(visitSlots)
    verify(service).getVisitSlotsWithCapacity("MDI", LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 1))
    verifyNoMoreInteractions(service)
  }
}
