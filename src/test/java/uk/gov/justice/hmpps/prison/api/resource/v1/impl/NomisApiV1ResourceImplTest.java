package uk.gov.justice.hmpps.prison.api.resource.v1.impl;

import com.google.common.collect.ImmutableSortedMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.prison.api.model.v1.AccountBalance;
import uk.gov.justice.hmpps.prison.api.model.v1.AccountTransaction;
import uk.gov.justice.hmpps.prison.api.model.v1.ActiveOffender;
import uk.gov.justice.hmpps.prison.api.model.v1.AvailableDates;
import uk.gov.justice.hmpps.prison.api.model.v1.CodeDescription;
import uk.gov.justice.hmpps.prison.api.model.v1.ContactList;
import uk.gov.justice.hmpps.prison.api.model.v1.ContactPerson;
import uk.gov.justice.hmpps.prison.api.model.v1.CreateTransaction;
import uk.gov.justice.hmpps.prison.api.model.v1.Event;
import uk.gov.justice.hmpps.prison.api.model.v1.Events;
import uk.gov.justice.hmpps.prison.api.model.v1.Hold;
import uk.gov.justice.hmpps.prison.api.model.v1.Image;
import uk.gov.justice.hmpps.prison.api.model.v1.LiveRoll;
import uk.gov.justice.hmpps.prison.api.model.v1.Offender;
import uk.gov.justice.hmpps.prison.api.model.v1.OffenderId;
import uk.gov.justice.hmpps.prison.api.model.v1.PaymentResponse;
import uk.gov.justice.hmpps.prison.api.model.v1.StorePaymentRequest;
import uk.gov.justice.hmpps.prison.api.model.v1.Transaction;
import uk.gov.justice.hmpps.prison.api.model.v1.Transfer;
import uk.gov.justice.hmpps.prison.api.model.v1.UnavailabilityReason;
import uk.gov.justice.hmpps.prison.api.model.v1.VisitRestriction;
import uk.gov.justice.hmpps.prison.api.model.v1.VisitSlotCapacity;
import uk.gov.justice.hmpps.prison.api.model.v1.VisitSlots;
import uk.gov.justice.hmpps.prison.repository.v1.model.TransferSP;
import uk.gov.justice.hmpps.prison.repository.v1.model.TransferSP.TransactionSP;
import uk.gov.justice.hmpps.prison.service.v1.NomisApiV1Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NomisApiV1ResourceImplTest {
    @Mock
    private NomisApiV1Service service;

    private NomisApiV1ResourceImpl nomisApiV1Resource;

    @BeforeEach
    public void setUp() {
        nomisApiV1Resource = new NomisApiV1ResourceImpl(service);
    }

    @Test
    public void transferTransaction() {
        when(service.transferTransaction(anyString(), anyString(), anyString(), anyString(), any(), any(), anyString(), anyString())).
                thenReturn(new TransferSP(CodeDescription.safeNullBuild("someCode", "desc"), new TransactionSP("someId")));
        final var createTransaction = new CreateTransaction();
        createTransaction.setAmount(1234L);
        createTransaction.setClientUniqueRef("clientRef");
        createTransaction.setDescription("desc");
        createTransaction.setType("type");
        createTransaction.setClientTransactionId("transId");

        final var transfer = nomisApiV1Resource.transferTransaction("client", "previous", "nomis", createTransaction);
        assertThat(transfer).isEqualTo(new Transfer(CodeDescription.safeNullBuild("someCode", "desc"), new Transaction("someId")));
    }

    @Test
    public void createTransaction() {
        when(service.createTransaction(anyString(), anyString(), anyString(), anyString(), any(), any(), anyString(), anyString())).
                thenReturn("someId");
        final var createTransaction = new CreateTransaction();
        createTransaction.setAmount(1234L);
        createTransaction.setClientUniqueRef("clientRef");
        createTransaction.setDescription("desc");
        createTransaction.setType("type");
        createTransaction.setClientTransactionId("transId");

        final var transfer = nomisApiV1Resource.createTransaction("client", "previous", "nomis", createTransaction);
        assertThat(transfer).isEqualTo(new Transaction("someId"));
    }

    @Test
    public void offenderPssDetail() {

        final var testEvent = Event.builder().id(0L).nomsId("A4014AE").prisonId("MDI").timestamp(LocalDateTime.now()).eventData("data").build();

        when(service.getOffenderPssDetail(anyString())).thenReturn(testEvent);
        final var actualEvent = nomisApiV1Resource.getOffenderPssDetail("A1404AE");

        assertThat(actualEvent).isEqualTo(testEvent);

        verify(service).getOffenderPssDetail(anyString());
        verifyNoMoreInteractions(service);
    }

    @Test
    public void offenderDetail() {

        when(service.getOffender(anyString())).thenReturn(Offender.builder().nomsId("A1404AE").build());

        final var offender = nomisApiV1Resource.getOffender("A1404AE");

        assertThat(offender).extracting(Offender::getNomsId).isEqualTo("A1404AE");

        verify(service).getOffender(anyString());
        verifyNoMoreInteractions(service);
    }

    @Test
    public void offenderImage() {

        when(service.getOffenderImage(anyString())).thenReturn(Image.builder().image("ABCDEFGHI").build());

        final var event = nomisApiV1Resource.getOffenderImage("A1404AE");

        assertThat(event).extracting(Image::getImage).isEqualTo("ABCDEFGHI");

        verify(service).getOffenderImage(anyString());
        verifyNoMoreInteractions(service);
    }

    @Test
    public void getHolds() {
        final var holds = List.of(new Hold(3L, "ref", "12345", "entry", null, 12345L, null));
        when(service.getHolds(anyString(), anyString(), anyString(), anyString())).thenReturn(holds);
        final var transfer = nomisApiV1Resource.getHolds("client", "prison", "nomis", "ref");
        assertThat(transfer).isEqualTo(holds);
    }

    @Test
    public void getEvents() {
        final var events = List.of(new Event("EVENT", 3L, "noms", "prison", LocalDateTime.now(), "entry"));
        when(service.getEvents(anyString(), any(), anyString(), any(), anyLong())).thenReturn(events);
        final var transfer = nomisApiV1Resource.getOffenderEvents("client", null, "nomis", null, 50L);
        assertThat(transfer).isEqualTo(new Events(events));
    }

    @Test
    public void getLiveRoll() {
        final var liveRoll = List.of("bob", "joe");
        when(service.getLiveRoll(anyString())).thenReturn(liveRoll);
        final var roll = nomisApiV1Resource.getLiveRoll("any");
        assertThat(roll).isEqualTo(new LiveRoll(liveRoll));
    }

    @Test
    public void storePayment() {
        final var request = StorePaymentRequest.builder().type("A_EARN").amount(1324L).clientTransactionId("CS123").description("Earnings for May").build();
        final var response = PaymentResponse.builder().message("Payment accepted").build();
        when(service.storePayment(anyString(), anyString(), anyString(), anyString(), any(), any(), anyString())).thenReturn(response);

        final var result = nomisApiV1Resource.storePayment("prison", "noms", request);

        assertThat(result.getMessage()).isEqualToIgnoringCase("payment accepted");

        verify(service).storePayment(anyString(), anyString(), anyString(), anyString(), any(), any(), anyString());
        verifyNoMoreInteractions(service);
    }

    @Test
    public void getBalance() {
        final var balanceResponse = AccountBalance.builder().cash(1234L).spends(5678L).savings(3434L).build();
        when(service.getAccountBalances(anyString(), anyString())).thenReturn(balanceResponse);
        final var result = nomisApiV1Resource.getAccountBalance("prison", "noms");

        assertThat(result.getCash()).isEqualTo(1234L);
        assertThat(result.getSpends()).isEqualTo(5678L);
        assertThat(result.getSavings()).isEqualTo(3434L);

        verify(service).getAccountBalances(anyString(), anyString());
        verifyNoMoreInteractions(service);
    }

    @Test
    public void getAccountTransactions() {

        final var accountTransactions = List.of(
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
                        .date(LocalDate.of(2019, 12, 1)).build()
        );

        when(service.getAccountTransactions(anyString(), anyString(), anyString(), any(), any())).thenReturn(accountTransactions);

        final var result = nomisApiV1Resource.getAccountTransactions("prison", "noms", "spends", null, null);

        assertThat(result.getTransactions()).containsAll(accountTransactions);

        verify(service).getAccountTransactions(anyString(), anyString(), anyString(), any(), any());
        verifyNoMoreInteractions(service);
    }

    @Test
    public void getTransactionByClientUniqueRef() {

        final var accountTransaction =
                AccountTransaction.builder()
                        .id("1111-1")
                        .description("Test transaction 1")
                        .type(CodeDescription.safeNullBuild("A", "AAAAA"))
                        .amount(1234L)
                        .date(LocalDate.of(2019, 12, 1)).build();

        when(service.getTransactionByClientUniqueRef(anyString(), anyString(), anyString())).thenReturn(accountTransaction);

        final var result = nomisApiV1Resource.getTransactionByClientUniqueRef("client", "prison", "nomis", "ref");

        assertThat(result).isEqualTo(accountTransaction);

        verify(service).getTransactionByClientUniqueRef("prison", "nomis", "client-ref");
        verifyNoMoreInteractions(service);
    }

    @Test
    public void getActiveOffender() {

        final var activeOffender = ActiveOffender.builder()
                .found(true)
                .offender(new OffenderId(1111111L))
                .build();

        when(service.getActiveOffender(anyString(), any(LocalDate.class))).thenReturn(activeOffender);

        final var result = nomisApiV1Resource.getActiveOffender("A4014AE", LocalDate.of(1970, 1, 1));

        assertThat(result).isEqualTo(activeOffender);

        verify(service).getActiveOffender("A4014AE", LocalDate.of(1970, 1, 1));
        verifyNoMoreInteractions(service);
    }

    @Test
    public void getAvailableDates() {

        final var availableDates = AvailableDates.builder()
                .dates(List.of(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 2)))
                .build();

        when(service.getVisitAvailableDates(anyLong(), any(LocalDate.class), any(LocalDate.class))).thenReturn(availableDates);

        final var result = nomisApiV1Resource.getVisitAvailableDates(1111111L, LocalDate.of(2019, 1, 1), LocalDate.of(2019, 2, 1));

        assertThat(result).isEqualTo(availableDates);

        verify(service).getVisitAvailableDates(1111111L, LocalDate.of(2019, 1, 1), LocalDate.of(2019, 2, 1));
        verifyNoMoreInteractions(service);
    }

    @Test
    public void getContactList() {

        final var contactList =
                ContactList.builder()
                        .contacts(List.of(
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
                                        .restrictions(List.of(VisitRestriction.builder()
                                                .restrictionType(CodeDescription.safeNullBuild("", ""))
                                                .effectiveDate(LocalDate.of(2000, 1, 1))
                                                .expiryDate(null)
                                                .commentText("XXXXX")
                                                .build()))
                                        .build()))
                        .build();

        when(service.getVisitContactList(anyLong())).thenReturn(contactList);

        final var result = nomisApiV1Resource.getVisitContactList(1111111L);

        assertThat(result).isEqualTo(contactList);

        verify(service).getVisitContactList(1111111L);
        verifyNoMoreInteractions(service);
    }

    @Test
    public void getUnavailableReasons() {

        final var date = "2070-01-01";
        final var returnMap = ImmutableSortedMap.of(date, new UnavailabilityReason());

        when(service.getVisitUnavailability(anyLong(), anyString())).thenReturn(returnMap);

        final var result = nomisApiV1Resource.getVisitUnavailability(1234567L, date);

        assertThat(result).isEqualTo(returnMap);

        verify(service).getVisitUnavailability(1234567L, date);
        verifyNoMoreInteractions(service);
    }

    @Test
    public void getVisitSlotsWithCapacity() {

        final var visitSlots = VisitSlots.builder()
                .slots(List.of(
                        VisitSlotCapacity
                                .builder()
                                .time("2019-01-01T13:30/16:00")
                                .capacity(402L)
                                .maxGroups(999L)
                                .maxAdults(999L)
                                .groupsBooked(4L)
                                .visitorsBooked(5L)
                                .adultsBooked(6L)
                                .build()))
                .build();

        when(service.getVisitSlotsWithCapacity(anyString(), any(LocalDate.class), any(LocalDate.class))).thenReturn(visitSlots);

        final var result = nomisApiV1Resource.getVisitSlotsWithCapacity("MDI", LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 1));

        assertThat(result).isEqualTo(visitSlots);

        verify(service).getVisitSlotsWithCapacity("MDI", LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 1));
        verifyNoMoreInteractions(service);
    }
}
