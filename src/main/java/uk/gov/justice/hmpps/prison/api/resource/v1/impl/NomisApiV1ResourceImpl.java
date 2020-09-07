package uk.gov.justice.hmpps.prison.api.resource.v1.impl;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.justice.hmpps.prison.api.model.v1.AccountBalance;
import uk.gov.justice.hmpps.prison.api.model.v1.AccountTransaction;
import uk.gov.justice.hmpps.prison.api.model.v1.AccountTransactions;
import uk.gov.justice.hmpps.prison.api.model.v1.ActiveOffender;
import uk.gov.justice.hmpps.prison.api.model.v1.Alerts;
import uk.gov.justice.hmpps.prison.api.model.v1.AvailableDates;
import uk.gov.justice.hmpps.prison.api.model.v1.Bookings;
import uk.gov.justice.hmpps.prison.api.model.v1.ContactList;
import uk.gov.justice.hmpps.prison.api.model.v1.CreateTransaction;
import uk.gov.justice.hmpps.prison.api.model.v1.Event;
import uk.gov.justice.hmpps.prison.api.model.v1.Events;
import uk.gov.justice.hmpps.prison.api.model.v1.Hold;
import uk.gov.justice.hmpps.prison.api.model.v1.Image;
import uk.gov.justice.hmpps.prison.api.model.v1.LiveRoll;
import uk.gov.justice.hmpps.prison.api.model.v1.Location;
import uk.gov.justice.hmpps.prison.api.model.v1.Offender;
import uk.gov.justice.hmpps.prison.api.model.v1.PaymentResponse;
import uk.gov.justice.hmpps.prison.api.model.v1.StorePaymentRequest;
import uk.gov.justice.hmpps.prison.api.model.v1.Transaction;
import uk.gov.justice.hmpps.prison.api.model.v1.Transfer;
import uk.gov.justice.hmpps.prison.api.model.v1.UnavailabilityReason;
import uk.gov.justice.hmpps.prison.api.model.v1.VisitSlots;
import uk.gov.justice.hmpps.prison.api.resource.v1.NomisApiV1Resource;
import uk.gov.justice.hmpps.prison.core.HasWriteScope;
import uk.gov.justice.hmpps.prison.core.ProxyUser;
import uk.gov.justice.hmpps.prison.service.v1.NomisApiV1Service;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.List;
import java.util.SortedMap;
import java.util.stream.Collectors;

import static uk.gov.justice.hmpps.prison.util.DateTimeConverter.optionalStrToLocalDateTime;
import static uk.gov.justice.hmpps.prison.util.ResourceUtils.getUniqueClientId;

@RestController
@RequestMapping("${api.base.path}/v1")
public class NomisApiV1ResourceImpl implements NomisApiV1Resource {

    private final NomisApiV1Service service;

    public NomisApiV1ResourceImpl(final NomisApiV1Service service) {
        this.service = service;
    }


