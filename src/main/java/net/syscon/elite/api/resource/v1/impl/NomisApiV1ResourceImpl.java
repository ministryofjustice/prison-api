package net.syscon.elite.api.resource.v1.impl;

import net.syscon.elite.api.model.v1.*;
import net.syscon.elite.api.resource.v1.NomisApiV1Resource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.v1.NomisApiV1Service;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.HttpStatus;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.stream.Collectors;

import static net.syscon.util.DateTimeConverter.optionalStrToLocalDateTime;

@RestResource
@Path("/v1")
public class NomisApiV1ResourceImpl implements NomisApiV1Resource {

    private final NomisApiV1Service service;

    public NomisApiV1ResourceImpl(NomisApiV1Service service) {
        this.service = service;
    }


    @Override
    public Offender getOffender(@NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId) {
        return service.getOffender(nomsId);

    }

    @Override
    public Image getOffenderImage(@NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId) {
        return service.getOffenderImage(nomsId);

    }

    @Override
    public Location getLatestBookingLocation(@NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId) {
        return service.getLatestBookingLocation(nomsId);
    }

    @Override
    public Bookings getBookings(@NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId) {
        return service.getBookings(nomsId);
    }

    @Override
    public Alerts getAlerts(@NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) String nomsId, String alertType, String modifiedSince, boolean includeInactive) {
        var alerts = service.getAlerts(nomsId, includeInactive, optionalStrToLocalDateTime(modifiedSince)).stream()
                .filter(a -> alertType == null || a.getType().getCode().equalsIgnoreCase(alertType))
                .collect(Collectors.toList());
        return Alerts.builder().alerts(alerts).build();
    }

    @Override
    public Response createTransaction(final String clientName,
                                                       @NotNull @Length(max = 3) final String prisonId,
                                                       @NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) final String nomsId,
                                                       final @NotNull @Valid CreateTransaction createTransaction) {

        var uniqueClientId = StringUtils.isNotBlank(clientName) ? clientName + "-" + createTransaction.getClientUniqueRef() : createTransaction.getClientUniqueRef();

        var result = service.createTransaction(prisonId, nomsId,
                createTransaction.getType(), createTransaction.getDescription(),
                createTransaction.getAmountInPounds(), LocalDate.now(),
                createTransaction.getClientTransactionId(), uniqueClientId);

        return Response.status(HttpStatus.CREATED.value()).entity(TransactionResponse.builder().id(result).build()).header("Content-Type", MediaType.APPLICATION_JSON).build();
    }


    @Override
    public OffenderPssDetailEvent getOffenderPssDetail(@NotNull @Pattern(regexp = NOMS_ID_REGEX_PATTERN) final String nomsId) {

        return service.getOffenderPssDetail(nomsId);
    }
}
