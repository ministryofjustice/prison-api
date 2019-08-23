package net.syscon.elite.api.resource.v1.impl;

import net.syscon.elite.api.model.v1.*;
import net.syscon.elite.api.resource.v1.NomisApiV1Resource;
import net.syscon.elite.core.ProxyUser;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.v1.NomisApiV1Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.SortedMap;
import java.util.stream.Collectors;

import static net.syscon.util.DateTimeConverter.optionalStrToLocalDateTime;

@RestResource
@Path("/v1")
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
    public Events getOffenderEvents(final String prisonId, final String offenderId, final String eventType, final String fromDateTime, final Long limit) {
        final var events = service.getEvents(prisonId, new OffenderIdentifier(offenderId), eventType, optionalStrToLocalDateTime(fromDateTime), limit);
        return new Events(events);
    }

    @Override
    @PreAuthorize("#oauth2.hasScope('write')")
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
    @PreAuthorize("#oauth2.hasScope('write')")
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

    private String getUniqueClientId(final String clientName, final String clientUniqueRef) {
        if (StringUtils.isBlank(clientUniqueRef)) {
            return null;
        }
        return StringUtils.isNotBlank(clientName) ? clientName + "-" + clientUniqueRef : clientUniqueRef;
    }

    @Override
    public Event getOffenderPssDetail(final String nomsId) {

        return service.getOffenderPssDetail(nomsId);
    }

    @Override
    @PreAuthorize("#oauth2.hasScope('write')")
    @ProxyUser
    public PaymentResponse storePayment(String prisonId, String nomsId, StorePaymentRequest payment) {

        return service.storePayment(prisonId, nomsId, payment.getType(), payment.getDescription(), payment.getAmountInPounds(), LocalDate.now(), payment.getClientTransactionId());
    }

    @Override
    public AccountBalance getAccountBalance(String prisonId, String nomsId) {

        return service.getAccountBalances(prisonId, nomsId);
    }

    @Override
    public AccountTransactions getAccountTransactions(String prisonId, String nomsId, String accountCode, LocalDate fromDate, LocalDate toDate) {

        final var transactions = service.getAccountTransactions(prisonId, nomsId, accountCode, fromDate, toDate);
        return new AccountTransactions(transactions);
    }

    @Override
    public AccountTransaction getTransactionByClientUniqueRef(final String clientName, String prisonId, String nomsId, String clientUniqueRef) {
        final var uniqueClientId = getUniqueClientId(clientName, clientUniqueRef);

        return service.getTransactionByClientUniqueRef(prisonId, nomsId, uniqueClientId);
    }

    @Override
    public ActiveOffender getActiveOffender(String nomsId, LocalDate birthDate) {

        return service.getActiveOffender(nomsId, birthDate);
    }

    @Override
    public AvailableDates getVisitAvailableDates(String offenderId, LocalDate fromDate, LocalDate toDate) {

        return service.getVisitAvailableDates(offenderId, fromDate, toDate);
    }

    @Override
    public ContactList getVisitContactList(String offenderId) {

        return service.getVisitContactList(offenderId);
    }

    @Override
    public SortedMap<String, UnavailabilityReason> getVisitUnavailability(Long offenderId, String dates) {

        return service.getVisitUnavailability(offenderId, dates);
    }


    @Override
    public VisitSlots getVisitSlotsWithCapacity(String prisonId, LocalDate fromDate, LocalDate toDate) {

        return service.getVisitSlotsWithCapacity(prisonId, fromDate, toDate);
    }
}