    @Override
    public Offender getOffender(@NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) final String nomsId) {
        return service.getOffender(nomsId);
    }

    @Override
    public Image getOffenderImage(@NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) final String nomsId) {
        return service.getOffenderImage(nomsId);
    }

    @Override
    public Location getLatestBookingLocation(@NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) final String nomsId) {
        return service.getLatestBookingLocation(nomsId);
    }

    @Override
    public Bookings getBookings(@NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) final String nomsId) {
        return service.getBookings(nomsId);
    }

    @Override
    public Alerts getAlerts(final String nomsId, final String alertType, final String modifiedSince, final boolean includeInactive) {
        final var alerts = service.getAlerts(nomsId, includeInactive, optionalStrToLocalDateTime(modifiedSince)).stream()
                .filter(a -> alertType == null || a.getType().getCode().equalsIgnoreCase(alertType))
                .collect(Collectors.toList());
        return Alerts.builder().alerts(alerts).build();
    }

    @Override
    public Events getOffenderEvents(final String prisonId, final String offenderIdentifier, final String eventType, final String fromDateTime, final Long limit) {
        final var events = service.getEvents(prisonId, new OffenderIdentifier(offenderIdentifier), eventType, optionalStrToLocalDateTime(fromDateTime), limit);
        return new Events(events);
    }

    @Override
    @HasWriteScope
    @ProxyUser
    public Transfer transferTransaction(final String clientName, final String previousPrisonId, final String nomsId,
                                        final CreateTransaction createTransaction) {

        final var uniqueClientId = getUniqueClientId(clientName, createTransaction.getClientUniqueRef());

        final var transfer = service.transferTransaction(previousPrisonId, nomsId,
                createTransaction.getType(), createTransaction.getDescription(),
                createTransaction.getAmountInPounds(), LocalDate.now(),
                createTransaction.getClientTransactionId(), uniqueClientId);

        return new Transfer(transfer.getCurrentLocation(), new Transaction(transfer.getTransaction().getId()));
    }

    @Override
    @HasWriteScope
    @ProxyUser
    public Transaction createTransaction(final String clientName, final String prisonId, final String nomsId,
                                         final CreateTransaction createTransaction) {

        final var uniqueClientId = getUniqueClientId(clientName, createTransaction.getClientUniqueRef());

        final var result = service.createTransaction(prisonId, nomsId,
                createTransaction.getType(), createTransaction.getDescription(),
                createTransaction.getAmountInPounds(), LocalDate.now(),
                createTransaction.getClientTransactionId(), uniqueClientId);

        return new Transaction(result);
    }

    @Override
    public List<Hold> getHolds(final String clientName, final String prisonId, final String nomsId, final String clientUniqueRef) {
        final var uniqueClientId = getUniqueClientId(clientName, clientUniqueRef);
        return service.getHolds(prisonId, nomsId, uniqueClientId, clientName);
    }

    @Override
    public LiveRoll getLiveRoll(final String prisonId) {
        return new LiveRoll(service.getLiveRoll(prisonId));
    }

    @Override
    public Event getOffenderPssDetail(final String nomsId) {
        return service.getOffenderPssDetail(nomsId);
    }

    @Override
    @HasWriteScope
    @ProxyUser
    public PaymentResponse storePayment(final String prisonId, final String nomsId, final StorePaymentRequest payment) {
        return service.storePayment(prisonId, nomsId, payment.getType(), payment.getDescription(), payment.getAmountInPounds(), LocalDate.now(), payment.getClientTransactionId());
    }

    @Override
    public AccountBalance getAccountBalance(final String prisonId, final String nomsId) {
        return service.getAccountBalances(prisonId, nomsId);
    }

    @Override
    public AccountTransactions getAccountTransactions(final String prisonId, final String nomsId, final String accountCode, final LocalDate fromDate, final LocalDate toDate) {
        final var transactions = service.getAccountTransactions(prisonId, nomsId, accountCode, fromDate, toDate);
        return new AccountTransactions(transactions);
    }

    @Override
    public AccountTransaction getTransactionByClientUniqueRef(final String clientName, final String prisonId, final String nomsId, final String clientUniqueRef) {
        final var uniqueClientId = getUniqueClientId(clientName, clientUniqueRef);

        return service.getTransactionByClientUniqueRef(prisonId, nomsId, uniqueClientId);
    }

    @Override
    public ActiveOffender getActiveOffender(final String nomsId, final LocalDate birthDate) {
        return service.getActiveOffender(nomsId, birthDate);
    }

    @Override
    public AvailableDates getVisitAvailableDates(final Long offenderId, final LocalDate fromDate, final LocalDate toDate) {
        return service.getVisitAvailableDates(offenderId, fromDate, toDate);
    }

    @Override
    public ContactList getVisitContactList(final Long offenderId) {
        return service.getVisitContactList(offenderId);
    }

    @Override
    public SortedMap<String, UnavailabilityReason> getVisitUnavailability(final Long offenderId, final String dates) {
        return service.getVisitUnavailability(offenderId, dates);
    }

    @Override
    public VisitSlots getVisitSlotsWithCapacity(final String prisonId, final LocalDate fromDate, final LocalDate toDate) {
        return service.getVisitSlotsWithCapacity(prisonId, fromDate, toDate);
    }
}
