package net.syscon.elite.api.resource.v1.impl;

import net.syscon.elite.api.model.v1.*;
import net.syscon.elite.api.resource.v1.NomisApiV1Resource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.v1.NomisApiV1Service;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.Path;
import java.time.LocalDate;
import java.util.List;
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
    public Alerts getAlerts(@NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) final String nomsId, final String alertType, final String modifiedSince, final boolean includeInactive) {
        final var alerts = service.getAlerts(nomsId, includeInactive, optionalStrToLocalDateTime(modifiedSince)).stream()
                .filter(a -> alertType == null || a.getType().getCode().equalsIgnoreCase(alertType))
                .collect(Collectors.toList());
        return Alerts.builder().alerts(alerts).build();
    }

    @Override
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

    private String getUniqueClientId(final String clientName, final String clientUniqueRef) {
        if (StringUtils.isBlank(clientUniqueRef)) {
            return null;
        }
        return StringUtils.isNotBlank(clientName) ? clientName + "-" + clientUniqueRef : clientUniqueRef;
    }

    @Override
    public OffenderPssDetailEvent getOffenderPssDetail(@NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) final String nomsId) {

        return service.getOffenderPssDetail(nomsId);
    }
}
